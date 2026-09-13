package com.lbcn;

import java.io.File;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 第二路线（不依赖织入 Ktor 类）：把内置主题 resources/liquidbounce/themes/liquidbounce/
 * 整个解压到临时目录，仅对主 bundle JS 打显示层汉化补丁，其余文件原样复制。
 * 随后由 SpaConfigMixin 把 Ktor SPA 的资源源从 classpath 切到该文件目录（useResources=false）。
 *
 * 纯 JDK 逻辑，不依赖 Minecraft/Ktor，可独立单测。
 */
public final class ThemePatcher {
    private ThemePatcher() {}

    /** jar 内主题根（classpath 路径，已与 0.40.0 字节码核对一致）。 */
    public static final String THEME_PREFIX = "resources/liquidbounce/themes/liquidbounce/";

    private static volatile Path themeRoot;
    private static volatile boolean done;

    /** 准备（仅一次）并返回补丁主题根目录绝对路径；失败返回 null（调用方回退原始行为）。 */
    public static synchronized Path prepare() {
        if (done) return themeRoot;
        done = true; // 即使失败也只尝试一次，避免反复刷
        try {
            String table = ZhTable.json();
            URL indexUrl = ThemePatcher.class.getClassLoader().getResource(THEME_PREFIX + "index.html");
            if (indexUrl == null) {
                LbcnMod.LOGGER.warn("[水影汉化-ClickGUI] 未在 classpath 找到内置主题 {}", THEME_PREFIX);
                return null;
            }

            Path root = Files.createTempDirectory("lbcn-theme-");
            root.toFile().deleteOnExit();

            URLConnection conn = indexUrl.openConnection();
            if (conn instanceof JarURLConnection) {
                try (JarFile jar = ((JarURLConnection) conn).getJarFile()) {
                    copyAndPatch(jar, root, table);
                }
            } else {
                // 开发/展开目录环境：资源来自文件系统目录
                File dir = new File(indexUrl.toURI()).getParentFile();
                copyFolder(dir.toPath(), root, table);
            }

            themeRoot = root;
            LbcnMod.LOGGER.info("[水影汉化-ClickGUI] 已生成补丁主题目录：{}", root);
            return root;
        } catch (Throwable t) {
            LbcnMod.LOGGER.warn("[水影汉化-ClickGUI] 生成补丁主题目录失败，回退原始资源: {}", t.toString());
            return null;
        }
    }

    private static void copyAndPatch(JarFile jar, Path root, String table) throws Exception {
        int copied = 0, patched = 0;
        Enumeration<JarEntry> en = jar.entries();
        while (en.hasMoreElements()) {
            JarEntry e = en.nextElement();
            String name = e.getName();
            if (!name.startsWith(THEME_PREFIX) || e.isDirectory()) continue;
            String rel = name.substring(THEME_PREFIX.length());
            Path target = root.resolve(rel);
            Files.createDirectories(target.getParent());
            if (rel.endsWith(".js")) {
                String js;
                try (InputStream in = jar.getInputStream(e)) {
                    js = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                }
                if (BundlePatcher.isMainBundle(js)) {
                    String p = BundlePatcher.patch(js, table);
                    Files.writeString(target, p, StandardCharsets.UTF_8);
                    patched++;
                    continue;
                }
            }
            try (InputStream in = jar.getInputStream(e)) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            copied++;
        }
        LbcnMod.LOGGER.info("[水影汉化-ClickGUI] 主题文件复制 {} 个，主 bundle 补丁 {} 个", copied, patched);
    }

    private static void copyFolder(Path src, Path dst, String table) throws Exception {
        try (var stream = Files.walk(src)) {
            for (Path p : (Iterable<Path>) stream::iterator) {
                String rel = src.relativize(p).toString().replace('\\', '/');
                Path target = dst.resolve(rel);
                if (Files.isDirectory(p)) {
                    Files.createDirectories(target);
                } else if (rel.endsWith(".js") && BundlePatcher.isMainBundle(Files.readString(p, StandardCharsets.UTF_8))) {
                    String js = Files.readString(p, StandardCharsets.UTF_8);
                    Files.writeString(target, BundlePatcher.patch(js, table), StandardCharsets.UTF_8);
                } else {
                    Files.createDirectories(target.getParent());
                    Files.copy(p, target, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }
}
