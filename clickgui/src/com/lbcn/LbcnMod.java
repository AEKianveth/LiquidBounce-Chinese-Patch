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
        LOGGER.info("[水影汉化-ClickGUI] 已加载 4.12.0：语言切换联动（禁用Ktor资源缓存 + setUrl时间戳导航 + index.html/bundle双层cache-busting）——切英文显英文、切中文简体显中文；StaticResourceMixin实时检测语言patch，SpaConfigMixin透传；ClickGUI前端显示层全汉化，列表支持中文搜索；词条4918，仅保留品牌·服务器·反作弊·代理协议专名；后端逻辑标识符保持英文");
        new LanguageReloader().register();
    }
}
