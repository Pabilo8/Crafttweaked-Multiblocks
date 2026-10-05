package pl.pabilo8.ctmb.common.block;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultiblockDefinitionTest
{
	private JsonObject json()
	{
		return new JsonParser().parse("{name:'test:machine',master:[1,1,0],bounds:{full:[0,0,0,16,16,16],none:[-64,-64,-64,-64,-64,-64]},positions:{'0,1':'full','2':'none'},poi:{input:[0,1]},rotations:{input:'up'},tactile:{future:true}}").getAsJsonObject();
	}

	private MultiblockDefinition parse(JsonObject json)
	{
		return MultiblockDefinition.fromJson(new ResourceLocation("test:machine"), json);
	}

	@Test
	void boundsUsePixelsAndEmptyBoundsRemainEmpty()
	{
		MultiblockDefinition definition = parse(json());
		assertEquals(1, definition.bounds(0, net.minecraft.util.EnumFacing.NORTH, false).get(0).maxX);
		assertTrue(definition.bounds(2, net.minecraft.util.EnumFacing.NORTH, true).isEmpty());
		definition.validate(new int[]{2, 1, 3});
		assertThrows(IllegalArgumentException.class, () -> definition.validate(new int[]{1, 1, 3}));
	}

	@Test
	void unknownBoundsDirectionsAndOverlappingPositionsFail()
	{
		JsonObject value = json();
		value.getAsJsonObject("positions").addProperty("1", "full");
		assertThrows(IllegalArgumentException.class, () -> parse(value));
		JsonObject unknown = json();
		unknown.getAsJsonObject("positions").addProperty("3", "missing");
		assertThrows(IllegalArgumentException.class, () -> parse(unknown));
		JsonObject malformed = json();
		malformed.getAsJsonObject("positions").addProperty("wat", "full");
		assertThrows(IllegalArgumentException.class, () -> parse(malformed));
		JsonObject direction = json();
		direction.getAsJsonObject("rotations").addProperty("input", "north");
		assertThrows(IllegalArgumentException.class, () -> parse(direction));
	}

	@Test
	void tactileIsPreservedAndCannotMutateTheDefinition()
	{
		MultiblockDefinition definition = parse(json());
		definition.extensionData().getAsJsonObject("tactile").addProperty("future", false);
		assertTrue(definition.extensionData().getAsJsonObject("tactile").get("future").getAsBoolean());
		int[] poi = definition.getPOI("input");
		poi[0] = 99;
		assertArrayEquals(new int[]{0, 1}, definition.getPOI("input"));
	}
}
