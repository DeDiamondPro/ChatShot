package dev.dediamondpro.chatshot.util;

import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSink;

public class CollectingCharacterVisitor implements FormattedCharSink {
    private final StringBuilder builder = new StringBuilder();

    @Override
    public boolean accept(int index, Style style, int codePoint) {
        // Skip private-use area characters (U+E000–U+F8FF, U+F0000–U+FFFFF) used by mods
        // for custom glyphs/widgets (e.g. Chat Heads player head placeholders).
        if ((codePoint >= 0xE000 && codePoint <= 0xF8FF)
                || (codePoint >= 0xF0000 && codePoint <= 0xFFFFF)) {
            return true;
        }
        builder.appendCodePoint(codePoint);
        return true;
    }

    public String collect() {
        return builder.toString();
    }
}
