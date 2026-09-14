package com.lbcn;

import net.ccbluex.liquidbounce.event.EventHook;
import net.ccbluex.liquidbounce.event.EventListener;
import net.ccbluex.liquidbounce.event.EventManager;
import net.ccbluex.liquidbounce.event.events.ClientLanguageChangedEvent;
import net.ccbluex.liquidbounce.integration.screen.ScreenManager;

import java.lang.reflect.Method;
import java.util.function.Consumer;

/**
 * 监听 LiquidBounce 客户端语言变化，重建集成浏览器，使汉化 bundle 按新语言重新加载。
 *
 * 为什么必须 restart() 而不是只给主浏览器 setUrl（v4.14.0 根因）：
 * 进入世界后，界面由多个相互独立的浏览器实例分别渲染——
 *   1) ScreenManager.mainBrowser：全屏/共享界面（设置页等）；
 *   2) ModuleClickGui 的 standalone/shared screen：ClickGUI（世界内为 #/clickgui?static）；
 *   3) ModuleHud 的 CustomOverlay：HUD 叠加层（#/hud?static），它持有自己独立的 Browser。
 * 早期版本只对 mainBrowser setUrl，主菜单下（仅有主浏览器）能切换；但世界内两个叠加层
 * 浏览器完全不被触碰，于是服务器虽按新语言返回了资源，屏幕上的 ClickGUI/HUD 仍是旧语言。
 *
 * ScreenManager.restart() 是 LiquidBounce 官方的“重建全部浏览器集成”方法（世界切换时它
 * 自己也走这条路径）：关闭并重建 mainBrowser、调用 ModuleClickGui.invalidate() 重建
 * ClickGUI、调用 ModuleHud.reopen() 重建 HUD 叠加层，三步各自 try-catch 不会整体崩溃。
 * 配合 StaticResourceMixin 对 Ktor 应用层资源缓存的禁用，所有新浏览器首次加载都会按当前
 * 语言实时 patch，从而主菜单与世界内都能一致切换。
 *
 * 线程：语言设置经 REST API 在 Ktor 线程改动，而浏览器必须在 MC 渲染线程操作，
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
        LbcnMod.LOGGER.info("[水影汉化-ClickGUI] 已注册客户端语言切换监听，切换语言将重建全部集成浏览器（主界面+ClickGUI+HUD叠加层）以加载对应语言");
    }

    private void onLanguageChanged(ClientLanguageChangedEvent event) {
        boolean zh = LangDetector.isChinese();
        LbcnMod.LOGGER.info("[水影汉化-ClickGUI] 检测到客户端语言切换（当前是否中文={}），调度重建全部集成浏览器……", zh);
        runOnMainThread(() -> {
            try {
                // 一次性重建主浏览器、ClickGUI、HUD 叠加层，覆盖世界内的全部独立浏览器实例。
                ScreenManager.INSTANCE.restart();
                LbcnMod.LOGGER.info("[水影汉化-ClickGUI] 已重建全部集成浏览器（{}），主界面/ClickGUI/HUD 将按该语言重新加载",
                        zh ? "中文" : "原文");
            } catch (Throwable t) {
                LbcnMod.LOGGER.warn("[水影汉化-ClickGUI] 语言切换后重建集成浏览器失败: {}", t.toString());
            }
        });
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
