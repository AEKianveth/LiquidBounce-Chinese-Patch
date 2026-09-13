# clickgui —— 水影中文汉化（ClickGUI 前端显示层）

LiquidBounce 中文汉化模组的源码工程，负责 ClickGUI（模块列表、模块设置、分类、中文搜索）
以及前端界面的中英文联动切换。

## 核心架构：后端保英文，前端显示层翻译

LiquidBounce 的 ClickGUI/HUD/Settings 是内嵌浏览器（CEF）加载的前端页面，由内置 Ktor
服务器提供静态资源。若直接在后端把模块 `name` 改成中文，会导致两类问题：

1. 前端驼峰格式化函数对纯中文 `match` 返回 `null` 而抛错，渲染整棵组件崩溃（列表/设置空白）；
2. `name` 同时是逻辑标识符，前端有大量硬编码的英文名匹配、事件匹配、选项索引，翻译后功能错乱。

因此本工程采用标准 i18n 思路：**后端所有标识符保持英文，只在前端“渲染给人看”的统一出口翻译**。

| 组件 | 职责 |
| --- | --- |
| `LbcnMod` | Fabric 客户端入口，注册语言切换监听器 |
| `LanguageReloader` | 监听 `ClientLanguageChangedEvent`，在 MC 主线程给内嵌浏览器 URL 加时间戳并重新导航 |
| `BundlePatcher` | 纯逻辑补丁器：注入词条表与查询函数、null-safe 驼峰格式化、搜索中文匹配、分类/占位符汉化 |
| `ThemePatcher` / `ZhTable` / `LangDetector` | 主题资源处理、词条表、语言判定（AUTO 跟随客户端，中文语言才启用补丁） |
| `mixin/StaticResourceMixin` | 织入 Ktor 资源解析：禁用应用层资源缓存、按当前语言实时处理 index.html 与 bundle |
| `mixin/SpaConfigMixin` | 透传 LiquidBounce 原始 SPA 资源配置（走 classpath 资源路线） |
| `tools/` | 翻译表生成工具与数据源（`data/` 为人工/自动词表，构建时由 `Export` 生成 `resources/zh_all.json`） |

### 语言切换为什么能即时生效（v4.12.0）

Ktor 在 ClassLoader 之上有一层应用级资源缓存 `resourceCache`，会把首次解析到的资源 URL
按 `类加载器哈希|路径` 永久缓存；若不处理，首次（中文）资源被缓存后，切换语言将不再调用
资源定位，补丁被完全绕过。`StaticResourceMixin` 让该缓存查询恒为未命中，配合：

- 导航 URL 加时间戳（绕开 CEF 浏览器缓存，且不使用会中止新导航的 forceReload）；
- index.html 每次重新生成、其引用的 bundle 也带语言与时间戳参数；

从而做到「切英文显英文、切中文显中文」，无需重启。

## 构建

依赖 jar 放入 `libs/`（不入库）：LiquidBounce 0.40.0 及其 Fabric/Ktor/Kotlin 等依赖。需 JDK 25。

```bash
# JAVA_HOME 指向 JDK25；未设置则使用 PATH 中的 javac/java/jar
JAVA_HOME=/path/to/jdk-25 bash build.sh
# 1) tools 经 Export 生成 resources/zh_all.json
# 2) 编译 src
# 3) 输出 build/liquidbounce-clickgui-cn-1.0.0.jar
```

## 边界

- 未收录词条安全回退为英文驼峰显示，不会崩溃；补词条后重新构建即可。
- 品牌名、服务器名、反作弊/代理协议等专有名词按惯例保留英文。
- 后端逻辑标识符、配置与存档全程保持英文。
