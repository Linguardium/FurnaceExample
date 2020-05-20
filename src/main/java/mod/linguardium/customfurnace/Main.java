package mod.linguardium.customfurnace;

import mod.linguardium.customfurnace.api.CustomFurnaceBlock;
import mod.linguardium.customfurnace.api.CustomFurnaceBlockEntity;
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

public class Main implements ModInitializer {

    public static Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "customfurnace";
    public static final String MOD_NAME = "CustomFurnace";
    public static BlockEntityType<WoodenFurnaceBlockEntity> WOODEN_FURNACE_ENTITY_TYPE;
    public static WoodenFurnace WOODEN_FURNACE = new WoodenFurnace(FabricBlockSettings.copyOf(Blocks.FURNACE));
    public static BlockItem WOODEN_FURNACE_ITEM = Registry.register(Registry.ITEM,new Identifier(MOD_ID,"wood_furnace"),new BlockItem(WOODEN_FURNACE,new Item.Settings()));
    @Override
    public void onInitialize() {
        log(Level.INFO, "Initializing");
        WOODEN_FURNACE_ENTITY_TYPE = Registry.register(Registry.BLOCK_ENTITY_TYPE,new Identifier(MOD_ID,"wood_furnace_blockentity"),BlockEntityType.Builder.create(WoodenFurnaceBlockEntity::new, WOODEN_FURNACE).build(null));

    }

    public static void log(Level level, String message){
        LOGGER.log(level, "["+MOD_NAME+"] " + message);
    }

}