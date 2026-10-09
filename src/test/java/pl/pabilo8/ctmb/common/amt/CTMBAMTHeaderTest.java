package pl.pabilo8.ctmb.common.amt;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CTMBAMTHeaderTest
{
	private JsonObject json(String text)
	{
		return new JsonParser().parse(text).getAsJsonObject();
	}

	@Test
	void typedPartsDoNotNeedAnObjGroupOrExplicitOrigin()
	{
		CTMBAMTHeader header = new CTMBAMTHeader(json("{\"origins\":{\"arm\":[16,32,-8]},\"types\":{\"cargo\":\"item\"},\"hierarchy\":{\"cargo\":\"arm\",\"arm\":\"root\"}}"));
		assertEquals("item", header.types().get("cargo"));
		assertTrue(header.names().contains("root"));
		assertEquals(1, header.getOffset("arm").x);
		assertEquals(2, header.getOffset("arm").y);
		assertEquals(-0.5, header.getOffset("arm").z);
		assertEquals(0, header.getOffset("cargo").lengthSquared());
	}

	@Test
	void legacyHeadersRemainValid()
	{
		CTMBAMTHeader header = new CTMBAMTHeader(json("{\"origins\":{\"body\":[0,0,0]},\"hierarchy\":{}}"));
		assertTrue(header.types().isEmpty());
		assertTrue(header.names().contains("body"));
	}

	@Test
	void malformedTypesVectorsAndCyclesAreRejected()
	{
		assertThrows(IllegalArgumentException.class, () -> new CTMBAMTHeader(json("{\"types\":{\"a\":\"itme\"}}")));
		assertThrows(IllegalArgumentException.class, () -> new CTMBAMTHeader(json("{\"origins\":{\"a\":[0,1]}}")));
		assertThrows(IllegalArgumentException.class, () -> new CTMBAMTHeader(json("{\"origins\":{\"a\":[0,\"NaN\",0]}}")));
		assertThrows(IllegalArgumentException.class, () -> new CTMBAMTHeader(json("{\"hierarchy\":{\"a\":\"b\",\"b\":\"a\"}}")));
		assertThrows(IllegalArgumentException.class, () -> new CTMBAMTHeader(json("{\"hierarchy\":{\"a\":\"a\"}}")));
	}
}
