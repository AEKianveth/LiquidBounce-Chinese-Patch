package com.lbcn;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 前端 bundle 显示层汉化补丁（纯逻辑，不依赖 Minecraft，可独立单测）。
 *
 * 设计原则：后端所有 JSON 的 name 保持英文（前端有 30+ 处用硬编码英文 name 做逻辑匹配，
 * 且 moduleToggle/choices/key 都依赖英文标识符，翻译 name 会导致功能与渲染崩溃）。
 * 只在"渲染给人看"的统一出口 qt() 做英→中映射，逻辑层全程英文，从而：
 *   1) 修复 qt 对纯中文 e.match(正则) 返回 null 再 .join 的 TypeError（旧版列表/设置空白根因）；
 *   2) 模块名、设置项名、选项名、HUD 名全部经 qt 显示，自动汉化；
 *   3) 未收录词条安全回退为英文驼峰加空格，绝不崩溃。
 */
public final class BundlePatcher {
    private BundlePatcher() {}

    public static final String MARK = "globalThis.__ZHD";
    // 原始 qt（特征串，也是要替换的目标）
    static final String QT_OLD =
            "function qt(e){const t=/[A-Z]?[a-z]+|[0-9]+|[A-Z]+(?![a-z])/g;return e.match(t).join(\" \")}";
    static final String QT_NEW =
            "function qt(e){e=globalThis.__zh(e);const t=/[A-Z]?[a-z]+|[0-9]+|[A-Z]+(?![a-z])/g;"
          + "const m=e.match(t);return m?globalThis.__zh(m.join(\" \")):e}";

    // COND?qt(EXPR):EXPR —— 两侧 EXPR 完全相同（反向引用 2）才归一为 qt(EXPR)，保证不误伤
    private static final Pattern RE_TERNARY = Pattern.compile(
            "([A-Za-z_$][\\w$]*(?:\\(\\))?)\\?qt\\(([^?:]+?)\\):\\2");
    // X.name.toLowerCase().includes(l(Y).toLowerCase()) —— 搜索框过滤，补中文匹配
    private static final Pattern RE_SEARCH = Pattern.compile(
            "([A-Za-z_$][\\w$]*)\\.name\\.toLowerCase\\(\\)\\.includes\\(l\\(([A-Za-z_$][\\w$]*)\\)\\.toLowerCase\\(\\)\\)");

    // 分类按钮：图标用英文小写名拼路径（必须保留英文），仅把显示文本 ft(f,i()) 汉化。该片段在 bundle 中唯一。
    static final String CAT_OLD = "Ot(u,\"src\",l(n)),ft(f,i())";
    static final String CAT_NEW = "Ot(u,\"src\",l(n)),ft(f,globalThis.__zh(i()))";

    // ClickGUI 面板分类标题（Misc/Player/Combat…）：图标路径保持英文，仅汉化标题文本 ft(R,c())。该片段唯一。
    static final String PANELCAT_OLD = "img/clickgui/icon-${z??\"\"}.svg`),ft(R,c())";
    static final String PANELCAT_NEW = "img/clickgui/icon-${z??\"\"}.svg`),ft(R,globalThis.__zh(c()))";

    // 搜索框占位符 / Bind 未绑定提示 / ClickGUI 固定文案（纯显示字面量，替换不影响任何逻辑）
    static final String[][] PLACEHOLDERS = {
            {"placeholder=\"Search...\"", "placeholder=\"搜索...\""},
            {"placeholder=\"Search\"", "placeholder=\"搜索\""},
            {"<span class=\"dimmed svelte-1pfgp8e\">None</span>", "<span class=\"dimmed svelte-1pfgp8e\">无</span>"},
            // 列表型设置项“添加值”按钮
            {"value:\"Add value\"", "value:\"添加值\""},
            // 按键绑定
            {"<span>Press any key...</span>", "<span>按任意键...</span>"},
            {"<span>Press any key</span>", "<span>按任意键</span>"},
            {"<div class=\"no-binds svelte-l5mzty\">No key bindings</div>", "<div class=\"no-binds svelte-l5mzty\">暂无按键绑定</div>"},
            {"<span class=\"title svelte-l5mzty\">Binds</span>", "<span class=\"title svelte-l5mzty\">按键绑定</span>"},
            // 空列表占位
            {"<div class=\"placeholder svelte-14vr88r\">No results</div>", "<div class=\"placeholder svelte-14vr88r\">无结果</div>"},
            {"<div class=\"placeholder svelte-rvd1lh\">No modules found</div>", "<div class=\"placeholder svelte-rvd1lh\">未找到模块</div>"},
            // 通用按钮 / 图标提示（title:"Add"、title:"Remove" 均为增删按钮，语义一致）
            {">Go</button>", ">前往</button>"},
            {"title:\"Add\"", "title:\"添加\""},
            {"title:\"Remove\"", "title:\"移除\""},
            {"title:\"Global Settings\"", "title:\"全局设置\""}
    };

    // 模块行右侧"展开设置"箭头：原版仅当模块存在除通用 Bind/Hidden 之外的自定义设置项时才渲染，
    // 导致 AutoConfig/AutoWalk/Parkour/SnapTap 等无自定义设置的模块没有箭头、无法展开（也就无法在 GUI 里绑定按键）。
    // 该片段在 bundle 中唯一；改为始终渲染箭头，展开后设置面板照常列出通用的 Bind/Hidden，与其它模块一致。
    static final String ARROW_OLD = "Z(B,W=>{l(p)&&W(q)})";
    static final String ARROW_NEW = "Z(B,W=>{W(q)})";

    // 通用下拉组件 ms(单选)/rs(多选)：选中判断与点击回写都用原始值 l(h)（绝不能改），
    // 只把“渲染文本”的 ft(...) 包一层 __zh；options 字面量保持英文，故不影响存储与后端。
    // 锚点均带各自 svelte class / active 判定，bundle 中唯一。
    static final String SEL_MS_OPTION = "ft(p,l(h))},[()=>({active:l(h)===i()})]";
    static final String SEL_MS_OPTION_ZH = "ft(p,globalThis.__zh(l(h)))},[()=>({active:l(h)===i()})]";
    static final String SEL_RS_OPTION = "ft(m,l(h))},[()=>({active:i().includes(l(h))})]";
    static final String SEL_RS_OPTION_ZH = "ft(m,globalThis.__zh(l(h)))},[()=>({active:i().includes(l(h))})]";
    // 单选下拉收起时标题旁显示的“当前值”
    static final String SEL_MS_CUR = "ft(h,` ${i()??\"\"}`)";
    static final String SEL_MS_CUR_ZH = "ft(h,` ${globalThis.__zh(i())??\"\"}`)";

    // 平铺选项列表叶子组件 yc（REGISTRY packet 列表 / 已选标签等都走它）：name 直接显示，补 __zh
    static final String LIST_ITEM_OLD = "ft(m,a())";
    static final String LIST_ITEM_ZH = "ft(m,globalThis.__zh(a()))";
    // 通用带搜索列表 Jh：过滤只匹配英文 name，补中文名一并参与匹配（输入中文也能搜到）
    static final String LIST_SEARCH_OLD = "const f=d.name.toLowerCase();return u.every(h=>f.includes(h))";
    static final String LIST_SEARCH_ZH = "const f=(d.name+\" \"+globalThis.__zh(d.name)).toLowerCase();return u.every(h=>f.includes(h))";
    // 账户卡片右上角类型徽标（Cracked/Microsoft…）：直接显示 type，补 __zh；品牌词查不到即保留原文
    static final String ACCOUNT_TYPE_OLD = "get text(){return l(I).type}";
    static final String ACCOUNT_TYPE_ZH = "get text(){return globalThis.__zh(l(I).type)}";
    // 账户条目内联类型（Cracked/Microsoft…）
    static final String ACCT_INLINE_OLD = "ft(Ie,l(Te).type)";
    static final String ACCT_INLINE_ZH = "ft(Ie,globalThis.__zh(l(Te).type))";
    // 通知 Toast 标题/正文（两处渲染组件）：包 __zh，固定标题可译、成句消息查不到则保留原文
    static final String NOTIFY_A_OLD = "ft(c,n()),ft(d,i())";
    static final String NOTIFY_A_ZH = "ft(c,globalThis.__zh(n())),ft(d,globalThis.__zh(i()))";
    static final String NOTIFY_B_OLD = "ft(d,l(r).notification.title),ft(h,l(r).notification.message)";
    static final String NOTIFY_B_ZH = "ft(d,globalThis.__zh(l(r).notification.title)),ft(h,globalThis.__zh(l(r).notification.message))";
    // HUD 组件描述（Add Component 列表副标题）
    static final String HUD_DESC_OLD = "ft(u,t.component.description)";
    static final String HUD_DESC_ZH = "ft(u,globalThis.__zh(t.component.description))";
    // 多值(TagList)设置新增输入框 placeholder=设置名
    static final String TAGLIST_PH_OLD = "Ot(k,\"placeholder\",r().name)";
    static final String TAGLIST_PH_ZH = "Ot(k,\"placeholder\",globalThis.__zh(r().name))";
    // 通用文本/密码输入框 placeholder=title（兜底动态英文 title；中文查不到原样返回）
    static final String INPUT_PH_A_OLD = "Ot(b,\"placeholder\",i())";
    static final String INPUT_PH_A_ZH = "Ot(b,\"placeholder\",globalThis.__zh(i()))";
    static final String INPUT_PH_B_OLD = "Ot(y,\"placeholder\",i())";
    static final String INPUT_PH_B_ZH = "Ot(y,\"placeholder\",globalThis.__zh(i()))";
    // 顶部当前账户条：service（Cracked/Microsoft 等）在线/离线两分支直接显示，未过翻译
    static final String ACCT_SERVICE_OLD = "nt(()=>ft(dt,l(d)))";
    static final String ACCT_SERVICE_ZH = "nt(()=>ft(dt,globalThis.__zh(l(d))))";
    // 子标签页按钮标题（AltManager 等界面的二级 tab）
    static final String SUBTAB_OLD = "sub-tab-button svelte-oa0oqv\",null,A,k),ft(E,l(x).title)";
    static final String SUBTAB_ZH = "sub-tab-button svelte-oa0oqv\",null,A,k),ft(E,globalThis.__zh(l(x).title))";
    // 标签页按钮标题（设置界面一级 tab）
    static final String TABBTN_OLD = "tab-button svelte-8tpz8n\",null,v,p),ft(g,l(d).title)";
    static final String TABBTN_ZH = "tab-button svelte-8tpz8n\",null,v,p),ft(g,globalThis.__zh(l(d).title))";
    // 设置组/设置项标题（EC 组件，setting-item 标题行）
    static final String SETTITLE_OLD = "Uc(u,()=>t.children),nt(()=>ft(o,t.title))";
    static final String SETTITLE_ZH = "Uc(u,()=>t.children),nt(()=>ft(o,globalThis.__zh(t.title)))";
    // HUD 已放置组件的 description tooltip（第二出口，与 Add Component 列表描述不同）
    static final String HUD_DESC2_OLD = "description svelte-1n35ikq\",null,p,w),ft(b,l(n).description)";
    static final String HUD_DESC2_ZH = "description svelte-1n35ikq\",null,p,w),ft(b,globalThis.__zh(l(n).description))";

    // 主菜单 / 账户(AltManager) / 多人服务器 / 代理 等界面的固定显示文案。
    // 全部带 title:/text:/alt= 等“显示属性”前缀，只改渲染文本，不碰任何逻辑值。
    // 品牌专名（LiquidBounce/Microsoft/Mojang/GitHub/Discord/Twitter/YouTube/TheAltening/
    // ViaFabricPlus/Realms/ClickGUI）与技术值（HTTP/SOCKS5/UUID）按惯例保留原文，不在此列。
    static final String[][] UI_STRINGS = {
            // —— 通用按钮 / 导航 ——
            {"title:\"Back\"", "title:\"返回\""},
            {"title:\"Check\"", "title:\"检查\""},
            {"title:\"Connect\"", "title:\"连接\""},
            {"title:\"Delete\"", "title:\"删除\""},
            {"title:\"Edit\"", "title:\"编辑\""},
            {"title:\"Exit\"", "title:\"退出\""},
            {"title:\"Favorite\"", "title:\"收藏\""},
            {"title:\"Forum\"", "title:\"论坛\""},
            {"title:\"Join\"", "title:\"加入\""},
            {"title:\"Login\"", "title:\"登录\""},
            {"title:\"Open\"", "title:\"打开\""},
            {"title=\"Open\"", "title=\"打开\""},
            {"title:\"Options\"", "title:\"选项\""},
            {"title:\"Random\"", "title:\"随机\""},
            {"title:\"Reconnect\"", "title:\"重连\""},
            {"title:\"Refresh\"", "title:\"刷新\""},
            {"title:\"Restore\"", "title:\"恢复\""},
            {"title:\"Disconnect\"", "title:\"断开连接\""},
            {"title:\"Settings\"", "title:\"设置\""},
            {"title:\"Singleplayer\"", "title:\"单人游戏\""},
            {"title:\"Multiplayer\"", "title:\"多人游戏\""},
            {"title:\"AltManager\"", "title:\"账户管理\""},
            {"title:\"Proxy Manager\"", "title:\"代理管理\""},
            {"title:\"ProxyManager\"", "title:\"代理管理\""},
            {"title:\"HUD Editor\"", "title:\"HUD 编辑器\""},
            {"title:\"Click GUI\"", "title:\"点击界面\""},
            {"title:\"Credentials\"", "title:\"凭据\""},
            {"title:\"Session\"", "title:\"会话\""},
            {"title:\"Direct\"", "title:\"直接\""},
            // —— 账户 AltManager ——
            {"title:\"Account Type\"", "title:\"账户类型\""},
            {"title:\"Add Account\"", "title:\"添加账户\""},
            {"title:\"Add Clipboard\"", "title:\"添加剪贴板\""},
            {"title:\"Cracked\"", "title:\"离线账户\""},
            {"title:\"Device Code\"", "title:\"设备代码\""},
            {"title:\"Direct Login\"", "title:\"直接登录\""},
            {"title:\"E-Mail\"", "title:\"邮箱\""},
            {"title:\"Forward Microsoft Authentication\"", "title:\"转发微软认证\""},
            {"title:\"Get Account Token\"", "title:\"获取账户令牌\""},
            {"title:\"Link Account\"", "title:\"关联账户\""},
            {"title:\"Password\"", "title:\"密码\""},
            {"title:\"Premium Only\"", "title:\"仅正版\""},
            {"title:\"Online only\"", "title:\"仅在线\""},
            {"title:\"Reconnect with random account\"", "title:\"随机账户重连\""},
            {"title:\"Reconnect with random username\"", "title:\"随机用户名重连\""},
            {"title:\"Requires Authentication\"", "title:\"需要认证\""},
            {"title:\"Restore initial session\"", "title:\"恢复初始会话\""},
            {"title:\"Sign in with web view\"", "title:\"网页方式登录\""},
            {"title:\"Token\"", "title:\"令牌\""},
            {"title:\"Use online UUID\"", "title:\"使用在线 UUID\""},
            {"title:\"Username\"", "title:\"用户名\""},
            {"title:\"Web View\"", "title:\"网页视图\""},
            {"text:\"Change account\"", "text:\"切换账户\""},
            {"text:\"Random username\"", "text:\"随机用户名\""},
            {"alt=\"change account\"", "alt=\"切换账户\""},
            {"alt=\"random username\"", "alt=\"随机用户名\""},
            {"alt=\"open-file\"", "alt=\"打开文件\""},
            {"alt=\"reset-file\"", "alt=\"重置文件\""},
            {"alt=\"taco no load :((\"", "alt=\"taco 加载失败 :((\""},
            {">Account list is empty<", ">账户列表为空<"},
            // —— 多人服务器 / 代理 ——
            {"title:\"Add Proxy\"", "title:\"添加代理\""},
            {"title:\"Add Server\"", "title:\"添加服务器\""},
            {"title:\"Edit Proxy\"", "title:\"编辑代理\""},
            {"title:\"Edit Server\"", "title:\"编辑服务器\""},
            {"title:\"Get Proxy\"", "title:\"获取代理\""},
            {"title:\"Proxy Type\"", "title:\"代理类型\""},
            {"title:\"Address\"", "title:\"地址\""},
            {"title:\"Host:Port\"", "title:\"主机:端口\""},
            {"title:\"Name\"", "title:\"名称\""},
            {"title:\"Country\"", "title:\"国家/地区\""},
            {"title:\"Type\"", "title:\"类型\""},
            {"title:\"Version\"", "title:\"版本\""},
            {"title:\"Difficulty\"", "title:\"难度\""},
            {"title:\"Game Mode\"", "title:\"游戏模式\""},
            {"title:\"Server Resource Packs\"", "title:\"服务器资源包\""},
            {"title:\"Direct Connection\"", "title:\"直接连接\""},
            {"title:\"Join Server\"", "title:\"加入服务器\""},
            {"title:\"Copy URL\"", "title:\"复制链接\""},
            {"title:\"Favorites Only\"", "title:\"仅收藏\""},
            {"title:\"Session ID\"", "title:\"会话 ID\""},
            {"placeholder=\"Enter URL\"", "placeholder=\"输入链接\""},
            {"text:\"Join Realms server\"", "text:\"加入 Realms 服务器\""},
            {"text:\"LAN\"", "text:\"局域网\""},
            // —— HUD 编辑器 / 其它 ——
            {"title:\"Toggle Shader\"", "title:\"切换背景\""},
            {"title:\"Install ViaFabricPlus\"", "title:\"安装 ViaFabricPlus\""},
            {"title=\"Open\"", "title=\"打开\""},
            {"title=\"Reset\"", "title=\"重置\""},
            {"title=\"Locate\"", "title=\"定位\""},
            {"title=\"Remove component\"", "title=\"移除组件\""},
            {"aria-label=\"Expand settings\"", "aria-label=\"展开设置\""},
            {">Add Component<", ">添加组件<"},
            {">No components found<", ">未找到组件<"},
            {">Force Reload<", ">强制重载<"},
            {"value:\"Module\"", "value:\"模块\""},
            // 颜色选择器同源自述（与可见的 save/cancel/clear 按钮对应）
            {"\"save and close\"", "\"保存并关闭\""},
            {"\"cancel and close\"", "\"取消并关闭\""},
            {"\"clear and close\"", "\"清除并关闭\""},
            // 第二处 None（class 与 PLACEHOLDERS 里那处不同）/ JSX 移除按钮
            {"class=\"none svelte-urxenx\">None</span>", "class=\"none svelte-urxenx\">无</span>"},
            {"title=\"Remove\"", "title=\"移除\""},
            // 开关标题（与模块译名一致）
            {"title:\"Auto Config\"", "title:\"自动配置\""},
            // 通知 toast（message 纯显示）
            {"message:\"Checking proxy from clipboard...\"", "message:\"正在检测剪贴板中的代理...\""},
            {"message:\"Connected to proxy\"", "message:\"代理已连接\""},
            {"message:\"Disconnected from proxy\"", "message:\"代理已断开\""},
            {"message:\"Proxy is working\"", "message:\"代理连接正常\""},
            {"message:\"The proxy is not working: \"", "message:\"代理无法连接： \""},
            // 服务器列表延迟为 0 时的红色失败提示（保留 §C 颜色码；bundle 中撇号被转义为 \'）
            {"§CCan't connect to server", "§C无法连接到服务器"},
            // 颜色选择器读屏（aria）标签，正常不可见，一并汉化以求彻底
            {"\"color input field\"", "\"颜色输入框\""},
            {"\"color picker dialog\"", "\"颜色选择器\""},
            {"\"color selection area\"", "\"颜色选择区\""},
            {"\"color swatch\"", "\"色块\""},
            {"\"hue selection slider\"", "\"色相滑块\""},
            {"\"selection slider\"", "\"选择滑块\""},
            {"\"toggle color picker dialog\"", "\"切换颜色选择器\""},
            {"\"use previous color\"", "\"使用上一个颜色\""},
            // 图标 <img alt> 兜底文本（图片正常时不显示，加载失败/读屏时可见，一并汉化）
            {"alt=\"absorption\"", "alt=\"吸收\""},
            {"alt=\"armor\"", "alt=\"护甲\""},
            {"alt=\"avatar\"", "alt=\"头像\""},
            {"aria-label=\"avatar\"", "aria-label=\"头像\""},
            {"alt=\"close\"", "alt=\"关闭\""},
            {"alt=\"disabled\"", "alt=\"已禁用\""},
            {"alt=\"drag\"", "alt=\"拖动\""},
            {"alt=\"enabled\"", "alt=\"已启用\""},
            {"alt=\"expand\"", "alt=\"展开\""},
            {"alt=\"fav\"", "alt=\"收藏\""},
            {"alt=\"health\"", "alt=\"生命\""},
            {"alt=\"icon\"", "alt=\"图标\""},
            {"alt=\"info\"", "alt=\"信息\""},
            {"alt=\"keybinds\"", "alt=\"按键绑定\""},
            {"alt=\"party-hat\"", "alt=\"派对帽\""},
            {"alt=\"preview\"", "alt=\"预览\""},
            {"alt=\"remove\"", "alt=\"移除\""},
            {"message:\"Logging in...\"", "message:\"正在登录...\""},
            {"message:\"Download it from liquidbounce.net!\"", "message:\"请前往 liquidbounce.net 下载！\""},
            // 启动加载页致谢语（中间 span 为动态名字，保持不动）
            {"<div>Thank you for ", "<div>感谢 "},
            {"<div>of LiquidBounce!</div>", "<div>为 LiquidBounce 做出贡献！</div>"},
            // 添加服务器默认名 / 添加物品按钮（锚点 bundle 唯一）
            {"?\"Cancel\":\"Add item\"", "?\"取消\":\"添加物品\""},
            {"$(\"Minecraft Server\")", "$(\"Minecraft 服务器\")"},
            {"S(r,\"Minecraft Server\")", "S(r,\"Minecraft 服务器\")"},
            // 颜色选择器（Pickr）按钮与无障碍标签
            {"\"btn:save\":\"Save\"", "\"btn:save\":\"保存\""},
            {"\"btn:cancel\":\"Cancel\"", "\"btn:cancel\":\"取消\""},
            {"\"btn:clear\":\"Clear\"", "\"btn:clear\":\"清除\""},
            {"\"aria:btn:save\":\"save and close\"", "\"aria:btn:save\":\"保存并关闭\""},
            {"\"aria:btn:cancel\":\"cancel and close\"", "\"aria:btn:cancel\":\"取消并关闭\""},
            {"\"aria:btn:clear\":\"clear and close\"", "\"aria:btn:clear\":\"清除并关闭\""},
            {"\"aria:input\":\"color input field\"", "\"aria:input\":\"颜色输入框\""},
            {"\"aria:palette\":\"color selection area\"", "\"aria:palette\":\"颜色选择区\""},
            {"\"aria:hue\":\"hue selection slider\"", "\"aria:hue\":\"色相滑块\""},
            {"\"aria:opacity\":\"selection slider\"", "\"aria:opacity\":\"透明度滑块\""},
            {"\"ui:dialog\":\"color picker dialog\"", "\"ui:dialog\":\"颜色选择器\""},
            {"\"btn:toggle\":\"toggle color picker dialog\"", "\"btn:toggle\":\"切换颜色选择器\""},
            {"\"btn:swatch\":\"color swatch\"", "\"btn:swatch\":\"色块\""},
    };

    /** 是否为需要补丁的主 bundle（含原始 qt 且尚未打过补丁）。 */
    public static boolean isMainBundle(String js) {
        return js != null && js.contains(QT_OLD) && !js.contains(MARK);
    }

    /**
     * 生成补丁后的 bundle。
     * @param js     原始前端 JS
     * @param zhJson 英→中词条 JSON 字符串
     * @return 补丁后 JS；若非目标 bundle 返回原文
     */
    public static String patch(String js, String zhJson) {
        if (!isMainBundle(js)) return js;

        // 1) 注入词条与查询函数。
        //    __zh 先精确查；查不到时把“带空格/撇号/连字符”的英文显示名规范化成驼峰再查，
        //    用同一套驼峰词条覆盖 MC 运行时英文名（Slow Falling→SlowFalling、
        //    Hero of the Village→HeroOfTheVillage、Dolphin's Grace→DolphinsGrace、
        //    Do Not Hide→DoNotHide）等 humanize 变体；仍查不到则原样返回，绝不抛错。
        String prelude = MARK + "=" + zhJson
                + ";globalThis.__zh=function(e){"
                + "if(typeof e!==\"string\")return e;"
                + "var d=globalThis.__ZHD;if(d[e])return d[e];"
                + "var k=e.replace(/['’`]/g,\"\").replace(/[\\s\\-]+(.)/g,function(m,c){return c.toUpperCase();}).replace(/[\\s\\-]/g,\"\");"
                + "return d[k]?d[k]:e;};";
        StringBuilder out = new StringBuilder(prelude.length() + js.length() + 4096).append(prelude);

        // 2) qt 改造（字面量替换）
        String body = js.replace(QT_OLD, QT_NEW);

        // 3) 条件三元归一
        Matcher tm = RE_TERNARY.matcher(body);
        body = tm.replaceAll("qt($2)");

        // 4) 搜索增强（中文命中）
        Matcher sm = RE_SEARCH.matcher(body);
        body = sm.replaceAll("(globalThis.__zh($1.name).toLowerCase().includes(l($2).toLowerCase())"
                + "||$1.name.toLowerCase().includes(l($2).toLowerCase()))");

        // 5) 分类按钮显示文本汉化（图标路径保持英文）
        body = body.replace(CAT_OLD, CAT_NEW);
        // 5b) ClickGUI 面板分类标题汉化（图标路径保持英文）
        body = body.replace(PANELCAT_OLD, PANELCAT_NEW);

        // 6) 搜索框占位符
        for (String[] ph : PLACEHOLDERS) body = body.replace(ph[0], ph[1]);

        // 6b) 通用下拉组件 ms/rs：只汉化显示出口，options 原始值与选中/回写逻辑保持英文
        body = body.replace(SEL_MS_OPTION, SEL_MS_OPTION_ZH);
        body = body.replace(SEL_RS_OPTION, SEL_RS_OPTION_ZH);
        body = body.replace(SEL_MS_CUR, SEL_MS_CUR_ZH);

        // 6b2) 平铺选项列表叶子 yc 显示出口 + 列表搜索中文命中（REGISTRY packet 列表等）
        body = body.replace(LIST_ITEM_OLD, LIST_ITEM_ZH);
        body = body.replace(LIST_SEARCH_OLD, LIST_SEARCH_ZH);
        body = body.replace(ACCOUNT_TYPE_OLD, ACCOUNT_TYPE_ZH);
        body = body.replace(ACCT_INLINE_OLD, ACCT_INLINE_ZH);
        body = body.replace(NOTIFY_A_OLD, NOTIFY_A_ZH);
        body = body.replace(NOTIFY_B_OLD, NOTIFY_B_ZH);
        body = body.replace(HUD_DESC_OLD, HUD_DESC_ZH);
        body = body.replace(TAGLIST_PH_OLD, TAGLIST_PH_ZH);
        body = body.replace(INPUT_PH_A_OLD, INPUT_PH_A_ZH);
        body = body.replace(INPUT_PH_B_OLD, INPUT_PH_B_ZH);
        body = body.replace(ACCT_SERVICE_OLD, ACCT_SERVICE_ZH);
        body = body.replace(SUBTAB_OLD, SUBTAB_ZH);
        body = body.replace(TABBTN_OLD, TABBTN_ZH);
        body = body.replace(SETTITLE_OLD, SETTITLE_ZH);
        body = body.replace(HUD_DESC2_OLD, HUD_DESC2_ZH);

        // 6c) 主菜单 / 账户 / 多人 / 代理 / HUD 等界面固定文案
        for (String[] u : UI_STRINGS) body = body.replace(u[0], u[1]);

        // 7) 无自定义设置项的模块也显示展开箭头（可在 GUI 内绑定按键 / 设置 Hidden）
        body = body.replace(ARROW_OLD, ARROW_NEW);

        return out.append(body).toString();
    }
}
