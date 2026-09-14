# LiquidBounce-Chinese-Patch（水影中文汉化）

为 [LiquidBounce](https://github.com/CCBlueX/LiquidBounce) 客户端制作的**简体中文汉化模组**，基于 Fabric，采用「后端逻辑标识符保持英文、只在前端显示出口翻译」的标准 i18n 思路，不改动任何功能逻辑，并与 LiquidBounce 自带的语言设置**双向联动**。

当前发布版本：**4.14.0**　适配：**LiquidBounce 0.40.0 / Minecraft 26.2 / Fabric**

---

## 一、功能特性

- **ClickGUI 界面全汉化**：模块名称、功能分类、模块设置项、设置组、子模式、枚举取值、HUD 组件名称与描述等前端显示文字全部翻译为简体中文。
- **中文搜索**：在 ClickGUI 搜索栏可直接用中文搜索模块（例如输入「自瞄」「搭桥」即可定位对应模块），同时保留英文搜索。
- **语言切换联动**：在 LiquidBounce 设置中切换语言时实时生效——
  - 选择「简体中文 / 繁體中文」→ 界面显示中文；
  - 选择「English (US)」等英文语言 → 界面恢复为英文原版。
  - 无需重启游戏。
- **不影响功能**：仅替换前端显示文本，模块、事件、配置、指令等内部标识符全程保持英文，存档与配置兼容性不受影响。
- 翻译总表约 **4900+ 条**，覆盖 ClickGUI 显示层；品牌名、服务器名、反作弊/代理协议等专有名词按惯例保留英文原文。

---

## 二、运行环境

| 项目 | 要求 |
| --- | --- |
| Minecraft | 26.2 |
| 模组加载器 | Fabric Loader（≥ 0.15.0） |
| 前置模组 1 | Fabric API（26.2 对应版本） |
| 前置模组 2 | Fabric Language Kotlin（如 2.4.0） |
| 目标客户端 | LiquidBounce 0.40.0（Fabric 版） |
| Java | Java 25（与 LiquidBounce 0.40.0 保持一致） |

> 启动器不限，PCL2、Plain Craft Launcher、官方启动器等均可。

---

## 三、安装与使用方法

1. 先安装好 **Fabric Loader**，并确认 `mods` 文件夹内已有 **Fabric API** 与 **Fabric Language Kotlin**，以及 **LiquidBounce 0.40.0** 本体。
2. 前往本仓库 **`release/`** 目录，下载 `liquidbounce-clickgui-cn-4.14.0.jar`（下载到的若是 `.zip` 压缩包，先解压得到里面的 `.jar`）。
3. 把该 `.jar` 放入 Minecraft 的 **`mods`** 文件夹，与 LiquidBounce 并列。
4. 启动游戏。日志中出现 `[水影汉化-ClickGUI] 已加载 4.14.0 ……` 即代表加载成功。
5. 进入游戏后打开 ClickGUI 即为中文界面。

### 切换中英文

- 在 LiquidBounce 的 **Settings（设置）→ 语言** 中选择「简体中文」显示中文，选择「English (US)」显示英文，切换即时生效。
- 若游戏语言设置为「自动」，模组会跟随 Minecraft 客户端语言：客户端为中文时显示中文，否则显示英文。

---

## 四、目录结构（源码）

```
.
├── README.md                  # 本说明
├── LICENSE                    # MIT
├── release/                   # 已构建的发布模组 jar
└── clickgui/                  # 汉化模组源码工程
    ├── build.sh               # 纯 javac 构建脚本（无需 Gradle）
    ├── resources/
    │   ├── fabric.mod.json    # Fabric 模组描述
    │   ├── lbcn.mixins.json   # Mixin 配置
    │   └── zh_all.json        # 前端翻译总表（约4900+条）
    ├── src/com/lbcn/
    │   ├── LbcnMod.java       # 模组入口
    │   ├── LanguageReloader.java  # 监听语言切换并驱动界面重载
    │   ├── BundlePatcher.java / ThemePatcher.java / ZhTable.java / LangDetector.java
    │   └── mixin/             # 织入 LiquidBounce / Ktor 的 Mixin
    ├── tools/                 # 翻译表生成工具与数据源（data/、autogen/）
    └── test/                  # 自测
```

### 从源码构建

```bash
cd clickgui
# 1) 在 libs/ 目录放入构建依赖（LiquidBounce 及其 Fabric/Ktor 等依赖 jar）
# 2) 使用 JDK 25 执行
JAVA_HOME=/path/to/jdk-25 bash build.sh
# 产物：build/liquidbounce-clickgui-cn-1.0.0.jar
```

---

## 五、实现原理（简述）

LiquidBounce 的 ClickGUI/HUD/Settings 是内嵌浏览器（CEF）加载的前端页面，由内置 Ktor 服务器提供静态资源。模组通过 Mixin：

1. 在 Ktor 解析前端资源时**禁用其应用层资源缓存**，保证每次都按当前语言重新解析；
2. 对主页面 `index.html` 与主脚本 bundle 按当前语言实时处理——中文注入翻译表，英文返回原版，并通过 cache-busting 让浏览器即时刷新；
3. 监听 LiquidBounce 的语言变更事件，在主线程驱动内嵌页面重新导航到对应语言资源。

因此可以做到「切英文显英文、切中文显中文」且不重启。

---

## 六、说明与免责

- 本项目为第三方民间汉化，与 LiquidBounce / CCBlueX 官方无隶属关系；相关商标、品牌归各自所有者所有。
- 汉化仅用于学习交流，请遵守 LiquidBounce 所在服务器的规则与当地法律法规。
- 如遇界面仍有少量英文或显示异常，可提交 Issue 并附上游戏版本、截图与日志。
