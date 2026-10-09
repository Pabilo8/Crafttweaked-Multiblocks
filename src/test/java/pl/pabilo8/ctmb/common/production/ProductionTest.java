package pl.pabilo8.ctmb.common.production;

import crafttweaker.api.item.IIngredient;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.mc1120.liquid.MCLiquidStack;
import net.minecraft.block.material.Material;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pl.pabilo8.ctmb.common.block.TileEntityMultiblock;
import pl.pabilo8.ctmb.common.block.crafttweaker.Multiblock;
import pl.pabilo8.ctmb.common.storage.StorageDefinition;
import pl.pabilo8.ctmb.common.storage.StorageSystem;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Regression sources for native matching, shared capacity and save/reload boundaries.
 */
class ProductionTest
{
	private static int sequence;

	@BeforeAll
	static void bootstrap()
	{
		Bootstrap.register();
	}

	private static final class TestMultiblock extends Multiblock
	{
		TestMultiblock()
		{
			super("test:production_"+(++sequence), new ResourceLocation("test:unused"), Material.IRON, null);
		}
	}

	private static final class Fixture
	{
		final Multiblock mb = new TestMultiblock();
		final TileEntityMultiblock tile = mock(TileEntityMultiblock.class);
		final StorageSystem storage;
		final ProductionHandler handler;

		Fixture(int lanes)
		{
			this(lanes, false);
		}

		Fixture(int lanes, boolean rotary)
		{
			mb.setItemStorage("items").withSize(2);
			mb.setFluidStorage("fluid").withSize(1000);
			mb.setEnergyStorage("power").withSize(1000);
			mb.setRedstoneStorage("signal");
			if(rotary) mb.setRotaryStorage("shaft").withRotaryLimits(100, 20);
			handler = mb.setProductionHandler("main").withInput(0, "items").withOutput(0, "fluid").withTime(3).withEnergy(2).withLanes(lanes).withRedstoneReaction("signal");
			if(rotary) handler.withRotaryStorage("shaft").withRotaryPower(10, 60, 2);
			mb.freeze();
			when(tile.getMultiblock()).thenReturn(mb);
			when(tile.hasWorld()).thenReturn(true);
			when(tile.getWorld()).thenReturn(mock(World.class));
			storage = new StorageSystem(tile);
			when(tile.getStorageSystem()).thenReturn(storage);
		}

		ProductionRecipe recipe()
		{
			return handler.add(new IIngredient[]{CraftTweakerMC.getIItemStack(new ItemStack(Items.IRON_INGOT)), new MCLiquidStack(new FluidStack(FluidRegistry.LAVA, 1000))});
		}

		void supply(int items, int energy)
		{
			storage.get("items").setItem(1, CraftTweakerMC.getIItemStack(new ItemStack(Items.IRON_INGOT, items)));
			storage.get("power").fillEnergy(energy);
		}
	}

	@Test
	void rotaryRequirementsPauseBeforeConsumptionAndResumeReservedInputs()
	{
		Fixture f = new Fixture(1, true);
		ProductionRecipe recipe = f.recipe();
		assertEquals(10, recipe.getMinSpeed());
		assertEquals(60, recipe.getMaxSpeed());
		assertEquals(2, recipe.getTorque());
		f.supply(1, 2);
		ProductionSystem system = new ProductionSystem(f.storage);
		system.tick();
		assertEquals("no_rotary_power", system.get("main").getState());
		assertEquals(1, f.storage.get("items").item(1).getCount());
		f.storage.get("shaft").setRotaryPower(20, 4);
		system.tick();
		assertTrue(f.storage.get("items").item(1).isEmpty());
		double progress = system.get("main").getProgress();
		f.storage.get("shaft").setRotaryPower(80, 4); // Outside the permitted range.
		system.tick();
		assertEquals("no_rotary_power", system.get("main").getState());
		assertEquals(progress, system.get("main").getProgress());
		f.storage.get("shaft").setRotaryPower(20, 4);
		system.tick();
		system.tick();
		assertEquals(1000, f.storage.get("fluid").fluidTank().getFluidAmount());
		assertEquals(0, f.storage.get("power").getEnergy());
	}

	@Test
	void chargesNonDivisibleEnergyExactlyWithoutOverflow()
	{
		for(int total : new int[]{0, 1, 2, 1601, Integer.MAX_VALUE})
			for(int time : new int[]{1, 3, 100, 201})
			{
				long sum = 0;
				for(int tick = 0; tick < time; tick++)
				{
					int cost = ProductionAccess.energyForTick(total, time, tick);
					assertTrue(cost >= 0);
					sum += cost;
					assertEquals((long)total*(tick+1)/time, sum);
				}
				assertEquals(total, sum);
			}
	}

	@Test
	void reloadResumesReservedInputWithoutConsumingTwice()
	{
		Fixture f = new Fixture(1);
		f.recipe();
		f.supply(2, 2);
		ProductionSystem original = new ProductionSystem(f.storage);
		original.tick();
		assertEquals(1, f.storage.get("items").item(1).getCount());
		NBTTagCompound saved = original.save();
		ProductionSystem restored = new ProductionSystem(f.storage);
		restored.restore(saved);
		restored.tick();
		restored.tick();
		assertEquals(1, f.storage.get("items").item(1).getCount());
		assertEquals(1000, f.storage.get("fluid").fluidTank().getFluidAmount());
		assertEquals(0, f.storage.get("power").getEnergy());
		assertTrue(restored.claimItems().isEmpty());
	}

	@Test
	void missingPowerAndFullOutputDoNotAdvanceOrDiscardEscrow()
	{
		Fixture f = new Fixture(1);
		f.recipe();
		f.supply(1, 0);
		ProductionSystem p = new ProductionSystem(f.storage);
		p.tick();
		assertEquals(1.0/3, p.get("main").getProgress());
		// first cumulative cost is zero
		p.tick();
		assertEquals("no_power", p.get("main").getState());
		assertEquals(1.0/3, p.get("main").getProgress());
		f.storage.get("power").fillEnergy(2);
		f.storage.get("fluid").fluidTank().fill(new FluidStack(FluidRegistry.LAVA, 1000), true);
		p.tick();
		assertEquals("output_blocked", p.get("main").getState());
		assertEquals(2, f.storage.get("power").getEnergy());
		f.storage.get("fluid").fluidTank().drain(1000, true);
		p.tick();
		p.tick();
		assertEquals(1000, f.storage.get("fluid").fluidTank().getFluidAmount());
		assertTrue(p.claimItems().isEmpty());
	}

	@Test
	void redstonePausesAndDisassemblyRefundsOnceAcrossLanes()
	{
		Fixture f = new Fixture(3);
		f.recipe();
		f.supply(3, 20);
		ProductionSystem p = new ProductionSystem(f.storage);
		f.storage.get("signal").setRedstone(15);
		p.tick();
		assertEquals(3, f.storage.get("items").item(1).getCount());
		f.storage.get("signal").setRedstone(0);
		p.tick();
		assertTrue(f.storage.get("items").item(1).isEmpty());
		double progress = p.get("main").getLaneProgress(2);
		f.storage.get("signal").setRedstone(15);
		p.tick();
		assertEquals(progress, p.get("main").getLaneProgress(2));
		assertEquals(3, p.claimItems().stream().mapToInt(ItemStack::getCount).sum());
		assertTrue(p.claimItems().isEmpty());
	}

	@Test
	void aggregateOutputsCannotOverbookOneTankAndSimulationIsIsolated()
	{
		Fixture f = new Fixture(1);
		RecipeValue output = RecipeValue.convert(new MCLiquidStack(new FluidStack(FluidRegistry.LAVA, 600)), StorageDefinition.Kind.FLUID, true);
		List<ProductionHandler.Binding> bindings = Arrays.asList(new ProductionHandler.Binding(0, "fluid", new int[0]), new ProductionHandler.Binding(1, "fluid", new int[0]));
		ProductionTransaction transaction = new ProductionTransaction(f.storage);
		assertFalse(transaction.apply(bindings, Arrays.asList(output, output), true));
		assertEquals(0, f.storage.get("fluid").fluidTank().getFluidAmount());
		f.supply(1, 0);
		RecipeValue input = RecipeValue.convert(CraftTweakerMC.getIItemStack(new ItemStack(Items.IRON_INGOT)), StorageDefinition.Kind.ITEM, false);
		ProductionTransaction consume = new ProductionTransaction(f.storage);
		assertTrue(consume.apply(f.handler.inputs(), Collections.singletonList(input), false));
		assertEquals(1, f.storage.get("items").item(1).getCount());
		// planning did not remove it
		assertTrue(consume.fork().apply(Collections.singletonList(bindings.get(0)), Collections.singletonList(output), true));
		consume.commit();
		assertTrue(f.storage.get("items").item(1).isEmpty());
		assertEquals(0, f.storage.get("fluid").fluidTank().getFluidAmount());
	}

	@Test
	void removedHandlersRetainEscrowAndChangedBindingsPause()
	{
		Fixture f = new Fixture(1);
		f.recipe();
		f.supply(1, 20);
		ProductionSystem p = new ProductionSystem(f.storage);
		p.tick();
		NBTTagCompound saved = p.save();
		NBTTagCompound lane = saved.getCompoundTag("main").getTagList("lanes", 10).getCompoundTagAt(0);
		lane.setString("bindings", "old_configuration");
		ProductionSystem changed = new ProductionSystem(f.storage);
		changed.restore(saved);
		changed.tick();
		assertEquals("configuration_changed", changed.get("main").getState());
		assertEquals(1, changed.claimItems().stream().mapToInt(ItemStack::getCount).sum());
		NBTTagCompound orphan = new NBTTagCompound();
		orphan.setTag("removed", saved.getCompoundTag("main"));
		ProductionSystem removed = new ProductionSystem(f.storage);
		removed.restore(orphan);
		assertTrue(removed.save().hasKey("removed"));
		assertEquals(1, removed.claimItems().stream().mapToInt(ItemStack::getCount).sum());
		assertTrue(removed.claimItems().isEmpty());
	}
}
