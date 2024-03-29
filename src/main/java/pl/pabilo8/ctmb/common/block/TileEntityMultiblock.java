package pl.pabilo8.ctmb.common.block;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorageAdvanced;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.network.MessageTileSync;
import crafttweaker.api.data.IData;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.mc1120.player.MCPlayer;
import crafttweaker.mc1120.world.MCVector3d;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import pl.pabilo8.ctmb.common.CommonProxy;
import pl.pabilo8.ctmb.common.CommonUtils;
import pl.pabilo8.ctmb.common.block.crafttweaker.Multiblock;
import pl.pabilo8.ctmb.common.block.crafttweaker.MultiblockTileCTWrapper;
import pl.pabilo8.ctmb.common.util.NBTTagCollector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @author Pabilo8
 * @since 30.01.2022
 */
@SuppressWarnings("unused")
public class TileEntityMultiblock extends TileEntityMultiblockMetal<TileEntityMultiblock, IMultiblockRecipe> implements IRedstoneOutput, IPlayerInteraction, IGuiTile, IAdvancedCollisionBounds, IAdvancedSelectionBounds, IBlockOverlayText
{
	/**
	 * Multiblock Instance for easy access
	 **/
	private Multiblock multiblock = Multiblock.DEFAULT_MULTIBLOCK;
	private MultiblockTileCTWrapper mbWrapper;

	public FluxStorage[] energy;
	public FluidTank[] tanks;
	public NonNullList<ItemStack> inventory = NonNullList.create();
	public CombinedInvWrapper itemHandler;

	private boolean shouldSendUpdate = false;

	public TileEntityMultiblock()
	{
		//DO NOT USE, method used when loading a saved TE only!
		super(Multiblock.DEFAULT_MULTIBLOCK, new int[0], 0, false);
	}

	public TileEntityMultiblock(Multiblock mb)
	{
		super(mb, mb.getSize(), 0, mb.redstonePositions.length > 0);
		this.multiblock = mb;
	}

	@SuppressWarnings("deprecation")
	void ensureMBLoaded(Block block)
	{
		if((multiblock==null||multiblock==Multiblock.DEFAULT_MULTIBLOCK))
		{
			if(block instanceof BlockCTMBMultiblock)
			{
				BlockCTMBMultiblock mbBlock = (BlockCTMBMultiblock)block;
				this.multiblock = mbBlock.multiblock;
				ReflectionHelper.setPrivateValue(TileEntityMultiblockMetal.class, this, mbBlock.multiblock, "mutliblockInstance");
				ReflectionHelper.setPrivateValue(TileEntityMultiblockPart.class, this, mbBlock.multiblock.getSize(), "structureDimensions");
				ReflectionHelper.setPrivateValue(TileEntityMultiblockMetal.class, this, new FluxStorageAdvanced(0), "energyStorage");
				ReflectionHelper.setPrivateValue(TileEntityMultiblockMetal.class, this, mbBlock.multiblock.redstonePositions.length > 0, "hasRedstoneControl");
				initStorage();
			}
			//else ur fucked)))
		}
	}

	//--- NBT Messages ---//

	@Override
	public void receiveMessageFromClient(@Nonnull NBTTagCompound message)
	{
		super.receiveMessageFromClient(message);

		if(multiblock.onReceiveMessage!=null)
			multiblock.onReceiveMessage.execute(getMbWrapper(), CraftTweakerMC.getIData(message), false);
	}

	@Override
	public void receiveMessageFromServer(@Nonnull NBTTagCompound message)
	{
		super.receiveMessageFromServer(message);

		if(multiblock.onReceiveMessage!=null)
			multiblock.onReceiveMessage.execute(getMbWrapper(), CraftTweakerMC.getIData(message), true);
	}

	public void sendMessageServer(NBTTagCompound message)
	{
		if(multiblock.onSendMessage!=null)
		{
			IData data = multiblock.onSendMessage.execute(getMbWrapper(), CraftTweakerMC.getIData(message), world.isRemote);
			message.merge(CraftTweakerMC.getNBTCompound(data));
		}

		ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(this, message));
	}

	public void sendMessageClients(NBTTagCompound message, int range)
	{
		if(multiblock.onSendMessage!=null)
			multiblock.onSendMessage.execute(getMbWrapper(), CraftTweakerMC.getIData(message), world.isRemote);

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

		if(multiblock!=Multiblock.DEFAULT_MULTIBLOCK)
			nbt.setString("multiblock", multiblock.getUniqueName());
		else if(hasWorld())
		{
			Block bb = world.getBlockState(getPos()).getBlock();
			if(bb instanceof BlockCTMBMultiblock)
				nbt.setString("multiblock", ((BlockCTMBMultiblock)bb).multiblock.getUniqueName());
		}

		if(inventory.size() > 0)
			nbt.setTag("inventory", Utils.writeInventory(inventory));
		if(tanks!=null&&tanks.length > 0)
			nbt.setTag("tanks", Arrays.stream(tanks).map(t -> t.writeToNBT(new NBTTagCompound())).collect(new NBTTagCollector()));
		if(energy!=null&&energy.length > 0)
			nbt.setTag("energy", Arrays.stream(energy).map(t -> t.writeToNBT(new NBTTagCompound())).collect(new NBTTagCollector()));

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

		if(multiblock==Multiblock.DEFAULT_MULTIBLOCK&&nbt.hasKey("multiblock"))
		{
			final String mb = nbt.getString("multiblock");
			CommonProxy.MULTIBLOCKS.stream().filter(multiblockBasic -> multiblockBasic.getUniqueName().equals(mb)).findFirst().ifPresent(m -> ensureMBLoaded(m.getBlock()));
		}

		initStorage();

		if(nbt.hasKey("inventory"))
			inventory = Utils.readInventory(nbt.getTagList("inventory", NBT.TAG_COMPOUND), inventory.size());
		if(nbt.hasKey("tanks"))
			readArrayedProperty(nbt, "tanks", tanks, FluidTank::readFromNBT);
		if(nbt.hasKey("energy"))
			readArrayedProperty(nbt, "energy", energy, FluxStorage::readFromNBT);

		//load storage
		if(!isDummy()&&nbt.hasKey("custom"))
			getMbWrapper().loadData(nbt.getCompoundTag("custom"));
	}

	//--- Update ---//

	@Override
	public void update()
	{
		ApiUtils.checkForNeedlessTicking(this);
		tickedProcesses = 0;
		if(!hasWorld()||world.isRemote||isDummy()) //||isRSDisabled()
			return;

		if(multiblock!=Multiblock.DEFAULT_MULTIBLOCK&&multiblock.onUpdate!=null)
			multiblock.onUpdate.execute(getMbWrapper());

		if(shouldSendUpdate)
		{
			markDirty();
			markContainingBlockForUpdate(null);
			shouldSendUpdate = false;
		}

		// TODO: 30.05.2022 processes
	}

	//--- Utility Methods ---//

	public MultiblockTileCTWrapper getMbWrapper()
	{
		if(this.mbWrapper==null)
		{
			this.mbWrapper = new MultiblockTileCTWrapper(this);
			this.initStorage();
		}
		return mbWrapper;
	}

	public int getInvOffset(int inv, int id)
	{
		return multiblock.inventory.get(inv).getOffset()+id;
	}

	@ParametersAreNonnullByDefault
	private <T> void readArrayedProperty(NBTTagCompound nbt, String name, T[] array, BiFunction<T, NBTTagCompound, T> readFromNBT)
	{
		NBTTagList t = nbt.getTagList(name, NBT.TAG_COMPOUND);
		final int lim = Math.min(t.tagCount(), array.length);
		for(int i = 0; i < lim; i++)
			array[i] = readFromNBT.apply(array[i], t.getCompoundTagAt(i));
	}

	private void initStorage()
	{
		inventory = NonNullList.withSize(
				multiblock.inventory.stream().mapToInt(i -> i.capacity).sum(),
				ItemStack.EMPTY);

		tanks = multiblock.tanks.stream()
				.map(i -> new FluidTank(i.capacity))
				.toArray(FluidTank[]::new);

		energy = multiblock.energy.stream()
				.map(i -> new FluxStorageAdvanced(i.capacity))
				.toArray(FluxStorageAdvanced[]::new);
	}

	//--- IE Methods ---//

	@Override
	@Nullable
	protected IMultiblockRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag)
	{
		return null;
	}

	// TODO: 01.06.2022 energy pos
	@Nonnull
	@Override
	public int[] getEnergyPos()
	{
		return new int[0];
	}

	@Nonnull
	@Override
	public int[] getRedstonePos()
	{
		return multiblock.redstonePositions;
	}

	@Nonnull
	@Override
	public IFluidTank[] getInternalTanks()
	{
		return new IFluidTank[0];
	}


	@Override
	@Nullable
	public IMultiblockRecipe findRecipeForInsertion(@Nonnull ItemStack inserting)
	{
		return null;
	}

	@Nonnull
	@Override
	public int[] getOutputSlots()
	{
		return new int[0];
	}

	@Nonnull
	@Override
	public int[] getOutputTanks()
	{
		return new int[0];
	}

	@Override
	public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess process)
	{
		return false;
	}

	@Override
	public void doProcessOutput(@Nonnull ItemStack output)
	{

	}

	@Override
	public void doProcessFluidOutput(@Nonnull FluidStack output)
	{

	}

	@Override
	public void onProcessFinish(@Nonnull MultiblockProcess process)
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
	public float getMinProcessDistance(@Nonnull MultiblockProcess process)
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
	protected IFluidTank[] getAccessibleFluidTanks(@Nonnull EnumFacing side)
	{
		return new IFluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource)
	{
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side)
	{
		return false;
	}

	@Nonnull
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
	public int getStrongRSOutput(@Nonnull IBlockState state, @Nonnull EnumFacing side)
	{
		return 0;
	}

	@Override
	public boolean canConnectRedstone(@Nonnull IBlockState state, @Nonnull EnumFacing side)
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
	public TileEntityMultiblock getGuiMaster()
	{
		return master();
	}

	@Override
	public boolean interact(@Nonnull EnumFacing side, @Nonnull EntityPlayer player, @Nonnull EnumHand hand, @Nonnull ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		TileEntityMultiblock master = master();

		if(master!=null&&multiblock.onInteract!=null)
			return multiblock.onInteract.execute(master.getMbWrapper(), pos, new MCPlayer(player),
					hand==EnumHand.MAIN_HAND, new MCVector3d(new Vec3d(hitX, hitY, hitZ)));
		return false;
	}

	public Multiblock getMultiblock()
	{
		return multiblock;
	}

	public void forceUpdate()
	{
		this.shouldSendUpdate = true;
	}

	//--- AABB ---//

	@Nonnull
	@Override
	public List<AxisAlignedBB> getAdvancedColisionBounds()
	{
		ArrayList<AxisAlignedBB> boxes = new ArrayList<>();
		for(AxisAlignedBB aabb : multiblock.getAABB(pos))
			boxes.add(aabb.offset(getPos()));
		return boxes;
	}

	@Override
	@Nonnull
	public List<AxisAlignedBB> getAdvancedSelectionBounds()
	{
		return getAdvancedColisionBounds();
	}

	@Override
	public boolean isOverrideBox(@Nonnull AxisAlignedBB box, @Nonnull EntityPlayer player, @Nonnull RayTraceResult mop, @Nonnull ArrayList<AxisAlignedBB> list)
	{
		return false;
	}

	@Override
	@Nonnull
	public String[] getOverlayText(@Nonnull EntityPlayer player, @Nullable RayTraceResult mop, boolean hammer)
	{
		TileEntityMultiblock master = master();

		if(master!=null&&multiblock.onTooltip!=null)
			return getMultiblock().onTooltip.execute(master.getMbWrapper(), pos, CraftTweakerMC.getIPlayer(player), hammer);

		return new String[0];
	}

	@Override
	public boolean useNixieFont(@Nonnull EntityPlayer player, @Nullable RayTraceResult mop)
	{
		return getMultiblock().tooltipNixieFont;
	}
}
