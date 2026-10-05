package pl.pabilo8.ctmb.client.gui.deco;

import crafttweaker.api.data.IData;
import pl.pabilo8.ctmb.common.gui.DecoComponentAccess;
import pl.pabilo8.immersiveintelligence.client.gui.deco.component.storage.DecoFluidTank;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * Provides script access to the II Deco fluidtank component.
 *
 * @author Pabilo8 (pabilo@iiteam.net)
 * @since 05.10.2026
 */
public class CTMBDecoFluidTank extends DecoFluidTank implements DecoComponentAccess
{
	public CTMBDecoFluidTank(int x, int y) { super(x, y); }

	@ZenMethod
	@Override
	public IData getData()
	{
		pl.pabilo8.immersiveintelligence.common.util.easynbt.EasyNBT data =
				pl.pabilo8.immersiveintelligence.common.util.easynbt.EasyNBT.wrapNBT(
						crafttweaker.api.minecraft.CraftTweakerMC.getNBTCompound(CTMBDecoData.read(this)));
		if(fluidTank!=null)
		{
			data.withInt("amount", fluidTank.getFluidAmount()).withInt("capacity", fluidTank.getCapacity());
			if(fluidTank.getFluid()!=null) data.withFluidStack("fluid", fluidTank.getFluid());
		}
		return crafttweaker.api.minecraft.CraftTweakerMC.getIData(data.unwrap());
	}

	@ZenMethod
	@Override
	public void setData(IData data) { CTMBDecoData.apply(this, data); }
}
