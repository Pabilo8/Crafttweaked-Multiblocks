package pl.pabilo8.ctmb.common.crafttweaker.gui.component;

import crafttweaker.annotations.ZenDoc;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.pabilo8.ctmb.client.gui.MultiblockGui;
import pl.pabilo8.ctmb.client.gui.elements.GuiFluidTankCTMB;
import pl.pabilo8.ctmb.common.CommonUtils;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nullable;
import java.util.Map;

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
		int styleID = 0, id = 0;
		int w = 24, h = 51;

		if(CommonUtils.dataCheck(data))
		{
			Map<String, IData> map = data.asMap();

			if(map.containsKey("id"))
				id = map.get("id").asInt();
			if(map.containsKey("id"))
				id = map.get("id").asInt();

			if(map.containsKey("w"))
				w = map.get("w").asInt();
			if(map.containsKey("h"))
				h = map.get("h").asInt();

			if(map.containsKey("style_id"))
				styleID = map.get("style_id").asInt();

		}

		return new GuiComponentFluidTank(x, y, w, h, name, id, styleID);
	}

	@SideOnly(Side.CLIENT)
	@Override
	@Nullable
	public Gui provide(int id, int x, int y, MultiblockGui gui)
	{
		return new GuiFluidTankCTMB(this, id, this.x+x, this.y+y, w, h, () -> gui.getTile().tanks[this.id], gui.getStyle(), styleID);
	}

}
