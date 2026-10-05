package pl.pabilo8.ctmb.common.manual;

import crafttweaker.annotations.ZenRegister;
import lombok.Getter;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * Defines a Markdown section rendered by IIManualPage.
 *
 * @author Pabilo8 (pabilo@iiteam.net)
 * @since 20.03.2022
 * @updated 05.10.2026
 */
@ZenRegister
@ZenClass("mods.ctmb.manual.Page")
@Getter
public class CTMBManualPage
{
	private final String name;

	private CTMBManualPage(String name)
	{
		if(name==null||!name.matches("[a-z0-9_]+")||name.equals("meta"))
			throw new IllegalArgumentException("Invalid manual section: "+name);
		this.name = name;
	}

	@ZenMethod
	public static CTMBManualPage create(String name) { return new CTMBManualPage(name); }
}
