package com.hesl.syntaxexplainer.editor

import com.hesl.syntaxexplainer.llm.Conversation
import com.hesl.syntaxexplainer.llm.OpenAiStreamClient
import com.hesl.syntaxexplainer.llm.PromptBuilder
import com.hesl.syntaxexplainer.llm.StreamHandle
import com.hesl.syntaxexplainer.settings.ApiKeyStore
import com.hesl.syntaxexplainer.settings.InvalidSettings
import com.hesl.syntaxexplainer.settings.LlmSettings
import com.hesl.syntaxexplainer.settings.SettingsValidator
import com.hesl.syntaxexplainer.settings.ValidatedSettings
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.editor.event.SelectionEvent
import com.intellij.openapi.editor.event.SelectionListener
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.util.concurrency.AppExecutorUtil
import java.util.IdentityHashMap
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

@Service(Service.Level.PROJECT)
class SelectionExplanationService(private val project: Project) : Disposable {
    private val sessions = IdentityHashMap<Editor, Session>()
    private val client = OpenAiStreamClient()
    private var started = false

    fun start() {
        if (started) return
        started = true
        val multicaster = EditorFactory.getInstance().eventMulticaster
        multicaster.addSelectionListener(object : SelectionListener {
            override fun selectionChanged(event: SelectionEvent) {
                if (event.editor.project !== project) return
                val editor = event.editor
                if (!editor.selectionModel.hasSelection()) {
                    clear(editor)
                } else if (LlmSettings.getInstance().state.autoExplain) {
                    schedule(editor, LlmSettings.getInstance().state.debounceMs.toLong())
                }
            }
        }, this)
        EditorFactory.getInstance().addEditorFactoryListener(object : EditorFactoryListener {
            override fun editorReleased(event: EditorFactoryEvent) {
                clear(event.editor)
            }
        }, this)
        project.messageBus.connect(this).subscribe(
            FileEditorManagerListener.FILE_EDITOR_MANAGER,
            object : FileEditorManagerListener {
                override fun selectionChanged(event: FileEditorManagerEvent) {
                    clearAll()
                }
            },
        )
    }

    fun explainNow(editor: Editor) {
        if (editor.project === project && editor.selectionModel.hasSelection()) schedule(editor, 0)
    }

    private fun schedule(editor: Editor, delayMs: Long) {
        clearAll()
        val selection = editor.selectionModel.selectedText?.takeIf { it.isNotBlank() } ?: return
        val state = LlmSettings.getInstance().state
        if (selection.length > state.maxSelectionChars) {
            show(editor, "选区过长，最多 ${state.maxSelectionChars} 个字符。")
            return
        }

        val session = Session()
        sessions[editor] = session
        session.pending = AppExecutorUtil.getAppScheduledExecutorService().schedule({
            startRequest(editor, session, selection)
        }, delayMs.coerceAtLeast(0), TimeUnit.MILLISECONDS)
    }

    private fun startRequest(editor: Editor, session: Session, selection: String) {
        if (session.cancelled) return
        val state = LlmSettings.getInstance().state
        when (val validation = SettingsValidator.validate(state.baseUrl.orEmpty(), state.model.orEmpty(), ApiKeyStore.load())) {
            is InvalidSettings -> update(editor, session, validation.message)
            is ValidatedSettings -> {
                val prompt = PromptBuilder.build(
                    language = editor.virtualFile?.fileType?.name ?: "Code",
                    selectedCode = selection,
                    instruction = state.prompt.orEmpty(),
                )
                session.conversation = Conversation(prompt)
                stream(editor, session, validation)
            }
        }
    }

    private fun followUp(editor: Editor, session: Session, question: String) {
        if (session.cancelled || session.streaming || sessions[editor] !== session) return
        val conversation = session.conversation ?: return
        val state = LlmSettings.getInstance().state
        when (val validation = SettingsValidator.validate(state.baseUrl.orEmpty(), state.model.orEmpty(), ApiKeyStore.load())) {
            is InvalidSettings -> update(editor, session, conversation.transcript() + "\n\n" + validation.message, true)
            is ValidatedSettings -> {
                if (!conversation.beginFollowUp(question)) return
                stream(editor, session, validation)
            }
        }
    }

    private fun stream(editor: Editor, session: Session, settings: ValidatedSettings) {
        val conversation = session.conversation ?: return
        session.streaming = true
        update(editor, session, conversation.transcript().ifEmpty { "正在解析…" }, false)
        session.stream = client.stream(
            settings = settings,
            messages = conversation.messages(),
            onDelta = { delta ->
                if (!session.cancelled) {
                    conversation.appendAssistantDelta(delta)
                    update(editor, session, conversation.transcript(), false)
                }
            },
            onComplete = {
                if (!session.cancelled) {
                    session.streaming = false
                    session.stream = null
                    conversation.completeAssistant()
                    val text = conversation.transcript().ifEmpty { "模型未返回可显示内容。" }
                    update(editor, session, text, true)
                }
            },
            onError = { message ->
                if (!session.cancelled) {
                    session.streaming = false
                    session.stream = null
                    conversation.failAssistant(message)
                    update(editor, session, conversation.transcript(), true)
                }
            },
        )
    }

    private fun show(editor: Editor, text: String) {
        val session = Session()
        sessions[editor] = session
        update(editor, session, text)
    }

    private fun update(editor: Editor, session: Session, text: String, inputEnabled: Boolean = false) {
        ApplicationManager.getApplication().invokeLater {
            if (project.isDisposed || editor.isDisposed || sessions[editor] !== session || session.cancelled) return@invokeLater
            val inlay = session.inlay ?: ExplanationInlay(editor, editor.selectionModel.selectionEnd) { question ->
                followUp(editor, session, question)
            }.also {
                session.inlay = it
            }
            inlay.update(text, inputEnabled)
        }
    }

    private fun clear(editor: Editor) {
        sessions.remove(editor)?.cancel()
    }

    private fun clearAll() {
        sessions.keys.toList().forEach(::clear)
    }

    override fun dispose() {
        clearAll()
    }

    private class Session {
        @Volatile var cancelled = false
        @Volatile var pending: ScheduledFuture<*>? = null
        @Volatile var stream: StreamHandle? = null
        @Volatile var streaming = false
        @Volatile var conversation: Conversation? = null
        var inlay: ExplanationInlay? = null

        fun cancel() {
            cancelled = true
            pending?.cancel(true)
            stream?.close()
            inlay?.dispose()
        }
    }
}
