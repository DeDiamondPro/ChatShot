package dev.dediamondpro.chatshot.mixins;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.dediamondpro.chatshot.util.ChatCopyUtil;
import net.minecraft.client.gl.Framebuffer;

@Mixin(Framebuffer.class)
public abstract class FramebufferMixin {
    @Inject(method = "beginWrite", at = @At("HEAD"))
    private void onBeginWrite(boolean setViewport, CallbackInfo ci) {
        if (ChatCopyUtil.tracking)
        {
            System.out.println("Binding framebuffer: " + this);
            Thread.dumpStack(); // Log stack trace to identify caller
        }
    }
}