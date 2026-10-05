package pl.pabilo8.ctmb.client.gui.deco;

import crafttweaker.api.data.IData;
import crafttweaker.api.minecraft.CraftTweakerMC;
import pl.pabilo8.ctmb.common.gui.DecoComponentAccess;
import pl.pabilo8.immersiveintelligence.client.IIClientUtils;
import pl.pabilo8.immersiveintelligence.client.gui.deco.component.label.DecoLabel;
import pl.pabilo8.immersiveintelligence.common.util.easynbt.EasyNBT;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * Provides script access to an II Deco label.
 *
 * @author Pabilo8 (pabilo@iiteam.net)
 * @since 05.10.2026
 */
public class CTMBDecoLabel extends DecoLabel implements DecoComponentAccess
{
	private String scriptText = "";

	public CTMBDecoLabel(int x, int y)
	{
		super(IIClientUtils.fontRegular, x, y);
	}

	@ZenMethod
	@Override
	public IData getData()
	{
		return CraftTweakerMC.getIData(EasyNBT.newNBT().withString("text", scriptText).unwrap());
	}

	@ZenMethod
	@Override
	public void setData(IData data)
	{
		EasyNBT nbt = EasyNBT.wrapNBT(CraftTweakerMC.getNBTCompound(data));
		if(nbt.hasKey("text_item"))
		{
			scriptText = new net.minecraft.item.ItemStack(nbt.getCompound("text_item")).getDisplayName();
			withRawText(scriptText);
		}
		if(nbt.hasKey("text"))
		{
			scriptText = nbt.getString("text");
			if(nbt.getBoolean("translated")) withText(scriptText);
			else withRawText(scriptText);
		}
		if(nbt.hasKey("visible")) visible = nbt.getBoolean("visible");
	}
}
