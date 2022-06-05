package pl.pabilo8.ctmb.common.crafttweaker.storage;

import crafttweaker.annotations.ZenRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import stanhebben.zenscript.annotations.ZenClass;

import java.util.function.Predicate;

/**
 * @author Pabilo8
 * @since 30.05.2022
 */
@ZenRegister
@ZenClass(value = "mods.ctmb.multiblock.Inventory")
public class MultiblockInventoryInfo extends MultiblockStorageInfo<NonNullList<ItemStack>>
{
	public Predicate<ItemStack> filter = fluidStack -> true;

	public MultiblockInventoryInfo(int id, int capacity, boolean canInput)
	{
		super(id, capacity, canInput);
	}

	public void setFilter(Predicate<ItemStack> filter)
	{
		this.filter = filter;
	}
}