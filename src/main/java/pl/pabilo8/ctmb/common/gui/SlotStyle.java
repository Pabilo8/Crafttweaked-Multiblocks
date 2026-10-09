package pl.pabilo8.ctmb.common.gui;

import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Side-neutral typed handles for II Deco slot styles.
 */
@ZenRegister
@ZenClass("mods.ctmb.gui.SlotStyle")
public final class SlotStyle
{
	private static final Map<String, SlotStyle> STYLES = new LinkedHashMap<>();

	static
	{
		for(String name : Arrays.asList("VANILLA", "IE", "IE_INPUT", "IE_OUTPUT", "IE_CUSTOM1", "IE_CUSTOM2", "IE_CUSTOM3", "IE_CUSTOM4", "IE_BRASS", "IE_BRASS_INPUT", "IE_BRASS_OUTPUT", "IE_BRASS_CUSTOM1", "IE_BRASS_CUSTOM2", "IE_BRASS_CUSTOM3", "IE_BRASS_CUSTOM4", "VANILLA_STEEL"))
			STYLES.put(name.toLowerCase(Locale.ROOT), new SlotStyle(name));
	}

	private final String name;

	private SlotStyle(String name)
	{
		this.name = name;
	}

	public String nativeName()
	{
		return name;
	}

	public static SlotStyle find(String name)
	{
		SlotStyle result = STYLES.get(name.toLowerCase(Locale.ROOT));
		if(result==null) throw new IllegalArgumentException("Unknown Deco slot style: "+name);
		return result;
	}

	public static String validate(String style)
	{
		return find(style).nativeName();
	}
}
