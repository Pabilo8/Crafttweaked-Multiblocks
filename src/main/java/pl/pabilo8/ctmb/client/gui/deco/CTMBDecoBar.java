package pl.pabilo8.ctmb.client.gui.deco;

import crafttweaker.api.data.IData;
import pl.pabilo8.ctmb.common.gui.DecoComponentAccess;
import pl.pabilo8.immersiveintelligence.client.gui.deco.component.storage.DecoBar;
import pl.pabilo8.immersiveintelligence.client.gui.deco.util.DecoTemplates;
import pl.pabilo8.immersiveintelligence.client.gui.deco.util.DecoTextures;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * Provides script access to the II Deco bar component.
 *
 * @author Pabilo8 (pabilo@iiteam.net)
 * @since 05.10.2026
 */
public class CTMBDecoBar extends DecoBar implements DecoComponentAccess
{
	private pl.pabilo8.ctmb.common.storage.StorageAccess source;

	private pl.pabilo8.ctmb.common.production.ProductionAccess production;

	public void bindProduction(pl.pabilo8.ctmb.common.production.ProductionAccess source)
	{
		production = source;
		withLimits(0, 10000, source::progressValue);
		withIconLocation(DecoTextures.ICON_PROGRESS);
	}

	public void bindEnergy(pl.pabilo8.ctmb.common.storage.StorageAccess source)
	{
		this.source = source;
		withTemplate(DecoTemplates.BAR_ELECTRIC_ENERGY.apply(source.energy()));
	}

	public CTMBDecoBar(int x, int y)
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
		pl.pabilo8.immersiveintelligence.common.util.easynbt.EasyNBT options = pl.pabilo8.immersiveintelligence.common.util.easynbt.EasyNBT.wrapNBT(crafttweaker.api.minecraft.CraftTweakerMC.getNBTCompound(data));
		if((source!=null||production!=null)&&(options.hasKey("value")||options.hasKey("min")||options.hasKey("max")))
			throw new IllegalArgumentException("Bound displays are read-only; the server owns their values");
		CTMBDecoData.apply(this, data);
	}
}
