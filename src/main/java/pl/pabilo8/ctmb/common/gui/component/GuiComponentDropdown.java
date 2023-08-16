package pl.pabilo8.ctmb.common.gui.component;

import crafttweaker.annotations.ZenDoc;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.pabilo8.ctmb.client.gui.IComponentGui;
import pl.pabilo8.ctmb.client.gui.StyledGuiUtils;
import pl.pabilo8.ctmb.client.gui.elements.buttons.GuiButtonCTMBDropdownList;
import pl.pabilo8.ctmb.common.util.GuiNBTData;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nullable;
import java.util.Arrays;
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
		GuiNBTData map = new GuiNBTData(data);

		return new GuiComponentDropdown(x, y,
				map.getWidth(64),
				name,
				map.getStyle(),
				map.getStringArray("entries"),
				map.getStringArray("translations"),
				map.getInt("per_page", 6),
				map.getTranslated()
		);
	}

	@SideOnly(Side.CLIENT)
	@Override
	@Nullable
	public Gui provide(int id, int x, int y, IComponentGui gui)
	{
		Stream<String> stream = Arrays.stream(translations).map(s -> StyledGuiUtils.processText(gui, s));
		if(translated)
			stream = stream.map(I18n::format);

		return new GuiButtonCTMBDropdownList(this, id, this.x+x, this.y+y,
				w, h, styleID, gui.getStyle(), perPage, entries,
				stream.toArray(String[]::new)
		);
	}
}
