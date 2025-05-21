package dev.dediamondpro.chatshot.util;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.MacosUtil;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.*;
import dev.dediamondpro.chatshot.compat.CompatCore;
import dev.dediamondpro.chatshot.config.Config;
import dev.dediamondpro.chatshot.util.clipboard.ClipboardUtil;
import dev.dediamondpro.chatshot.util.clipboard.MacOSCompat;
import it.unimi.dsi.fastutil.objects.Object2ObjectSortedMaps;

import net.minecraft.Util;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.List;

public class ChatCopyUtil {

    static public RenderType CUSTOM_TEXT_LAYER = RenderType.create(
            "chatshot_text",
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
            VertexFormat.Mode.QUADS,
            786432,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_TEXT_SHADER)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setOutputState(new RenderStateShard.OutputStateShard("chatshot_fbo", () -> {
                    }, () -> {
                    }))
                    .createCompositeState(false));

    public static void copy(List<GuiMessage.Line> lines, Minecraft client) {
        if (GLFW.glfwGetKey(client.getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS || GLFW.glfwGetKey(client.getWindow().getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS) {
            if (Config.INSTANCE.shiftClickAction == Config.CopyType.TEXT) copyString(lines, client);
            else copyImage(lines, client);
        } else {
            if (Config.INSTANCE.clickAction == Config.CopyType.TEXT) copyString(lines, client);
            else copyImage(lines, client);
        }
    }

    public static void copyString(List<GuiMessage.Line> lines, Minecraft client) {
        CollectingCharacterVisitor visitor = new CollectingCharacterVisitor();
        for (GuiMessage.Line line : lines) {
            line.content().accept(visitor);
        }
        client.keyboardHandler.setClipboard(visitor.collect());
        if (Config.INSTANCE.showCopyMessage) {
            client.gui.getChat().addMessage(Component.translatable("chatshot.text.success"));
        }
    }

    public static class OverrideVertexProvider extends MultiBufferSource.BufferSource {
        private RenderType currentLayer = CUSTOM_TEXT_LAYER;
        public BufferBuilder bufferBuilder;

        private OverrideVertexProvider(ByteBufferBuilder bufferAllocator) {
            super(bufferAllocator, Object2ObjectSortedMaps.emptyMap());
            this.bufferBuilder = new BufferBuilder(this.sharedBuffer, CUSTOM_TEXT_LAYER.mode(), CUSTOM_TEXT_LAYER.format());
        }

        @Override
        public VertexConsumer getBuffer(RenderType renderType) {
            return this.bufferBuilder;
        }

        public void finish_drawing() {
            this.startedBuilders.put(this.currentLayer, this.bufferBuilder);
            this.endBatch(this.currentLayer);
        }
    }

    public static void copyImage(List<GuiMessage.Line> lines, Minecraft client) {
        boolean shadow = Config.INSTANCE.shadow;
        int scaleFactor = Config.INSTANCE.scale;

        // Force mods doing things like hud-batching to draw immediately before we start messing with framebuffers
        CompatCore.INSTANCE.drawChatHud();

        int width = 0;
        for (GuiMessage.Line line : lines) {
            FormattedCharSequence content = line.content();
            width = Math.max(width, client.font.width(content));
        }
        int height = lines.size() * 9;
        RenderTarget fb;
        try {
            fb = createBuffer(width * scaleFactor, height * scaleFactor);
        } catch (IllegalArgumentException e) {
            // If we get this error that mean the window is too big or the chat is empty
            client.gui.getChat().addMessage(Component.translatable("chatshot.noMessageFound"));
            return;
        }
        OverrideVertexProvider customConsumer = new OverrideVertexProvider(new ByteBufferBuilder(256));
        customConsumer.getBuffer(CUSTOM_TEXT_LAYER);
        GuiGraphics context = new GuiGraphics(client, customConsumer);

        context.pose().scale(
                (float) client.getWindow().getGuiScaledWidth() / width,
                (float) client.getWindow().getGuiScaledHeight() / height,
                1f
        );
        fb.bindWrite(false);
        int y = 0;
        for (GuiMessage.Line line : lines) {
            context.drawString(client.font, line.content(), 0, y, 0xFFFFFF, shadow);
            y += 9;
        }

        // Force mods doing things like hud-batching to draw immediately
        CompatCore.INSTANCE.drawChatHud();
        context.flush();
        customConsumer.finish_drawing();

        fb.unbindWrite();
        try (NativeImage nativeImage = Screenshot.takeScreenshot(fb)) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            WritableByteChannel writableChannel = Channels.newChannel(outputStream);
            nativeImage.writeToChannel(writableChannel);
            writableChannel.close();

            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            BufferedImage image = ImageIO.read(inputStream);

            BufferedImage transparentImage = imageToBufferedImage(makeColorTransparent(image, new Color(0x36, 0x39, 0x3F)));
            boolean copySuccessfull = false;
            if (Config.INSTANCE.saveImage || MacosUtil.IS_MACOS) {
                File screenShotDir = new File("screenshots/chat");
                screenShotDir.mkdirs();
                File screenshotFile = getScreenshotFilename(screenShotDir);
                ImageIO.write(transparentImage, "png", screenshotFile);
                if (MacosUtil.IS_MACOS) {
                    copySuccessfull = MacOSCompat.doCopyMacOS(screenshotFile.getAbsolutePath());
                    if (!Config.INSTANCE.saveImage) screenshotFile.delete();
                }
            }
            if (!MacosUtil.IS_MACOS) copySuccessfull = ClipboardUtil.copy(transparentImage);
            Component message = null;
            if (copySuccessfull) {
                if (Config.INSTANCE.showCopyMessage) message = Component.translatable("chatshot.image.success");
            } else {
                message = Component.translatable("chatshot.image.fail");
            }
            if (message != null) client.gui.getChat().addMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
        client.getMainRenderTarget().bindWrite(true);
    }

    private static File getScreenshotFilename(File directory) {
        String string = Util.getFilenameFormattedDateTime();
        int i = 1;
        File file;
        while ((file = new File(directory, string + (i == 1 ? "" : "_" + i) + ".png")).exists()) {
            ++i;
        }
        return file;
    }

    private static RenderTarget createBuffer(int width, int height) {
        RenderTarget fb = new TextureTarget(width, height, true);
        fb.setClearColor(0x36 / 255f, 0x39 / 255f, 0x3F / 255f, 0f);
        fb.clear();
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
