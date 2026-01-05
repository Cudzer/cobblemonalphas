package dev.cudzer.cobblemonalphas.block;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.ai.PokemonBrain;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.cudzer.cobblemonalphas.CobblemonAlphasMod;
import dev.cudzer.cobblemonalphas.block.item.AlphaSpawnerBlockItem;
import dev.cudzer.cobblemonalphas.blockEntity.AlphaSpawnBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(CobblemonAlphasMod.MOD_ID, Registries.BLOCK);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(CobblemonAlphasMod.MOD_ID, Registries.ITEM);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(CobblemonAlphasMod.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<Block> ALPHA_SPAWNER_BLOCK =
            BLOCKS.register("alpha_spawner", () -> new AlphaSpawnBlock(BlockBehaviour.Properties.of().noOcclusion().strength(5.0f)));

    public static final RegistrySupplier<BlockItem> ALPHA_SPAWNER_ITEM =
            ITEMS.register("alpha_spawner", () -> new AlphaSpawnerBlockItem(ALPHA_SPAWNER_BLOCK.get(), new Item.Properties()));

    public static final RegistrySupplier<BlockEntityType<AlphaSpawnBlockEntity>> ALPHA_SPAWNER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("alpha_spawner", () -> BlockEntityType.Builder.of(AlphaSpawnBlockEntity::new, ALPHA_SPAWNER_BLOCK.get()).build(null));
}
