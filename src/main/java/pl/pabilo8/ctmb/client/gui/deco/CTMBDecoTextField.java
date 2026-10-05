package pl.pabilo8.ctmb.client.gui.deco;

import crafttweaker.api.data.IData;
import pl.pabilo8.ctmb.common.gui.DecoComponentAccess;
import pl.pabilo8.immersiveintelligence.client.gui.deco.component.text.DecoTextField;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * Provides script access to the II Deco textfield component.
 *
 * @author Pabilo8 (pabilo@iiteam.net)
 * @since 05.10.2026
 */
public class CTMBDecoTextField extends DecoTextField implements DecoComponentAccess
{
	public CTMBDecoTextField(int x, int y)
	{
		super(x, y);
	}

	@ZenMethod
	@Override
	public IData getData()
	{
		return CTMBDecoData.read(this);
	}

	@ZenMethod
	@Override
	public void setData(IData data)
	{
		CTMBDecoData.apply(this, data);
	}
}
