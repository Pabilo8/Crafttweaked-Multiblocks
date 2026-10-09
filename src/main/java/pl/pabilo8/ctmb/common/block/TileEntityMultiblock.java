package pl.pabilo8.ctmb.common.block;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.items.CapabilityItemHandler;
import pl.pabilo8.ctmb.common.CommonProxy;
import pl.pabilo8.ctmb.common.block.crafttweaker.Multiblock;
import pl.pabilo8.ctmb.common.block.crafttweaker.MultiblockTileCTWrapper;
import pl.pabilo8.ctmb.common.storage.*;
import pl.pabilo8.immersiveintelligence.api.rotary.CapabilityRotaryEnergy;
import pl.pabilo8.immersiveintelligence.common.network.IIPacketHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Pabilo8
 * @since 30.01.2022
 */
@SuppressWarnings("unused")
public class TileEntityMultiblock extends TileEntityMultiblockMetal<TileEntityMultiblock, IMultiblockRecipe> implements pl.pabilo8.immersiveintelligence.api.data.device.IDataDevice, IRedstoneOutput, IPlayerInteraction, IGuiTile, IAdvancedCollisionBounds, IAdvancedSelectionBounds, IBlockOverlayText, pl.pabilo8.immersiveintelligence.common.entity.tactile.TactileManager.ITactileListener
{
	/**
	 * Multiblock Instance for easy access
	 **/
	private Multiblock multiblock = Multiblock.DEFAULT_MULTIBLOCK;
	private MultiblockTileCTWrapper mbWrapper;

	private StorageSystem storage;

	private pl.pabilo8.ctmb.common.production.ProductionSystem production;

	private boolean shouldSendUpdate = false;
	private final pl.pabilo8.ctmb.common.amt.CTMBAMTState amtState = new pl.pabilo8.ctmb.common.amt.CTMBAMTState();
	private pl.pabilo8.ctmb.common.amt.CTMBTactileManager tactile;

	public TileEntityMultiblock()
	{
		//DO NOT USE, method used when loading a saved TE only!
		super(Multiblock.DEFAULT_MULTIBLOCK, new int[0], 0, false);
	}

	public TileEntityMultiblock(Multiblock mb)
	{
		super(mb, mb.getSize(), 0, false);
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
				ReflectionHelper.setPrivateValue(TileEntityMultiblockMetal.class, this, new blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorageAdvanced(0), "energyStorage");
				ReflectionHelper.setPrivateValue(TileEntityMultiblockMetal.class, this, false, "hasRedstoneControl");
			}
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
		if(message.hasKey("ctmbGuiStorage", NBT.TAG_COMPOUND))
		{
			if(!isDummy())
			{
				NBTTagCompound snapshot = message.getCompoundTag("ctmbGuiStorage");
				getStorageSystem().restore(snapshot);
				getProductionSystem().restore(snapshot.getCompoundTag("@production"));
			}
			if(message.hasKey("ctmbGuiLayout"))
				pl.pabilo8.ctmb.CTMB.proxy.confirmGuiLayout(this, message.getString("ctmbGuiLayout"), message.getInteger("ctmbWindow"));
			return;
		}
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

		ImmersiveEngineering.packetHandler.sendToAllAround(new MessageTileSync(this, message), IIPacketHandler.targetPointFromTile(this, range));
	}

	/**
	 * Returns the storage state for open GUI listeners.
	 */
	public NBTTagCompound getGuiStorageData()
	{
		NBTTagCompound tag = getStorageSystem().save();
		tag.setTag("@production", getProductionSystem().save());
		return tag;
	}

	public pl.pabilo8.ctmb.common.production.ProductionSystem getProductionSystem()
	{
		if(isDummy())
		{
			TileEntityMultiblock master = master();
			if(master==null) throw new IllegalStateException("Missing production master");
			return master.getProductionSystem();
		}
		if(production==null)
			production = new pl.pabilo8.ctmb.common.production.ProductionSystem(getStorageSystem());
		return production;
	}

	public StorageSystem getStorageSystem()
	{
		if(isDummy())
		{
			TileEntityMultiblock master = master();
			if(master==null) throw new IllegalStateException("Missing CTMB master at "+getPos());
			return master.getStorageSystem();
		}
		if(storage==null) storage = new StorageSystem(this);
		return storage;
	}

	@Override
	@Nullable
	public TileEntityMultiblock master()
	{
		TileEntityMultiblock master = super.master();
		return master!=null&&master.getMultiblock()==multiblock?master: null;
	}

	@Nullable
	private StoragePortView portView(EnumFacing side)
	{
		if(!formed||side==null) return null;
		TileEntityMultiblock master = master();
		return master==null?null: master.getStorageSystem().view(pos, side);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing side)
	{
		StoragePortView view = portView(side);
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return view!=null&&view.has(StorageDefinition.Kind.ITEM);
		if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
			return view!=null&&view.has(StorageDefinition.Kind.FLUID);
		if(capability==CapabilityEnergy.ENERGY) return view!=null&&view.has(StorageDefinition.Kind.ENERGY);
		if(capability==CapabilityRotaryEnergy.ROTARY_ENERGY) return view!=null&&view.has(StorageDefinition.Kind.ROTARY);
		if(capability==DustCapability.CAPABILITY) return view!=null&&view.has(StorageDefinition.Kind.DUST);
		return super.hasCapability(capability, side);
	}

	@Override
	@Nullable
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing side)
	{
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY||capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
				||capability==CapabilityEnergy.ENERGY||capability==CapabilityRotaryEnergy.ROTARY_ENERGY||capability==DustCapability.CAPABILITY)
			return hasCapability(capability, side)?capability.cast((T)portView(side)): null;
		return super.getCapability(capability, side);
	}

	@Override
	public int receiveEnergy(EnumFacing side, int amount, boolean simulate)
	{
		StoragePortView view = portView(side);
		return view==null?0: view.receiveEnergy(amount, simulate);
	}

	@Override
	public int extractEnergy(EnumFacing side, int amount, boolean simulate)
	{
		StoragePortView view = portView(side);
		return view==null?0: view.extractEnergy(amount, simulate);
	}

	@Override
	public int getEnergyStored(EnumFacing side)
	{
		StoragePortView view = portView(side);
		return view==null?0: view.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(EnumFacing side)
	{
		StoragePortView view = portView(side);
		return view==null?0: view.getMaxEnergyStored();
	}

	@Override
	public FluxStorage getFluxStorage()
	{
		TileEntityMultiblock master = master();
		if(master!=null) for(StorageAccess provider : master.getStorageSystem().providers())
			if(provider.definition.kind==StorageDefinition.Kind.ENERGY) return provider.energy();
		return energyStorage;
	}

	@Override
	public SideConfig getEnergySideConfig(EnumFacing side)
	{
		StoragePortView view = portView(side);
		return view==null?SideConfig.NONE: view.canReceive()?SideConfig.INPUT: view.canExtract()?SideConfig.OUTPUT: SideConfig.NONE;
	}

	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(EnumFacing side)
	{
		StoragePortView view = portView(side);
		return view!=null&&view.has(StorageDefinition.Kind.ENERGY)?new IEForgeEnergyWrapper(this, side)
		{
			@Override
			public boolean canReceive()
			{
				return view.canReceive();
			}

			@Override
			public boolean canExtract()
			{
				return view.canExtract();
			}
		}: null;
	}

	@Override
	public void onReceive(pl.pabilo8.immersiveintelligence.api.data.DataPacket packet, @Nullable EnumFacing side)
	{
		TileEntityMultiblock master = master();
		if(formed&&master!=null) master.getStorageSystem().receive(packet, pos, side);
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

		if(!isDummy()&&storage!=null) nbt.setTag("storage", storage.save());

		if(!isDummy()&&production!=null) nbt.setTag("production", production.save());
		if(!isDummy()) nbt.setTag("amt", amtState.save());

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


		if(!isDummy())
		{
			if(nbt.hasKey("storage", NBT.TAG_COMPOUND)) getStorageSystem().restore(nbt.getCompoundTag("storage"));
			getProductionSystem().restore(nbt.getCompoundTag("production"));
			getMbWrapper().loadData(nbt.getCompoundTag("custom"));
			amtState.restore(nbt.getCompoundTag("amt"));
		}

	}

	//--- Update ---//

	@Override
	public void update()
	{
		if(!formed&&tactile!=null)
		{
			tactile.forceReload();
			tactile = null;
		}
		ApiUtils.checkForNeedlessTicking(this);
		tickedProcesses = 0;
		if(!hasWorld()||world.isRemote||isDummy()||!formed) //||isRSDisabled()
			return;

		getStorageSystem().tick();
		getProductionSystem().tick();

		if(multiblock!=Multiblock.DEFAULT_MULTIBLOCK&&multiblock.onUpdate!=null)
			multiblock.onUpdate.execute(getMbWrapper());

		pl.pabilo8.ctmb.common.amt.CTMBTactileManager tactile = getTactileHandler();
		if(tactile!=null)
		{
			java.util.Map<net.minecraft.util.ResourceLocation, Float> samples = amtState.samples();
			pl.pabilo8.immersiveintelligence.common.util.ResLoc[] locations = samples.keySet().stream()
					.map(pl.pabilo8.immersiveintelligence.common.util.ResLoc::of).toArray(pl.pabilo8.immersiveintelligence.common.util.ResLoc[]::new);
			float[] times = new float[samples.size()];
			int index = 0;
			for(float value : samples.values()) times[index++] = value;
			tactile.update(locations, times);
		}

		getStorageSystem().tickOutputs();

		if(shouldSendUpdate)
		{
			markDirty();
			markContainingBlockForUpdate(null);
			shouldSendUpdate = false;
		}

		// TODO: 30.05.2022 processes
	}

	public pl.pabilo8.ctmb.common.amt.CTMBAMTState getAMTState()
	{
		TileEntityMultiblock master = isDummy()?master(): this;
		if(master==null) throw new IllegalStateException("Missing AMT master");
		return master.amtState;
	}

	@Nullable
	@Override
	public pl.pabilo8.ctmb.common.amt.CTMBTactileManager getTactileHandler()
	{
		if(!hasWorld()||world.isRemote||!formed) return null;
		if(isDummy())
		{
			TileEntityMultiblock master = master();
			return master==null?null: master.getTactileHandler();
		}
		if(multiblock.tactileModel()==null) return null;
		if(tactile==null) tactile = new pl.pabilo8.ctmb.common.amt.CTMBTactileManager(this, multiblock.tactileModel());
		return tactile;
	}

	@Override
	public boolean onTactileInteract(pl.pabilo8.immersiveintelligence.common.entity.tactile.EntityAMTTactile part, EntityPlayer player, EnumHand hand)
	{
		return tactile!=null&&multiblock.onTactileInteract!=null&&multiblock.onTactileInteract.execute(
				getMbWrapper(), tactile.partName(part), CraftTweakerMC.getIPlayer(player), hand==EnumHand.MAIN_HAND);
	}

	@Override
	public void invalidate()
	{
		if(tactile!=null) tactile.forceReload();
		super.invalidate();
	}

	@Override
	public void onChunkUnload()
	{
		if(tactile!=null)
		{
			tactile.forceReload();
			tactile = null;
		}
		super.onChunkUnload();
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(multiblock==Multiblock.DEFAULT_MULTIBLOCK) return super.getRenderBoundingBox();
		// Animations can extend beyond the static formation bounds (e.g. a crane arm).
		return isDummy()?super.getRenderBoundingBox(): INFINITE_EXTENT_AABB;
	}

	//--- Utility Methods ---//

	public MultiblockTileCTWrapper getMbWrapper()
	{
		if(this.mbWrapper==null)
		{
			this.mbWrapper = new MultiblockTileCTWrapper(this);
		}
		return mbWrapper;
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
		return positionsFor(StorageDefinition.Kind.ENERGY);
	}

	private int[] positionsFor(StorageDefinition.Kind kind)
	{
		if(multiblock.definition==null) return new int[0];
		return multiblock.storages.values().stream().filter(d -> d.kind==kind).flatMap(d -> d.ports().stream())
				.flatMapToInt(p -> java.util.Arrays.stream(multiblock.definition.getPOI(p.poi))).distinct().sorted().toArray();
	}

	@Nonnull
	@Override
	public int[] getRedstonePos()
	{
		return positionsFor(StorageDefinition.Kind.REDSTONE);
	}

	@Nonnull
	@Override
	public IFluidTank[] getInternalTanks()
	{
		return getStorageSystem().providers().stream().filter(p -> p.definition.kind==StorageDefinition.Kind.FLUID)
				.map(StorageAccess::fluidTank).toArray(IFluidTank[]::new);
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
		List<AxisAlignedBB> bounds = multiblock.getAABB(pos, facing, mirrored);
		if(bounds.isEmpty()) return new float[]{0, 0, 0, 0, 0, 0};
		AxisAlignedBB combined = bounds.get(0);
		for(int i = 1; i < bounds.size(); i++) combined = combined.union(bounds.get(i));
		return new float[]{(float)combined.minX, (float)combined.minY, (float)combined.minZ, (float)combined.maxX, (float)combined.maxY, (float)combined.maxZ};
	}

	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return isDummy()?NonNullList.create(): getStorageSystem().inventory;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return slot >= 0&&slot < getInventory().size();
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return slot >= 0&&slot < getInventory().size()?64: 0;
	}

	@Override
	public void doGraphicalUpdates(int slot)
	{
		forceUpdate();
	}

	// TODO: 20.02.2022 redstone
	@Override
	public int getStrongRSOutput(@Nonnull IBlockState state, @Nonnull EnumFacing side)
	{
		return getWeakRSOutput(state, side);
	}

	@Override
	public int getWeakRSOutput(@Nonnull IBlockState state, @Nonnull EnumFacing side)
	{
		TileEntityMultiblock master = master();
		return formed&&master!=null?master.getStorageSystem().redstone(pos, side.getOpposite()): 0;
	}

	@Override
	public boolean canConnectRedstone(@Nonnull IBlockState state, @Nullable EnumFacing side)
	{
		TileEntityMultiblock master = master();
		return formed&&master!=null&&side!=null&&master.getStorageSystem().redstonePort(pos, side);
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
		for(AxisAlignedBB aabb : multiblock.getAABB(pos, facing, mirrored))
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
