package dev.cudzer.cobblemonalphas.blockEntity;

import com.mojang.datafixers.types.Type;
import dev.cudzer.cobblemonalphas.CobblemonAlphasMod;
import dev.cudzer.cobblemonalphas.block.ModBlocks;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntityType<T extends BlockEntity> {
    public static BlockEntityType<AlphaSpawnBlockEntity> ALPHA_SPAWNER;

    public static void init(){
        CobblemonAlphasMod.LOGGER.info("Registering block entities...");
        ALPHA_SPAWNER = register(CobblemonAlphasMod.cobblemonAlphasResource("alpha_spawner"), BlockEntityType.Builder.of(AlphaSpawnBlockEntity::new, ModBlocks.ALPHA_SPAWNER));
    }

    private static <T extends BlockEntity> BlockEntityType<T> register(ResourceLocation key, BlockEntityType.Builder<T> builder) {
        Type<?> type = Util.fetchChoiceType(References.BLOCK_ENTITY, key.getPath());
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, key, builder.build(type));
    }
}
