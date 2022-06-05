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
import pl.pabilo8.ctmb.client.gui.elements.buttons.GuiButtonCTMBDropdownList;
import pl.pabilo8.ctmb.common.CommonUtils;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Pabilo8
 * @since 04.03.2022
 */
@ZenClass("mods.ctmb.gui.component.Dropdown")
@ZenRegister
public class GuiComponentDropdown extends GuiComponent
{
	private final boolean translated;
	private final String[] entries, translations;
	private final int perPage;
	private final int styleID;

	public GuiComponentDropdown(int x, int y, int w, String name, int styleID, String[] entries, String[] translations, int perPage, boolean translated)
	{
		super(x, y, name, w, 12);
		this.entries = entries;
		this.translations = translations;

		this.perPage = perPage;

		this.translated = translated;
		this.styleID = styleID;
	}

	@ZenMethod()
	@ZenDoc("Creates a new Gui Component instance")
	public static GuiComponentDropdown create(int x, int y, String name, IData data)
	{
		int styleID = 0, perPage = 6;
		boolean translated = false;
		String[] entries = new String[0], translations = new String[0];

		int w = 64;

		if(CommonUtils.dataCheck(data))
		{
			Map<String, IData> map = data.asMap();

			if(map.containsKey("width"))
				w = map.get("width").asInt();

			if(map.containsKey("entries"))
				entries = translations = CommonUtils.getStringArray(map.get("entries"));
			if(map.containsKey("translations"))
				translations = CommonUtils.getStringArray(map.get("translations"));

			if(map.containsKey("translated"))
				translated = map.get("translated").asBool();
			if(map.containsKey("per_page"))
				perPage = map.get("per_page").asInt();
			if(map.containsKey("style_id"))
				styleID = map.get("style_id").asInt();
		}

		return new GuiComponentDropdown(x, y, w, name, styleID, entries, translations, perPage, translated);
	}

	@SideOnly(Side.CLIENT)
	@Override
	@Nonnull
	public Gui provide(int id, int x, int y, MultiblockGui gui)
	{
		Stream<String> stream = Arrays.stream(translations).map(s -> StyledGuiUtils.processText(gui, s));
		if(translated)
			stream = stream.map(I18n::format);

		return new GuiButtonCTMBDropdownList(this, id, this.x+x, this.y+y, w, h, styleID, gui.getStyle(), perPage, entries, stream.toArray(String[]::new));
	}
}
