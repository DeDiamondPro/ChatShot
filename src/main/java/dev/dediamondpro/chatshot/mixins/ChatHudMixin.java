package dev.dediamondpro.chatshot.mixins;

import dev.dediamondpro.chatshot.config.Config;
import dev.dediamondpro.chatshot.util.ChatCopyUtil;
import dev.dediamondpro.chatshot.util.Textures;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {
    @Shadow
    protected abstract int getLineHeight();

    @Shadow
    protected abstract boolean isChatFocused();

    @Shadow
    @Final
    private List<ChatHudLine.Visible> visibleMessages;

    @Shadow
    protected abstract double toChatLineY(double y);

    @Shadow
    protected abstract int getMessageIndex(double chatLineX, double chatLineY);

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    public abstract double getChatScale();

    @Shadow
    protected abstract double toChatLineX(double x);

    @Shadow
    private int scrolledLines;

    @Shadow
    public abstract int getVisibleLineCount();

    @Unique
    private boolean mousePressed = false;

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;III)I",
                    ordinal = 0, shift = At.Shift.AFTER, by = 1
            ), locals = LocalCapture.CAPTURE_FAILSOFT
    )
    void onLineRender(DrawContext context, int currentTick, int mouseX, int mouseY, CallbackInfo ci, int i, int j, boolean bl, float f, int k, int l, int m, int n, double d, double e, double g, int o, int p, int q, int r, int s, ChatHudLine.Visible visible, int t, double h, int u, int v, int w, int x, int y) {
        double scaledX = toChatLineX(mouseX);
        double scale = getChatScale();
        if (scaledX > k + 24 || mouseY / scale < y || mouseY / scale >= y + getLineHeight() || !isChatFocused()) return;
        int buttonX = k + 10;
        int buttonY = x - o;
        boolean hovering = scaledX >= buttonX && scaledX <= buttonX + 9;
        context.fill(buttonX, buttonY, buttonX + 9, buttonY + 9, hovering ? 0xFFFFFF + (v << 24) : v << 24);
        context.drawTexture(Textures.COPY, buttonX, buttonY, 0, 0, 9, 9, 9, 9);
        if (hovering && Config.INSTANCE.tooltip) {
            ArrayList<Text> tooltip = new ArrayList<>();
            tooltip.add(Text.translatable("chatshot.copy"));
            tooltip.add(Text.translatable("chatshot.click" + (Config.INSTANCE.clickAction == Config.CopyType.TEXT ? "Text" : "Image")));
            tooltip.add(Text.translatable("chatshot.shiftClick" + (Config.INSTANCE.shiftClickAction == Config.CopyType.TEXT ? "Text" : "Image")));
            context.drawTooltip(this.client.textRenderer, tooltip, mouseX, mouseY);
        }
        if (hovering && this.mousePressed) {
            context.draw(); // Make sure the current context has drawn everything before we start messing with frameBuffers
            int index = getMessageIndex(0, toChatLineY(mouseY));
            if (index == -1) return;
            ArrayList<ChatHudLine.Visible> messageParts = new ArrayList<>();
            messageParts.add(this.visibleMessages.get(index));
            for (int ii = index + 1; ii < this.visibleMessages.size(); ii++) {
                if (this.visibleMessages.get(ii).endOfEntry()) break;
                messageParts.add(0, this.visibleMessages.get(ii));
            }
            if (messageParts.isEmpty()) return;
            ChatCopyUtil.copy(messageParts, this.client);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    void onDraw(DrawContext context, int currentTick, int mouseX, int mouseY, CallbackInfo ci) {
        if (!isChatFocused()) return;
        int buttonX = this.client.getWindow().getScaledWidth() - 12;
        int buttonY = this.client.getWindow().getScaledHeight() - 26;
        boolean hovering = mouseX >= buttonX && mouseX <= buttonX + 10 && mouseY >= buttonY && mouseY <= buttonY + 10;
        int color = this.client.options.getTextBackgroundColor(Integer.MIN_VALUE);
        context.fill(buttonX, buttonY, buttonX + 10, buttonY + 10, hovering ? 0xFFFFFF + color : color);
        context.drawTexture(Textures.SCREENSHOT, buttonX, buttonY, 0, 0, 10, 10, 10, 10);
        if (this.mousePressed && hovering) {
            ArrayList<ChatHudLine.Visible> lines = new ArrayList<>();
            for (int i = this.scrolledLines; i < this.visibleMessages.size() && i < getVisibleLineCount() + this.scrolledLines; i++) {
                lines.add(0, this.visibleMessages.get(i));
            }
            ChatCopyUtil.copyImage(lines, this.client);
        }
        this.mousePressed = false;
    }

    @Inject(method = "mouseClicked", at = @At("RETURN"))
    void onMouseClick(double mouseX, double mouseY, CallbackInfoReturnable<Boolean> cir) {
        this.mousePressed = !cir.getReturnValue();
    }
}
