package pl.pabilo8.ctmb.common.block.crafttweaker.storage;

import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorageAdvanced;
import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;

/**
 * @author Pabilo8
 * @since 30.05.2022
 */
@ZenRegister
@ZenClass(value = "mods.ctmb.multiblock.Energy")
public class MultiblockEnergyInfo extends MultiblockStorageInfo<FluxStorageAdvanced>
{
	public MultiblockEnergyInfo(int id, int capacity)
	{
		super(id, capacity);
	}
}