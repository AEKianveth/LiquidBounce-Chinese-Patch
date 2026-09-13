package com.lbcn.mixin;

import com.lbcn.LbcnMod;
import io.ktor.server.http.content.SPAConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * SPA 配置透传（v4.10.0 起）：始终保持 LiquidBounce 原始的 classpath 资源路线
 * （useResources=true），不再在启动时一次性切到补丁文件目录。
 *
 * 原因：旧版在服务器启动时根据语言一次性把资源源切到文件目录，之后用户在设置里
 * 切换语言（中↔英）不会重新触发 startServer，补丁目录永远是启动时的语言版本，
 * 导致"切英文仍显示中文"。
 *
 * 改为透传后，统一由 StaticResourceMixin 在每次前端请求 .js 时实时检测当前语言
 * 并按需 patch（中文→补丁版，英文→原始版），从而切换语言后下次加载界面即生效。
 */
@Mixin(targets = "net/ccbluex/liquidbounce/integration/interop/ClientInteropServer$startServer$engine$1", remap = false)
public class SpaConfigMixin {

    private static final AtomicBoolean LOGGED = new AtomicBoolean();

    @Redirect(
            method = "invokeSuspend$lambda$4$0",
            at = @At(value = "INVOKE",
                    target = "Lio/ktor/server/http/content/SPAConfig;setUseResources(Z)V",
                    remap = false),
            remap = false)
    private static void lbcn$useResources(SPAConfig config, boolean original) {
        if (LOGGED.compareAndSet(false, true)) {
            LbcnMod.LOGGER.info("[水影汉化-SPA] v4.10.0 动态模式：保持 useResources={}（走classpath），由 StaticResourceMixin 按语言实时patch",
                    original);
        }
        config.setUseResources(original); // 透传：始终保持原始值（true=走classpath）
    }

    @Redirect(
            method = "invokeSuspend$lambda$4$0",
            at = @At(value = "INVOKE",
                    target = "Lio/ktor/server/http/content/SPAConfig;setFilesPath(Ljava/lang/String;)V",
                    remap = false),
            remap = false)
    private static void lbcn$filesPath(SPAConfig config, String originalPath) {
        config.setFilesPath(originalPath); // 透传：保持原始路径
    }
}
