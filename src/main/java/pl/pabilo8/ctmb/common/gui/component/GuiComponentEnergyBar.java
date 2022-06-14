package pl.pabilo8.ctmb.common.gui.component;

import crafttweaker.annotations.ZenDoc;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.pabilo8.ctmb.client.gui.MultiblockGui;
import pl.pabilo8.ctmb.client.gui.elements.GuiEnergyBar;
import pl.pabilo8.ctmb.common.CommonUtils;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nullable;
import java.util.Map;

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
		int styleID = 0, id = 0;
		int w = 12, h = 48;

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

		return new GuiComponentEnergyBar(x, y, w, h, name, id, styleID);
	}

	@SideOnly(Side.CLIENT)
	@Override
	@Nullable
	public Gui provide(int id, int x, int y, MultiblockGui gui)
	{
		return new GuiEnergyBar(this, id, this.x+x, this.y+y, w, h, gui.getStyle(), () -> gui.getTile().energy[this.id], styleID);
	}

}
