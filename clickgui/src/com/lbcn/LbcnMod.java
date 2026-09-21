package com.lbcn;

import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 水影中文汉化 - ClickGUI 子模块入口。
 * 架构：后端 JSON 全程保持英文，只在前端显示出口翻译（标准 i18n）。
 * v4.10.0 起改为单路线动态模式，v4.12.0 修复切换语言不生效（三层协同）：
 *  0) StaticResourceMixin 禁用 Ktor 应用层资源缓存 resourceCache（真正根因：它缓存
 *     首次解析的 URL，导致切语言后不再调用 getResources、语言 patch 被完全绕过）；
 *  1) LanguageReloader 监听 ClientLanguageChangedEvent，在 MC 主线程给浏览器 URL
 *     追加 _lbcn 时间戳并 setUrl 强制导航到全新 URL（不能用 forceReload，会中止
 *     尚未完成的新导航 ERR_ABORTED 并回退到旧 URL 缓存）；
 *  2) StaticResourceMixin 每次请求实时检测语言，index.html 每次重新生成、给 bundle
 *     引用加 ?lbcn=语言&_t=时间戳（cache-busting），主文档与子资源都绕开 CEF 缓存。
 */
public class LbcnMod implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("lbcn-clickgui");

    @Override
    public void onInitializeClient() {
        LOGGER.info("[水影汉化-ClickGUI] 已加载 4.16.0：修复切换语言后从设置页弹回ClickGUI的问题（顶部三标签页状态用localStorage记忆，中英文bundle均应用行为补丁，切换后停留在当前页面）；补全模块/组件/设置项描述(tooltip)汉化；语言切换联动（禁用Ktor资源缓存 + restart重建主界面/ClickGUI/HUD全部浏览器，主菜单与世界内均可切换），含模组图标——切英文显英文、切中文简体显中文；ClickGUI前端显示层全汉化，列表支持中文搜索；仅保留品牌·服务器·反作弊·代理协议专名；后端逻辑标识符保持英文；开源协议GPL-3.0");
        new LanguageReloader().register();
    }
}
