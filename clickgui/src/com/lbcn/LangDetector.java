package com.lbcn;

import java.lang.reflect.Method;

/**
 * 语言检测。直接复用 LiquidBounce 自身的"最终生效语言"判定 currentLanguageChoice()，
 * 与 LB 翻译模块描述所用的语言保持 100% 一致（含 AUTO 解析），不自行推断。
 * 前端 bundle 只在打开界面时请求极少次，故每次实时反射判定，不做缓存，彻底避免
 * "启动早期语言尚未加载而误判、bundle 被浏览器缓存成英文"的时机问题。
 */
public final class LangDetector {
    private LangDetector() {}

    private static volatile Boolean logged = false;

    public static boolean isChinese() {
        try {
            Class<?> lmClass = Class.forName("net.ccbluex.liquidbounce.lang.LanguageManager");
            Object instance = lmClass.getField("INSTANCE").get(null);

            // 1) 最权威：LB 解析 AUTO 后的最终语言选择（private）
            String current = invokeEnum(lmClass, instance, "currentLanguageChoice", true);
            // 2) 兜底：手动组合
            if (current == null) {
                String client = invokeEnum(lmClass, instance, "getClientLanguage", false);
                if (client != null && !"AUTO".equals(client)) {
                    current = client;
                } else {
                    current = invokeEnum(lmClass, instance, "getMINECRAFT_LANGUAGE", false);
                }
            }
            boolean zh = current != null && current.startsWith("ZH");
            logOnce(current, null);
            return zh;
        } catch (Throwable t) {
            logOnce(null, t);
            return false;
        }
    }

    private static String invokeEnum(Class<?> owner, Object instance, String getter, boolean declared) {
        try {
            Method m = declared ? owner.getDeclaredMethod(getter) : owner.getMethod(getter);
            if (declared) m.setAccessible(true);
            Object v = m.invoke(instance);
            if (v instanceof Enum<?>) return ((Enum<?>) v).name();
            return v == null ? null : v.toString();
        } catch (Throwable t) {
            return null;
        }
    }

    private static void logOnce(String current, Throwable err) {
        if (logged) return;
        logged = true;
        if (err != null) {
            LbcnMod.LOGGER.warn("[水影汉化-ClickGUI] 语言检测异常，默认不汉化: {}", err.toString());
        } else {
            LbcnMod.LOGGER.info("[水影汉化-ClickGUI] LB 当前生效语言={}，是否中文={}", current,
                    current != null && current.startsWith("ZH"));
        }
    }
}
