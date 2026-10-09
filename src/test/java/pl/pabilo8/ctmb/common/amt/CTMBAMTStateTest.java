package pl.pabilo8.ctmb.common.amt;

import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class CTMBAMTStateTest
{
	@Test
	void savedSamplesPreserveLayerOrderAndZeroProgress()
	{
		CTMBAMTState source = new CTMBAMTState();
		assertTrue(source.set("ctmb:default", 0));
		assertTrue(source.set("ctmb:rotate", 0.75f));
		assertFalse(source.set("ctmb:rotate", 0.75f));
		CTMBAMTState restored = new CTMBAMTState();
		restored.restore(source.save());
		assertEquals(source.samples(), restored.samples());
		assertEquals(Arrays.asList(new ResourceLocation("ctmb:default"), new ResourceLocation("ctmb:rotate")), new ArrayList<>(restored.samples().keySet()));
		assertTrue(restored.clear("ctmb:rotate"));
		assertFalse(restored.clear("ctmb:rotate"));
		assertTrue(restored.clear());
		assertFalse(restored.clear());
	}

	@Test
	void invalidProgressCannotPoisonRenderOrCollisionMaps()
	{
		CTMBAMTState state = new CTMBAMTState();
		assertThrows(IllegalArgumentException.class, () -> state.set("ctmb:rotate", Float.NaN));
		assertThrows(IllegalArgumentException.class, () -> state.set("ctmb:rotate", Float.POSITIVE_INFINITY));
		state.set("ctmb:rotate", 10);
		assertEquals(1f, state.samples().get(new ResourceLocation("ctmb:rotate")).floatValue());
		state.set("ctmb:rotate", -1);
		assertEquals(0f, state.samples().get(new ResourceLocation("ctmb:rotate")).floatValue());
	}
}
