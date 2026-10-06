package pl.pabilo8.ctmb.common.manual;

import crafttweaker.annotations.ZenRegister;
import lombok.Getter;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * Defines a scripted II manual category and its entries.
 *
 * @author Pabilo8 (pabilo@iiteam.net)
 * @since 05.10.2026
 */
@ZenRegister
@ZenClass("mods.ctmb.manual.Category")
@Getter
public class CTMBManualCategory
{
	private final String name;

	CTMBManualCategory(String name)
	{
		this.name = name;
	}

	@ZenMethod
	public CTMBManualEntry addEntry(String name)
	{
		return ManualTweaker.addEntry(name, this.name);
	}

	@ZenMethod
	public CTMBManualEntry addEntry(String name, CTMBManualPage... pages)
	{
		return ManualTweaker.addEntry(name, this.name, pages);
	}
}
