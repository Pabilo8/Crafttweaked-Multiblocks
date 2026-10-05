package pl.pabilo8.ctmb.client.gui.deco;

import crafttweaker.api.data.IData;
import pl.pabilo8.ctmb.common.gui.DecoComponentAccess;
import pl.pabilo8.immersiveintelligence.client.gui.deco.component.collection.DecoDropdown;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * Provides script access to the II Deco dropdown component.
 *
 * @author Pabilo8 (pabilo@iiteam.net)
 * @since 05.10.2026
 */
public class CTMBDecoDropdown extends DecoDropdown<String> implements DecoComponentAccess
{
	public CTMBDecoDropdown(int x, int y) { super(x, y); }

	public void setScriptSelection(int index)
	{
		selectedEntry = net.minecraft.util.math.MathHelper.clamp(index, -1, entries.size()-1);
	}

	@ZenMethod
	@Override
	public IData getData() { return CTMBDecoData.read(this); }

	@ZenMethod
	@Override
	public void setData(IData data) { CTMBDecoData.apply(this, data); }
}
