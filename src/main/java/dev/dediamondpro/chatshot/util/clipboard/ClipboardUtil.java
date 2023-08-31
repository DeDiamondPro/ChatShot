package dev.dediamondpro.chatshot.util.clipboard;

import net.minecraft.client.MinecraftClient;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;

public class ClipboardUtil {

    public static boolean copy(BufferedImage img) {
        return copy(new TransferableImage(img));
    }

    public static boolean copy(Transferable transferable) {
        if (MinecraftClient.IS_SYSTEM_MAC) return false;
        try {
            Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
            c.setContents(transferable, null);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}