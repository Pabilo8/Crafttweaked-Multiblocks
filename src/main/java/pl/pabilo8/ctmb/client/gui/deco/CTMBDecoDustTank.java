package pl.pabilo8.ctmb.client.gui.deco;

import crafttweaker.api.data.IData;
import crafttweaker.api.minecraft.CraftTweakerMC;
import pl.pabilo8.ctmb.common.gui.DecoComponentAccess;
import pl.pabilo8.ctmb.common.storage.StorageAccess;
import pl.pabilo8.immersiveintelligence.client.gui.deco.component.storage.DecoDustTank;
import pl.pabilo8.immersiveintelligence.common.util.easynbt.EasyNBT;
import stanhebben.zenscript.annotations.ZenMethod;

public class CTMBDecoDustTank extends DecoDustTank implements DecoComponentAccess
{
	private StorageAccess source;

	public CTMBDecoDustTank(int x, int y)
	{
		super(x, y);
	}

	public void bind(StorageAccess source)
	{
		this.source = source;
		withDustTank(source.dustTank(), source.getSize());
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
		if(options.hasKey("contents")||options.hasKey("capacity"))
			throw new IllegalArgumentException("Dust displays are read-only; modify the server storage");
		CTMBDecoData.apply(this, data);
	}
}
