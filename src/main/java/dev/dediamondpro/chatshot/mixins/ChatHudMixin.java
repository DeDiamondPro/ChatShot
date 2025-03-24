package dev.dediamondpro.chatshot.mixins;

import dev.dediamondpro.chatshot.data.ChatHudLocals;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin implements ChatHudLocals {

    @Unique
    private int chatY = -1;

    @Unique
    private int chatBackgroundColor = -1;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    
    //#if MC >= 12104
    public void onDrawChatLine(DrawContext context, int currentTick, int mouseX, int mouseY, boolean focused, CallbackInfo ci, int i, int j, Profiler profiler, float f, int chatEnd, int l, int m, int n, double d, double e, double g, int o, int p, int q, int r, int s, ChatHudLine.Visible visible, int t, double h, int u, int v) {
    //#elseif MC >= 12005
    //$$ public void onDrawChatLine(DrawContext context, int currentTick, int mouseX, int mouseY, boolean focused, CallbackInfo ci, int i, int j, float f, int chatEnd, int l, int m, int n, double d, double e, double g, int o, int p, int q, int r, int s, ChatHudLine.Visible visible, int t, double h, int u, int v) {
    //#else
    //$$ public void onDrawChatLine(DrawContext context, int currentTick, int mouseX, int mouseY, CallbackInfo ci, int i, int j, boolean bl, float f, int chatEnd, int l, int m, int n, double d, double e, double g, int o, int p, int q, int r, int s, ChatHudLine.Visible visible, int t, double h, int u, int v) {
    //#endif
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
