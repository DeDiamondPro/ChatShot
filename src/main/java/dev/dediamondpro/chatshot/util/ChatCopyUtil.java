package dev.dediamondpro.chatshot.util;

import dev.dediamondpro.chatshot.compat.CompatCore;
import dev.dediamondpro.chatshot.config.Config;
import dev.dediamondpro.chatshot.util.clipboard.ClipboardUtil;
import dev.dediamondpro.chatshot.util.clipboard.MacOSCompat;
import it.unimi.dsi.fastutil.objects.Object2ObjectSortedMaps;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteAtlasTexture;
//#if MC >= 12104
import net.minecraft.client.util.BufferAllocator;
//#endif
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBIWriteCallback;
import org.lwjgl.stb.STBImageWrite;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.logging.Logger;

public class ChatCopyUtil {

    //#if MC >= 12104
    static public RenderLayer CUSTOM_TEXT_LAYER = RenderLayer.of(
                    "chatshot_text",
                    VertexFormats.POSITION_COLOR_TEXTURE_LIGHT,
                    VertexFormat.DrawMode.QUADS,
                    786432,
                    RenderLayer.MultiPhaseParameters.builder()
                        .program(RenderPhase.TEXT_PROGRAM)
                        .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                        .lightmap(RenderPhase.ENABLE_LIGHTMAP)
                        .cull(RenderPhase.DISABLE_CULLING)
                        .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
                        .target(new RenderPhase.Target("chatshot_fbo", () -> {
                        }, () -> {})) 
                        .build(false));
    //#endif
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
            //#if MC >= 12100 && FABRIC == 1
            line.comp_896().accept(visitor);
            //#else
            //$$line.content().accept(visitor);
            //#endif
        }
        client.keyboard.setClipboard(visitor.collect());
        if (Config.INSTANCE.showCopyMessage) {
            client.inGameHud.getChatHud().addMessage(Text.translatable("chatshot.text.success"));
        }
    }
    //#if MC >= 12104
    public static class OverrideVertexProvider extends VertexConsumerProvider.Immediate {
        private RenderLayer currentLayer = CUSTOM_TEXT_LAYER;
        public BufferBuilder bufferBuilder;
        private OverrideVertexProvider(BufferAllocator bufferAllocator) {
            super(bufferAllocator, Object2ObjectSortedMaps.emptyMap());
            this.bufferBuilder = new BufferBuilder(this.allocator, CUSTOM_TEXT_LAYER.getDrawMode(), CUSTOM_TEXT_LAYER.getVertexFormat());
        }
        @Override
        public VertexConsumer getBuffer(RenderLayer renderLayer)
        {
            return this.bufferBuilder;
        }
        public void finish_drawing() {
            this.pending.put(this.currentLayer, this.bufferBuilder);
            this.draw(this.currentLayer);
        }
    }
    //#endif
    public static void copyImage(List<ChatHudLine.Visible> lines, MinecraftClient client) {
        boolean shadow = Config.INSTANCE.shadow;
        int scaleFactor = Config.INSTANCE.scale;

        // Force mods doing things like hud-batching to draw immediately before we start messing with framebuffers
        CompatCore.INSTANCE.drawChatHud();

        int width = 0;
        for (ChatHudLine.Visible line : lines) {
            OrderedText content =
            //#if MC >= 12100 && FABRIC == 1
            line.comp_896();
            //#else
            //$$    line.content();
            //#endif
            width = Math.max(width, client.textRenderer.getWidth(content));
        }
        int height = lines.size() * 9;
        Framebuffer fb;
        try {
             fb = createBuffer(width * scaleFactor, height * scaleFactor);
        } catch (IllegalArgumentException e) {
            // If we get this error that mean the window is too big or the chat is empty
            client.inGameHud.getChatHud().addMessage(Text.translatable("chatshot.noMessageFound"));
            return;
        }
        //#if MC >= 12104
        OverrideVertexProvider customConsumer = new OverrideVertexProvider(new BufferAllocator(256));
        customConsumer.getBuffer(CUSTOM_TEXT_LAYER);
        DrawContext context = new DrawContext(client, customConsumer);
        //#else
        //$$ DrawContext context = new DrawContext(client, client.getBufferBuilders().getEntityVertexConsumers());
        //#endif
        
        context.getMatrices().scale((float) client.getWindow().getScaledWidth() / width, (float) client.getWindow().getScaledHeight() / height, 1f);
        fb.beginWrite(false);
        int y = 0;
        for (ChatHudLine.Visible line : lines) {
            OrderedText content =
            //#if MC >= 12100 && FABRIC == 1
            line.comp_896();
            //#else
            //$$    line.content();
            //#endif
            context.drawText(client.textRenderer, content, 0, y, 0xFFFFFF, shadow);
            y += 9;
        }

        // Force mods doing things like hud-batching to draw immediately
        CompatCore.INSTANCE.drawChatHud();
        //#if MC >= 12104
        context.draw();
        customConsumer.finish_drawing();
        //#endif
        fb.endWrite();
        try (NativeImage nativeImage = ScreenshotRecorder.takeScreenshot(fb)) {
            //#if MC >= 12104
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            WritableByteChannel writableChannel = Channels.newChannel(outputStream);
            nativeImage.write(writableChannel);
            writableChannel.close();
            
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            BufferedImage image = ImageIO.read(inputStream);
            //#else
            //$$ BufferedImage image = ImageIO.read(new ByteArrayInputStream(nativeImage.getBytes()));
            //#endif

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
          
        //#if MC > 12100 && FABRIC == 1
        Framebuffer fb = new SimpleFramebuffer(width, height, true);
        fb.setClearColor(0x36 / 255f, 0x39 / 255f, 0x3F / 255f, 0f);
        fb.clear();
        //#elseif MC > 12100
        //$$ RenderTarget fb = new TextureTarget(width, height, true);
        //$$ fb.setClearColor(0x36 / 255f, 0x39 / 255f, 0x3F / 255f, 0f);
        //$$ fb.clear();
        //#else
        //$$ Framebuffer fb = new SimpleFramebuffer(width, height, true, false);
        //$$ fb.setClearColor(0x36 / 255f, 0x39 / 255f, 0x3F / 255f, 0f);
        //$$ fb.clear(false);
        //#endif
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
