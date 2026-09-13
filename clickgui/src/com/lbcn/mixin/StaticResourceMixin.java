package com.lbcn.mixin;

import com.lbcn.BundlePatcher;
import com.lbcn.LangDetector;
import com.lbcn.LbcnMod;
import com.lbcn.ZhTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 前端资源运行时动态 patch（v4.12.0：语言切换联动）。
 *
 * 三层协同，缺一不可：
 *  0. lbcn$bypassResourceCache：禁用 Ktor 应用层资源缓存 resourceCache。Ktor 首次解析后
 *     会把资源 URL 按 classLoader哈希|路径 永久缓存，之后同资源直接命中、不再调
 *     getResources——首次（中文 patch 临时文件）URL 被缓存后，切语言就再也走不到 patch，
 *     这是“切英文仍显中文、连资源请求日志都没有”的真正根因（与浏览器侧缓存无关）。
 *  1. index.html：每次重新生成，给主 bundle 的 script src 加 ?lbcn=语言&_t=时间戳，
 *     主文档与子资源 URL 都不同，绕开 CEF 浏览器缓存。
 *  2. *.js：实时检测当前语言——中文返回补丁 bundle，英文/其它返回原始 bundle。
 *  3. 其余资源原样返回。
 */
@Pseudo
@Mixin(targets = "io/ktor/server/http/content/StaticContentResolutionKt", remap = false)
public class StaticResourceMixin {

    private static final Map<String, URL> PATCHED = new ConcurrentHashMap<>();
    private static volatile Path tempDir;
    private static volatile boolean loggedOnce;
    private static volatile int traceCount;
    private static final AtomicReference<String> lastLang = new AtomicReference<>();
    /** 匹配 index.html 中引用 assets/index-xxxx.js 的 script 标签（带或不带已有 query） */
    private static final Pattern SCRIPT_SRC = Pattern.compile(
            "(<script[^>]+src=[\"'])(\\./assets/[A-Za-z0-9_\\-]+\\.js)(\\?[^\"']*)?([\"'])");

    @Inject(method = "<clinit>", at = @At("HEAD"), remap = false, require = 0)
    private static void lbcn$clinitProbe(CallbackInfo ci) {
        LbcnMod.LOGGER.info("[水影汉化-ClickGUI] 已成功织入 Ktor 资源解析类 StaticContentResolutionKt（动态语言切换模式 v4.12，已禁用应用层资源缓存）");
    }

    @Redirect(
            method = "resolveResource(Lio/ktor/server/application/Application;Ljava/lang/String;Ljava/lang/String;Ljava/lang/ClassLoader;Lkotlin/jvm/functions/Function1;)Lkotlin/Pair;",
            at = @At(value = "INVOKE",
                    target = "Ljava/lang/ClassLoader;getResources(Ljava/lang/String;)Ljava/util/Enumeration;",
                    remap = false),
            remap = false, require = 0)
    private static Enumeration<URL> lbcn$resourcesApp(ClassLoader classLoader, String name) {
        return lbcn$redirect(classLoader, name);
    }

    @Redirect(
            method = "resolveResource(Lio/ktor/server/application/ApplicationCall;Ljava/lang/String;Ljava/lang/String;Ljava/lang/ClassLoader;Lkotlin/jvm/functions/Function1;)Lio/ktor/http/content/OutgoingContent$ReadChannelContent;",
            at = @At(value = "INVOKE",
                    target = "Ljava/lang/ClassLoader;getResources(Ljava/lang/String;)Ljava/util/Enumeration;",
                    remap = false),
            remap = false, require = 0)
    private static Enumeration<URL> lbcn$resourcesCall(ClassLoader classLoader, String name) {
        return lbcn$redirect(classLoader, name);
    }

    /**
     * 关键：禁用 Ktor 的应用层资源缓存 resourceCache（ConcurrentHashMap，key=classLoader哈希|path）。
     * Ktor 首次解析资源后会把 URL 永久缓存，之后同一资源直接命中缓存、不再调用
     * ClassLoader.getResources——于是首次（如中文 patch 临时文件）URL 被缓存后，切换语言
     * 再也不会走到上面的语言 patch，这正是“切英文仍显中文、且连资源请求日志都没有”的根因。
     * 这里让缓存查询恒为未命中，每次都重新走 getResources（按当前语言实时 patch）。
     */
    @Redirect(
            method = "resolveResource(Lio/ktor/server/application/Application;Ljava/lang/String;Ljava/lang/String;Ljava/lang/ClassLoader;Lkotlin/jvm/functions/Function1;)Lkotlin/Pair;",
            at = @At(value = "INVOKE",
                    target = "Ljava/util/concurrent/ConcurrentHashMap;get(Ljava/lang/Object;)Ljava/lang/Object;",
                    remap = false),
            remap = false, require = 0)
    private static Object lbcn$bypassResourceCache(ConcurrentHashMap<?, ?> cache, Object key) {
        return null; // 恒未命中，强制每次重新解析资源
    }

    private static Enumeration<URL> lbcn$redirect(ClassLoader classLoader, String rawName) {
        // 分离可能存在的 query string（cache-busting），用纯路径定位 classpath 资源
        String name = rawName;
        if (name != null) {
            int qi = name.indexOf('?');
            if (qi >= 0) name = name.substring(0, qi);
        }

        Enumeration<URL> original;
        try {
            original = classLoader.getResources(name);
        } catch (Throwable t) {
            return Collections.emptyEnumeration();
        }

        List<URL> urls = new ArrayList<>();
        while (original.hasMoreElements()) urls.add(original.nextElement());

        if (name != null && (name.endsWith(".js") || name.endsWith(".html"))) {
            LbcnMod.LOGGER.info("[水影汉化-ClickGUI] 资源请求：name={} 候选URL数={}", name, urls.size());
        }

        try {
            boolean zh = LangDetector.isChinese();
            String lang = zh ? "zh" : "en";
            String prev = lastLang.getAndSet(lang);
            if (prev != null && !prev.equals(lang)) {
                LbcnMod.LOGGER.info("[水影汉化-ClickGUI] 语言切换检测：{} -> {}，本次请求返回{}资源",
                        prev, lang, zh ? "中文" : "原始");
            }

            if (name == null) return Collections.enumeration(urls);

            // 1) HTML：每次都重新生成（不缓存），bundle 引用带语言+时间戳，
            //    确保切换语言后 index.html 与其子资源 bundle 都不命中 CEF 缓存。
            if (name.endsWith(".html")) {
                List<URL> out = new ArrayList<>();
                for (URL u : urls) out.add(lbcn$patchHtml(u, lang));
                return Collections.enumeration(out);
            }

            // 2) 非中文：JS 与其它资源一律原样返回
            if (!zh || !name.endsWith(".js")) {
                return Collections.enumeration(urls);
            }

            // 3) 中文 JS：patch 主 bundle
            String table = ZhTable.json();
            if (table == null) return Collections.enumeration(urls);

            List<URL> out = new ArrayList<>(urls.size());
            for (URL u : urls) out.add(lbcn$patchIfMain(name, u, table, lang));
            return Collections.enumeration(out);
        } catch (Throwable t) {
            LbcnMod.LOGGER.warn("[水影汉化-ClickGUI] 资源重定向异常，回退原始资源 {}: {}", name, t.toString());
            return Collections.enumeration(urls);
        }
    }

    /**
     * 给 index.html 内的主 bundle script 标签加 ?lbcn=语言&_t=时间戳 参数。
     * 时间戳保证每次生成的 bundle 引用 URL 都不同，CEF 无法用缓存，必须重新请求。
     * 固定文件名覆盖写，不会产生大量临时文件。
     */
    private static URL lbcn$patchHtml(URL url, String lang) {
        try {
            byte[] bytes;
            try (InputStream in = url.openStream()) {
                bytes = in.readAllBytes();
            }
            String html = new String(bytes, StandardCharsets.UTF_8);
            if (!html.contains("assets/index-")) return url; // 非目标页面
            String nonce = lang + "&_t=" + System.nanoTime();
            Matcher m = SCRIPT_SRC.matcher(html);
            StringBuffer sb = new StringBuffer();
            boolean changed = false;
            while (m.find()) {
                String repl = m.group(1) + m.group(2) + "?lbcn=" + nonce + m.group(4);
                m.appendReplacement(sb, Matcher.quoteReplacement(repl));
                changed = true;
            }
            m.appendTail(sb);
            if (!changed) return url;
            Path file = lbcn$writeTemp("index.html", sb.toString().getBytes(StandardCharsets.UTF_8));
            URL fileUrl = file.toUri().toURL();
            LbcnMod.LOGGER.info("[水影汉化-ClickGUI] index.html 已注入 bundle 缓存参数 lbcn={}（带时间戳）", lang);
            return fileUrl;
        } catch (Throwable t) {
            LbcnMod.LOGGER.warn("[水影汉化-ClickGUI] patch index.html 失败，回退: {}", t.toString());
            return url;
        }
    }

    private static URL lbcn$patchIfMain(String name, URL url, String table, String lang) {
        try {
            String cacheKey = name + "|js|" + lang;
            URL cached = PATCHED.get(cacheKey);
            if (cached != null) return cached;

            byte[] bytes;
            try (InputStream in = url.openStream()) {
                bytes = in.readAllBytes();
            }
            String js = new String(bytes, StandardCharsets.UTF_8);
            if (!BundlePatcher.isMainBundle(js)) return url;

            String patched = BundlePatcher.patch(js, table);
            Path file = lbcn$writeTemp("bundle.js", patched.getBytes(StandardCharsets.UTF_8));
            URL fileUrl = file.toUri().toURL();
            PATCHED.put(cacheKey, fileUrl);
            if (!loggedOnce) {
                loggedOnce = true;
                LbcnMod.LOGGER.info("[水影汉化-ClickGUI] 已替换前端主 bundle：{}（{} 字节 -> {} 字节，语言={}）",
                        name, bytes.length, patched.length(), lang);
            }
            return fileUrl;
        } catch (Throwable t) {
            LbcnMod.LOGGER.warn("[水影汉化-ClickGUI] 补丁主 bundle 失败，回退原始 URL {}: {}", name, t.toString());
            return url;
        }
    }

    private static synchronized Path lbcn$writeTemp(String leaf, byte[] data) throws Exception {
        if (tempDir == null) {
            Path dir = Files.createTempDirectory("lbcn-clickgui-");
            dir.toFile().deleteOnExit();
            tempDir = dir;
        }
        if (!leaf.endsWith(".js") && !leaf.endsWith(".html")) leaf = leaf + ".tmp";
        Path target = tempDir.resolve(leaf);
        Files.write(target, data); // 覆盖写入，更新最后修改时间
        target.toFile().deleteOnExit();
        return target;
    }
}
