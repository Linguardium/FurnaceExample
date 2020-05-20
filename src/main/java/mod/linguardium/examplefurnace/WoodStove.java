package mod.linguardium.examplefurnace;

import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.container.NameableContainerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class WoodStove extends AbstractFurnaceBlock {


    protected WoodStove(Settings settings) {
        super(settings);
    }

    protected void openContainer(World world, BlockPos pos, PlayerEntity player) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof WoodStoveEntity) {
            player.openContainer((NameableContainerFactory)blockEntity);
            player.incrementStat(Stats.INTERACT_WITH_FURNACE);
        }

    }

    @Override
    public BlockEntity createBlockEntity(BlockView view) {
        return new WoodStoveEntity();
    }
}
