package mod.linguardium.examplefurnace;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.container.Container;
import net.minecraft.container.FurnaceContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.recipe.RecipeType;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import static mod.linguardium.examplefurnace.ExampleFurnace.WOOD_STOVE_ENTITY_TYPE;

public class WoodStoveEntity extends AbstractFurnaceBlockEntity {

    public WoodStoveEntity() {
        super(WOOD_STOVE_ENTITY_TYPE, RecipeType.SMELTING);
    }

    @Override
    protected Text getContainerName() {
        return new TranslatableText("blockentity.examplefurnace.wood_stove");
    }

    protected Container createContainer(int i, PlayerInventory playerInventory) {
        return new FurnaceContainer(i, playerInventory, this, this.propertyDelegate);
    }
}
