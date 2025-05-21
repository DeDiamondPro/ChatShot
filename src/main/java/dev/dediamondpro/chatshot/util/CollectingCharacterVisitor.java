package dev.dediamondpro.chatshot.util;

import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSink;

public class CollectingCharacterVisitor implements FormattedCharSink {
    private final StringBuilder builder = new StringBuilder();

    @Override
    public boolean accept(int index, Style style, int codePoint) {
        builder.append((char) codePoint);
        return true;
    }

    public String collect() {
        return builder.toString();
    }
}
