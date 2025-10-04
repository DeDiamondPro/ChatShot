package dev.dediamondpro.chatshot.util;
//? if >=1.21.6 {

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;

public interface  GuiRendererInterface {
     void chatShot$render(GpuBufferSlice gpuBufferSlice, RenderTarget renderTarget) ;
}
 
//?}