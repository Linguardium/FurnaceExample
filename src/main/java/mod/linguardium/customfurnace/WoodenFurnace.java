package mod.linguardium.customfurnace;

import mod.linguardium.customfurnace.api.CustomFurnaceBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.BlockView;

public class WoodenFurnace extends CustomFurnaceBlock {
    protected WoodenFurnace(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockView view) {
        return new WoodenFurnaceBlockEntity();
    }
}
