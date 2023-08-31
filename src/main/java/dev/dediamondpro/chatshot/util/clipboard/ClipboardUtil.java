package dev.dediamondpro.chatshot.util.clipboard;

import net.minecraft.client.MinecraftClient;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;

public class ClipboardUtil {

    public static void copy(BufferedImage img) {
        copy(new TransferableImage(img));
    }

    public static void copy(Transferable transferable) {
        if (MinecraftClient.IS_SYSTEM_MAC) return;
        try {
            Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
            c.setContents(transferable, null);
        } catch (Exception ignored) {
        }
    }
}