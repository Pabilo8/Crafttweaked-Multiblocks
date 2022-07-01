package pl.pabilo8.ctmb.common.gui.component;

import crafttweaker.annotations.ZenDoc;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.pabilo8.ctmb.client.gui.MultiblockGui;
import pl.pabilo8.ctmb.client.gui.elements.buttons.GuiButtonCTMBRegular;
import pl.pabilo8.ctmb.common.util.GuiNBTData;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nullable;

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
		GuiNBTData map = new GuiNBTData(data);

		return new GuiComponentButton(x, y,
				map.getWidth(60), map.getHeight(20),
				name,
				map.getText(name),
				map.getTranslated(),
				map.getStyle()
		);
	}

	@SideOnly(Side.CLIENT)
	@Override
	@Nullable
	public Gui provide(int id, int x, int y, MultiblockGui gui)
	{
		String text = getTranslation(translated, gui, this.text);

		return new GuiButtonCTMBRegular(this, id, this.x+x, this.y+y, w, h, styleID, text, gui.getStyle());
	}
}
