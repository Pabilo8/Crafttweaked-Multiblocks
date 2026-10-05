package pl.pabilo8.ctmb.client.gui.deco;

import crafttweaker.api.data.IData;
import crafttweaker.api.minecraft.CraftTweakerMC;
import pl.pabilo8.ctmb.common.gui.DecoComponentAccess;
import pl.pabilo8.ctmb.common.storage.StorageAccess;
import pl.pabilo8.immersiveintelligence.client.gui.deco.component.storage.DecoFluidTank;
import pl.pabilo8.immersiveintelligence.common.util.easynbt.EasyNBT;
import stanhebben.zenscript.annotations.ZenMethod;

public class CTMBDecoFluidTank extends DecoFluidTank implements DecoComponentAccess
{
	private StorageAccess source;

	public CTMBDecoFluidTank(int x, int y)
	{
		super(x, y);
	}

	public void bind(StorageAccess source)
	{
		this.source = source;
		withFluidTank(source.fluidTank());
	}

	@ZenMethod
	@Override
	public IData getData()
	{
		return source.getData();
	}

	@ZenMethod
	@Override
	public void setData(IData data)
	{
		EasyNBT options = EasyNBT.wrapNBT(CraftTweakerMC.getNBTCompound(data));
		if(options.hasKey("contents")||options.hasKey("capacity")||options.hasKey("amount")||options.hasKey("fluid"))
			throw new IllegalArgumentException("Fluid displays are read-only; modify the server storage");
		CTMBDecoData.apply(this, data);
	}
}
