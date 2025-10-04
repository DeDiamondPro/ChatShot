package dev.dediamondpro.chatshot.mixins;

import dev.dediamondpro.chatshot.data.ChatHudLocals;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ChatComponent.class)
public abstract class ChatHudMixin implements ChatHudLocals {

    @Unique
    private int chatY = -1;

    @Unique
    private int chatBackgroundColor = -1;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    //? if >=1.21.6 {
    public void onDrawChatLine(GuiGraphics graphics, int currentTick, int mouseX, int mouseY, boolean focused, CallbackInfo ci, int i, int j, ProfilerFiller profiler, float f, int chatEnd, int l, int m, int n, float g, float h, double d, int o, int p, long q, int r, int v) {
        //?} else {
    
    /*public void onDrawChatLine(GuiGraphics graphics, int currentTick, int mouseX, int mouseY, boolean focused, CallbackInfo ci, int i, int j, ProfilerFiller profiler, float f, int chatEnd, int l, int m, int n, double d, double e, double g, int o, int p, int q, int r, int s, GuiMessage.Line visible, int t, double h, int u, int v) {
    *///?}

        // Collect some locals here that are used in ChatScreenMixin to draw the buttons,
        // we draw in the screen for Exordium compatibility,
        this.chatY = m;
        this.chatBackgroundColor = v << 24;
    }

    @Unique
    @Override
    public int chatShot$getChatY() {
        return chatY;
    }


    @Unique
    @Override
    public int chatShot$getChatBackgroundColor() {
        return chatBackgroundColor;
    }
}
