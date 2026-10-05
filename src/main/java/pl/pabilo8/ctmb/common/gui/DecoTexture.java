package pl.pabilo8.ctmb.common.gui;

import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;

/**
 * An atlas location; no client class is loaded when scripts initialise it.
 */
@ZenRegister
@ZenClass("mods.ctmb.gui.Texture")
public final class DecoTexture
{
	public final String location;

	DecoTexture(String location)
	{
		this.location = new net.minecraft.util.ResourceLocation(location).toString();
	}

	@ZenGetter("location")
	public String getLocation()
	{
		return location;
	}
}
