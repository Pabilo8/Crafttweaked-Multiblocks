package pl.pabilo8.ctmb.common.gui.component;

import crafttweaker.annotations.ZenDoc;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.pabilo8.ctmb.client.gui.IComponentGui;
import pl.pabilo8.ctmb.client.gui.elements.buttons.GuiButtonCTMBState;
import pl.pabilo8.ctmb.common.util.GuiNBTData;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nullable;

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
		GuiNBTData map = new GuiNBTData(data);

		String text = map.getText(name);

		return new GuiComponentButtonState(x, y,
				map.getWidth(60), map.getHeight(20),
				name,
				map.getText("text_on", text),
				map.getText("text_off", text),
				map.getTranslated(),
				map.getProperty("defaultState"),
				map.getStyle()
		);
	}

	@SideOnly(Side.CLIENT)
	@Override
	@Nullable
	public Gui provide(int id, int x, int y, IComponentGui gui)
	{
		String on = getTranslation(translated,gui,textOn);
		String off = getTranslation(translated,gui,textOff);

		return new GuiButtonCTMBState(this, id, this.x+x, this.y+y, w, h, styleID, on, off, gui.getStyle(), defaultState);
	}
}
