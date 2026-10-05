package pl.pabilo8.ctmb.common.block;

import crafttweaker.annotations.ZenRegister;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenProperty;

import java.util.Locale;

/**
 * Relative directions in the multiblock's frame; mirroring swaps the quarter turns.
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
		if(this==UP) return EnumFacing.UP;
		if(this==DOWN) return EnumFacing.DOWN;
		Direction direction = this;
		if(mirrored)
		{
			if(direction==CLOCKWISE_90) direction = COUNTERCLOCKWISE_90;
			else if(direction==COUNTERCLOCKWISE_90) direction = CLOCKWISE_90;
		}
		return Rotation.valueOf(direction.name()).rotate(facing);
	}
}
