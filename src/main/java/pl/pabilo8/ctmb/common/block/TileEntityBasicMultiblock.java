package pl.pabilo8.ctmb.common.block;

import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorageAdvanced;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IRedstoneOutput;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import pl.pabilo8.ctmb.common.crafttweaker.MultiblockBasic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Pabilo8
 * @since 30.01.2022
 */
@SuppressWarnings("unused")
public class TileEntityBasicMultiblock extends TileEntityMultiblockMetal<TileEntityBasicMultiblock, IMultiblockRecipe> implements IRedstoneOutput, IPlayerInteraction, IGuiTile
{
	/**
	 * Multiblock Instance for easy access
	 **/
	private MultiblockBasic multiblock = MultiblockBasic.DEFAULT_MULTIBLOCK;

	public TileEntityBasicMultiblock()
	{
		//DO NOT USE, method used when loading a saved TE only!
		super(MultiblockBasic.DEFAULT_MULTIBLOCK, new int[0], 0, false);
	}

	public TileEntityBasicMultiblock(MultiblockBasic mb)
	{
		super(mb, mb.getSize(), mb.energyCapacity, mb.redstoneControl);
		this.multiblock = mb;
	}

	@Override
	public void onLoad()
	{
		ensureMBLoaded();
		super.onLoad();
	}

	private void ensureMBLoaded()
	{
		if((multiblock==null||multiblock==MultiblockBasic.DEFAULT_MULTIBLOCK)&&world!=null)
		{
			Block block = world.getBlockState(getPos()).getBlock();
			if(block instanceof BlockCTMBMultiblock)
			{
				BlockCTMBMultiblock mbBlock = (BlockCTMBMultiblock)block;

				ReflectionHelper.setPrivateValue(TileEntityMultiblockMetal.class, this, mbBlock.multiblock, "mutliblockInstance");
				ReflectionHelper.setPrivateValue(TileEntityMultiblockPart.class, this, mbBlock.multiblock.getSize(), "structureDimensions");
				ReflectionHelper.setPrivateValue(TileEntityMultiblockMetal.class, this, new FluxStorageAdvanced(mbBlock.multiblock.energyCapacity), "energyStorage");
				ReflectionHelper.setPrivateValue(TileEntityMultiblockMetal.class, this, mbBlock.multiblock.redstoneControl, "hasRedstoneControl");
			}
			//else ur fucked)))
		}
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		ensureMBLoaded();
		super.readCustomNBT(nbt, descPacket);
	}

	@Override
	protected IMultiblockRecipe readRecipeFromNBT(NBTTagCompound tag)
	{
		return null;
	}

	@Override
	public int[] getEnergyPos()
	{
		return multiblock.energyPositions;
	}

	@Override
	public int[] getRedstonePos()
	{
		return multiblock.redstonePositions;
	}

	@Override
	public IFluidTank[] getInternalTanks()
	{
		return new IFluidTank[0];
	}

	@Override
	public IMultiblockRecipe findRecipeForInsertion(ItemStack inserting)
	{
		return null;
	}

	@Override
	public int[] getOutputSlots()
	{
		return new int[0];
	}

	@Override
	public int[] getOutputTanks()
	{
		return new int[0];
	}

	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess process)
	{
		return false;
	}

	@Override
	public void doProcessOutput(ItemStack output)
	{

	}

	@Override
	public void doProcessFluidOutput(FluidStack output)
	{

	}

	@Override
	public void onProcessFinish(MultiblockProcess process)
	{

	}

	@Override
	public int getMaxProcessPerTick()
	{
		return 0;
	}

	@Override
	public int getProcessQueueMaxLength()
	{
		return 0;
	}

	@Override
	public float getMinProcessDistance(MultiblockProcess process)
	{
		return 0;
	}

	@Override
	public boolean isInWorldProcessingMachine()
	{
		return false;
	}

	@Nonnull
	@Override
	protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side)
	{
		return new IFluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource)
	{
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, EnumFacing side)
	{
		return false;
	}

	@Override
	public float[] getBlockBounds()
	{
		return new float[0];
	}

	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return null;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return false;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 0;
	}

	@Override
	public void doGraphicalUpdates(int slot)
	{

	}

	// TODO: 20.02.2022 redstone
	@Override
	public int getStrongRSOutput(IBlockState state, EnumFacing side)
	{
		return 0;
	}

	@Override
	public boolean canConnectRedstone(IBlockState state, EnumFacing side)
	{
		return false;
	}

	@Override
	public boolean canOpenGui()
	{
		return false;
	}

	@Override
	public int getGuiID()
	{
		return 0;
	}

	@Nullable
	@Override
	public TileEntityBasicMultiblock getGuiMaster()
	{
		return master();
	}

	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		return false;
	}

	public MultiblockBasic getMultiblock()
	{
		return multiblock;
	}
}
