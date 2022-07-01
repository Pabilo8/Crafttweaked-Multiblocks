package pl.pabilo8.ctmb.common.gui.component;

import crafttweaker.annotations.ZenDoc;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.pabilo8.ctmb.client.gui.MultiblockGui;
import pl.pabilo8.ctmb.client.gui.elements.buttons.GuiButtonCTMBTab;
import pl.pabilo8.ctmb.common.util.GuiNBTData;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nullable;

/**
 * @author Pabilo8
 * @since 04.03.2022
 */
@ZenClass("mods.ctmb.gui.component.Tab")
@ZenRegister
public class GuiComponentTab extends GuiComponent
{
	@Nullable
	private final IData data;
	private final int styleID;

	public GuiComponentTab(int x, int y, int w, int h, String name, @Nullable IData data, int styleID)
	{
		super(x, y, name, w, h);

		this.data = data;
		this.styleID = styleID;
	}

	@ZenMethod()
	@ZenDoc("Creates a new Gui Component instance")
	public static GuiComponentTab create(int x, int y, String name, IData data)
	{
		GuiNBTData map = new GuiNBTData(data);
		boolean vertical = map.getProperty("vertical");

		return new GuiComponentTab(x, y,
				vertical?32: 28,
				vertical?12: 24,
				name,
				map.get("display"),
				map.getStyle()
		);
	}

	@SideOnly(Side.CLIENT)
	@Override
	@Nullable
	public Gui provide(int id, int x, int y, MultiblockGui gui)
	{
		return new GuiButtonCTMBTab(this, id, this.x+x, this.y+y, w, h, gui.getStyle(), styleID, data);
	}
}
