package dev.dediamondpro.chatshot.mixins;

import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(ChatComponent.class)
public interface ChatHudAccessor {

    @Accessor("chatScrollbarPos")
    int getScrolledLines();

    @Accessor("trimmedMessages")
    List<GuiMessage.Line> getVisibleMessages();

    @Invoker("getMessageEndIndexAt")
    int getMessageIndexA(double chatLineX, double chatLineY);

    @Invoker("screenToChatY")
    double toChatLineYA(double y);

    @Invoker("getLineHeight")
    int getLineHeightA();
}
