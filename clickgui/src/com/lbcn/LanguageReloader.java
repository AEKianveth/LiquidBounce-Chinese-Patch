package com.lbcn;

import net.ccbluex.liquidbounce.event.EventHook;
import net.ccbluex.liquidbounce.event.EventListener;
import net.ccbluex.liquidbounce.event.EventManager;
import net.ccbluex.liquidbounce.event.events.ClientLanguageChangedEvent;
import net.ccbluex.liquidbounce.integration.backend.browser.Browser;
import net.ccbluex.liquidbounce.integration.screen.ScreenManager;

import java.lang.reflect.Method;
import java.util.function.Consumer;

/**
 * 监听 LiquidBounce 客户端语言变化，自动重载集成浏览器，使汉化 bundle 按新语言重新加载。
 *
 * 背景：LB 切换语言只发出 ClientLanguageChangedEvent（并经 WebSocket 推给前端），
 * 但前端并不会因此重新下载 bundle.js；而本汉化是对 bundle 的静态补丁，不重新加载就
 * 永远停留在上一种语言。这里在语言变化时给浏览器当前 URL 追加一个每次不同的
 * _lbcn 时间戳参数并 setUrl 强制导航到全新 URL——CEF 对新 URL 不可能命中任何缓存，
 * 必然重新请求 index.html；StaticResourceMixin 每次生成 index.html 时给 bundle 引用
 * 也加上时间戳，确保子资源同样不命中缓存。注意不能紧接着 forceReload：loadURL 是
 * 异步的，forceReload 会取消尚未完成的新导航、转而重载旧 URL，反而命中缓存。
 *
 * 线程：语言设置经 REST API 在 Ktor 线程改动，而 CEF 浏览器必须在 MC 渲染线程操作，
 * 故通过反射 Minecraft.getInstance().execute(...) 调度到主线程（编译期不依赖 MC jar）。
 */
public class LanguageReloader implements EventListener {

    private static final short PRIORITY_NORMAL = 0;
    private static Method mcExecute;
    private static Object mcInstance;
    private static boolean schedulerResolved;

    /** 恒为运行中，确保语言变化事件一定能分发到本监听器。 */
    @Override
    public boolean getRunning() {
        return true;
    }

    @Override
    public EventListener parent() {
        return null;
    }

    public void register() {
        Consumer<ClientLanguageChangedEvent> consumer = this::onLanguageChanged;
        EventHook<ClientLanguageChangedEvent> hook =
                new EventHook<>(this, PRIORITY_NORMAL, consumer);
        EventManager.INSTANCE.registerEventHook(ClientLanguageChangedEvent.class, hook);
        LbcnMod.LOGGER.info("[水影汉化-ClickGUI] 已注册客户端语言切换监听，切换语言将强制导航到带时间戳的新URL（彻底绕缓存）以加载对应语言界面");
    }

    private void onLanguageChanged(ClientLanguageChangedEvent event) {
        boolean zh = LangDetector.isChinese();
        LbcnMod.LOGGER.info("[水影汉化-ClickGUI] 检测到客户端语言切换（当前是否中文={}），调度浏览器强制导航……", zh);
        runOnMainThread(() -> {
            try {
                ScreenManager sm = ScreenManager.INSTANCE;
                Browser browser = sm.getMainBrowser();
                if (browser == null) {
                    LbcnMod.LOGGER.info("[水影汉化-ClickGUI] 集成浏览器尚未创建，跳过本次重载（界面打开时会按当前语言加载）");
                    return;
                }
                // 最彻底的缓存绕过：给当前 URL 追加一个每次不同的 _lbcn 时间戳参数，
                // 再 setUrl 强制导航到全新 URL——CEF 对新 URL 不可能命中任何缓存，
                // 必然重新请求 index.html，StaticResourceMixin 按新语言生成带时间戳的
                // bundle 引用。不能紧接着 forceReload：loadURL 异步，forceReload 会取消
                // 尚未完成的新导航、转而重载旧 URL，反而命中缓存。
                String url = browser.getUrl();
                String newUrl = appendCacheBuster(url, "_lbcn", Long.toString(System.nanoTime()));
                browser.setUrl(newUrl);
                LbcnMod.LOGGER.info("[水影汉化-ClickGUI] 已强制导航到新URL（{}），原URL={} 新URL={}",
                        zh ? "中文" : "原文", url, newUrl);
            } catch (Throwable t) {
                LbcnMod.LOGGER.warn("[水影汉化-ClickGUI] 语言切换后强制导航浏览器失败: {}", t.toString());
            }
        });
    }

    /** 给 URL 追加/替换 query 参数，保留 hash 部分。 */
    private static String appendCacheBuster(String url, String key, String value) {
        if (url == null || url.isEmpty()) return url;
        String base = url;
        String hash = "";
        int hashIdx = url.indexOf('#');
        if (hashIdx >= 0) {
            base = url.substring(0, hashIdx);
            hash = url.substring(hashIdx);
        }
        // 移除已有的同名参数，避免叠加
        base = base.replaceAll("[?&]" + key + "=[^&]*", "");
        String sep = base.contains("?") ? "&" : "?";
        return base + sep + key + "=" + value + hash;
    }

    /** 反射调用 Minecraft.getInstance().execute(Runnable)，把任务切到 MC 渲染线程。 */
    private static void runOnMainThread(Runnable task) {
        try {
            if (!schedulerResolved) {
                schedulerResolved = true;
                Class<?> mcClass = Class.forName("net.minecraft.client.Minecraft");
                Method getInstance = mcClass.getMethod("getInstance");
                mcInstance = getInstance.invoke(null);
                mcExecute = mcClass.getMethod("execute", Runnable.class);
            }
            if (mcInstance != null && mcExecute != null) {
                mcExecute.invoke(mcInstance, task);
                return;
            }
        } catch (Throwable t) {
            LbcnMod.LOGGER.warn("[水影汉化-ClickGUI] 获取MC主线程调度器失败，回退当前线程执行: {}", t.toString());
        }
        task.run();
    }
}
