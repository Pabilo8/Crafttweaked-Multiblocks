package pl.pabilo8.ctmb.common.gui.component;

import crafttweaker.annotations.ZenDoc;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.pabilo8.ctmb.client.gui.IComponentGui;
import pl.pabilo8.ctmb.client.gui.elements.GuiFluidTankCTMB;
import pl.pabilo8.ctmb.common.util.GuiNBTData;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nullable;

/**
 * @author Pabilo8
 * @since 04.03.2022
 */
@ZenClass("mods.ctmb.gui.component.FluidTank")
@ZenRegister
public class GuiComponentFluidTank extends GuiComponent
{
	private final int id;
	private final int styleID;

	public GuiComponentFluidTank(int x, int y, int w, int h, String name, int id, int styleID)
	{
		super(x, y, name, w, h);
		this.id = id;
		this.styleID = styleID;
	}

	@ZenMethod()
	@ZenDoc("Creates a new Gui Component instance")
	public static GuiComponentFluidTank create(int x, int y, String name, IData data)
	{
		GuiNBTData map = new GuiNBTData(data);

		return new GuiComponentFluidTank(x, y,
				map.getWidth(12), map.getHeight(48),
				name,
				map.getID(),
				map.getStyle()
		);
	}

	@SideOnly(Side.CLIENT)
	@Override
	@Nullable
	public Gui provide(int id, int x, int y, IComponentGui gui)
	{
		return new GuiFluidTankCTMB(this, id, this.x+x, this.y+y, w, h,
				() -> gui.hasTile()?gui.getTile().tanks[this.id]:(new FluidTank(0)),
				gui.getStyle(), styleID
		);
	}

}
