# 路线

当前只维护 **Minecraft 1.21.1 / Connector 2.x**。不推远端，不开 3.x 分支，不在本文件里选下一个补丁。

## 2.x

`synia:debugify`（tag `synia-debugify-0.1.0`）是这条线上第一个闭环模块。它用 early service 在 Debugify 读配置之前，把 `config/debugify.json` 里的 `MC-93384` 和 `MC-176559` 写成 `false`。

这条线服务还能在本机验证的 1.21.1 整合包。上游若修好这两项，模块退役，[MODULE_PIPELINE.md](MODULE_PIPELINE.md) 保留。

## 3.x（未开始，形态未定）

Connector 3.x（26.x）的官方扩展入口是 `TransformerPlugin`：可以对单个模组 jar 注册 `MethodPatch`，也可以注册 jar 级转换器。这不是已经选定的 Debugify 迁移方案。

已经确定的约束：

- 插件仍要作为 early service，在模组发现前进入 classpath。
- Early service jar 不能兼作普通模组。若还要游戏内 UI，仍用 Jar-in-Jar。见 [ARCHITECTURE.md](ARCHITECTURE.md)。

相对 2.x 会变掉的，是时序赌博：不再抢在 Debugify 读配置之前把两项写成 `false`，而是在转换阶段针对 `debugify` 这个 jar 处理 `MC-93384` / `MC-176559`。

尚未确定、必须对着 3.x API 和那两个 Mixin 看过才能写的：

- 关掉注入
- 改注入点
- 改 jar 内配置

在看过之前，不写实现，不双线维护。3.x 模块等 26.x 出现可验证的真实整合包，并且上面三项有了证据之后再开。

## 第二个模块何时开工

与 [MODULE_PIPELINE.md](MODULE_PIPELINE.md) 第 1 节是同一组条件，不另列条目。下面三条满足任意两条，才进入流水线。同一帖里的跟帖算一个来源。

1. 至少 3 个独立来源报告同一个模组的同一故障（GitHub issue、Modrinth 评论、MC百科留言等）。
2. 该模组有明确入口，且原因单一（配置、单个 Mixin 或单个缺失类）。
3. 该模组在 1.21.1 整合包中高频出现。

入口不明确的不开工。触发之后仍要走完时序、A ≠ B、B ≈ C 和回归。

## 发布

第一轮只准备两处，且都等明确点头后再发出：Modrinth 项目页，以及 Connector Discussions。两处粘贴 [PUBLISH.md](PUBLISH.md) 里的同一段话，其中写明非官方外围补丁，不是 Connector 的一部分。

CurseForge 和 MC百科不在这一轮。
