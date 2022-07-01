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
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.oredict.OreDictionary;
import pl.pabilo8.ctmb.common.block.TileEntityMultiblock;
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
	@ZenDoc("Tries to fill or drain a bucket from/into a tank. Returns true if action was performed ; false when nothing happened.")
	public static void bucketIntoTank(MultiblockTileCTWrapper ct, int tank, int inv, int bucketInputSlot, int bucketOutputSlot, boolean fillBucket)
	{
		TileEntityMultiblock te = ct.te;

		ItemStack bucket = CraftTweakerMC.getItemStack(ct.getItem(inv, bucketInputSlot));
		ItemStack out = CraftTweakerMC.getItemStack(ct.getItem(inv, bucketOutputSlot));

		IFluidHandlerItem capBucket = bucket.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
		FluidTank t = te.tanks[tank];
		int prev = t.getFluidAmount();

		if(capBucket!=null)
		{
			ItemStack emptyContainer;
			if(fillBucket)
			{
				if(t.getCapacity()==0)
					return;
				emptyContainer = blusunrize.immersiveengineering.common.util.Utils.fillFluidContainer(t,
						bucket, out, null);
			}
			else
			{
				if(capBucket.getTankProperties()[0].getContents()==null)
					return;
				emptyContainer = blusunrize.immersiveengineering.common.util.Utils.drainFluidContainer(t,
						bucket, out, null);
			}

			if(prev!=t.getFluidAmount())
			{
				if(!te.inventory.get(te.getInvOffset(inv, bucketOutputSlot)).isEmpty()&&OreDictionary.itemMatches(te.inventory.get(te.getInvOffset(inv, bucketOutputSlot)), emptyContainer, true))
					te.inventory.get(te.getInvOffset(inv, bucketOutputSlot)).grow(emptyContainer.getCount());
				else if(te.inventory.get(te.getInvOffset(inv, bucketOutputSlot)).isEmpty())
					te.inventory.set(te.getInvOffset(inv, bucketOutputSlot), emptyContainer.copy());
				te.inventory.get(te.getInvOffset(inv, bucketInputSlot)).shrink(1);
				if(te.inventory.get(te.getInvOffset(inv, bucketInputSlot)).getCount() <= 0)
					te.inventory.set(te.getInvOffset(inv, bucketInputSlot), ItemStack.EMPTY);

				te.forceUpdate();
			}
		}
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

		if(mineral==null||mineral.getType()==null||mineral.current<=0)
			return null;

		int cap = Math.min(mineral.current, amount);
		PumpjackHandler.depleteFluid(CraftTweakerMC.getWorld(world), pos.getX()>>4, pos.getZ()>>4, amount);

		return new MCLiquidStack(new FluidStack(mineral.getType().getFluid(), cap));

	}


}
