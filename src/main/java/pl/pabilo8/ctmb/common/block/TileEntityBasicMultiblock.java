package pl.pabilo8.ctmb.common.block;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorageAdvanced;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.network.MessageTileSync;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import pl.pabilo8.ctmb.common.CommonProxy;
import pl.pabilo8.ctmb.common.CommonUtils;
import pl.pabilo8.ctmb.common.crafttweaker.MultiblockBasic;
import pl.pabilo8.ctmb.common.crafttweaker.MultiblockTileCTWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Pabilo8
 * @since 30.01.2022
 */
@SuppressWarnings("unused")
public class TileEntityBasicMultiblock extends TileEntityMultiblockMetal<TileEntityBasicMultiblock, IMultiblockRecipe> implements IRedstoneOutput, IPlayerInteraction, IGuiTile, IAdvancedCollisionBounds, IAdvancedSelectionBounds
{
	/**
	 * Multiblock Instance for easy access
	 **/
	private MultiblockBasic multiblock = MultiblockBasic.DEFAULT_MULTIBLOCK;
	private MultiblockTileCTWrapper mbWrapper;

	public FluxStorageAdvanced[] energy;
	public FluidTank[] tanks;
	public NonNullList<ItemStack> inventory = NonNullList.create();
	public CombinedInvWrapper itemHandler;

	public TileEntityBasicMultiblock()
	{
		//DO NOT USE, method used when loading a saved TE only!
		super(MultiblockBasic.DEFAULT_MULTIBLOCK, new int[0], 0, false);
	}

	public TileEntityBasicMultiblock(MultiblockBasic mb)
	{
		super(mb, mb.getSize(), 0, mb.redstonePositions.length > 0);
		this.multiblock = mb;
	}

	void ensureMBLoaded(Block block)
	{
		if((multiblock==null||multiblock==MultiblockBasic.DEFAULT_MULTIBLOCK))
		{
			if(block instanceof BlockCTMBMultiblock)
			{
				BlockCTMBMultiblock mbBlock = (BlockCTMBMultiblock)block;
				this.multiblock = mbBlock.multiblock;
				this.mbWrapper = new MultiblockTileCTWrapper(this);
				ReflectionHelper.setPrivateValue(TileEntityMultiblockMetal.class, this, mbBlock.multiblock, "mutliblockInstance");
				ReflectionHelper.setPrivateValue(TileEntityMultiblockPart.class, this, mbBlock.multiblock.getSize(), "structureDimensions");
				ReflectionHelper.setPrivateValue(TileEntityMultiblockMetal.class, this, new FluxStorageAdvanced(0), "energyStorage");
				ReflectionHelper.setPrivateValue(TileEntityMultiblockMetal.class, this, mbBlock.multiblock.redstonePositions.length > 0, "hasRedstoneControl");
			}
			//else ur fucked)))
		}
	}

	//--- NBT Messages ---//

	@Override
	public void receiveMessageFromClient(NBTTagCompound message)
	{
		super.receiveMessageFromClient(message);

		if(multiblock.onReceiveMessage!=null)
			multiblock.onReceiveMessage.execute(mbWrapper, CraftTweakerMC.getIData(message), false);
	}

	@Override
	public void receiveMessageFromServer(NBTTagCompound message)
	{
		super.receiveMessageFromServer(message);

		if(multiblock.onReceiveMessage!=null)
			multiblock.onReceiveMessage.execute(mbWrapper, CraftTweakerMC.getIData(message), true);
	}

	public void sendMessageServer(NBTTagCompound message)
	{
		if(multiblock.onSendMessage!=null)
			multiblock.onSendMessage.execute(mbWrapper, CraftTweakerMC.getIData(message), world.isRemote);

		ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(this, message));
	}

	public void sendMessageClients(NBTTagCompound message, int range)
	{
		if(multiblock.onSendMessage!=null)
			multiblock.onSendMessage.execute(mbWrapper, CraftTweakerMC.getIData(message), world.isRemote);

		ImmersiveEngineering.packetHandler.sendToAllAround(new MessageTileSync(this, message), CommonUtils.targetPointFromTile(this, range));
	}

	//--- NBT ---//

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		//Base
		nbt.setBoolean("formed", formed);
		nbt.setInteger("pos", pos);
		nbt.setIntArray("offset", offset);
		nbt.setBoolean("mirrored", mirrored);
		nbt.setInteger("facing", facing.ordinal());

		if(multiblock!=MultiblockBasic.DEFAULT_MULTIBLOCK)
			nbt.setString("multiblock", multiblock.getUniqueName());
		else if(hasWorld())
		{
			Block bb = world.getBlockState(getPos()).getBlock();
			if(bb instanceof BlockCTMBMultiblock)
				nbt.setString("multiblock", ((BlockCTMBMultiblock)bb).multiblock.getUniqueName());
		}

		if(inventory.size()>0)
			nbt.setTag("inventory",Utils.writeInventory(inventory));
		if(tanks.length>0)
		{
			NBTTagList list = new NBTTagList();
			//Arrays.stream(tanks).map(t->t.writeToNBT(new NBTTagCompound())).collect(Collectors.toList())
			nbt.setTag("inventory",Utils.writeInventory(inventory));
		}

		if(mbWrapper!=null)
		{
			NBTTagCompound custom = mbWrapper.saveData();
			if(!custom.hasNoTags())
				nbt.setTag("custom", custom);
		}

	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		//Base
		formed = nbt.getBoolean("formed");
		pos = nbt.getInteger("pos");
		offset = nbt.getIntArray("offset");
		mirrored = nbt.getBoolean("mirrored");
		facing = EnumFacing.getFront(nbt.getInteger("facing"));

		if(multiblock==MultiblockBasic.DEFAULT_MULTIBLOCK&&nbt.hasKey("multiblock"))
		{
			final String mb = nbt.getString("multiblock");
			CommonProxy.multiblocks.stream().filter(multiblockBasic -> multiblockBasic.getUniqueName().equals(mb)).findFirst().ifPresent(m -> ensureMBLoaded(m.getBlock()));
		}

		inventory = NonNullList.withSize(
				multiblock.inventory.stream().mapToInt(i -> i.capacity).sum(),
				ItemStack.EMPTY);
		if(nbt.hasKey("inventory"))
			inventory = Utils.readInventory(nbt.getTagList("inventory", NBT.TAG_COMPOUND),inventory.size());
		if(nbt.hasKey("tanks"))
		{
			NBTTagList t = nbt.getTagList("tanks", NBT.TAG_COMPOUND);
			tanks = multiblock.tanks.stream().map(i -> new FluidTank(i.capacity)).toArray(FluidTank[]::new);
			final int lim = Math.min(t.tagCount(),tanks.length);
			for(int i =0;i<lim;i++)
				tanks[i].readFromNBT(t.getCompoundTagAt(i));
		}
		if(nbt.hasKey("energy"))
		{
			NBTTagList t = nbt.getTagList("energy", NBT.TAG_COMPOUND);
			energy = multiblock.energy.stream().map(i -> new FluxStorageAdvanced(i.capacity)).toArray(FluxStorageAdvanced[]::new);
			final int lim = Math.min(t.tagCount(),energy.length);
			for(int i =0;i<lim;i++)
				energy[i].readFromNBT(t.getCompoundTagAt(i));
		}

		//load storage
		if(!isDummy()&&nbt.hasKey("custom"))
		{
			if(mbWrapper==null)
				mbWrapper = new MultiblockTileCTWrapper(this);
			mbWrapper.loadData(nbt.getCompoundTag("custom"));
		}
	}

	//--- Update ---//

	@Override
	public void update()
	{
		ApiUtils.checkForNeedlessTicking(this);
		tickedProcesses = 0;
		if(world.isRemote||isDummy()) //||isRSDisabled()
			return;

		if(multiblock!=MultiblockBasic.DEFAULT_MULTIBLOCK&&multiblock.onUpdate!=null)
			multiblock.onUpdate.execute(this.mbWrapper);

		// TODO: 30.05.2022 processes
	}

	//--- Utility Methods ---//

	public MultiblockTileCTWrapper getMbWrapper()
	{
		return mbWrapper;
	}

	//--- IE Methods ---//

	@Override
	protected IMultiblockRecipe readRecipeFromNBT(NBTTagCompound tag)
	{
		return null;
	}

	// TODO: 01.06.2022 energy pos
	@Override
	public int[] getEnergyPos()
	{
		return new int[0];
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
		return inventory;
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
		return multiblock.mainGui!=null;
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

	//--- AABB ---//

	@Override
	public List<AxisAlignedBB> getAdvancedColisionBounds()
	{
		ArrayList<AxisAlignedBB> boxes = new ArrayList<>();
		for(AxisAlignedBB aabb : multiblock.getAABB(pos))
			boxes.add(aabb.offset(getPos()));
		return boxes;
	}

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds()
	{
		return getAdvancedColisionBounds();
	}

	@Override
	public boolean isOverrideBox(AxisAlignedBB box, EntityPlayer player, RayTraceResult mop, ArrayList<AxisAlignedBB> list)
	{
		return false;
	}
}
