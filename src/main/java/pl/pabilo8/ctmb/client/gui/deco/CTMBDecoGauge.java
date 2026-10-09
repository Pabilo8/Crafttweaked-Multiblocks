package pl.pabilo8.ctmb.client.gui.deco;

import crafttweaker.api.data.IData;
import pl.pabilo8.ctmb.common.gui.DecoComponentAccess;
import pl.pabilo8.immersiveintelligence.client.gui.deco.component.button.DecoGauge;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * Native II component with side-neutral script access.
 */
public class CTMBDecoGauge extends DecoGauge implements DecoComponentAccess
{
	public CTMBDecoGauge(int x, int y)
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
