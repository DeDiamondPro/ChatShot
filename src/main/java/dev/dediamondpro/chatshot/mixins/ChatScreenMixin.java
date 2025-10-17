package dev.dediamondpro.chatshot.mixins;

import dev.dediamondpro.chatshot.compat.CompatCore;
import dev.dediamondpro.chatshot.config.Config;
import dev.dediamondpro.chatshot.data.ChatHudLocals;
import dev.dediamondpro.chatshot.util.ChatCopyUtil;
import dev.dediamondpro.chatshot.util.Textures;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//? if >=1.21.9 {
import net.minecraft.client.input.MouseButtonEvent;
//?}

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {
    protected ChatScreenMixin(Component title) {
        super(title);
    }

    @Unique
    private boolean mouseClicked = false;

    @Inject(method = "render", at = @At("TAIL"))
    void onDraw(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        drawLineButton(context, mouseX, mouseY);
        drawScreenshotButton(context, mouseX, mouseY);
        this.mouseClicked = false;
    }

    @Unique
    private void drawLineButton(GuiGraphics context, int mouseX, int mouseY) {
        ChatComponent chatHud = getChatHud();
        ChatHudAccessor chatHudA = getChatHudA();
        ChatHudLocals chatHudL = (ChatHudLocals) chatHud;

        float chatScale = (float) chatHud.getScale();
        int chatLineY = (int) chatHudA.toChatLineYA(mouseY);
        int messageIndex = chatHudA.getMessageIndexA(0, chatLineY);
        int buttonX = (int) (chatHud.getWidth() + 14 * chatScale);
        if (messageIndex == -1 || mouseX > buttonX + 14 * chatScale) return;

        int color = chatHudL.chatShot$getChatBackgroundColor();
        int chatY = chatHudL.chatShot$getChatY();
        // If we couldn't find the chat locals, use the vanilla values and hope they are correct
        if (chatY == -1) {
            chatY = (int) ((height - 40) / chatScale);
            color = (int) (this.minecraft.options.textBackgroundOpacity().get() * 255.0) << 24;
        }
        int buttonSize = (int) (9 * chatScale);
        int lineHeight = chatHudA.getLineHeightA();
        int scaledButtonX = (int) (chatHud.getWidth() / chatScale + 14);
        int scaledButtonY = chatY - (chatLineY + 1) * lineHeight + (int) Math.ceil((lineHeight - 9) / 2.0);
        float buttonY = scaledButtonY * chatScale;
        boolean hovering = mouseX >= buttonX && mouseX <= buttonX + buttonSize && mouseY >= buttonY && mouseY <= buttonY + buttonSize;

        //? if <1.21.6 {
        /*context.pose().pushPose();
         *///?} else
        context.pose().pushMatrix();
        context.pose().scale(chatScale, chatScale /*? if <1.21.6 {*/ /*,1f *//*?}*/);
        context.fill(scaledButtonX, scaledButtonY, scaledButtonX + 9, scaledButtonY + 9, hovering ? 0xFFFFFF | color : color);
        context.blit(
                /*? if <1.21.6 {*/ /*RenderType::guiTextured *//*?} else {*/ RenderPipelines.GUI_TEXTURED /*?}*/,
                Textures.COPY, scaledButtonX, scaledButtonY, 0, 0, 9, 9, 9, 9
        );
        //? if <1.21.6 {
        /*context.pose().popPose();
         *///?} else
        context.pose().popMatrix();

        if (hovering && Config.INSTANCE.tooltip) {
            // Draw the tooltip
            ArrayList<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.translatable("chatshot.copy"));
            tooltip.add(Component.translatable("chatshot.click" + (Config.INSTANCE.clickAction == Config.CopyType.TEXT ? "Text" : "Image")));
            tooltip.add(Component.translatable("chatshot.shiftClick" + (Config.INSTANCE.shiftClickAction == Config.CopyType.TEXT ? "Text" : "Image")));
            //? if <1.21.6 {
            /*context.renderComponentTooltip(this.minecraft.font, tooltip, mouseX, mouseY);
             *///?} else
            context.setTooltipForNextFrame(this.minecraft.font, tooltip, Optional.empty(), mouseX, mouseY);
        }
        if (hovering && this.mouseClicked) {
            //? if <1.21.6
            /*context.flush();*/ // Make sure the current context has drawn everything before we start messing with frameBuffers
            List<GuiMessage.Line> visibleMessages = chatHudA.getVisibleMessages();
            ArrayList<GuiMessage.Line> messageParts = new ArrayList<>();
            // Collect all lines of the message
            messageParts.add(visibleMessages.get(messageIndex));
            for (int i = messageIndex + 1; i < visibleMessages.size(); i++) {
                if (visibleMessages.get(i).endOfEntry()) break;
                messageParts.addFirst(visibleMessages.get(i));
            }
            if (messageParts.isEmpty()) return;
            ChatCopyUtil.copy(messageParts, chatHudL.chatshot$getMessageForLine(chatLineY), this.minecraft);
        }
    }

    @Unique
    private void drawScreenshotButton(GuiGraphics context, int mouseX, int mouseY) {
        int buttonX = this.width - 12 - CompatCore.INSTANCE.getButtonOffset();
        int buttonY = this.height - 26;
        boolean hovering = mouseX >= buttonX && mouseX <= buttonX + 10 && mouseY >= buttonY && mouseY <= buttonY + 10;
        int color = this.minecraft.options.getBackgroundColor(Integer.MIN_VALUE);
        context.fill(buttonX, buttonY, buttonX + 10, buttonY + 10, hovering ? 0xFFFFFF + color : color);
        context.blit(
                /*? if <1.21.6 {*/ /*RenderType::guiTextured *//*?} else {*/ RenderPipelines.GUI_TEXTURED /*?}*/,
                Textures.SCREENSHOT, buttonX, buttonY, 0, 0, 10, 10, 10, 10);
        if (hovering && Config.INSTANCE.tooltip) {
            ArrayList<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.translatable("chatshot.copy"));
            tooltip.add(Component.translatable("chatshot.clickImage"));
            //? if <1.21.6 {
            /*context.renderComponentTooltip(this.minecraft.font, tooltip, mouseX, mouseY);
             *///?} else
            context.setTooltipForNextFrame(this.minecraft.font, tooltip, Optional.empty(), mouseX, mouseY);
        }
        if (this.mouseClicked && hovering) {
            //? if <1.21.6
            /*context.flush();*/ // Make sure the current context has drawn everything before we start messing with frameBuffers
            ArrayList<GuiMessage.Line> lines = new ArrayList<>();
            int scrolledLines = getChatHudA().getScrolledLines();
            List<GuiMessage.Line> visibleMessages = getChatHudA().getVisibleMessages();
            for (int i = scrolledLines; i < visibleMessages.size() && i < getChatHud().getLinesPerPage() + scrolledLines; i++) {
                lines.addFirst(visibleMessages.get(i));
            }
            ChatCopyUtil.copyImage(lines, this.minecraft);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"))
    //? if <1.21.9 {
    /*void onMouseClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
         if (button != 0) return;
    *///?} else {
    void onMouseClick(MouseButtonEvent arg, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        if (arg.button() != 0) return;
    //?}

        this.mouseClicked = true;
    }

    @Unique
    private ChatComponent getChatHud() {
        return this.minecraft.gui.getChat();
    }

    @Unique
    private ChatHudAccessor getChatHudA() {
        return (ChatHudAccessor) getChatHud();
    }
}
