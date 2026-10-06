package pl.pabilo8.ctmb.common.block;

import crafttweaker.annotations.ZenRegister;
import net.minecraft.util.EnumFacing;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenProperty;

import java.util.Locale;

/**
 * Relative directions exported by IIToolkit. Its quarter turns are opposite
 * Minecraft's Rotation names; mirroring reverses them once more.
 */
@ZenRegister
@ZenClass("mods.ctmb.multiblock.Direction")
public enum Direction
{
	@ZenProperty NONE,
	@ZenProperty CLOCKWISE_90,
	@ZenProperty CLOCKWISE_180,
	@ZenProperty COUNTERCLOCKWISE_90,
	@ZenProperty UP,
	@ZenProperty DOWN;

	@ZenMethod
	public static Direction parse(String value)
	{
		try {return valueOf(value.toUpperCase(Locale.ROOT));} catch(IllegalArgumentException e)
		{
			throw new IllegalArgumentException("Unknown CTMB direction: "+value, e);
		}
	}

	@ZenGetter("name")
	public String scriptName()
	{
		return name().toLowerCase(Locale.ROOT);
	}

	public EnumFacing resolve(EnumFacing facing, boolean mirrored)
	{
		switch(this)
		{
			case NONE:
				return facing;
			case CLOCKWISE_90:
				return mirrored?facing.rotateY(): facing.rotateYCCW();
			case CLOCKWISE_180:
				return facing.getOpposite();
			case COUNTERCLOCKWISE_90:
				return mirrored?facing.rotateYCCW(): facing.rotateY();
			case UP:
				return EnumFacing.UP;
			case DOWN:
				return EnumFacing.DOWN;
			default:
				throw new AssertionError(this);
		}
	}
}
