package pl.pabilo8.ctmb.common.storage;

import org.junit.jupiter.api.Test;
import pl.pabilo8.immersiveintelligence.api.crafting.DustStack;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CTMBDustTankTest
{
	@Test
	void fillIsBoundedAndSimulationDoesNotMutate()
	{
		AtomicInteger changes = new AtomicInteger();
		CTMBDustTank tank = new CTMBDustTank(100, changes::incrementAndGet);
		assertEquals(60, tank.fill(new DustStack("iron", 60), false));
		assertTrue(tank.getDustStack().isEmpty());
		assertEquals(0, changes.get());
		assertEquals(60, tank.fill(new DustStack("iron", 60), true));
		assertEquals(40, tank.fill(new DustStack("iron", 90), true));
		assertEquals(100, tank.getDustStack().amount);
		assertEquals(2, changes.get());
		assertEquals(0, tank.fill(new DustStack("copper", 10), true));
		assertEquals(100, tank.drain(200, false).amount);
		assertEquals(100, tank.getDustStack().amount);
		assertEquals(100, tank.drain(200, true).amount);
		assertTrue(tank.getDustStack().isEmpty());
	}
}
