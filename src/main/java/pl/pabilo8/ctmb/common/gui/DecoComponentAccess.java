package pl.pabilo8.ctmb.common.gui;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * Provides script access to a live Deco component.
 *
 * @author Pabilo8 (pabilo@iiteam.net)
 * @since 05.10.2026
 */
@ZenRegister
@ZenClass("mods.ctmb.gui.ComponentAccess")
public interface DecoComponentAccess
{
	@ZenMethod
	IData getData();

	@ZenMethod
	void setData(IData data);
}
