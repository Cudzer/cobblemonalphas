package dev.cudzer.cobblemonalphas.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.cudzer.cobblemonalphas.CobblemonAlphasMod;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class PokemonUtils {
    public static final ResourceLocation ALPHA_ICON = ResourceLocation.fromNamespaceAndPath(CobblemonAlphasMod.MOD_ID, "textures/billboard/alpha_icon.png");


    public static void buildAlphaIcon(
        PoseStack poseStack,
        float x,
        float y,
        float width,
        float height,
        float scale
    ){
        draw(poseStack, x, y, height, width, scale);
    }

    private static void draw(@NotNull PoseStack matrixStack, @NotNull Number x, @NotNull Number y, @NotNull Number height, @NotNull Number width, float scale){

        RenderSystem.setShaderTexture(0, PokemonUtils.ALPHA_ICON);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        RenderSystem.setShaderColor(1, 1, 1, 1);
        matrixStack.pushPose();
        matrixStack.scale(scale, scale, 1.0F);
        Matrix4f lastPose = matrixStack.last().pose();
        drawRectangle(lastPose, x.floatValue(), y.floatValue(), x.floatValue() + width.floatValue(), y.floatValue() + height.floatValue(), 0 / width.floatValue(), (0 + width.floatValue()) / width.floatValue(), 0 / height.floatValue(), (0 + height.floatValue()) / height.floatValue());
        matrixStack.popPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void drawRectangle(@NotNull Matrix4f matrix, float x, float y, float endX, float endY, float minU, float maxU, float minV, float maxV){
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.addVertex(matrix, x, endY, 0).setUv(minU, maxV);
        bufferbuilder.addVertex(matrix, endX, endY, 0).setUv(maxU, maxV);
        bufferbuilder.addVertex(matrix, endX, y, 0).setUv(maxU, minV);
        bufferbuilder.addVertex(matrix, x, y, 0).setUv(minU, minV);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
    }
}
