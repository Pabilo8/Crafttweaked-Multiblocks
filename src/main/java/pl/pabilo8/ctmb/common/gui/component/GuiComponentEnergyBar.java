package pl.pabilo8.ctmb.common.gui.component;

import crafttweaker.annotations.ZenDoc;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.pabilo8.ctmb.client.gui.MultiblockGui;
import pl.pabilo8.ctmb.client.gui.elements.GuiEnergyBar;
import pl.pabilo8.ctmb.common.util.GuiNBTData;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nullable;

/**
 * @author Pabilo8
 * @since 04.03.2022
 */
@ZenClass("mods.ctmb.gui.component.EnergyBar")
@ZenRegister
public class GuiComponentEnergyBar extends GuiComponent
{
	private final int id;
	private final int styleID;

	public GuiComponentEnergyBar(int x, int y, int w, int h, String name, int id, int styleID)
	{
		super(x, y, name, w, h);
		this.id = id;
		this.styleID = styleID;
	}

	@ZenMethod()
	@ZenDoc("Creates a new Gui Component instance")
	public static GuiComponentEnergyBar create(int x, int y, String name, IData data)
	{
		GuiNBTData map = new GuiNBTData(data);

		return new GuiComponentEnergyBar(x, y,
				map.getWidth(12), map.getHeight(48),
				name,
				map.getID(),
				map.getStyle()
		);
	}

	@SideOnly(Side.CLIENT)
	@Override
	@Nullable
	public Gui provide(int id, int x, int y, MultiblockGui gui)
	{
		return new GuiEnergyBar(this, id, this.x+x, this.y+y, w, h, gui.getStyle(), () -> gui.getTile().energy[this.id], styleID);
	}

}
