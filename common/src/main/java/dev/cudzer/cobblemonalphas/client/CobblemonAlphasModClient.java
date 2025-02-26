package dev.cudzer.cobblemonalphas.client;

import dev.cudzer.cobblemonalphas.blockEntity.ModBlockEntityType;
import dev.cudzer.cobblemonalphas.client.renderer.blockentity.AlphaSpawnerRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.BiConsumer;

public class CobblemonAlphasModClient {
    public static void registerBlockEntityRenderers(BiConsumer<BlockEntityType<? extends BlockEntity>, BlockEntityRendererProvider> consumer){
        consumer.accept(ModBlockEntityType.ALPHA_SPAWNER, AlphaSpawnerRenderer::new);
    }
}
