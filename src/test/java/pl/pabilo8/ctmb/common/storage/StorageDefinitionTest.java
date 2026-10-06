package pl.pabilo8.ctmb.common.storage;

import com.google.gson.JsonParser;
import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.Test;
import pl.pabilo8.ctmb.common.block.MultiblockDefinition;

import static org.junit.jupiter.api.Assertions.*;

class StorageDefinitionTest
{
	private MultiblockDefinition definition()
	{
		return MultiblockDefinition.fromJson(new ResourceLocation("test:machine"), new JsonParser().parse("{name:'test:machine',master:[0,0,0],bounds:{},positions:{},poi:{input:0,output:1},rotations:{input:'none',output:'down'}}").getAsJsonObject());
	}

	@Test
	void selectorIsCopiedAndFrozenProvidersRejectEdits()
	{
		int[] slots = {2, 0, 2};
		StorageDefinition provider = new StorageDefinition("items", StorageDefinition.Kind.ITEM).withSize(3).withInputPort("input", slots).withOutputPort("output", new int[]{2});
		slots[0] = 1;
		provider.freeze(definition());
		assertTrue(provider.ports().get(0).includes(0));
		assertTrue(provider.ports().get(0).includes(2));
		assertFalse(provider.ports().get(0).includes(1));
		assertFalse(provider.ports().get(1).includes(0));
		assertTrue(provider.ports().get(1).includes(2));
		assertThrows(IllegalStateException.class, () -> provider.withSize(4));
	}

	@Test
	void badNamesKindsSelectorsAndPortsFail()
	{
		assertThrows(IllegalArgumentException.class, () -> new StorageDefinition("bad name", StorageDefinition.Kind.ITEM));
		assertThrows(IllegalArgumentException.class, () -> new StorageDefinition("fluid", StorageDefinition.Kind.FLUID).withInputPort("input", new int[]{0}));
		assertThrows(IllegalArgumentException.class, () -> new StorageDefinition("items", StorageDefinition.Kind.ITEM).withInputPort("missing").freeze(definition()));
		assertThrows(IllegalArgumentException.class, () -> new StorageDefinition("items", StorageDefinition.Kind.ITEM).withInputPort("input", new int[]{1}).freeze(definition()));
	}

	@Test
	void rotaryLimitsAndOutputRatesRequireTheirMatchingKindsAndFreeze()
	{
		assertThrows(IllegalArgumentException.class, () -> new StorageDefinition("shaft", StorageDefinition.Kind.ROTARY).withSize(2));
		assertThrows(IllegalArgumentException.class, () -> new StorageDefinition("shaft", StorageDefinition.Kind.ROTARY).withRotaryLimits(Float.NaN, 4));
		assertThrows(IllegalArgumentException.class, () -> new StorageDefinition("items", StorageDefinition.Kind.ITEM).withRotaryLimits(20, 4));
		assertThrows(IllegalArgumentException.class, () -> new StorageDefinition("items", StorageDefinition.Kind.ITEM).withOutputRate(0));
		assertThrows(IllegalArgumentException.class, () -> new StorageDefinition("shaft", StorageDefinition.Kind.ROTARY).withOutputRate(20));
		StorageDefinition tank = new StorageDefinition("fluid", StorageDefinition.Kind.FLUID).withOutputRate(250).withAutoOutput(false);
		tank.freeze(definition());
		assertEquals(250, tank.outputRate());
		assertFalse(tank.autoOutput());
		assertThrows(IllegalStateException.class, () -> tank.withAutoOutput(true));
	}
}
