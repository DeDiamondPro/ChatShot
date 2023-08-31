package dev.dediamondpro.chatshot.mixins;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DrawContext.class)
public interface DrawContextMixin {
    @Accessor("matrices")
    void setMatrixStack(MatrixStack matrixStack);
}
