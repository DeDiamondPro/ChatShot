package dev.dediamondpro.chatshot.util;

import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.Style;

public class CollectingCharacterVisitor implements CharacterVisitor {
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
