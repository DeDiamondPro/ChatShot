package dev.dediamondpro.chatshot.util.clipboard;

import com.mojang.blaze3d.platform.MacosUtil;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;

public class ClipboardUtil {

    public static boolean copy(BufferedImage img) {
        return copy(new TransferableImage(img));
    }

    public static boolean copy(Transferable transferable) {
        if (MacosUtil.IS_MACOS) return false;
        try {
            Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
            c.setContents(transferable, null);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}