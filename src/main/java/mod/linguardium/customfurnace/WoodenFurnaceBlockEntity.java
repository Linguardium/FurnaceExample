package mod.linguardium.customfurnace;

import mod.linguardium.customfurnace.api.CustomFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntityType;

import static mod.linguardium.customfurnace.Main.WOODEN_FURNACE_ENTITY_TYPE;

public class WoodenFurnaceBlockEntity extends CustomFurnaceBlockEntity {
    public WoodenFurnaceBlockEntity() {
        super(WOODEN_FURNACE_ENTITY_TYPE);
    }

    @Override
    public void tick() {
        this.burnTime=1000;
        super.tick();
    }

    @Override
    protected int getCookTime() {
        return super.getCookTime() * 2;
    }
}
