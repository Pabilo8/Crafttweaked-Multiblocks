package pl.pabilo8.ctmb.client.gui.deco;

import crafttweaker.api.data.IData;
import pl.pabilo8.ctmb.common.gui.DecoComponentAccess;
import pl.pabilo8.immersiveintelligence.client.gui.deco.component.button.DecoTab;
import stanhebben.zenscript.annotations.ZenMethod;

public class CTMBDecoTab extends DecoTab implements DecoComponentAccess
{
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
