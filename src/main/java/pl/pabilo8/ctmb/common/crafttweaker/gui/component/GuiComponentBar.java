package pl.pabilo8.ctmb.common.crafttweaker.gui.component;

import crafttweaker.annotations.ZenDoc;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.pabilo8.ctmb.client.gui.MultiblockGui;
import pl.pabilo8.ctmb.client.gui.elements.GuiBar;
import pl.pabilo8.ctmb.common.CommonUtils;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nullable;
import java.util.Map;

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
		int styleID = 0, id = 0;
		int w = 12, h = 48;
		int color1 = 0xb51500, color2 = 0x600b00;

		if(CommonUtils.dataCheck(data))
		{
			Map<String, IData> map = data.asMap();

			if(map.containsKey("w"))
				w = map.get("w").asInt();
			if(map.containsKey("h"))
				h = map.get("h").asInt();

			if(map.containsKey("color1"))
				color1 = CommonUtils.getColorFromData(map.get("color1"));
			if(map.containsKey("color2"))
				color2 = CommonUtils.getColorFromData(map.get("color2"));

			if(map.containsKey("style_id"))
				styleID = map.get("style_id").asInt();
		}

		return new GuiComponentBar(x, y, w, h, name, color1, color2, styleID);
	}

	@SideOnly(Side.CLIENT)
	@Override
	@Nullable
	public Gui provide(int id, int x, int y, MultiblockGui gui)
	{
		return new GuiBar(this, id, this.x+x, this.y+y, w, h, gui.getStyle(), styleID,color1,color2);
	}

}
