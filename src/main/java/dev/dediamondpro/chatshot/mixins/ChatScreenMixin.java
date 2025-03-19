package dev.dediamondpro.chatshot.mixins;

import dev.dediamondpro.chatshot.data.ChatHudLocals;
import dev.dediamondpro.chatshot.compat.CompatCore;
import dev.dediamondpro.chatshot.config.Config;
import dev.dediamondpro.chatshot.util.ChatCopyUtil;
import dev.dediamondpro.chatshot.util.Textures;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {
    protected ChatScreenMixin(Text title) {
        super(title);
    }

    @Unique
    private boolean mouseClicked = false;

    @Inject(method = "render", at = @At("TAIL"))
    void onDraw(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        drawLineButton(context, mouseX, mouseY);
        drawScreenshotButton(context, mouseX, mouseY);
        this.mouseClicked = false;
    }

    @Unique
    private void drawLineButton(DrawContext context, int mouseX, int mouseY) {
        ChatHud chatHud = getChatHud();
        ChatHudAccessor chatHudA = getChatHudA();
        ChatHudLocals chatHudL = (ChatHudLocals) chatHud;

        float chatScale = (float) chatHud.getChatScale();
        int chatLineY = (int) chatHudA.toChatLineYA(mouseY);
        int messageIndex = chatHudA.getMessageIndexA(0, chatLineY);
        int buttonX = (int) (chatHud.getWidth() + 14 * chatScale);
        if (messageIndex == -1 || mouseX > buttonX + 14 * chatScale) return;

        int color = chatHudL.chatShot$getChatBackgroundColor();
        int chatY = chatHudL.chatShot$getChatY();
        // If we couldn't find the chat locals, use the vanilla values and hope they are correct
        if (chatY == -1) {
            // chatY = (int) ((height - 40) / chatScale);
            // color = (int) (this.client.options.getTextBackgroundOpacity().getValue() * 255.0) << 24;
        }
        int buttonSize = (int) (9 * chatScale);
        int lineHeight = chatHudA.getLineHeightA();
        int scaledButtonX = (int) (chatHud.getWidth() / chatScale + 14);
        int scaledButtonY = chatY - (chatLineY + 1) * lineHeight + (int) Math.ceil((lineHeight - 9) / 2.0);
        float buttonY = scaledButtonY * chatScale;
        boolean hovering = mouseX >= buttonX && mouseX <= buttonX + buttonSize && mouseY >= buttonY && mouseY <= buttonY + buttonSize;

        context.getMatrices().push();
        context.getMatrices().scale(chatScale, chatScale, 1f);
        context.fill(scaledButtonX, scaledButtonY, scaledButtonX + 9, scaledButtonY + 9, hovering ? 0xFFFFFF | color : color);
        //#if MC < 12104
        context.drawTexture(Textures.COPY, scaledButtonX, scaledButtonY, 0, 0, 9, 9, 9, 9);
        //#else
        //$$ context.drawTexture((identifier) -> RenderLayer.getGuiOverlay(), Textures.COPY, scaledButtonX,scaledButtonY, 0, 0, 9, 9, 9, 9);
        //#endif
        context.getMatrices().pop();

        if (hovering && Config.INSTANCE.tooltip) {
            // Draw the tooltip
            ArrayList<Text> tooltip = new ArrayList<>();
            tooltip.add(Text.translatable("chatshot.copy"));
            tooltip.add(Text.translatable("chatshot.click" + (Config.INSTANCE.clickAction == Config.CopyType.TEXT ? "Text" : "Image")));
            tooltip.add(Text.translatable("chatshot.shiftClick" + (Config.INSTANCE.shiftClickAction == Config.CopyType.TEXT ? "Text" : "Image")));
            context.drawTooltip(this.client.textRenderer, tooltip, mouseX, mouseY);
        }
        if (hovering && this.mouseClicked) {
            context.draw(); // Make sure the current context has drawn everything before we start messing with frameBuffers
            List<ChatHudLine.Visible> visibleMessages = chatHudA.getVisibleMessages();
            ArrayList<ChatHudLine.Visible> messageParts = new ArrayList<>();
            // Collect all lines of the message
            messageParts.add(visibleMessages.get(messageIndex));
            for (int i = messageIndex + 1; i < visibleMessages.size(); i++) {
                //#if MC < 12100 || FABRIC == 0
                if (visibleMessages.get(i).endOfEntry()) break;
                //#else
                //$$ if (visibleMessages.get(i).comp_898()) break;
                //#endif
                messageParts.add(0, visibleMessages.get(i));
            }
            if (messageParts.isEmpty()) return;
            ChatCopyUtil.copy(messageParts, this.client);
        }
    }

    @Unique
    private void drawScreenshotButton(DrawContext context, int mouseX, int mouseY) {
        int buttonX = this.width - 12 - CompatCore.INSTANCE.getButtonOffset();
        int buttonY = this.height - 26;
        boolean hovering = mouseX >= buttonX && mouseX <= buttonX + 10 && mouseY >= buttonY && mouseY <= buttonY + 10;
        int color = this.client.options.getTextBackgroundColor(Integer.MIN_VALUE);
        context.fill(buttonX, buttonY, buttonX + 10, buttonY + 10, hovering ? 0xFFFFFF + color : color);
        // context.drawTexture(Textures.SCREENSHOT, buttonX, buttonY, 0, 0, 10, 10, 10, 10);
        //#if MC < 12104 
        context.drawTexture(Textures.SCREENSHOT, buttonX, buttonY, 0, 0, 10, 10, 10, 10);
        //#else
        //$$ context.drawTexture((identifier) -> RenderLayer.getGuiOverlay(),  Textures.SCREENSHOT, buttonX, buttonY, 0, 0, 10, 10, 10, 10);
        //#endif
        if (hovering && Config.INSTANCE.tooltip) {
            ArrayList<Text> tooltip = new ArrayList<>();
            tooltip.add(Text.translatable("chatshot.copy"));
            tooltip.add(Text.translatable("chatshot.clickImage"));
            context.drawTooltip(this.client.textRenderer, tooltip, mouseX, mouseY);
        }
        if (this.mouseClicked && hovering) {
            context.draw(); // Make sure the current context has drawn everything before we start messing with frameBuffers
            ArrayList<ChatHudLine.Visible> lines = new ArrayList<>();
            int scrolledLines = getChatHudA().getScrolledLines();
            List<ChatHudLine.Visible> visibleMessages = getChatHudA().getVisibleMessages();
            for (int i = scrolledLines; i < visibleMessages.size() && i < getChatHud().getVisibleLineCount() + scrolledLines; i++) {
                lines.add(0, visibleMessages.get(i));
            }
            ChatCopyUtil.copyImage(lines, this.client);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"))
    void onMouseClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button != 0) return;
        this.mouseClicked = true;
    }

    @Unique
    private ChatHud getChatHud() {
        return this.client.inGameHud.getChatHud();
    }

    @Unique
    private ChatHudAccessor getChatHudA() {
        return (ChatHudAccessor) getChatHud();
    }
}
