package dev.cudzer.cobblemonalphas.blockEntity;

import dev.cudzer.cobblemonalphas.block.ModBlocks;
import dev.cudzer.cobblemonalphas.config.ModConfig;
import dev.cudzer.cobblemonalphas.data.AlphaJsonDataManager;
import dev.cudzer.cobblemonalphas.entity.Alpha;
import dev.cudzer.cobblemonalphas.entity.spawner.AlphaSpawner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AlphaSpawnBlockEntity extends BlockEntity {

    protected int ticks = 0;
    private static final String ALPHA_ID_KEY = "AlphaId";
    private ResourceLocation forcedAlpha = null;

    public AlphaSpawnBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlocks.ALPHA_SPAWNER_BLOCK_ENTITY.get(), blockPos, blockState);
    }

    public ResourceLocation getForcedAlpha() {
        return forcedAlpha;
    }

    public void setForcedAlpha(ResourceLocation id) {
        this.forcedAlpha = id;
        setChanged();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        if (tag.contains(ALPHA_ID_KEY)) {
            forcedAlpha = ResourceLocation.tryParse(tag.getString(ALPHA_ID_KEY));
        } else {
            forcedAlpha = null;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);

        if (forcedAlpha != null) {
            tag.putString(ALPHA_ID_KEY, forcedAlpha.toString());
        }
    }

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, AlphaSpawnBlockEntity be) {
        if (++be.ticks % 40 != 0) return;
        if (level == null || !level.isLoaded(blockPos)) return;
        if (blockState == null) return;

        Player player = level.getNearestPlayer(
                blockPos.getX(), blockPos.getY(), blockPos.getZ(),
                8,
                EntitySelector.NO_CREATIVE_OR_SPECTATOR
        );

        if (player == null) return;
        Alpha alpha = null;

        if (be.getForcedAlpha() != null) {
            alpha = AlphaJsonDataManager.getAlphaById(be.getForcedAlpha()) ;
        }

        if (alpha == null) {
            var alphaMap = AlphaJsonDataManager.getRandomAlphaForBiome(
                    level,
                    level.getBiome(blockPos).unwrapKey().get(),
                    false
            );

            if (alphaMap == null || alphaMap.isEmpty()) return;

            alpha = alphaMap.values().iterator().next();
        }
        level.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
        AlphaSpawner.getInstance().spawnAlphaEntity(alpha, level, blockPos, ModConfig.doHerdSpawning);
    }
}
