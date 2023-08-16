package pl.pabilo8.ctmb.common.gui.component;

import crafttweaker.annotations.ZenDoc;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.pabilo8.ctmb.client.gui.IComponentGui;
import pl.pabilo8.ctmb.client.gui.elements.GuiBar;
import pl.pabilo8.ctmb.common.util.GuiNBTData;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nullable;

/**
 * @author Pabilo8
 * @since 04.03.2022
 */
@ZenClass("mods.ctmb.gui.component.Bar")
@ZenRegister
public class GuiComponentBar extends GuiComponent
{
	private final int color1, color2;
	private final int styleID;

	public GuiComponentBar(int x, int y, int w, int h, String name, int color1, int color2, int styleID)
	{
		super(x, y, name, w, h);
		this.color1 = color1;
		this.color2 = color2;
		this.styleID = styleID;
	}

	@ZenMethod()
	@ZenDoc("Creates a new Gui Component instance")
	public static GuiComponentBar create(int x, int y, String name, IData data)
	{
		GuiNBTData map = new GuiNBTData(data);

		return new GuiComponentBar(x, y,
				map.getWidth(200),
				map.getHeight(11),
				name,
				map.getColor("color1", 0xb51500),
				map.getColor("color2", 0x600b00),
				map.getStyle()
		);
	}

	@SideOnly(Side.CLIENT)
	@Override
	@Nullable
	public Gui provide(int id, int x, int y, IComponentGui gui)
	{
		return new GuiBar(this, id, this.x+x, this.y+y, w, h, gui.getStyle(), styleID, color1, color2);
	}

}
