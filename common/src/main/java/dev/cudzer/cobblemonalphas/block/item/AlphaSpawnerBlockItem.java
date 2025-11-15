package dev.cudzer.cobblemonalphas.block.item;

import dev.cudzer.cobblemonalphas.blockEntity.AlphaSpawnBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class AlphaSpawnerBlockItem extends BlockItem {
    public AlphaSpawnerBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, Player player,
                                                 ItemStack stack, BlockState state) {

        CustomData data = stack.get(DataComponents.CUSTOM_DATA);

        if (data != null && data.contains("AlphaId")) {

            String alphaId = data.getUnsafe().getString("AlphaId");

            if (level.getBlockEntity(pos) instanceof AlphaSpawnBlockEntity be) {
                be.setForcedAlpha(ResourceLocation.tryParse(alphaId));
            }
        }

        return super.updateCustomBlockEntityTag(pos, level, player, stack, state);
    }
}
