package com.lbcn;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** 词条表加载（classpath /zh_all.json，一次性缓存）。 */
public final class ZhTable {
    private static volatile String json;
    private static volatile boolean loaded;

    private ZhTable() {}

    public static String json() {
        if (!loaded) {
            synchronized (ZhTable.class) {
                if (!loaded) {
                    try (InputStream in = ZhTable.class.getResourceAsStream("/zh_all.json")) {
                        json = in == null ? null : new String(in.readAllBytes(), StandardCharsets.UTF_8);
                    } catch (Throwable t) {
                        LbcnMod.LOGGER.warn("[水影汉化-ClickGUI] 读取 zh_all.json 失败: {}", t.toString());
                        json = null;
                    }
                    loaded = true;
                }
            }
        }
        return json;
    }
}
