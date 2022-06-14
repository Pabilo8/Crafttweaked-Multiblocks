package pl.pabilo8.ctmb.common.gui.component;

import crafttweaker.annotations.ZenDoc;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.pabilo8.ctmb.client.gui.MultiblockGui;
import pl.pabilo8.ctmb.client.gui.StyledGuiUtils;
import pl.pabilo8.ctmb.client.gui.elements.buttons.GuiButtonCTMBSwitch;
import pl.pabilo8.ctmb.common.CommonUtils;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author Pabilo8
 * @since 04.03.2022
 */
@ZenClass("mods.ctmb.gui.component.Switch")
@ZenRegister
public class GuiComponentSwitch extends GuiComponent
{
	private final String textOn, textOff;
	private final boolean translated, defaultState;
	private final int styleID;
	private final int colorOn, colorOff;
	private final int textWidth;

	public GuiComponentSwitch(int x, int y, String name, String textOn, String textOff, boolean defaultState, int colorOn, int textWidth, int colorOut, int styleID, boolean translated)
	{
		super(x, y, name, 18, 9);
		this.textOn = textOn;
		this.textOff = textOff;

		this.translated = translated;
		this.defaultState = defaultState;
		this.styleID = styleID;

		this.colorOn = colorOn;
		this.colorOff = colorOut;
		this.textWidth = textWidth;
	}

	@ZenMethod()
	@ZenDoc("Creates a new Gui Component instance")
	public static GuiComponentSwitch create(int x, int y, String name, IData data)
	{
		int styleID = 0;
		boolean translated = false, defaultState = false;
		String textOn = name, textOff = name;
		int textWidth = 999;
		int colorOn = 0xffffff, colorOff = 0xffffff;

		if(CommonUtils.dataCheck(data))
		{
			Map<String, IData> map = data.asMap();

			if(map.containsKey("text_width"))
				textWidth = map.get("text_width").asInt();

			if(map.containsKey("text"))
				textOn = textOff = map.get("text").asString();
			if(map.containsKey("text_off"))
				textOff = map.get("text_off").asString();
			if(map.containsKey("text_on"))
				textOn = map.get("text_on").asString();

			if(map.containsKey("color"))
				colorOn = colorOff = CommonUtils.getColorFromData(map.get("color"));

			if(map.containsKey("color_on"))
				colorOn = CommonUtils.getColorFromData(map.get("color_on"));
			if(map.containsKey("color_off"))
				colorOff = CommonUtils.getColorFromData(map.get("color_off"));

			if(map.containsKey("default_state"))
				defaultState = map.get("default_state").asBool();
			if(map.containsKey("translated"))
				translated = map.get("translated").asBool();
			if(map.containsKey("style_id"))
				styleID = map.get("style_id").asInt();
		}

		return new GuiComponentSwitch(x, y, name, textOn, textOff, defaultState, colorOn, textWidth, colorOff, styleID, translated);
	}

	@SideOnly(Side.CLIENT)
	@Override
	@Nullable
	public Gui provide(int id, int x, int y, MultiblockGui gui)
	{
		String on = translated?I18n.format(StyledGuiUtils.processText(gui, this.textOn)): StyledGuiUtils.processText(gui, this.textOn);
		String off = translated?I18n.format(StyledGuiUtils.processText(gui, this.textOff)): StyledGuiUtils.processText(gui, this.textOff);

		return new GuiButtonCTMBSwitch(this, id, this.x+x, this.y+y, textWidth, styleID, on, off, gui.getStyle(), defaultState, gui.getStyle().getMainColor(), colorOn, colorOff);
	}
}
