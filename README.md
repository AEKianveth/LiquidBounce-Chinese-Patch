# LiquidBounce-Chinese-Patch（水影中文汉化）

给 [LiquidBounce](https://github.com/CCBlueX/LiquidBounce) 客户端做的简体中文汉化，打包成了一个 Fabric 模组，丢进 mods 文件夹就能用。

只翻界面上看得到的文字，模块名、指令、配置键这些内部标识一律保持英文，所以不影响功能，也不动存档。语言跟游戏设置走：在 LiquidBounce 设置里选中文就显示中文，选回英文就恢复原版，不用重启游戏。

当前版本 **4.16.0**，对应 **LiquidBounce 0.40.0 / Minecraft 26.2 / Fabric**。

## 都翻了哪些地方

- ClickGUI 里能看到的基本都翻了：模块名、分类、设置项、设置组、子模式、枚举取值，还有 HUD 组件的名字和说明。
- 搜索框可以直接输中文，比如打「自瞄」「搭桥」就能找到模块，原来的英文搜索也照常能用。
- 切语言即时生效，而且会停在你当前打开的页面，不会一切就被弹回 ClickGUI 主界面。
- 翻译表大概 5600 条。品牌名、服务器名、反作弊和网络协议这类专有名词按习惯保留英文，具体哪些、为什么不翻，写在下面。

## 哪些英文是故意没翻的

不是漏翻。下面这些翻了反而让人看不懂，或者直接把功能搞坏：

**品牌和软件名**
LiquidBounce、ClickGUI、HUD、Discord、GitHub、Microsoft、Mojang、Twitter / X、YouTube、TheAltening、Realms、ViaFabricPlus、OpenAI、osu!（命中音效名）等。

**服务器、反作弊、客户端名**
Hypixel、CubeCraft、Gomme、Mineland、Pika、Sentinel、Sigma、Dortware、Hycraft、Hylex、Grim、NCP、Verus、Intave、Vulcan、Polar、BattleAsya、Funtime、Miniblox、CheatBreaker、Badlion、Lunar、Feather、BungeeCord 等。

**技术和协议术语**
HTTP、HTTPS、SOCKS5、JWT、UUID、API、IP、DNS、IRC、SSL/TLS、TCP/UDP、URL、JSON、XML、HTML、CSS、WebSocket、Base64、SHA/AES/RSA、NBT、RGB/HSV/HEX、TPS/FPS、Ping、Tick、Unicode、Cookie、Pong 等。

**键盘按键**
Enter、Escape、Tab、Home 这些。前端是拿 `key === "Enter"` 这种写法判断按键的，翻了按键就会失灵；游戏里实际显示的按键名由 Minecraft 本体负责本地化。

**模块内部标识**
模块的内部 name（比如 `killAura`）、选项枚举值、配置键名、指令名。这些是模块匹配和存档读取的依据，翻了会让开关、保存设置、搜索这些功能出问题。

**第三方库内部的东西**
dayjs、flatpickr、chartjs 这些库自带的报错（Invalid date、No dataset found 之类）和代码标识符（Object、Map、Set、Event），界面上本来就不会显示。

**日期库里的数据**
月份、星期、Today/Tomorrow 这些。ClickGUI 里没有日历或日期选择器，玩家看不到。

要是你觉得上面哪一类其实也该翻，或者在界面上看到了清单之外的英文，欢迎提 Issue 带上截图，我看着补。

## 运行环境

| 项目 | 要求 |
| --- | --- |
| Minecraft | 26.2 |
| 模组加载器 | Fabric Loader（≥ 0.15.0） |
| 前置模组 1 | Fabric API（26.2 对应版本） |
| 前置模组 2 | Fabric Language Kotlin（如 2.4.0） |
| 目标客户端 | LiquidBounce 0.40.0（Fabric 版） |
| Java | Java 25（跟 LiquidBounce 0.40.0 保持一致） |

启动器无所谓，PCL2、Plain Craft Launcher、官方启动器都行。

## 安装

1. 先装好 Fabric Loader，确认 mods 文件夹里已经有 Fabric API、Fabric Language Kotlin 和 LiquidBounce 0.40.0 本体。
2. 在本仓库 `release/` 目录（或 Releases 页面）下载 `liquidbounce-clickgui-cn-4.16.0.jar`。如果拿到的是 `.zip`，先解压出里面的 `.jar`。
3. 把 jar 放进 Minecraft 的 `mods` 文件夹，跟 LiquidBounce 放一起。
4. 启动游戏，日志里出现 `[水影汉化-ClickGUI] 已加载 4.16.0 ……` 就是装好了。
5. 进游戏打开 ClickGUI，看到的就是中文。

### 切换中英文

在 LiquidBounce 的 Settings（设置）→ 语言里选「简体中文」显示中文，选「English (US)」显示英文，切完立刻生效。
如果游戏语言设成「自动」，模组会跟着 Minecraft 客户端语言走：客户端是中文就显示中文，否则显示英文。

## 源码目录

```
.
├── README.md                  # 本说明
├── LICENSE                    # MIT
├── release/                   # 构建好的发布 jar
└── clickgui/                  # 模组源码工程
    ├── build.sh               # 纯 javac 构建脚本（不用 Gradle）
    ├── resources/
    │   ├── fabric.mod.json    # Fabric 模组描述
    │   ├── lbcn.mixins.json   # Mixin 配置
    │   └── zh_all.json        # 前端翻译总表（约 5600 条）
    ├── src/com/lbcn/
    │   ├── LbcnMod.java       # 模组入口
    │   ├── LanguageReloader.java  # 监听语言切换、驱动界面重载
    │   ├── BundlePatcher.java / ThemePatcher.java / ZhTable.java / LangDetector.java
    │   └── mixin/             # 织入 LiquidBounce / Ktor 的 Mixin
    ├── tools/                 # 翻译表生成工具和数据源（data/、autogen/）
    └── test/                  # 自测
```

### 自己构建

```bash
cd clickgui
# 1) 把构建依赖（LiquidBounce 及其 Fabric/Ktor 等 jar）放进 libs/
# 2) 用 JDK 25 执行
JAVA_HOME=/path/to/jdk-25 bash build.sh
# 产物在 build/liquidbounce-clickgui-cn-1.0.0.jar
```

## 大概是怎么实现的

LiquidBounce 的 ClickGUI、HUD、Settings 其实是内嵌浏览器（CEF）加载的网页，资源由它自带的 Ktor 服务器提供。模组用 Mixin 做了几件事：

1. 让 Ktor 别缓存前端资源，保证每次都按当前语言重新取；
2. 按当前语言处理主页和 JS：中文就把翻译表注进去，英文保持原样，再让浏览器立刻刷新；
3. 监听语言切换事件，切回主线程让内嵌页面重新加载对应语言的资源。

这样切语言不用重启，中英文都能即时换。当前停在哪个标签页（点击界面 / HUD 编辑器 / 设置）会记在浏览器本地，切换语言重建页面后还回到原来那一页。

## 协议

- 代码用 MIT 协议开源，全文见 [LICENSE](LICENSE)。
- 这是第三方汉化，跟 LiquidBounce / CCBlueX 官方没有关系，各自的商标、品牌归各自所有，LiquidBounce 本体协议以它官方仓库为准。
- 仅供学习交流，使用时请遵守所在服务器的规则和当地法律法规。
- 碰到没翻到的英文或者显示异常，提 [Issue](https://github.com/AEKianveth/LiquidBounce-Chinese-Patch/issues) 时带上游戏版本、截图和日志，方便排查。
