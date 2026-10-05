package pl.pabilo8.ctmb.common.gui;

import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenProperty;

import java.util.Arrays;
import java.util.Locale;

@ZenRegister
@ZenClass("mods.ctmb.gui.SlotStyle")
public final class SlotStyle
{
	@ZenProperty
	public static final String VANILLA = "VANILLA";
	@ZenProperty
	public static final String IE = "IE";
	@ZenProperty
	public static final String IE_INPUT = "IE_INPUT";
	@ZenProperty
	public static final String IE_OUTPUT = "IE_OUTPUT";
	@ZenProperty
	public static final String IE_CUSTOM1 = "IE_CUSTOM1";
	@ZenProperty
	public static final String IE_CUSTOM2 = "IE_CUSTOM2";
	@ZenProperty
	public static final String IE_CUSTOM3 = "IE_CUSTOM3";
	@ZenProperty
	public static final String IE_CUSTOM4 = "IE_CUSTOM4";
	@ZenProperty
	public static final String IE_BRASS = "IE_BRASS";
	@ZenProperty
	public static final String IE_BRASS_INPUT = "IE_BRASS_INPUT";
	@ZenProperty
	public static final String IE_BRASS_OUTPUT = "IE_BRASS_OUTPUT";
	@ZenProperty
	public static final String IE_BRASS_CUSTOM1 = "IE_BRASS_CUSTOM1";
	@ZenProperty
	public static final String IE_BRASS_CUSTOM2 = "IE_BRASS_CUSTOM2";
	@ZenProperty
	public static final String IE_BRASS_CUSTOM3 = "IE_BRASS_CUSTOM3";
	@ZenProperty
	public static final String IE_BRASS_CUSTOM4 = "IE_BRASS_CUSTOM4";
	@ZenProperty
	public static final String VANILLA_STEEL = "VANILLA_STEEL";

	private SlotStyle()
	{
	}

	public static String validate(String style)
	{
		String name = style.toUpperCase(Locale.ROOT);
		if(!Arrays.asList("VANILLA", "IE", "IE_INPUT", "IE_OUTPUT", "IE_CUSTOM1", "IE_CUSTOM2", "IE_CUSTOM3", "IE_CUSTOM4", "IE_BRASS", "IE_BRASS_INPUT", "IE_BRASS_OUTPUT", "IE_BRASS_CUSTOM1", "IE_BRASS_CUSTOM2", "IE_BRASS_CUSTOM3", "IE_BRASS_CUSTOM4", "VANILLA_STEEL").contains(name))
			throw new IllegalArgumentException("Unknown Deco slot style "+style);
		return name;
	}
}
