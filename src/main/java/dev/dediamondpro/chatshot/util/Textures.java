package dev.dediamondpro.chatshot.util;

import net.minecraft.util.Identifier;

public class Textures {
    //#if MC>=12100
    public static final Identifier COPY = Identifier.of("chatshot", "copy.png");
    public static final Identifier SCREENSHOT = Identifier.of("chatshot", "screenshot.png");
    //#else
    //$$ public static final Identifier COPY = new Identifier("chatshot", "copy.png");
    //$$ public static final Identifier SCREENSHOT = new Identifier("chatshot", "screenshot.png");
    //#endif
}
