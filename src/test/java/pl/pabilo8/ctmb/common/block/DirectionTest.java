package pl.pabilo8.ctmb.common.block;

import net.minecraft.util.EnumFacing;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DirectionTest
{
	@Test
	void horizontalAndVerticalFacesResolveForEveryOrientation()
	{
		for(EnumFacing facing : EnumFacing.HORIZONTALS)
			for(boolean mirror : new boolean[]{false, true})
			{
				assertEquals(facing, Direction.NONE.resolve(facing, mirror));
				assertEquals(facing.getOpposite(), Direction.CLOCKWISE_180.resolve(facing, mirror));
				assertEquals(mirror?facing.rotateY(): facing.rotateYCCW(), Direction.CLOCKWISE_90.resolve(facing, mirror));
				assertEquals(mirror?facing.rotateYCCW(): facing.rotateY(), Direction.COUNTERCLOCKWISE_90.resolve(facing, mirror));
				assertEquals(EnumFacing.UP, Direction.UP.resolve(facing, mirror));
				assertEquals(EnumFacing.DOWN, Direction.DOWN.resolve(facing, mirror));
			}
	}
}
