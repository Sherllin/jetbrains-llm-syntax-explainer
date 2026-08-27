package com.hesl.syntaxexplainer.editor

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class SyntaxExplainerStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        project.service<SelectionExplanationService>().start()
    }
}
