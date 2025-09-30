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
import com.mojang.blaze3d.textures.GpuTexture;
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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import java.util.function.Function;

public class ChatCopyUtil {

    static public Function<RenderTarget, RenderType> CUSTOM_TEXT_LAYER = (RenderTarget rt) -> (RenderType.create("chatshot_text",
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
        private final RenderType currentLayer;
        public BufferBuilder bufferBuilder;

        private OverrideVertexProvider(ByteBufferBuilder bufferAllocator, RenderTarget rt) {
            super(bufferAllocator, Object2ObjectSortedMaps.emptyMap());
            this.currentLayer = CUSTOM_TEXT_LAYER.apply(rt);
            this.bufferBuilder = new BufferBuilder(this.sharedBuffer, currentLayer.mode(), currentLayer.format());
        }

        @Override
        public @NotNull VertexConsumer getBuffer(RenderType renderType) {
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
        RenderTarget rt;

        try {
            rt = new TextureTarget(null, width * scaleFactor, height * scaleFactor, false);
            fb = device.createBuffer(null, BufferType.PIXEL_PACK, BufferUsage.STATIC_READ, width * scaleFactor * height * scaleFactor * rt.getColorTexture().getFormat().pixelSize());
        } catch (IllegalArgumentException e) {
            // If we get this error that mean the window is too big or the chat is empty
            client.gui.getChat().addMessage(Component.translatable("chatshot.noMessageFound"));
            return;
        }
        OverrideVertexProvider customConsumer = new OverrideVertexProvider(new ByteBufferBuilder(256), rt);
        GuiGraphics context = new GuiGraphics(client, customConsumer);
        cmd.clearColorTexture(rt.getColorTexture(), 0x00000000);

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
                () -> ChatCopyUtil.saveImage(rt, client),
                0
        );
    }

    private static void saveImage(RenderTarget rt, Minecraft client) {
        int i = rt.width;
        int j = rt.height;

        GpuTexture gpuTexture = rt.getColorTexture();
        GpuBuffer gpuBuffer = RenderSystem.getDevice()
                .createBuffer(null, BufferType.PIXEL_PACK, BufferUsage.STATIC_READ, i * j * gpuTexture.getFormat().pixelSize());
        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();

        RenderSystem.getDevice().createCommandEncoder().copyTextureToBuffer(gpuTexture, gpuBuffer, 0, () -> {
            try (GpuBuffer.ReadView readView = commandEncoder.readBuffer(gpuBuffer);
                 NativeImage nativeImage = new NativeImage(i, j, false)) {

                for (int k = 0; k < j; k++) {
                    for (int l = 0; l < i; l++) {
                        int m = readView.data().getInt((l + k * i) * gpuTexture.getFormat().pixelSize());
                        nativeImage.setPixelABGR(l, j - k - 1, m);
                    }
                }
                try {
                    boolean copySuccessful = false;
                    if (Config.INSTANCE.saveImage || MacosUtil.IS_MACOS) {
                        File screenShotDir = new File("screenshots/chat");
                        screenShotDir.mkdirs();
                        File screenshotFile = getScreenshotFilename(screenShotDir);
                        nativeImage.writeToFile(screenshotFile);
                        if (MacosUtil.IS_MACOS) {
                            copySuccessful = MacOSCompat.doCopyMacOS(screenshotFile.getAbsolutePath());
                            if (!Config.INSTANCE.saveImage) screenshotFile.delete();
                        }
                    }
                    if (!MacosUtil.IS_MACOS) {
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        WritableByteChannel writableChannel = Channels.newChannel(outputStream);
                        nativeImage.writeToChannel(writableChannel);
                        writableChannel.close();

                        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                        BufferedImage image = ImageIO.read(inputStream);
                        copySuccessful = ClipboardUtil.copy(image);
                    }
                    Component message = null;
                    if (copySuccessful) {
                        if (Config.INSTANCE.showCopyMessage) message = Component.translatable("chatshot.image.success");
                    } else {
                        message = Component.translatable("chatshot.image.fail");
                    }
                    if (message != null) client.gui.getChat().addMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0);
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
}
