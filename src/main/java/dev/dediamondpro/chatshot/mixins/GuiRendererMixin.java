package dev.dediamondpro.chatshot.mixins;

//? if >=1.21.6 {

//?if >26.1 {
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.state.WindowRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
//?} else {
/*import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.CachedOrthoProjectionMatrixBuffer;
*///?}

//?if >= 26.2 {
import net.minecraft.client.renderer.StagedVertexBuffer;
//?}

import com.mojang.blaze3d.ProjectionType;
//?if >=26.3 {

import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import com.mojang.renderpearl.api.textures.GpuTexture;

//?} else {

/*import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.vertex.VertexFormat;
*///?}

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.dediamondpro.chatshot.util.GuiRendererInterface;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.MappableRingBuffer;
//?if 26.2 {
/*
import com.mojang.blaze3d.textures.GpuTexture;
*///?}

//? if neoforge {
/*import net.neoforged.neoforge.client.gui.PictureInPictureRendererPool;
 *///?}
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Mixin(GuiRenderer.class)
public class GuiRendererMixin implements GuiRendererInterface {
    //? if neoforge {
    /*@Shadow
    private Map<Class<? extends PictureInPictureRenderState>, PictureInPictureRendererPool<?>> pictureInPictureRendererPools;
     *///?}

    @Final
    @Shadow
    private List<GuiRenderer.Draw> draws;
    @Shadow
    private int firstDrawIndexAfterBlur;
    //?if <26.2 {
    /*@Final
    @Shadow
    private List<GuiRenderer.MeshToDraw> meshesToDraw;
    *///?}
    @Final
    @Shadow
    GuiRenderState renderState;
    //?if <26.2 {
    /*@Final
    @Shadow
    private Map<VertexFormat, MappableRingBuffer> vertexBuffers;
    *///?} else {
    @Final
    @Shadow
    private StagedVertexBuffer vertexBuffer;
    //?}

    //?if >26.1 {
    @Final
    @Shadow
    private Projection guiProjection;
    @Final
    @Shadow
    private ProjectionMatrixBuffer guiProjectionMatrixBuffer;
    //?} else {
    /*@Final
    @Shadow
    private CachedOrthoProjectionMatrixBuffer guiProjectionMatrixBuffer;
    *///?}

    @Shadow
    private void prepare() {
    }

    @Shadow
    private void clearUnusedOversizedItemRenderers() {
    }

    //?if <26.2 {
    /*@Shadow
    private void executeDrawRange(Supplier<String> supplier, RenderTarget arg, GpuBufferSlice gpuBufferSlice, GpuBufferSlice gpuBufferSlice2, GpuBuffer gpuBuffer, VertexFormat.IndexType arg2, int j, int k) {
    }
    *///?} else {
    @Shadow
    private void executeDrawRange(Supplier<String> supplier, RenderTarget arg, GpuBufferSlice gpuBufferSlice, int j, int k) {
    }
    //?}

    @Unique
    void chatShot$draw(GpuBufferSlice gpuBufferSlice, RenderTarget renderTarget) {
        if (!this.draws.isEmpty()) {
            //?if >=26.2 {
            WindowRenderState windowState = Minecraft.getInstance().gameRenderer.gameRenderState().windowRenderState;
            this.guiProjection.setupOrtho(1000.0F, 11000.0F, (float) windowState.width / windowState.guiScale, (float) windowState.height / windowState.guiScale, true);
            RenderSystem.setProjectionMatrix(this.guiProjectionMatrixBuffer.getBuffer(this.guiProjection), ProjectionType.ORTHOGRAPHIC);
            GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(new Matrix4f().setTranslation(0.0F, 0.0F, -11000.0F));
            if (this.firstDrawIndexAfterBlur > 0) {
                this.executeDrawRange(() -> "GUI before blur", renderTarget, dynamicTransforms, 0, Math.min(this.firstDrawIndexAfterBlur, this.draws.size()));
            }
            if (this.draws.size() > this.firstDrawIndexAfterBlur) {
                GpuTexture depthTexture = renderTarget.getDepthTexture();
                if (depthTexture == null) {
                    return;
                }
                RenderSystem.getDevice().createCommandEncoder().clearDepthTexture(depthTexture, 1.0F);
                this.executeDrawRange(() -> "GUI after blur", renderTarget, dynamicTransforms, this.firstDrawIndexAfterBlur, this.draws.size());
            }
            //?} else {
            /*//?if >26.1 {
            WindowRenderState windowState = Minecraft.getInstance().gameRenderer.getGameRenderState().windowRenderState;
            this.guiProjection.setupOrtho(1000.0F, 11000.0F, (float)windowState.width / (float)windowState.guiScale, (float)windowState.height / (float)windowState.guiScale, true);
            RenderSystem.setProjectionMatrix(this.guiProjectionMatrixBuffer.getBuffer(this.guiProjection), ProjectionType.ORTHOGRAPHIC);
            //?} else
            //RenderSystem.setProjectionMatrix(this.guiProjectionMatrixBuffer.getBuffer((float) Minecraft.getInstance().getWindow().getGuiScaledWidth(), (float) Minecraft.getInstance().getWindow().getGuiScaledHeight()), ProjectionType.ORTHOGRAPHIC);

            int i = 0;

            for (GuiRenderer.Draw guirenderer$draw : this.draws) {
                if (guirenderer$draw.indexCount > i) {
                    i = guirenderer$draw.indexCount;
                }
            }

            RenderSystem.AutoStorageIndexBuffer rendersystem$autostorageindexbuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
            GpuBuffer gpubuffer = rendersystem$autostorageindexbuffer.getBuffer(i);
            VertexFormat.IndexType vertexformat$indextype = rendersystem$autostorageindexbuffer.type();
            GpuBufferSlice gpubufferslice = RenderSystem.getDynamicUniforms().writeTransform(
                    (new Matrix4f()).setTranslation(0.0F, 0.0F, -11000.0F),
                    new Vector4f(1.0F, 1.0F, 1.0F, 1.0F),
                    new Vector3f(),
                    new Matrix4f()
                    //?if <1.21.11
                    //, 0.0F
            );
            if (this.firstDrawIndexAfterBlur > 0) {
                this.executeDrawRange(() -> "GUI before blur", renderTarget, gpuBufferSlice, gpubufferslice, gpubuffer, vertexformat$indextype, 0, Math.min(this.firstDrawIndexAfterBlur, this.draws.size()));
            }

            if (this.draws.size() > this.firstDrawIndexAfterBlur) {
                RenderSystem.getDevice().createCommandEncoder().clearDepthTexture(renderTarget.getDepthTexture(), (double) 1.0F);
                this.executeDrawRange(() -> "GUI after blur", renderTarget, gpuBufferSlice, gpubufferslice, gpubuffer, vertexformat$indextype, this.firstDrawIndexAfterBlur, this.draws.size());
            }
            *///?}
        }
    }

    @Override
    public void chatShot$render(GpuBufferSlice gpuBufferSlice, RenderTarget renderTarget) {
        this.prepare();
        //?if >=26.2 {
        this.vertexBuffer.upload();
        //?}
        this.chatShot$draw(gpuBufferSlice, renderTarget);

        //?if <26.2 {
        /*for (MappableRingBuffer mappableringbuffer : this.vertexBuffers.values()) {
            mappableringbuffer.rotate();
        }
        *///?}
        //?if >=26.2 {
        this.vertexBuffer.endDraw();
        this.vertexBuffer.endFrame();
        //?}

        this.draws.clear();
        //?if <26.2 {
        /*this.meshesToDraw.clear();
         *///?}
        this.renderState.reset();
        this.firstDrawIndexAfterBlur = Integer.MAX_VALUE;
        this.clearUnusedOversizedItemRenderers();
        //? if neoforge {
        /*this.pictureInPictureRendererPools.values().forEach(PictureInPictureRendererPool::clearUnusedRenderers);
         *///?}
    }
}
//?}