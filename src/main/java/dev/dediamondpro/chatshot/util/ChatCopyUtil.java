package dev.dediamondpro.chatshot.util;

import dev.dediamondpro.chatshot.compat.CompatCore;
import dev.dediamondpro.chatshot.config.Config;
import dev.dediamondpro.chatshot.util.clipboard.ClipboardUtil;
import dev.dediamondpro.chatshot.util.clipboard.MacOSCompat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ChatCopyUtil {

    public static void copy(List<ChatHudLine.Visible> lines, MinecraftClient client) {
        if (GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS || GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS) {
            if (Config.INSTANCE.shiftClickAction == Config.CopyType.TEXT) copyString(lines, client);
            else copyImage(lines, client);
        } else {
            if (Config.INSTANCE.clickAction == Config.CopyType.TEXT) copyString(lines, client);
            else copyImage(lines, client);
        }
    }

    public static void copyString(List<ChatHudLine.Visible> lines, MinecraftClient client) {
        CollectingCharacterVisitor visitor = new CollectingCharacterVisitor();
        for (ChatHudLine.Visible line : lines) {
            //#if MC < 12100 || FABRIC == 0
            line.content().accept(visitor);
            //#else
            //$$ line.comp_896().accept(visitor);
            //#endif
        }
        client.keyboard.setClipboard(visitor.collect());
        if (Config.INSTANCE.showCopyMessage) {
            client.inGameHud.getChatHud().addMessage(Text.translatable("chatshot.text.success"));
        }
    }

    public static void copyImage(List<ChatHudLine.Visible> lines, MinecraftClient client) {
        boolean shadow = Config.INSTANCE.shadow;
        int scaleFactor = Config.INSTANCE.scale;

        // Force mods doing things like hud-batching to draw immediately before we start messing with framebuffers
        CompatCore.INSTANCE.drawChatHud();

        DrawContext context = new DrawContext(client, client.getBufferBuilders().getEntityVertexConsumers());
        int width = 0;
        for (ChatHudLine.Visible line : lines) {
            OrderedText content =
            //#if MC < 12100 || FABRIC == 0
                    line.content();
            //#else
            //$$    line.comp_896();
            //#endif
            width = Math.max(width, client.textRenderer.getWidth(content));
        }
        int height = lines.size() * 9;
        Framebuffer fb;
        try {
             fb = createBuffer(width * scaleFactor, height * scaleFactor);
        } catch (IllegalArgumentException e) {
            // If we get this error that mean the window is too big or the chat is empty
            ClientPlayerEntity player = client.player;
            player.sendMessage(Text.translatable("chatshot.noMessageFound"));
            return;
        }

        context.getMatrices().scale((float) client.getWindow().getScaledWidth() / width, (float) client.getWindow().getScaledHeight() / height, 1f);
        fb.beginWrite(false);
        int y = 0;
        for (ChatHudLine.Visible line : lines) {
            OrderedText content =
            //#if MC < 12100 || FABRIC == 0
                    line.content();
            //#else
            //$$    line.comp_896();
            //#endif
            context.drawText(client.textRenderer, content, 0, y, 0xFFFFFF, shadow);
            y += 9;
        }
        // Force mods doing things like hud-batching to draw immediately
        CompatCore.INSTANCE.drawChatHud();
        fb.endWrite();

        try (NativeImage nativeImage = ScreenshotRecorder.takeScreenshot(fb)) {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(nativeImage.getBytes()));
            BufferedImage transparentImage = imageToBufferedImage(makeColorTransparent(image, new Color(0x36, 0x39, 0x3F)));
            boolean copySuccessfull = false;
            if (Config.INSTANCE.saveImage || MinecraftClient.IS_SYSTEM_MAC) {
                File screenShotDir = new File("screenshots/chat");
                screenShotDir.mkdirs();
                File screenshotFile = getScreenshotFilename(screenShotDir);
                ImageIO.write(transparentImage, "png", screenshotFile);
                if (MinecraftClient.IS_SYSTEM_MAC) {
                    copySuccessfull = MacOSCompat.doCopyMacOS(screenshotFile.getAbsolutePath());
                    if (!Config.INSTANCE.saveImage) screenshotFile.delete();
                }
            }
            if (!MinecraftClient.IS_SYSTEM_MAC) copySuccessfull = ClipboardUtil.copy(transparentImage);
            Text message = null;
            if (copySuccessfull) {
                if (Config.INSTANCE.showCopyMessage) message = Text.translatable("chatshot.image.success");
            } else {
                message = Text.translatable("chatshot.image.fail");
            }
            if (message != null) client.inGameHud.getChatHud().addMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
        client.getFramebuffer().beginWrite(true);
    }

    private static File getScreenshotFilename(File directory) {
        String string = Util.getFormattedCurrentTime();
        int i = 1;
        File file;
        while ((file = new File(directory, string + (i == 1 ? "" : "_" + i) + ".png")).exists()) {
            ++i;
        }
        return file;
    }

    private static Framebuffer createBuffer(int width, int height) {
        //
        Framebuffer fb = new SimpleFramebuffer(width, height, true, false);
        //
        fb.setClearColor(0x36 / 255f, 0x39 / 255f, 0x3F / 255f, 0f);
        fb.clear(false);
        return fb;
    }

    /*
     * Taken from: https://stackoverflow.com/a/665483
     */
    public static Image makeColorTransparent(BufferedImage im, final Color color) {
        ImageFilter filter = new RGBImageFilter() {

            // the color we are looking for... Alpha bits are set to opaque
            public final int markerRGB = color.getRGB() | 0xFF000000;

            public final int filterRGB(int x, int y, int rgb) {
                if ((rgb | 0xFF000000) == markerRGB) {
                    // Mark the alpha bits as zero - transparent
                    return 0x00FFFFFF & rgb;
                } else {
                    // nothing to do
                    return rgb;
                }
            }
        };

        ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
        return Toolkit.getDefaultToolkit().createImage(ip);
    }

    /*
     * Taken from: https://stackoverflow.com/a/665483
     */
    private static BufferedImage imageToBufferedImage(Image image) {
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bufferedImage.createGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        return bufferedImage;
    }
}
