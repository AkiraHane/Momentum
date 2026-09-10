# 动量（Momentum）

[English](README.md)

Momentum 是一个 Minecraft 跑酷移动模组，提供能够延续动量的地面、空中、墙面和水中机动，并配有对应的第一/第三人称动画与视效。

`1.3.0-beta` 开始使用 NeoForge/Fabric 共用的多加载器代码结构，目前定位为下一稳定版本发布前的测试版本。

| | |
|---|---|
| **模组 ID** | `momentum` |
| **当前源码版本** | `1.3.0-beta` |
| **Minecraft** | `26.1.2` |
| **加载器** | NeoForge `26.1.2.64-beta`；Fabric Loader `0.18.6` |
| **Java** | `25` |
| **作者** | AkiraHane |
| **协议** | All Rights Reserved |

## 1.3.0 Beta 重点更新

- 新增 `common` 公共模块，由 NeoForge 与 Fabric 共用核心跑酷逻辑。
- 完成 Fabric 适配，包括 Trinkets Updated、配置文件、调试信息、动画、摄像头视效与联机状态同步。
- 状态变化只广播给正在追踪该玩家实体的客户端，并在开始追踪时补发完整状态快照。
- 改善“墙跑 → 蹬墙跳 → 墙跑”的连续衔接：蹬墙跳保留已有动量，遵循正确的输入/视角方向，并提供短暂的墙跑再进入宽限。
- 蹬墙跳加速冷却默认值调整为 `10 tick`。
- 装备助推器墙跑时，无论原本向上还是向下，纵向速度都会逐渐趋近于零；普通墙跑仍使用原有重力，墙顶锁定仍是单独规则。
- 修复梯子异常升空、海豚跳接触梯子无法攀爬、坡面方向加速、滑铲保持，以及 Fabric 摄像头倾斜方向等问题。

## 安装

联机时，客户端与服务端都应安装本模组及对应的必需依赖。

1. 根据加载器选择 `momentum-neoforge-...jar` 或 `momentum-fabric-...jar`。
2. 安装相同 Minecraft 版本、相同加载器的 Player Animation Library。
3. Fabric 版本还必须安装 Fabric API。
4. NeoForge 的 Curios 和 Fabric 的 Trinkets Updated 均为可选依赖，用于给喷气助推器提供腰带饰品栏；不安装时仍可使用原版腿部装备槽。

### 依赖版本

| 依赖 | 版本 | 要求 |
|---|---|---|
| Player Animation Library | `1.2.3+mc.26.1` | 两个加载器均必需 |
| Fabric API | `0.145.4+26.1.2` | Fabric 必需 |
| Curios | `15.0.0-beta.2+26.1.2` | NeoForge 可选 |
| Trinkets Updated | `4.0.0+26.1` | Fabric 可选 |

## 按键

所有按键均可在 Minecraft 按键设置中重新绑定。

| 默认按键 | 功能 |
|---|---|
| `C` | 降低重心：匍匐、滑铲、准备受身、翻入低矮通道 |
| `Shift + M` | 为当前玩家开启或关闭 Momentum 机动模式 |
| `Shift + N` | 显示或隐藏情境按键提示 |
| 原版移动按键 | 墙跑、爬墙、蹬墙跳、闪避、蓄力跳和游泳使用玩家已绑定的移动/跳跃/疾跑/潜行键 |

## 动作系统

Momentum 当前共有 18 个状态机状态：17 个机动状态和 1 个原版回退状态。客户端每 tick 选择优先级最高且满足条件的状态，再通过服务端同步。

| 分类 | 动作 |
|---|---|
| **地面** | 行走、滑铲、匍匐、蓄力跳 |
| **空中/落地** | 空中移动、准备受身、受身、闪避 |
| **墙面** | 爬墙、滑墙、墙跑、挂墙、蹬墙跳、向上翻越、翻入低矮通道 |
| **水中** | 游泳和水中推进，包括冲出水面的海豚跳 |
| **回退** | 原版状态；关闭 Momentum 或没有机动动作匹配时使用 |

### 物理与动作衔接

- 空气阻力、空中转向、动作最低速度、饱食度消耗和冷却均可配置。
- 滑铲会降低摩擦。下坡时逐渐向检测到的坡面方向加速并逼近软速度上限，上坡时损失速度。
- 在梯子上按住疾跑键会提高纵向速度；向上速度设有边界，避免逐 tick 重复相乘造成异常升空。
- 墙跑维持沿墙切线方向的速度，只施加少量墙面法线推力。蹬墙跳保留当前水平动量，不再分别重写世界坐标 X/Z 分量。
- 受身可以减免摔落伤害；保留足够向前动量时可以继续衔接滑铲。
- 闪避与水中推进共用配置的恢复资源。

### 喷气助推器

喷气助推器可装备在原版腿部槽、Curios 腰带槽或 Trinkets Updated 腰带槽。装备后：

- 提高移动速度、跳跃强度和上台阶高度；
- 允许空中闪避；
- 减少摔落伤害和等效摔落距离；
- 在非墙顶区域移除墙跑重力，并让纵向速度平滑趋近于零；
- 在支持的动作中播放专用助推器音效。

该物品可以附魔，并拥有模组内置合成配方。

## 配置

多数动作同时具有服务端开关和客户端开关，两者都开启时动作才会生效。

- **NeoForge：** 使用 `ModConfigSpec`。客户端设置可通过加载器的配置界面编辑；服务端设置使用 NeoForge 的服务器配置生命周期。
- **Fabric：** 生成并校验 `config/momentum-client.json` 和 `config/momentum-server.json`，玩家加入时会同步服务端权威配置。目前 Fabric 尚无游戏内配置界面和实时文件重载；编辑 JSON 后需要重启或重新加入服务器。

已经存在的配置文件会保留保存过的数值，不会因为代码默认值变化而自动覆盖。例如已有配置中的 `wallKickAccelerationCooldown: 20`，若要采用新默认值，需要手动改成 `10`。

## 开发与构建

项目需要 JDK 25。IntelliJ IDEA 应将构建和测试委托给 Gradle；IDEA 原生构建器无法完整解析本项目为加载器元数据使用的资源占位符展开规则。

```bash
./gradlew build                       # 构建 common、Fabric 和 NeoForge
./gradlew :fabric:runClient           # Fabric 测试客户端 1
./gradlew :fabric:runClient2          # Fabric 测试客户端 2
./gradlew :fabric:runServer           # Fabric 测试服务端
./gradlew :neoforge:runClient         # NeoForge 测试客户端 1
./gradlew :neoforge:runClient2        # NeoForge 测试客户端 2
./gradlew :neoforge:runServer         # NeoForge 测试服务端
./gradlew :neoforge:runData           # 生成 NeoForge 数据/资源
```

Windows 下将 `./gradlew` 替换为 `gradlew.bat`。两个可发布 JAR 分别生成在：

```text
fabric/build/libs/
neoforge/build/libs/
```

### 项目结构

| 模块 | 职责 |
|---|---|
| `common` | Minecraft 26.1.2 的状态机、物理/效果系统、共享数据包、动画、资源和加载器无关 Mixin |
| `fabric` | Fabric 入口、生命周期事件、网络传输、附件数据、JSON 配置、渲染钩子和 Trinkets 兼容 |
| `neoforge` | NeoForge 入口/事件、网络传输、附件数据、`ModConfigSpec`、渲染事件和 Curios 兼容 |
| `build-logic` | 公共 Gradle 约定和资源处理逻辑 |

`common` 是“加载器无关”，不是“Minecraft 版本无关”。Minecraft API 和 Mixin 差异仍按 Minecraft 版本分支适配；完全不依赖 Minecraft 的逻辑以后可以继续下沉到纯 Java 引擎模块。移植约定参见 [PORTING.md](PORTING.md)。

### 联机同步模型

本地客户端计算动作状态转换并向服务端发送精简状态数据。服务端应用状态后，只广播给正在追踪该实体的玩家，并额外给自身发送一份以兼容回放工具。其他客户端开始追踪玩家实体时，服务端会补发状态快照，避免远程动画必须等到下一次状态变化才正确显示。

## 自动发布

推送版本标签后，GitHub Actions 会构建两个加载器 JAR、创建 GitHub Release，并分别将 NeoForge/Fabric 文件发布到 Modrinth 和 CurseForge。

GitHub 仓库需要配置以下 Actions Secrets：

- `MODRINTH_TOKEN`
- `CURSEFORGE_TOKEN`

标签必须与 `gradle.properties` 组合出的版本完全一致：

```text
v<mod_version>.<mod_build>-<mod_prerelease>+mc<minecraft_version>
```

当前源码版本对应的标签为 `v1.3.0-beta+mc26.1.2`。
GitHub 构建产物包名、发布平台版本标识和加载器 JAR 文件名也都会包含 Minecraft 版本，例如：

```text
momentum-neoforge-26.1.2-1.3.0-beta.jar
momentum-fabric-26.1.2-1.3.0-beta.jar
```

## 反馈

报告问题时，请附上 Momentum 版本、加载器、Minecraft 版本、已安装的可选依赖和复现步骤。联机同步问题还请注明影响本地玩家、远程玩家，或两者都会出现。

| 平台 | 联系方式 |
|---|---|
| QQ | `1796334524` |
| Bilibili | [@AkiraHane](https://space.bilibili.com/27666009) |

---

> 本文档由人工智能辅助整理，并根据当前源码进行了核对。
