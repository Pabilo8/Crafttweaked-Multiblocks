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
import pl.pabilo8.ctmb.client.gui.elements.buttons.GuiButtonCTMBRegular;
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
@ZenClass("mods.ctmb.gui.component.Button")
@ZenRegister
public class GuiComponentButton extends GuiComponent
{
	private final String text;
	private final boolean translated;
	private final int styleID;

	public GuiComponentButton(int x, int y, int w, int h, String name, String text, boolean translated, int styleID)
	{
		super(x, y, name, w, h);

		this.text = text;
		this.translated = translated;
		this.styleID = styleID;
	}

	@ZenMethod()
	@ZenDoc("Creates a new Gui Component instance")
	public static GuiComponentButton create(int x, int y, String name, IData data)
	{
		int styleID = 0;
		boolean translated = false;
		String text = name;
		int w = 60, h = 20;

		if(CommonUtils.dataCheck(data))
		{
			Map<String, IData> map = data.asMap();

			if(map.containsKey("width"))
				w = map.get("width").asInt();
			if(map.containsKey("height"))
				h = map.get("height").asInt();

			if(map.containsKey("text"))
				text = map.get("text").asString();

			if(map.containsKey("translated"))
				translated = map.get("translated").asBool();
			if(map.containsKey("style_id"))
				styleID = map.get("style_id").asInt();
		}

		return new GuiComponentButton(x, y, w, h, name, text, translated, styleID);
	}

	@SideOnly(Side.CLIENT)
	@Override
	@Nullable
	public Gui provide(int id, int x, int y, MultiblockGui gui)
	{
		String t = translated?I18n.format(StyledGuiUtils.processText(gui, this.text)): StyledGuiUtils.processText(gui, this.text);

		return new GuiButtonCTMBRegular(this, id, this.x+x, this.y+y, w, h, styleID, t, gui.getStyle());
	}
}
