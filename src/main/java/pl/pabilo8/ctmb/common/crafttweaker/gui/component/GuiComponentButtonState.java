package pl.pabilo8.ctmb.common.crafttweaker.gui.component;

import crafttweaker.annotations.ZenDoc;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.pabilo8.ctmb.client.gui.MultiblockGui;
import pl.pabilo8.ctmb.client.gui.StyledGuiUtils;
import pl.pabilo8.ctmb.client.gui.elements.buttons.GuiButtonCTMBState;
import pl.pabilo8.ctmb.common.CommonUtils;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author Pabilo8
 * @since 04.03.2022
 */
@ZenClass("mods.ctmb.gui.component.ButtonState")
@ZenRegister
public class GuiComponentButtonState extends GuiComponent
{
	private final String textOn, textOff;
	private final boolean translated, defaultState;
	private final int styleID;

	public GuiComponentButtonState(int x, int y, int w, int h, String name, String textOn, String textOff, boolean translated, boolean defaultState, int styleID)
	{
		super(x, y, name, w, h);
		this.textOn = textOn;
		this.textOff = textOff;

		this.translated = translated;
		this.defaultState = defaultState;
		this.styleID = styleID;
	}

	@ZenMethod()
	@ZenDoc("Creates a new Gui Component instance")
	public static GuiComponentButtonState create(int x, int y, String name, IData data)
	{
		int styleID = 0;
		boolean translated = false, defaultState = false;
		String textOn = name, textOff = name;
		int w = 60, h = 20;

		if(CommonUtils.dataCheck(data))
		{
			Map<String, IData> map = data.asMap();

			if(map.containsKey("width"))
				w = map.get("width").asInt();
			if(map.containsKey("height"))
				h = map.get("height").asInt();

			if(map.containsKey("text"))
				textOn = textOff = map.get("text").asString();
			if(map.containsKey("text_off"))
				textOff = map.get("text_off").asString();
			if(map.containsKey("text_on"))
				textOn = map.get("text_on").asString();

			if(map.containsKey("default_state"))
				defaultState = map.get("default_state").asBool();
			if(map.containsKey("translated"))
				translated = map.get("translated").asBool();
			if(map.containsKey("style_id"))
				styleID = map.get("style_id").asInt();
		}

		return new GuiComponentButtonState(x, y, w, h, name, textOn, textOff, translated, defaultState, styleID);
	}

	@SideOnly(Side.CLIENT)
	@Override
	@Nullable
	public Gui provide(int id, int x, int y, MultiblockGui gui)
	{
		String on = translated?I18n.format(StyledGuiUtils.processText(gui, this.textOn)): StyledGuiUtils.processText(gui, this.textOn);
		String off = translated?I18n.format(StyledGuiUtils.processText(gui, this.textOff)): StyledGuiUtils.processText(gui, this.textOff);

		return new GuiButtonCTMBState(this, id, this.x+x, this.y+y, w, h, styleID, on, off, gui.getStyle(),defaultState);
	}
}
