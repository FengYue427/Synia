# Synia 架构备忘

## 产品定位

Connector 的外围「兼容性外科医生」：按模组拆分的精准门控，不修改 Connector 翻译引擎。

## 版本边界（硬约束）

| 通道 | Connector | Synia 交付方式 |
|------|-----------|----------------|
| **1.21.1 LTS（当前）** | 2.x | early FML service（`IModFileCandidateLocator`）+ 配置/行为门控 |
| 26.x | 3.x | 官方 `TransformerPlugin` / `MethodPatch`（尚未实现；形态未定，见 [ROADMAP.md](ROADMAP.md)） |

本仓库补丁默认标注为 **Connector 2.x / Minecraft 1.21.1 专用**。  
不要期望它们在 Connector 3.x 上自动等价；3.x 应走 TransformerPlugin，避免双重干预。

## Early service 硬约束

来自 [Connector Plugins 文档](https://moddedmc.wiki/en/project/connector/latest/docs/plugins)：

> Early service jars **cannot double as mods**.  
> 若项目包含 FML Mod，必须用 Jar-in-Jar 捆进主 jar。

因此：

- 每个 `patches/<id>` 默认只产出 **纯 early-service jar**（无 `neoforge.mods.toml`）。
- 若某模块既要 early hook、又要游戏内 UI / `@Mod`，拆成：
  - 外层 early-service jar（SPI）
  - 内嵌 JiJ 的普通 FML mod jar  
  或两个独立发布物。

## 模块约定

1. **独立 jar / 独立版本号** — 一个补丁失效不影响其它。
2. **日志前缀** — `synia:<module>`（例：`synia:debugify`）。
3. **可关闭门控** — 每个模块提供 `-Dsynia.<module>.enabled=false`（及对应环境变量）。
4. **条件门控** — 只在目标存在或相关配置路径上动手；禁止无差别改所有 jar。

## Debugify 时序假设（须用游戏日志验证）

```
FML 加载 early services
  → SyniaDebugifyLocator.findCandidates  （写 debugify.json + write_ts_*）
  → 模组发现 / Connector 转换 Fabric jar
  → Debugify MixinPlugin.onLoad
       → Debugify.onPreInitialize / CONFIG.preload  （读 debugify.json）
  → shouldApplyMixin 按配置决定是否应用 MC-93384 / MC-176559
```

**加载成功 ≠ 门控生效。**  
一锤定音协议见 **[VERIFICATION.md](VERIFICATION.md)**。`synia:debugify` 已按该协议闭环（2026-10-22）。
