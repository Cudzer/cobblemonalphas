package dev.cudzer.cobblemonalphas.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.cudzer.cobblemonalphas.blockEntity.AlphaSpawnBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

@Environment(EnvType.CLIENT)
public class AlphaSpawnerRenderer implements BlockEntityRenderer<AlphaSpawnBlockEntity> {


    public AlphaSpawnerRenderer(BlockEntityRendererProvider.Context context){
        super();
    }

    @Override
    public void render(AlphaSpawnBlockEntity blockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {

    }
}
