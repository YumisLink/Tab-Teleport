# TTP — NeoForge 1.21.1

这是原 Forge 版本的独立移植，包含玩家传送、临时标记、三个持久标记槽位、重命名和跨维度传送。

## 安装

- Minecraft 1.21.1，NeoForge 21.1.235 或更新的 21.1.x，Java 21。
- 客户端和服务端均将 `ttp-neoforge-1.21.1-1.0.2.jar` 放入 `mods` 文件夹。
- Tab 打开面板；默认 V 放置临时标记；Ctrl + V 保存到标记队列。

## 构建

在此目录执行，Java 21 需已配置为 `JAVA_HOME`：

```powershell
.\gradlew.bat build --no-daemon
```

产物：`build/libs/ttp-neoforge-1.21.1-1.0.2.jar`。

构建采用 Gradle 8.8、ModDevGradle 2.0.148、NeoForge 21.1.235 和 Minecraft 官方映射。初次构建会下载依赖。

开发运行命令：`runClient` 或 `runServer`。

## ModernUI 兼容修复（1.0.2）

玩家列表和标记替换界面每帧只在绘制面板前渲染一次背景。Minecraft 1.21.1 的 `Screen.render()` 会自动调用 `renderBackground()`；旧版在手动画完面板和文字后又调用它，导致第二次背景模糊处理影响已经绘制的内容，而随后绘制的按钮仍然清晰。

修复保留背景模糊，将面板、标题、玩家名和坐标绘制在模糊处理之后；重命名输入框也只绘制一次。无需安装 ModernUI 依赖或修改其配置。

上游实现：[ModernUI 1.21.1 BlurHandler](https://github.com/BloCamLimb/ModernUI-MC/blob/1.21.1/common/src/main/java/icyllis/modernui/mc/BlurHandler.java)。
