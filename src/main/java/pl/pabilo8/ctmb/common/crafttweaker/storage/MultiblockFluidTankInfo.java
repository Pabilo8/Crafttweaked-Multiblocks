package pl.pabilo8.ctmb.common.crafttweaker.storage;

import crafttweaker.annotations.ZenRegister;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import stanhebben.zenscript.annotations.ZenClass;

import java.util.function.Predicate;

/**
 * @author Pabilo8
 * @since 30.05.2022
 */
@ZenRegister
@ZenClass(value = "mods.ctmb.multiblock.Tank")
public class MultiblockFluidTankInfo extends MultiblockStorageInfo<FluidTank>
{
	public Predicate<FluidStack> filter = fluidStack -> true;

	public MultiblockFluidTankInfo(int id, int capacity, boolean canInput)
	{
		super(id, capacity, canInput);
	}


}
