package dev.dediamondpro.chatshot.compat;

public interface ICompatHandler {
    default int getButtonOffset() {
        return 0;
    }

    default void drawChatHud() {
    }
}
