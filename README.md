<div align="center">

# LLM Syntax Explainer

在 JetBrains 编辑器里选中代码，就地获得流式语法讲解。

</div>

## 特点

- **选中即解析**：选区稳定 600ms 后自动开始
- **就地显示**：结果出现在代码下方的虚拟行，不修改源文件
- **真实流式输出**：内容随模型响应逐步展示
- **通用模型接口**：支持 OpenAI、DeepSeek、Ollama 等兼容服务

适用于 PyCharm、IntelliJ IDEA、WebStorm、GoLand 等 JetBrains IDE。

## 快速开始

1. 打开 `Settings → Plugins`
2. 点击齿轮，选择 `Install Plugin from Disk...`
3. 选择 `build/distributions/jetbrains-llm-syntax-explainer-0.1.0.zip`
4. 重启 IDE
5. 打开 `Settings → Tools → Code Syntax LLM` 完成模型配置

### DeepSeek 配置

| 配置项 | 填写内容 |
| --- | --- |
| Base URL | `https://api.deepseek.com` |
| API Key | DeepSeek 平台创建的 `sk-...` |
| Model | `deepseek-v4-flash` |

填写后点击“测试连接”。选中任意代码并停留片刻，解析结果会显示在选区下方。点击其他位置即可清除。

也可以右键选择“用 LLM 解析所选代码”，或按 `Alt+Shift+E` 手动触发。

## 隐私

插件只发送当前选中的代码，不读取文件的其他内容。API Key 通过 JetBrains `PasswordSafe` 保存，不写入项目配置或 Git。

## 二次开发

项目使用 Kotlin 和 IntelliJ Platform Gradle Plugin，无需 Docker、Python 服务或预装 Gradle。

```bash
./gradlew test
./gradlew runIde
./gradlew buildPlugin
```

```text
editor/    选区监听、请求取消、Block Inlay 渲染
llm/       提示词、OpenAI 请求、SSE 流解析
settings/  配置页、参数校验、API Key 存储
```

安装包生成在 `build/distributions/`。
