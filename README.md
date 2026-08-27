# LLM Syntax Explainer

一个最小化的原生 PyCharm 插件：选中代码并停留 600ms 后，在选区下一行显示不修改源文件的虚拟解析块，并流式输出 OpenAI 兼容模型的讲解。点击编辑器其他位置、选择新代码或切换文件时，解析块会自动移除。

## 使用

1. 在 PyCharm 中打开 `Settings → Plugins`。
2. 点击齿轮，选择 `Install Plugin from Disk...`。
3. 选择 `build/distributions/pycharm-llm-syntax-explainer-0.1.0.zip`。
4. 重启 PyCharm。
5. 打开 `Settings → Tools → Code Syntax LLM`，填写 Base URL、API Key 和模型名。

默认会自动解析选区。也可以右键选择“用 LLM 解析所选代码”，或按 `Alt+Shift+E` 手动触发。

Base URL 示例：

- OpenAI：`https://api.openai.com/v1`
- DeepSeek：`https://api.deepseek.com/v1`
- Ollama OpenAI 兼容接口：`http://localhost:11434/v1`

API Key 通过 JetBrains `PasswordSafe` 保存，不写入源码、项目配置或 Git。插件只发送当前选中的代码，不读取或上传文件其他内容。

## 二次开发

要求：JDK 17+、PyCharm 2024.2+。项目自带 Gradle Wrapper，不需要安装 Gradle、Docker或 Python 服务。

```bash
./gradlew test
./gradlew runIde
./gradlew buildPlugin
```

如果本机已经安装 PyCharm，可以复用它进行快速编译：

```bash
./gradlew test -PlocalIdePath=/Applications/PyCharm.app --no-configuration-cache
./gradlew buildPlugin -PlocalIdePath=/Applications/PyCharm.app --no-configuration-cache
```

核心结构保持三块：

```text
editor/    选区监听、600ms 防抖、请求取消、Block Inlay 渲染
llm/       提示词、OpenAI 请求、SSE 流式解析
settings/  配置页、校验和 PasswordSafe 密钥存储
```

安装包输出到 `build/distributions/`。修改版本号后重新执行 `buildPlugin` 即可生成新的 ZIP。
