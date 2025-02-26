package dev.cudzer.cobblemonalphas.block;

import dev.cudzer.cobblemonalphas.CobblemonAlphasMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

public class ModBlocks {

    public static Block ALPHA_SPAWNER;

    private static Block register(ResourceLocation key, Block block) {
        Block registeredBlock = Registry.register(BuiltInRegistries.BLOCK, key, block);
        Registry.register(BuiltInRegistries.ITEM, key, new BlockItem(registeredBlock, new Item.Properties()));
        return registeredBlock;
    }

    public static void init(){
        CobblemonAlphasMod.LOGGER.info("Registering blocks...");
        ALPHA_SPAWNER = register(ResourceLocation.fromNamespaceAndPath(CobblemonAlphasMod.MOD_ID,"alpha_spawner"), new AlphaSpawnBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(5.0F).sound(SoundType.METAL).noOcclusion()));
    }
}
