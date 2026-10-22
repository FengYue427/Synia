# 发布草稿

第一轮只有两处：Modrinth 项目页，以及 Connector Discussions。两处都粘贴下面这一段，不改写。CurseForge 和 MC百科不在这一轮。账号和实际上传等明确点头。

## 同一段话

Synia 是非官方外围补丁，不是 Connector 的一部分，也不修改 Connector 的翻译引擎。

当前只有 `synia-debugify` 0.1.0。它面向 NeoForge 1.21.1、Connector 2.x，以 early service 运行。把 jar 放进 `mods`。它只在 Debugify 读配置之前，把 `MC-93384` 和 `MC-176559` 写成 `false`。不要加入 YACL：3.8.2 在 Connector 2.x 上因缺少 refmap 会启动即崩。

杀开关要在启动前设置环境变量 `SYNIA_DEBUGIFY_ENABLED=false`。已经打开的 PCL 不会重读 JVM 附加参数。加载不等于生效：日志里出现 `synia:debugify` 只说明服务被加载，还要看写入是否早于 Debugify，以及行为是否改变。

反馈哪些模组在 1.21.1 上有类似的、入口明确的冲突。验证流程在仓库的 `docs/MODULE_PIPELINE.md`。
