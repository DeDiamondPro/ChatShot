package dev.dediamondpro.chatshot.util;

import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.MacosUtil;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.minecraft.client.renderer.RenderPipelines;
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
import java.util.function.Function;

public class ChatCopyUtil {

    static public Function<RenderTarget, RenderType> CUSTOM_TEXT_LAYER = (RenderTarget rt)->(RenderType.create("chatshot_text",
            786432,
            false,
            false,
            RenderPipelines.TEXT,
            RenderType.CompositeState.builder().setTextureState(RenderStateShard.NO_TEXTURE)
                    .setOutputState(new RenderStateShard.OutputStateShard("chatshot_fbo", () -> (rt)))
            .setLightmapState(RenderStateShard.LIGHTMAP).createCompositeState(false)));

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
        private RenderType currentLayer;
        public BufferBuilder bufferBuilder;

        private OverrideVertexProvider(ByteBufferBuilder bufferAllocator, RenderTarget rt) {
            super(bufferAllocator, Object2ObjectSortedMaps.emptyMap());
            currentLayer = CUSTOM_TEXT_LAYER.apply(rt);
            this.bufferBuilder = new BufferBuilder(this.sharedBuffer, currentLayer.mode(), currentLayer.format());
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
        GpuDevice device = RenderSystem.getDevice();
        CommandEncoder cmd = device.createCommandEncoder();

        GpuBuffer fb;
        RenderTarget rt ;

        try {
            fb = createBuffer(width * scaleFactor, height * scaleFactor, true);
            rt = new TextureTarget("test", width*scaleFactor, height*scaleFactor, true);
        } catch (IllegalArgumentException e) {
            // If we get this error that mean the window is too big or the chat is empty
            client.gui.getChat().addMessage(Component.translatable("chatshot.noMessageFound"));
            return;
        }
        OverrideVertexProvider customConsumer = new OverrideVertexProvider(new ByteBufferBuilder(256), rt);
        GuiGraphics context = new GuiGraphics(client, customConsumer);
        cmd.clearColorAndDepthTextures(rt.getColorTexture(),new Color(0x36, 0x39, 0x3F).getRGB() , rt.getDepthTexture(), 1);
        context.pose().scale(
                (float) client.getWindow().getGuiScaledWidth() / width,
                (float) client.getWindow().getGuiScaledHeight() / height,
                1f
        );

        int y = 0;
        for (GuiMessage.Line line : lines) {
            context.drawString(client.font, line.content(), 0, y, 0xFFFFFF, shadow);
            y += 9;
        }

        // Force mods doing things like hud-batching to draw immediately
        CompatCore.INSTANCE.drawChatHud();
        context.flush();
        customConsumer.finish_drawing();
        cmd.copyTextureToBuffer(
                rt.getColorTexture(),
                fb,
                0,
                ()-> Screenshot.takeScreenshot(
                        rt,
                        (image)->ChatCopyUtil.saveImage(image, client)
                ),
             0);
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

    private static GpuBuffer createBuffer(int width, int height, boolean read) {
        GpuDevice device = RenderSystem.getDevice();
        return device.createBuffer(null, BufferType.PIXEL_PACK, read ? BufferUsage.STATIC_READ : BufferUsage.STATIC_WRITE , width * height * 4);
    }

    private static void saveImage(NativeImage nativeImage, Minecraft client)
    {
        try  {
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
