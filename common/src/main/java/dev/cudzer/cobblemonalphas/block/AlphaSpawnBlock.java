package dev.cudzer.cobblemonalphas.block;

import com.mojang.serialization.MapCodec;
import dev.cudzer.cobblemonalphas.blockEntity.AlphaSpawnBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class AlphaSpawnBlock extends Block implements EntityBlock {
    private static final MapCodec<AlphaSpawnBlock> CODEC = simpleCodec(AlphaSpawnBlock::new);

    public AlphaSpawnBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new AlphaSpawnBlockEntity(blockPos, blockState);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (type == ModBlocks.ALPHA_SPAWNER_BLOCK_ENTITY.get()) {
            return (lvl, pos, st, be) -> {
                if (be instanceof AlphaSpawnBlockEntity spawner) {
                    AlphaSpawnBlockEntity.tick(lvl, pos, st, spawner);
                }
            };
        }
        
        return null;
    }
}
