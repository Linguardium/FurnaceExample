package mod.linguardium.examplefurnace;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExampleFurnace implements ModInitializer {

    public static Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "examplefurnace";
    public static final String MOD_NAME = "FurnaceExample";
    public static BlockEntityType<WoodStoveEntity> WOOD_STOVE_ENTITY_TYPE;
    public static WoodStove WOOD_STOVE = new WoodStove(FabricBlockSettings.copyOf(Blocks.FURNACE));
    public static BlockItem WOOD_STOVE_ITEM = Registry.register(Registry.ITEM,new Identifier(MOD_ID,"wood_furnace"),new BlockItem(WOOD_STOVE,new Item.Settings()));
    @Override
    public void onInitialize() {
        log(Level.INFO, "Initializing");
        WOOD_STOVE_ENTITY_TYPE = Registry.register(Registry.BLOCK_ENTITY_TYPE,new Identifier(MOD_ID,"wood_stove_blockentity"),BlockEntityType.Builder.create(WoodStoveEntity::new, WOOD_STOVE).build(null));

    }

    public static void log(Level level, String message){
        LOGGER.log(level, "["+MOD_NAME+"] " + message);
    }

}