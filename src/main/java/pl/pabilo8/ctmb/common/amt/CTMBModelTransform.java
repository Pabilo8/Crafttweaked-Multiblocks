package pl.pabilo8.ctmb.common.amt;

import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.util.EnumFacing;

/**
 * Same centred facing rotation as the supplied Blockbench blockstate generator.
 */
public final class CTMBModelTransform
{
	private CTMBModelTransform()
	{
	}

	public static Matrix4 matrix(EnumFacing facing, boolean mirrored)
	{
		Matrix4 matrix = new Matrix4().translate(0.5, 0.5, 0.5)
				.rotate(Math.toRadians(facing.getOpposite().getHorizontalAngle()), 0, 1, 0);
		if(mirrored) matrix.scale(-1, 1, 1);
		return matrix.translate(-0.5, -0.5, -0.5);
	}
}
