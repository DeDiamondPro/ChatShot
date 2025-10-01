package dev.dediamondpro.chatshot.data;

import net.minecraft.network.chat.Component;

public interface ChatHudLocals {
    int chatShot$getChatY();

    int chatShot$getChatBackgroundColor();

    Component chatshot$getMessageForLine(int index);
}
