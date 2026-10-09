package pl.pabilo8.ctmb.common.amt;

import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.gson.JsonParser;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CTMBTactileGeometryTest
{
	@Test
	void exportedNorthRotationAndMirroringUseTheBlockCentre()
	{
		Vec3d normal = CTMBModelTransform.matrix(EnumFacing.NORTH, false).apply(new Vec3d(2, 3, 4));
		Vec3d mirrored = CTMBModelTransform.matrix(EnumFacing.NORTH, true).apply(new Vec3d(2, 3, 4));
		assertEquals(2, normal.x, 1e-9);
		assertEquals(3, normal.y, 1e-9);
		assertEquals(4, normal.z, 1e-9);
		assertEquals(-1, mirrored.x, 1e-9);
		assertEquals(3, mirrored.y, 1e-9);
		assertEquals(4, mirrored.z, 1e-9);
	}

	@Test
	void transformedBoundsIncludeAllEightCornersOfAThinRotatedArm()
	{
		AxisAlignedBB box = new AxisAlignedBB(-2, -0.25, -0.5, 2, 0.25, 0.5);
		AxisAlignedBB rotated = CTMBTactileManager.transformBounds(box, new Matrix4().rotate(Math.PI/4, 0, 1, 0));
		double radius = 2.5/Math.sqrt(2);
		assertEquals(-radius, rotated.minX, 1e-9);
		assertEquals(radius, rotated.maxX, 1e-9);
		assertEquals(-radius, rotated.minZ, 1e-9);
		assertEquals(radius, rotated.maxZ, 1e-9);
		assertEquals(-0.25, rotated.minY, 1e-9);
		assertEquals(0.25, rotated.maxY, 1e-9);
	}

	@Test
	void boundsUsePixelsAndRejectInvertedBoxes()
	{
		AxisAlignedBB bounds = CTMBTactileManager.bounds(new JsonParser().parse("[-8,0,-8,8,16,8]"));
		assertEquals(-0.5, bounds.minX);
		assertEquals(1, bounds.maxY);
		assertThrows(IllegalArgumentException.class, () -> CTMBTactileManager.bounds(new JsonParser().parse("[16,0,0,0,16,16]")));
	}
}
