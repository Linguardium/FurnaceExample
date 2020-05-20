package mod.linguardium.customfurnace.api;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.container.Container;
import net.minecraft.container.FurnaceContainer;
import net.minecraft.container.PropertyDelegate;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.*;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class CustomFurnaceBlockEntity extends LockableContainerBlockEntity implements SidedInventory, RecipeUnlocker, RecipeInputProvider, Tickable {

    protected static final int[] TOP_SLOTS = new int[]{0};
    protected static final int[] BOTTOM_SLOTS = new int[]{2, 1};
    protected static final int[] SIDE_SLOTS = new int[]{1};
    protected DefaultedList<ItemStack> inventory;
    protected int burnTime;
    protected int fuelTime;
    protected int cookTime;
    protected int cookTimeTotal;
    protected final PropertyDelegate propertyDelegate;
    protected final Map<Identifier, Integer> recipesUsed;
    protected final RecipeType<? extends AbstractCookingRecipe> recipeType=RecipeType.SMELTING;


    public CustomFurnaceBlockEntity(BlockEntityType<? extends CustomFurnaceBlockEntity> type) {
        super(type);
        this.inventory = DefaultedList.ofSize(3, ItemStack.EMPTY);
        this.propertyDelegate = new PropertyDelegate() {
            public int get(int index) {
                switch(index) {
                    case 0:
                        return CustomFurnaceBlockEntity.this.burnTime;
                    case 1:
                        return CustomFurnaceBlockEntity.this.fuelTime;
                    case 2:
                        return CustomFurnaceBlockEntity.this.cookTime;
                    case 3:
                        return CustomFurnaceBlockEntity.this.cookTimeTotal;
                    default:
                        return 0;
                }
            }

            public void set(int index, int value) {
                switch(index) {
                    case 0:
                        CustomFurnaceBlockEntity.this.burnTime = value;
                        break;
                    case 1:
                        CustomFurnaceBlockEntity.this.fuelTime = value;
                        break;
                    case 2:
                        CustomFurnaceBlockEntity.this.cookTime = value;
                        break;
                    case 3:
                        CustomFurnaceBlockEntity.this.cookTimeTotal = value;
                }

            }

            public int size() {
                return 4;
            }
        };
        this.recipesUsed = Maps.newHashMap();
    }
    
    private boolean isBurning() {
        return this.burnTime > 0;
    }

    public void fromTag(CompoundTag tag) {
        super.fromTag(tag);
        this.inventory = DefaultedList.ofSize(this.getInvSize(), ItemStack.EMPTY);
        Inventories.fromTag(tag, this.inventory);
        this.burnTime = tag.getShort("BurnTime");
        this.cookTime = tag.getShort("CookTime");
        this.cookTimeTotal = tag.getShort("CookTimeTotal");
        this.fuelTime = this.getFuelTime(this.inventory.get(1));
        int i = tag.getShort("RecipesUsedSize");

        for(int j = 0; j < i; ++j) {
            Identifier identifier = new Identifier(tag.getString("RecipeLocation" + j));
            int k = tag.getInt("RecipeAmount" + j);
            this.recipesUsed.put(identifier, k);
        }

    }

    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        tag.putShort("BurnTime", (short)this.burnTime);
        tag.putShort("CookTime", (short)this.cookTime);
        tag.putShort("CookTimeTotal", (short)this.cookTimeTotal);
        Inventories.toTag(tag, this.inventory);
        tag.putShort("RecipesUsedSize", (short)this.recipesUsed.size());
        int i = 0;

        for(Iterator<Map.Entry<Identifier,Integer>> recipeUsedEntry = this.recipesUsed.entrySet().iterator(); recipeUsedEntry.hasNext(); ++i) {
            Map.Entry<Identifier, Integer> entry = recipeUsedEntry.next();
            tag.putString("RecipeLocation" + i, (entry.getKey()).toString());
            tag.putInt("RecipeAmount" + i, entry.getValue());
        }

        return tag;
    }
    @Override
    protected Text getContainerName() {
        return new TranslatableText("container.furnace");
    }

    @Override
    protected Container createContainer(int i, PlayerInventory playerInventory) {
        return new FurnaceContainer(i, playerInventory, this, this.propertyDelegate);
    }

    public void tick() {
        boolean burning = this.isBurning();
        boolean needsUpdating = false;
        if (this.isBurning()) {
            --this.burnTime;
        }

        if (!this.world.isClient) {
            ItemStack itemStack = this.inventory.get(1);
            if (!this.isBurning() && (itemStack.isEmpty() || (this.inventory.get(0)).isEmpty())) {
                if (!this.isBurning() && this.cookTime > 0) {
                    this.cookTime = MathHelper.clamp(this.cookTime - 2, 0, this.cookTimeTotal);
                }
            } else {
                Recipe<?> recipe = this.world.getRecipeManager().getFirstMatch(this.recipeType, this, this.world).orElse(null);
                if (!this.isBurning() && this.canAcceptRecipeOutput(recipe)) {
                    this.burnTime = this.getFuelTime(itemStack);
                    this.fuelTime = this.burnTime;
                    if (this.isBurning()) {
                        needsUpdating = true;
                        if (!itemStack.isEmpty()) {
                            Item item = itemStack.getItem();
                            itemStack.decrement(1);
                            if (itemStack.isEmpty()) {
                                Item item2 = item.getRecipeRemainder();
                                this.inventory.set(1, item2 == null ? ItemStack.EMPTY : new ItemStack(item2));
                            }
                        }
                    }
                }

                if (this.isBurning() && this.canAcceptRecipeOutput(recipe)) {
                    ++this.cookTime;
                    if (this.cookTime == this.cookTimeTotal) {
                        this.cookTime = 0;
                        this.cookTimeTotal = this.getCookTime();
                        this.craftRecipe(recipe);
                        needsUpdating = true;
                    }
                } else {
                    this.cookTime = 0;
                }
            }

            if (burning != this.isBurning()) {
                needsUpdating = true;
                this.world.setBlockState(this.pos, this.world.getBlockState(this.pos).with(AbstractFurnaceBlock.LIT, this.isBurning()), 3);
            }
        }

        if (needsUpdating) {
            this.markDirty();
        }

    }

    protected boolean canAcceptRecipeOutput(Recipe<?> recipe) {
        if (!(this.inventory.get(0)).isEmpty() && recipe != null) {
            ItemStack itemStack = recipe.getOutput();
            if (itemStack.isEmpty()) {
                return false;
            } else {
                ItemStack itemStack2 = this.inventory.get(2);
                if (itemStack2.isEmpty()) {
                    return true;
                } else if (!itemStack2.isItemEqualIgnoreDamage(itemStack)) {
                    return false;
                } else if (itemStack2.getCount() < this.getInvMaxStackAmount() && itemStack2.getCount() < itemStack2.getMaxCount()) {
                    return true;
                } else {
                    return itemStack2.getCount() < itemStack.getMaxCount();
                }
            }
        } else {
            return false;
        }
    }

    private void craftRecipe(Recipe<?> recipe) {
        if (recipe != null && this.canAcceptRecipeOutput(recipe)) {
            ItemStack itemStack = this.inventory.get(0);
            ItemStack itemStack2 = recipe.getOutput();
            ItemStack itemStack3 = this.inventory.get(2);
            if (itemStack3.isEmpty()) {
                this.inventory.set(2, itemStack2.copy());
            } else if (itemStack3.getItem() == itemStack2.getItem()) {
                itemStack3.increment(1);
            }

            if (!this.world.isClient()) {
                this.setLastRecipe(recipe);
            }

            if (itemStack.getItem() == Blocks.WET_SPONGE.asItem() && !(this.inventory.get(1)).isEmpty() && (this.inventory.get(1)).getItem() == Items.BUCKET) {
                this.inventory.set(1, new ItemStack(Items.WATER_BUCKET));
            }

            itemStack.decrement(1);
        }
    }

    protected int getFuelTime(ItemStack fuel) {
        if (fuel.isEmpty()) {
            return 0;
        } else {
            Item item = fuel.getItem();
            return FuelRegistry.INSTANCE.get(item);
        }
    }

    protected int getCookTime() {
        return this.world.getRecipeManager().getFirstMatch(this.recipeType, this, this.world).map(AbstractCookingRecipe::getCookTime).orElse(200);
    }

    public int[] getInvAvailableSlots(Direction side) {
        if (side == Direction.DOWN) {
            return BOTTOM_SLOTS;
        } else {
            return side == Direction.UP ? TOP_SLOTS : SIDE_SLOTS;
        }
    }

    public boolean canInsertInvStack(int slot, ItemStack stack, Direction dir) {
        return this.isValidInvStack(slot, stack);
    }

    public boolean canExtractInvStack(int slot, ItemStack stack, Direction dir) {
        if (dir == Direction.DOWN && slot == 1) {
            Item item = stack.getItem();
            if (item != Items.WATER_BUCKET && item != Items.BUCKET) {
                return false;
            }
        }

        return true;
    }

    public int getInvSize() {
        return this.inventory.size();
    }

    public boolean isInvEmpty() {
        Iterator var1 = this.inventory.iterator();

        ItemStack itemStack;
        do {
            if (!var1.hasNext()) {
                return true;
            }

            itemStack = (ItemStack)var1.next();
        } while(itemStack.isEmpty());

        return false;
    }

    public ItemStack getInvStack(int slot) {
        return this.inventory.get(slot);
    }

    public ItemStack takeInvStack(int slot, int amount) {
        return Inventories.splitStack(this.inventory, slot, amount);
    }

    public ItemStack removeInvStack(int slot) {
        return Inventories.removeStack(this.inventory, slot);
    }

    public void setInvStack(int slot, ItemStack stack) {
        ItemStack itemStack = (ItemStack)this.inventory.get(slot);
        boolean bl = !stack.isEmpty() && stack.isItemEqualIgnoreDamage(itemStack) && ItemStack.areTagsEqual(stack, itemStack);
        this.inventory.set(slot, stack);
        if (stack.getCount() > this.getInvMaxStackAmount()) {
            stack.setCount(this.getInvMaxStackAmount());
        }

        if (slot == 0 && !bl) {
            this.cookTimeTotal = this.getCookTime();
            this.cookTime = 0;
            this.markDirty();
        }

    }

    public boolean canPlayerUseInv(PlayerEntity player) {
        if (this.world.getBlockEntity(this.pos) != this) {
            return false;
        } else {
            return player.squaredDistanceTo((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
        }
    }

    public boolean isValidInvStack(int slot, ItemStack stack) {
        if (slot == 2) {
            return false;
        } else if (slot != 1) {
            return true;
        } else {
            ItemStack itemStack = (ItemStack)this.inventory.get(1);
            return FuelRegistry.INSTANCE.get(stack.getItem())!=null || stack.getItem() == Items.BUCKET && itemStack.getItem() != Items.BUCKET;
        }
    }

    public void clear() {
        this.inventory.clear();
    }

    public void setLastRecipe(Recipe<?> recipe) {
        if (recipe != null) {
            this.recipesUsed.compute(recipe.getId(), (identifier, integer) -> {
                return 1 + (integer == null ? 0 : integer);
            });
        }

    }


    public Recipe<?> getLastRecipe() {
        return null;
    }

    public void unlockLastRecipe(PlayerEntity player) {
    }

    public void dropExperience(PlayerEntity player) {
        List<Recipe<?>> list = Lists.newArrayList();
        Iterator var3 = this.recipesUsed.entrySet().iterator();

        while(var3.hasNext()) {
            Map.Entry<Identifier, Integer> entry = (Map.Entry)var3.next();
            player.world.getRecipeManager().get(entry.getKey()).ifPresent((recipe) -> {
                list.add(recipe);
                dropExperience(player, entry.getValue(), ((AbstractCookingRecipe)recipe).getExperience());
            });
        }

        player.unlockRecipes(list);
        this.recipesUsed.clear();
    }

    private static void dropExperience(PlayerEntity player, int totalExperience, float experienceFraction) {
        int i;
        if (experienceFraction == 0.0F) {
            totalExperience = 0;
        } else if (experienceFraction < 1.0F) {
            i = MathHelper.floor((float)totalExperience * experienceFraction);
            if (i < MathHelper.ceil((float)totalExperience * experienceFraction) && Math.random() < (double)((float)totalExperience * experienceFraction - (float)i)) {
                ++i;
            }

            totalExperience = i;
        }

        while(totalExperience > 0) {
            i = ExperienceOrbEntity.roundToOrbSize(totalExperience);
            totalExperience -= i;
            player.world.spawnEntity(new ExperienceOrbEntity(player.world, player.getX(), player.getY() + 0.5D, player.getZ() + 0.5D, i));
        }

    }

    public void provideRecipeInputs(RecipeFinder recipeFinder) {
        Iterator var2 = this.inventory.iterator();

        while(var2.hasNext()) {
            ItemStack itemStack = (ItemStack)var2.next();
            recipeFinder.addItem(itemStack);
        }

    }
}
