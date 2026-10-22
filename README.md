# Synia

面向 [Sinytra Connector](https://github.com/Sinytra/Connector) 的**外围兼容性补丁集**。  
定位是「兼容性外科医生」：按模组拆分、条件门控、独立发布——不修改 Connector 翻译引擎本身。

本项目维护一条可复用的微创门控流水线。单个补丁的有效性随上游变化；上游修好对应冲突后，该模块退役，流水线保留。

> **版本边界：Connector 2.x / Minecraft 1.21.1 专用。**  
> Connector 3.x 有 `TransformerPlugin`，但 Debugify 在 3.x 上的具体门控形态还没定。见 [docs/ROADMAP.md](docs/ROADMAP.md)。本 early-service 路径不保证在 3.x 上适用或无冲突。架构约束见 [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)。

## 模块状态

| 模块 | 状态 | 范围 |
|------|------|------|
| `synia:debugify`（`synia-debugify-0.1.0`） | **已闭环** | Minecraft 1.21.1 / Connector 2.x |

加载成功不等于门控生效。日志里出现服务名，只说明 early service 被加载了；还要看到写入早于目标模组预加载、磁盘内容正确，并且 A ≠ B、B ≈ C。检查点见 [docs/MODULE_PIPELINE.md](docs/MODULE_PIPELINE.md)，Debugify 的实测见 [docs/VERIFICATION.md](docs/VERIFICATION.md) 第 5 节。

## 版本锚点

| 项目 | 版本 |
|------|------|
| Minecraft | **1.21.1** |
| NeoForge | 21.1.x |
| Connector | 2.0.0-beta.17+1.21.1（LTS / 2.x 通道） |

不追踪 26.x 主线，不做「新版开发 + 回移植」。

## 重要技术说明：1.21.1 上没有 TransformerPlugin

官方 `TransformerPlugin` / `MethodPatch` API 随 **Connector 3.x（26.x）** 引入，**未**下放到 1.21.1 的 Connector 2.x。

因此 Synia 在 1.21.1 上的交付方式是：

1. 产出 **early FML service jar**（`IModFileCandidateLocator` SPI）
2. 在模组发现阶段、Debugify mixin 预加载**之前**写入/修正 `config/debugify.json`
3. 强制关闭已知冲突项 `MC-93384`、`MC-176559`（与社区对 Connector#1322 / #1770 的手工规避同构）

插件身份字符串为 `synia:debugify`。

> Early service jar **不能**同时作为普通 NeoForge 模组（无 `neoforge.mods.toml`）。日后若做游戏内诊断 UI，需 Jar-in-Jar 嵌套 FML mod。

## 仓库结构

```
Synia/
  docs/ARCHITECTURE.md
  docs/MODULE_PIPELINE.md
  docs/PUBLISH.md
  docs/ROADMAP.md
  docs/VERIFICATION.md
  patches/
    debugify/          # 首个已闭环补丁，tag synia-debugify-0.1.0
  README.md
```

新增靶点时复制 `patches/debugify` 为 `patches/<id>`，并在 `settings.gradle.kts` 中 `include`。每个模块按 [docs/MODULE_PIPELINE.md](docs/MODULE_PIPELINE.md) 走完再开下一个。

## 构建

需要 **JDK 21**（Gradle Kotlin DSL 在部分 JDK 25 上会失败）。把 `JAVA_HOME` 指到本机的 JDK 21 后执行：

```powershell
./gradlew.bat :patches:debugify:build
```

产物：`patches/debugify/build/libs/synia-debugify-0.1.0.jar`

## 本机实测（已打包）

见 **[testrun/请先读我.md](testrun/请先读我.md)**：模组已下载到 `testrun/mods/`，用脚本导入实例并切换 A/B/C。

重新拉取模组（可选）：

```powershell
# 模组 jar 在 testrun/mods/；若被清理可再跑准备流程或从 Modrinth 按 versions.lock.json 下载
```

### 一键禁用门控（无需删 jar）

```text
-Dsynia.debugify.enabled=false
```

或环境变量：`SYNIA_DEBUGIFY_ENABLED=false`

## 如何确认「加载」与「门控生效」

完整判定标准、三种失败分支、A/B/C 行为对照 → **[docs/VERIFICATION.md](docs/VERIFICATION.md)**（启动一次即可按表下结论）。

摘要：

1. `service_ts` → early service 已加载  
2. `write_ts_end` **早于** Debugify `Preloading Debugify`  
3. `disk_audit=PASS` 且两项为 `false`  
4. 行为对照：**A**（门控开）≠ **B**（`-Dsynia.debugify.enabled=false`），且 **B ≈ C**（无 Synia jar）

任一环失败：先定层级（加载 / 时序 / 路径 / 缓存），再决定是否走 companion/JiJ——**不要**在未闭环前开第二个补丁模块。

跑完把 `latest.log` 里上述几行 + A/B/C 一句话观察贴回来即可判读。

## Scaffold 验收清单

- [x] `./gradlew :patches:debugify:build` 成功
- [x] jar 内含 early-service SPI → `SyniaDebugifyLocator`
- [x] 插件身份 `synia:debugify`；门控键 `MC-93384` / `MC-176559`
- [x] `-Dsynia.debugify.enabled=false` 杀开关
- [x] `write_ts_*` + `disk_audit` 日志
- [x] 验证协议钉死（[docs/VERIFICATION.md](docs/VERIFICATION.md)）
- [x] Debugify 模块已闭环（2026-10-22，见验证文档第 5 节）

## 本周非目标

- Connector Extras 式 API 桥
- 崩溃诊断 GUI / 兼容性数据库 / Probe 自动化
- 26.x / 1.20.1 多版本矩阵
- 把多个补丁打成单一「万能补丁」

## 许可证

MIT（见 [LICENSE](LICENSE)）
