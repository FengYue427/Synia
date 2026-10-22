# Debugify 门控验证协议（一锤定音）

> **加载成功 ≠ 门控生效。**  
> `Early service active` 只证明 FML 加载了 early service。  
> 必须：**时间戳顺序** + **disk_audit** + **A/B/C 行为对照** 三者全过，本模块才算闭环。

## 0. 启动前准备

1. NeoForge **1.21.1** + Connector 2.x + Forgified Fabric API + Fabric Debugify + `synia-debugify-*.jar`
2. 删掉或备份旧的 `config/debugify.json`（首次对照建议从干净配置开始）
3. 启动一次，保留完整 `logs/latest.log`

---

## 1. 日志判定清单（按时间顺序核对）

在 `latest.log` 中按下列顺序检索：

### ① `[synia:debugify] ... service_ts=...`

| 结果 | 含义 | 动作 |
|------|------|------|
| 出现 | early service 被 FML 加载 | 继续 ② |
| **缺失** | 服务未注册成功 | **停**。查 SPI 文件名是否精确为 `META-INF/services/net.neoforged.neoforgespi.locating.IModFileCandidateLocator`；查 jar 是否被当成普通 mod（不应有 `neoforge.mods.toml`）；查 jar 是否真在 `mods/` |

### ② `[synia:debugify] write_ts_start=...` / `write_ts_end=...`

| 结果 | 含义 | 动作 |
|------|------|------|
| 成对出现 | 写入动作执行完毕 | 继续 ③ |
| 只有 start 无 end | 写入中抛异常 | **停**。查权限 / 路径 / 文件锁；看紧随其后的 ERROR 栈 |
| 因杀开关跳过 | 日志含 `Gate DISABLED` | 属预期；做对照 B 时用 |

### ③ `[synia:debugify] ... disk_audit=PASS values={...}`

| 结果 | 含义 | 动作 |
|------|------|------|
| `PASS` 且 `MC-93384=false, MC-176559=false` | 落盘内容正确 | 继续 ④ |
| **`FAIL`** | 写入路径与期望不一致，或写后读回不对 | **停**。比时序更底层：核对 `FMLPaths.GAMEDIR` → `config/debugify.json` 是否即 Debugify 实际读取路径 |

### ④ Debugify：`Preloading Debugify`（或等价预加载日志）

| 结果 | 含义 | 动作 |
|------|------|------|
| `write_ts_end` **早于** 该行 | 时序假设成立 | 进入第 2 节行为对照 |
| **晚于** 或 Debugify 更早出现 | **时序失败**，2.x 配置门控不可靠 | **停止扩展**。转 companion/JiJ 拦截，或接受「2.x 不做 Debugify 门控」；不要继续改门控布尔逻辑 |

---

## 2. 三种典型失败分支（速查）

| 现象 | 含义 | 动作 |
|------|------|------|
| `service_ts` 缺失 | early service 未被加载 | 查 SPI 文件名；查 jar 是否被 FML 当作普通 mod 而非 early candidate |
| `write_ts_end` 晚于 `Preloading Debugify` | 时序失败，2.x 配置门控不可靠 | 停止扩展 → companion/JiJ，或放弃本靶点 |
| `disk_audit=PASS` 但行为对照显示 fix 仍生效 | 写入正确但 Debugify 未采用（缓存 / 默认值 / 另一路径） | 查是否预加载后缓存布尔值；查 `config/debugify/` 子目录或 `-Ddebugify.config` 等覆盖 |

第三种最隐蔽：日志顺序全对、审计也 PASS，但内部用的是预加载缓存而非磁盘新值 → **时间戳会假阳性**，必须靠行为对照抓出。

---

## 3. 行为对照（终验）— 靶点 `MC-176559`

选 **MC-176559**（连续破坏方块相关 fix）：副作用比 MC-93384 更容易稳定观察。

| 组 | 配置 | 期望 |
|----|------|------|
| **A** | Synia 门控开启（默认，`MC-176559=false`） | 出现原版/未修复侧的连续破坏表现（破坏动画重置、掉落延迟等社区已知 glitch，以你本机可稳定复现的现象为准） |
| **B** | `-Dsynia.debugify.enabled=false` | 回到 Debugify 修复后的表现 |
| **C** | 完全移除 Synia jar，仅 Debugify | 与 **B** 一致 |

### 通过标准

- **A 与 B 可区分**，且 **B ≈ C** → 门控真正改变了 Debugify 的 Mixin 应用决策。闭环成立。
- **A ≈ B** → 写入被忽略（失败分支第三种）。不要开第二个补丁模块。

操作建议：同一世界、同一工具、同一破坏节奏；每组至少重复 3 次，避免手感噪声。

---

## 4. 一锤定音结论模板

跑完后把 `latest.log` 中下列行贴回（可打码路径）：

```text
[synia:debugify] ... service_ts=...
[synia:debugify] ... write_ts_start=... write_ts_end=... ... disk_audit=... values=...
（Debugify）Preloading Debugify ...
```

并注明 A/B/C 各自观察到的现象（一句话即可）。

| 结论 | 条件 |
|------|------|
| **闭环通过** | ①②③④ 全过 + A≠B + B≈C |
| **仅加载成功** | 有 service_ts，但时序或行为任一失败 |
| **加载失败** | 无 service_ts |

---

## 5. Debugify（`synia:debugify`）— 已闭环

版本 tag：`synia-debugify-0.1.0`。后续模块按 [MODULE_PIPELINE.md](MODULE_PIPELINE.md) 复用同一组检查点。

实测环境：NeoForge 21.1.264 / Minecraft 1.21.1 / Connector `2.0.0-beta.17+1.21.1` / Debugify `1.21.1+1.1` / Forgified Fabric API `0.116.15+2.3.6+1.21.1`。日期 2026-10-22。YACL 不在测试包内（3.8.2 在 Connector 下因缺 refmap 启动即崩，且只是 Debugify 的可选依赖）。

### 日志

| 检查 | 结果 |
|------|------|
| `service_ts` | 有。例：`2026-10-22T04:09:39.215810Z` |
| `write_ts_end` + `disk_audit` | `PASS`，`MC-93384=false`，`MC-176559=false`。写入早于 Debugify 应用修复列表 |
| 杀开关 | `Gate DISABLED` 时不写 `debugify.json` |
| Debugify 是否采用 | 门控开：`Enabled 56 bug fixes`，列表中无 `MC-93384` / `MC-176559`。门控关：`Enabled 58 bug fixes`，这两项都在 |

无配置文件时 Debugify 不一定打印 `Preloading Debugify`。等价证据是随后的 `Enabled N bug fixes` 列表。

### 行为（一句话）

| 组 | 结论 |
|----|------|
| **A** 门控开 | 可以挖掘，能看到裂痕；连续快速点同一格时进度会重置 |
| **B** 杀开关，jar 仍在 | 只有粒子，没有裂痕，无法挖掘 |
| **C** 移除 Synia jar | 与 B 相同 |
| **回归** 放回 jar、关掉杀开关 | 回到可挖掘。日志再次 `disk_audit=PASS`，修复数回到 56 |

**A ≠ B，且 B ≈ C。** 写入内容被 Debugify 采用，不是「日志顺序对、Mixin 已照常应用」的假阳性。不需要 companion / Jar-in-Jar。

杀开关若只写在 PCL 的 `VersionAdvanceJvm` 里，已打开的 PCL 不会重新读取。本次 B 组最终靠用户环境变量 `SYNIA_DEBUGIFY_ENABLED=false` 生效；测完已清除。
