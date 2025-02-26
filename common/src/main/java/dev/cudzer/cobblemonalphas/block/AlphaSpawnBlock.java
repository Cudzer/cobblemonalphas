package dev.cudzer.cobblemonalphas.block;

import com.mojang.serialization.MapCodec;
import dev.cudzer.cobblemonalphas.blockEntity.AlphaSpawnBlockEntity;
import dev.cudzer.cobblemonalphas.blockEntity.ModBlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AlphaSpawnBlock extends BaseEntityBlock {
    public static final MapCodec<AlphaSpawnBlock> CODEC = simpleCodec(AlphaSpawnBlock::new);

    public AlphaSpawnBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new AlphaSpawnBlockEntity(blockPos, blockState);
    }

    protected @NotNull RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlockEntityType.ALPHA_SPAWNER, level.isClientSide ? AlphaSpawnBlockEntity::clientTick : AlphaSpawnBlockEntity::serverTick);
    }
}
