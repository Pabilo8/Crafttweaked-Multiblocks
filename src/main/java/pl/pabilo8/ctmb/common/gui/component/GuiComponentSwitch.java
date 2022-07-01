package pl.pabilo8.ctmb.common.gui.component;

import crafttweaker.annotations.ZenDoc;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.pabilo8.ctmb.client.gui.MultiblockGui;
import pl.pabilo8.ctmb.client.gui.elements.buttons.GuiButtonCTMBSwitch;
import pl.pabilo8.ctmb.common.util.GuiNBTData;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nullable;

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
		GuiNBTData map = new GuiNBTData(data);
		String text = map.getText(name);
		int color = map.getColor(0xffffff);

		return new GuiComponentSwitch(x, y,
				name,
				map.getText("text_on", text),
				map.getText("text_off", text),
				map.getProperty("default_state"),
				map.getColor("color_on", color),
				map.getWidth(999),
				map.getColor("color_off", color),
				map.getStyle(),
				map.getTranslated()
		);
	}

	@SideOnly(Side.CLIENT)
	@Override
	@Nullable
	public Gui provide(int id, int x, int y, MultiblockGui gui)
	{
		String on = getTranslation(translated, gui, textOn);
		String off = getTranslation(translated, gui, textOff);

		return new GuiButtonCTMBSwitch(this, id, this.x+x, this.y+y,
				textWidth, styleID,
				on, off,
				gui.getStyle(),
				defaultState,
				gui.getStyle().getMainColor(), colorOn, colorOff
		);
	}
}
