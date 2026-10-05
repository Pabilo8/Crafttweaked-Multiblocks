package pl.pabilo8.ctmb.client.gui.deco;

import crafttweaker.api.data.IData;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.util.math.MathHelper;
import pl.pabilo8.immersiveintelligence.client.gui.deco.component.DecoComponent;
import pl.pabilo8.immersiveintelligence.client.gui.deco.component.DecoTextBasedComponent;
import pl.pabilo8.immersiveintelligence.common.util.IIColor;
import pl.pabilo8.immersiveintelligence.common.util.easynbt.EasyNBT;

/**
 * Converts script NBT to the public state of native Deco components.
 *
 * @author Pabilo8 (pabilo@iiteam.net)
 * @since 05.10.2026
 */
public final class CTMBDecoData
{
	private CTMBDecoData() {}

	public static IData read(DecoComponent<?> component)
	{
		EasyNBT data = EasyNBT.newNBT().withBoolean("visible", component.visible)
				.withBoolean("enabled", component.enabled);
		if(component instanceof CTMBDecoCheckbox) data.withBoolean("checked", ((CTMBDecoCheckbox)component).isChecked());
		if(component instanceof CTMBDecoSwitch) data.withBoolean("state", ((CTMBDecoSwitch)component).getState());
		if(component instanceof CTMBDecoSlider) data.withFloat("value", ((CTMBDecoSlider)component).getValue());
		if(component instanceof CTMBDecoDropdown) data.withInt("selected", ((CTMBDecoDropdown)component).selectedEntry);
		if(component instanceof CTMBDecoTextField) data.withString("text", ((CTMBDecoTextField)component).getText());
		if(component instanceof CTMBDecoBar)
		{
			CTMBDecoBar bar = (CTMBDecoBar)component;
			data.withInt("value", bar.getCurrentValue()).withInt("min", bar.getMinValue()).withInt("max", bar.getMaxValue());
		}
		return CraftTweakerMC.getIData(data.unwrap());
	}

	public static void apply(DecoComponent<?> component, IData value)
	{
		EasyNBT data = EasyNBT.wrapNBT(CraftTweakerMC.getNBTCompound(value));
		if(data.hasKey("visible")) component.visible = data.getBoolean("visible");
		if(data.hasKey("enabled")) component.withDisabled(!data.getBoolean("enabled"));
		if(data.hasKey("text")&&component instanceof DecoTextBasedComponent)
		{
			DecoTextBasedComponent<?> text = (DecoTextBasedComponent<?>)component;
			if(data.getBoolean("translated")) text.withText(data.getString("text"));
			else text.withRawText(data.getString("text"));
		}
		if(component instanceof CTMBDecoCheckbox&&data.hasKey("checked")) ((CTMBDecoCheckbox)component).withChecked(data.getBoolean("checked"));
		if(component instanceof CTMBDecoSwitch&&data.hasKey("state")) ((CTMBDecoSwitch)component).withCurrentState(data.getBoolean("state"));
		if(component instanceof CTMBDecoSlider&&data.hasKey("value"))
			((CTMBDecoSlider)component).setScriptValue(data.getFloat("value"));
		if(component instanceof CTMBDecoDropdown&&data.hasKey("selected"))
			((CTMBDecoDropdown)component).setScriptSelection(data.getInt("selected"));
		if(component instanceof CTMBDecoTextField&&data.hasKey("text"))
			((CTMBDecoTextField)component).withText(data.getString("text"));
		if(component instanceof CTMBDecoBar)
		{
			CTMBDecoBar bar = (CTMBDecoBar)component;
			if(data.hasKey("value"))
			{
				int min = data.hasKey("min")?data.getInt("min"):bar.getMinValue();
				int max = data.hasKey("max")?data.getInt("max"):bar.getMaxValue();
				if(max <= min) throw new IllegalArgumentException("Bar maximum must exceed minimum");
				int current = MathHelper.clamp(data.getInt("value"), min, max);
				bar.withLimits(min, max, () -> current);
			}
			if(data.hasKey("color")) bar.withColor(IIColor.fromPackedRGB(data.getInt("color")));
		}
	}
}
