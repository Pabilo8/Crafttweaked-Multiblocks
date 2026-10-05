package pl.pabilo8.ctmb.common.block.crafttweaker;

import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenDoc;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.world.IBlockPos;
import crafttweaker.api.world.IWorld;
import crafttweaker.mc1120.liquid.MCLiquidStack;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Optional.Method;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nullable;

/**
 * @author Pabilo8
 * @since 10.06.2022
 */
@ZenRegister
@ZenClass(value = "mods.ctmb.multiblock.MultiblockUtils")
public class MultiblockTileCTUtils
{
	//--- Fluid Interaction ---//

	@ZenMethod
	@ZenDoc("Transfers one fluid container between named storage providers; returns whether it succeeded.")
	public static boolean bucketIntoTank(pl.pabilo8.ctmb.common.storage.StorageAccess tank,
										 pl.pabilo8.ctmb.common.storage.StorageAccess items, int input, int output, boolean fillBucket)
	{
		tank.require(pl.pabilo8.ctmb.common.storage.StorageDefinition.Kind.FLUID);
		items.require(pl.pabilo8.ctmb.common.storage.StorageDefinition.Kind.ITEM);
		if(tank.system!=items.system)
			throw new IllegalArgumentException("Container transfer requires the same machine");
		if(!tank.system.isServer()||input==output||items.item(input).isEmpty()) return false;
		ItemStack container = items.item(input).copy();
		container.setCount(1);
		net.minecraftforge.fluids.FluidActionResult preview = fillBucket?
				net.minecraftforge.fluids.FluidUtil.tryFillContainer(container, tank.fluidTank(), Integer.MAX_VALUE, null, false):
				net.minecraftforge.fluids.FluidUtil.tryEmptyContainer(container, tank.fluidTank(), Integer.MAX_VALUE, null, false);
		if(!preview.isSuccess()||!items.insert(output, preview.getResult(), true).isEmpty()) return false;
		net.minecraftforge.fluids.FluidActionResult result = fillBucket?
				net.minecraftforge.fluids.FluidUtil.tryFillContainer(container, tank.fluidTank(), Integer.MAX_VALUE, null, true):
				net.minecraftforge.fluids.FluidUtil.tryEmptyContainer(container, tank.fluidTank(), Integer.MAX_VALUE, null, true);
		if(!result.isSuccess()) return false;
		items.extract(input, 1, false);
		items.insert(output, result.getResult(), false);
		return true;
	}

	//--- Excavator ---//

	@ZenMethod
	@ZenDoc("Checks for excavator ores at a position. Returns true if there is an undepleted deposit")
	public static boolean hasExcavatorOres(IWorld world, IBlockPos pos)
	{
		ExcavatorHandler.MineralMix mineral = ExcavatorHandler.getRandomMineral(CraftTweakerMC.getWorld(world),
				pos.getX()>>4, pos.getZ()>>4);
		return mineral!=null;
	}

	@ZenMethod
	@ZenDoc("Returns a random ore from an excavator deposit at a position. Will deplete the deposit by default.")
	public static IItemStack mineExcavatorOres(IWorld world, IBlockPos pos, @Optional(valueBoolean = true) boolean deplete)
	{
		ExcavatorHandler.MineralMix mineral = ExcavatorHandler.getRandomMineral(CraftTweakerMC.getWorld(world),
				pos.getX()>>4, pos.getZ()>>4);
		if(deplete)
			ExcavatorHandler.depleteMinerals(CraftTweakerMC.getWorld(world), pos.getX()>>4, pos.getZ()>>4);

		//float failChance = Utils.RAND.nextFloat();

		return CraftTweakerMC.getIItemStack(mineral==null?ItemStack.EMPTY: mineral.getRandomOre(Utils.RAND));

	}

	//--- IP Pumpjack ---//

	@Method(modid = "immersivepetroleum")
	@ModOnly("immersivepetroleum")
	@ZenMethod
	@ZenDoc("Checks for excavator ores at a position. Returns true if there is an undepleted deposit")
	public static boolean hasPumpjackReservoir(IWorld world, IBlockPos pos)
	{
		PumpjackHandler.OilWorldInfo mineral = PumpjackHandler.getOilWorldInfo(CraftTweakerMC.getWorld(world),
				pos.getX()>>4, pos.getZ()>>4);
		return mineral!=null;
	}

	@Nullable
	@Method(modid = "immersivepetroleum")
	@ModOnly("immersivepetroleum")
	@ZenMethod
	@ZenDoc("Returns a random ore from an excavator deposit at a position. Will deplete the deposit by default.")
	public static ILiquidStack minePumpjackReservoir(IWorld world, IBlockPos pos, int amount)
	{
		PumpjackHandler.OilWorldInfo mineral = PumpjackHandler.getOilWorldInfo(CraftTweakerMC.getWorld(world),
				pos.getX()>>4, pos.getZ()>>4);

		if(mineral==null||mineral.getType()==null||mineral.current <= 0)
			return null;

		int cap = Math.min(mineral.current, amount);
		PumpjackHandler.depleteFluid(CraftTweakerMC.getWorld(world), pos.getX()>>4, pos.getZ()>>4, amount);

		return new MCLiquidStack(new FluidStack(mineral.getType().getFluid(), cap));

	}


}
