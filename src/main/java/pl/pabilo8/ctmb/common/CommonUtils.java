package pl.pabilo8.ctmb.common;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import pl.pabilo8.ctmb.CTMB;

import java.util.function.Predicate;

/**
 * @author Pabilo8
 * @since 29.01.2022
 */
public class CommonUtils
{
	/**
	 * GUI Text Colors
	 */
	public static final int COLOR_H1 = 0x0a0a0a, COLOR_H2 = 0x1a1a1a;

	/**
	 * @return a {@link TargetPoint} for a network message
	 */
	public static TargetPoint targetPointFromTile(TileEntity tile, int range)
	{
		return new TargetPoint(tile.getWorld().provider.getDimension(), tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ(), range);
	}

	/**
	 * @return a {@link TargetPoint} for a network message
	 */
	public static TargetPoint targetPointFromPos(Vec3d pos, World world, int range)
	{
		return new TargetPoint(world.provider.getDimension(), pos.x, pos.y, pos.z, range);
	}

	/**
	 * Same as method below, but without filter
	 */
	public static <T extends IFluidTank & IFluidHandler> boolean handleBucketTankInteraction(T tank, NonNullList<ItemStack> inventory, int bucketInputSlot, int bucketOutputSlot, boolean fillBucket)
	{
		return handleBucketTankInteraction(tank, inventory, bucketInputSlot, bucketOutputSlot, fillBucket, fluidStack -> true);
	}

	/**
	 * @param tank
	 * @param inventory
	 * @param bucketInputSlot
	 * @param bucketOutputSlot
	 * @param fillBucket
	 * @param filter
	 * @param <T>
	 * @return
	 */
	public static <T extends IFluidTank & IFluidHandler> boolean handleBucketTankInteraction(T tank, NonNullList<ItemStack> inventory, int bucketInputSlot, int bucketOutputSlot, boolean fillBucket, Predicate<FluidStack> filter)
	{
		if(inventory.get(bucketInputSlot).hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null))
		{
			IFluidHandlerItem capability = inventory.get(bucketInputSlot).getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
			if(!filter.test(capability.getTankProperties()[0].getContents()))
				return false;

			int amount_prev = tank.getFluidAmount();
			ItemStack emptyContainer;

			if(fillBucket)
			{
				if(tank.getTankProperties()[0].getContents()==null)
					return false;
				emptyContainer = blusunrize.immersiveengineering.common.util.Utils.fillFluidContainer(tank, inventory.get(bucketInputSlot), inventory.get(bucketOutputSlot), null);
			}
			else
			{
				if(capability.getTankProperties()[0].getContents()==null)
					return false;
				emptyContainer = blusunrize.immersiveengineering.common.util.Utils.drainFluidContainer(tank, inventory.get(bucketInputSlot), inventory.get(bucketOutputSlot), null);
			}

			if(amount_prev!=tank.getFluidAmount())
			{
				if(!inventory.get(bucketOutputSlot).isEmpty()&&OreDictionary.itemMatches(inventory.get(bucketOutputSlot), emptyContainer, true))
					inventory.get(bucketOutputSlot).grow(emptyContainer.getCount());
				else if(inventory.get(bucketOutputSlot).isEmpty())
					inventory.set(bucketOutputSlot, emptyContainer.copy());
				inventory.get(bucketInputSlot).shrink(1);
				if(inventory.get(bucketInputSlot).getCount() <= 0)
					inventory.set(bucketInputSlot, ItemStack.EMPTY);

				return true;
			}
		}
		return false;
	}

	/**
	 * Safely handles input into a tank
	 *
	 * @param tank   to be outputted to
	 * @param amount of fluid
	 * @param pos    of the {@link TileEntity}
	 * @param world  of the {@link TileEntity}
	 * @param side   of output
	 * @return whether the tank filling was a success
	 */
	public static boolean outputFluidToTank(FluidTank tank, int amount, BlockPos pos, World world, EnumFacing side)
	{
		if(tank.getFluidAmount() > 0)
		{
			FluidStack out = blusunrize.immersiveengineering.common.util.Utils.copyFluidStackWithAmount(tank.getFluid(), Math.min(tank.getFluidAmount(), amount), false);
			IFluidHandler output = FluidUtil.getFluidHandler(world, pos.offset(side), side);
			if(output!=null)
			{
				int accepted = output.fill(out, false);
				if(accepted > 0)
				{
					int drained = output.fill(blusunrize.immersiveengineering.common.util.Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
					tank.drain(drained, true);
					return true;
				}
			}
		}
		return false;
	}

	public static float[] rgbIntToRGB(int rgb)
	{
		float r = ((rgb >> 16)&255)/255f;
		float g = ((rgb >> 8)&255)/255f;
		float b = (rgb&255)/255f;
		return new float[]{r, g, b};
	}

	//Converts snake_case to camelCase or CamelCase
	//Copy as you wish
	public static String toCamelCase(String string, boolean startSmall)
	{
		StringBuilder result = new StringBuilder();
		String[] all = string.split("_");
		for(String s : all)
		{
			result.append(Character.toUpperCase(s.charAt(0)));
			result.append(s.substring(1));
		}
		if(startSmall)
			result.setCharAt(0, Character.toLowerCase(result.charAt(0)));
		return result.toString();
	}

	public static void registerTile(Class<? extends TileEntity> tile)
	{
		String s = tile.getSimpleName();
		s = s.substring(s.indexOf("TileEntity")+"TileEntity".length());
		GameRegistry.registerTileEntity(tile, new ResourceLocation(CTMB.MODID+":"+s));
	}
}
