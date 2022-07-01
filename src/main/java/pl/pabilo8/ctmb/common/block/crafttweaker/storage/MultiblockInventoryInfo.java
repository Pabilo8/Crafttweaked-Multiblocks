package pl.pabilo8.ctmb.common.block.crafttweaker.storage;

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
	private final int offset;
	public Predicate<ItemStack> filter = fluidStack -> true;

	public MultiblockInventoryInfo(int id, int capacity, int offset)
	{
		super(id, capacity);
		this.offset = offset;
	}

	// TODO: 01.07.2022 finish
	public void setFilter(Predicate<ItemStack> filter)
	{
		this.filter = filter;
	}

	public int getOffset()
	{
		return offset;
	}
}