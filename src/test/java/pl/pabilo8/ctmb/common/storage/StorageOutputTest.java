package pl.pabilo8.ctmb.common.storage;

import com.google.gson.JsonParser;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pl.pabilo8.ctmb.common.block.MultiblockDefinition;
import pl.pabilo8.ctmb.common.block.TileEntityMultiblock;
import pl.pabilo8.ctmb.common.block.crafttweaker.Multiblock;
import pl.pabilo8.immersiveintelligence.api.rotary.CapabilityRotaryEnergy;
import pl.pabilo8.immersiveintelligence.api.rotary.IRotaryEnergy.RotationSide;
import pl.pabilo8.immersiveintelligence.api.rotary.RotaryStorage;
import pl.pabilo8.immersiveintelligence.api.crafting.DustStack;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.concurrent.atomic.AtomicInteger;

class StorageOutputTest
{
	private static int sequence;

	@BeforeAll
	static void bootstrap() {Bootstrap.register();}

	private static final class TestMultiblock extends Multiblock
	{
		TestMultiblock()
		{
			super("test:output_"+(++sequence), new ResourceLocation("test:unused"), Material.IRON,
					MultiblockDefinition.fromJson(new ResourceLocation("test:output"), new JsonParser().parse(
							"{name:'test:output',master:[0,0,0],bounds:{},positions:{},poi:{in:0,out:[1,2],more:3},rotations:{in:'counterclockwise_90',out:'up',more:'up'}}").getAsJsonObject()));
		}
	}

	private static final class Fixture
	{
		final Multiblock mb = new TestMultiblock();
		final TileEntityMultiblock tile = mock(TileEntityMultiblock.class);
		final World world = mock(World.class);
		final TileEntity target = mock(TileEntity.class);
		StorageSystem storage;

		Fixture()
		{
			when(tile.getMultiblock()).thenReturn(mb);
			when(tile.hasWorld()).thenReturn(true);
			when(tile.getWorld()).thenReturn(world);
			when(tile.getBlockType()).thenReturn(Blocks.IRON_BLOCK);
			when(tile.getBlockPosForPos(anyInt())).thenAnswer(call -> new BlockPos((int)call.getArgument(0), 0, 0));
			tile.facing = EnumFacing.NORTH;
			when(world.isBlockLoaded(any(BlockPos.class))).thenReturn(true);
			when(world.getTileEntity(any(BlockPos.class))).thenReturn(target);
		}

		StorageAccess freeze(String name)
		{
			mb.freeze();
			storage = new StorageSystem(tile);
			when(tile.getStorageSystem()).thenReturn(storage);
			return storage.get(name);
		}
	}

	@Test
	void itemSelectionPartialAcceptanceAndSharedBudgetConserveStacks()
	{
		Fixture f = new Fixture();
		f.mb.setItemStorage("items").withSize(2).withOutputPort("out", new int[]{1}).withOutputRate(5);
		StorageAccess source = f.freeze("items");
		f.storage.inventory.set(0, new ItemStack(Items.IRON_INGOT, 8));
		f.storage.inventory.set(1, new ItemStack(Items.IRON_INGOT, 10));
		ItemStackHandler receiver = new ItemStackHandler(1);
		when(f.target.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.DOWN)).thenReturn(receiver);
		f.storage.tickOutputs();
		assertEquals(8, source.item(0).getCount());
		assertEquals(5, source.item(1).getCount());
		assertEquals(5, receiver.getStackInSlot(0).getCount()); // One budget for both POIs.
		receiver.setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 63));
		f.storage.tickOutputs();
		assertEquals(4, source.item(1).getCount());
		assertEquals(64, receiver.getStackInSlot(0).getCount());
		f.storage.tickOutputs();
		assertEquals(4, source.item(1).getCount());
	}

	@Test
	void fluidBackpressureRetainsTheUnacceptedAmount()
	{
		Fixture f = new Fixture();
		f.mb.setFluidStorage("tank").withSize(1000).withOutputPort("out").withOutputRate(400);
		StorageAccess source = f.freeze("tank");
		source.fluidTank().fill(new FluidStack(FluidRegistry.LAVA, 1000), true);
		FluidTank receiver = new FluidTank(150);
		when(f.target.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.DOWN)).thenReturn(receiver);
		f.storage.tickOutputs();
		assertEquals(150, receiver.getFluidAmount());
		assertEquals(850, source.fluidTank().getFluidAmount());
		f.storage.tickOutputs();
		assertEquals(850, source.fluidTank().getFluidAmount());
	}

	@Test
	void dustUsesTheExistingDustPortCapabilityAndKeepsBackpressure()
	{
		Fixture f = new Fixture();
		f.mb.setDustStorage("dust").withSize(100).withOutputPort("out").withOutputRate(10);
		StorageAccess source = f.freeze("dust");
		source.dustTank().fill(new DustStack("test_dust", 100), true);
		CTMBDustTank tank = new CTMBDustTank(8, () -> {});
		IDustHandler receiver = new IDustHandler()
		{
			@Override public int fill(DustStack stack, boolean execute) {return tank.fill(stack, execute);}
			@Override public DustStack drain(DustStack stack, boolean execute) {return tank.drain(stack, execute);}
			@Override public DustStack drainDust(int amount, boolean execute) {return tank.drain(amount, execute);}
		};
		when(f.target.getCapability(DustCapability.CAPABILITY, EnumFacing.DOWN)).thenReturn(receiver);
		f.storage.tickOutputs();
		assertEquals(8, tank.getDustStack().amount);
		assertEquals(92, source.dustTank().getDustStack().amount);
		f.storage.tickOutputs();
		assertEquals(92, source.dustTank().getDustStack().amount);
	}

	@Test
	void forgeEnergyOutputDebitsOnlyTheAcceptedFlux()
	{
		Fixture f = new Fixture();
		f.mb.setEnergyStorage("power").withSize(1000).withOutputPort("out").withOutputRate(120);
		StorageAccess source = f.freeze("power");
		source.fillEnergy(1000);
		EnergyStorage receiver = new EnergyStorage(55);
		when(f.target.hasCapability(CapabilityEnergy.ENERGY, EnumFacing.DOWN)).thenReturn(true);
		when(f.target.getCapability(CapabilityEnergy.ENERGY, EnumFacing.DOWN)).thenReturn(receiver);
		f.storage.tickOutputs();
		assertEquals(55, receiver.getEnergyStored());
		assertEquals(945, source.getEnergy());
		f.storage.tickOutputs();
		assertEquals(945, source.getEnergy());
	}

	@Test
	void legacyIEReceiverAndOwnPartsAreHandledWithoutCapabilityFallback()
	{
		Fixture f = new Fixture();
		f.mb.setEnergyStorage("power").withSize(1000).withOutputPort("out").withOutputRate(120);
		StorageAccess source = f.freeze("power");
		source.fillEnergy(1000);
		TileEntityMultiblock receiver = mock(TileEntityMultiblock.class);
		AtomicInteger stored = new AtomicInteger();
		when(receiver.canConnectEnergy(EnumFacing.DOWN)).thenReturn(true);
		when(receiver.receiveEnergy(eq(EnumFacing.DOWN), anyInt(), eq(false))).thenAnswer(call -> {
			int amount = Math.min((int)call.getArgument(1), 55-stored.get());
			stored.addAndGet(amount);
			return amount;
		});
		when(f.world.getTileEntity(any(BlockPos.class))).thenReturn(receiver);
		f.storage.tickOutputs();
		assertEquals(55, stored.get());
		assertEquals(945, source.getEnergy());
		when(receiver.master()).thenReturn(f.tile);
		clearInvocations(receiver);
		f.storage.tickOutputs();
		verify(receiver, never()).receiveEnergy(any(EnumFacing.class), anyInt(), anyBoolean());
		assertEquals(945, source.getEnergy());
	}

	@Test
	void twoRotaryProvidersCannotOwnTheSamePhysicalFace()
	{
		Fixture f = new Fixture();
		f.mb.setRotaryStorage("first").withOutputPort("out");
		f.mb.setRotaryStorage("second").withInputPort("out");
		assertThrows(IllegalArgumentException.class, f.mb::freeze);
	}

	@Test
	void disabledOutputUnloadedNeighbourAndClientNeverTransfer() throws Exception
	{
		Fixture f = new Fixture();
		f.mb.setItemStorage("items").withOutputPort("out").withAutoOutput(false);
		f.freeze("items");
		f.storage.inventory.set(0, new ItemStack(Items.IRON_INGOT, 8));
		f.storage.tickOutputs();
		verify(f.world, never()).getTileEntity(any(BlockPos.class));

		Fixture unloaded = new Fixture();
		unloaded.mb.setItemStorage("items").withOutputPort("out");
		unloaded.freeze("items");
		when(unloaded.world.isBlockLoaded(any(BlockPos.class))).thenReturn(false);
		unloaded.storage.tickOutputs();
		verify(unloaded.world, never()).getTileEntity(any(BlockPos.class));

		Fixture client = new Fixture();
		client.mb.setItemStorage("items").withOutputPort("out");
		client.freeze("items");
		java.lang.reflect.Field remote = World.class.getField("isRemote");
		remote.setAccessible(true);
		remote.setBoolean(client.world, true);
		client.storage.tickOutputs();
		verify(client.world, never()).getTileEntity(any(BlockPos.class));
	}

	@Test
	void rotaryCapabilityRolesLimitsPersistenceAndDisconnectedInput()
	{
		Fixture f = new Fixture();
		f.mb.setRotaryStorage("shaft").withRotaryLimits(100, 20).withInputPort("in").withOutputPort("out");
		StorageAccess shaft = f.freeze("shaft");
		shaft.setRotaryPower(200, 40);
		assertEquals(100f, shaft.getRotationSpeed());
		assertEquals(20f, shaft.getTorque());
		StoragePortView input = f.storage.view(0, EnumFacing.EAST), output = f.storage.view(1, EnumFacing.UP);
		assertEquals(RotationSide.INPUT, input.getSide(EnumFacing.EAST));
		assertEquals(RotationSide.OUTPUT, output.getSide(EnumFacing.UP));
		assertEquals(RotationSide.NONE, output.getSide(EnumFacing.DOWN));
		output.setRotationSpeed(0);
		assertEquals(100f, shaft.getRotationSpeed());
		NBTTagCompound saved = f.storage.save();
		shaft.setRotaryPower(0, 0);
		f.storage.restore(saved);
		assertEquals(100f, shaft.getRotationSpeed());
		assertEquals(20f, shaft.getTorque());
		RotaryStorage source = new RotaryStorage(10, 50)
		{
			@Override public RotationSide getSide(EnumFacing face) {return RotationSide.OUTPUT;}
		};
		when(f.target.getCapability(CapabilityRotaryEnergy.ROTARY_ENERGY, EnumFacing.WEST)).thenReturn(source);
		f.storage.tick();
		assertTrue(shaft.getRotationSpeed()>0);
		when(f.world.getTileEntity(any(BlockPos.class))).thenReturn(null);
		f.storage.tick();
		assertEquals(0f, shaft.getRotationSpeed());
		assertEquals(0f, shaft.getTorque());
	}

	@Test
	void rotaryOutputCommunicatesPowerAndExplicitStopWithoutDrainingSource()
	{
		Fixture f = new Fixture();
		f.mb.setRotaryStorage("shaft").withOutputPort("out");
		StorageAccess shaft = f.freeze("shaft");
		shaft.setRotaryPower(50, 10);
		RotaryStorage receiver = new RotaryStorage()
		{
			@Override public RotationSide getSide(EnumFacing face) {return RotationSide.INPUT;}
		};
		when(f.target.getCapability(CapabilityRotaryEnergy.ROTARY_ENERGY, EnumFacing.DOWN)).thenReturn(receiver);
		f.storage.tickOutputs();
		assertTrue(receiver.getRotationSpeed()>0);
		assertTrue(receiver.getTorque()>0);
		assertEquals(50f, shaft.getRotationSpeed());
		assertEquals(10f, shaft.getTorque());
		shaft.setRotaryPower(0, 0);
		f.storage.tickOutputs();
		assertEquals(0f, receiver.getRotationSpeed());
		assertEquals(0f, receiver.getTorque());
	}

	@Test
	void redstoneRestoresItsOutputAndPublishesOnlyOnChange()
	{
		Fixture f = new Fixture();
		f.mb.setRedstoneStorage("signal").withOutputPort("out");
		StorageAccess signal = f.freeze("signal");
		signal.setRedstone(15);
		assertEquals(15, f.storage.redstone(1, EnumFacing.UP));
		NBTTagCompound saved = f.storage.save();
		clearInvocations(f.world);
		f.storage.restore(saved);
		f.storage.tickOutputs();
		f.storage.tickOutputs();
		verify(f.world, times(2)).notifyNeighborsOfStateChange(any(BlockPos.class), eq(Blocks.IRON_BLOCK), eq(false));
	}
}
