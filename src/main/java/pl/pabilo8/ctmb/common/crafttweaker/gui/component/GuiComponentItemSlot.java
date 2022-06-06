package pl.pabilo8.ctmb.common.crafttweaker.gui.component;

import crafttweaker.annotations.ZenDoc;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import net.minecraft.client.gui.Gui;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.pabilo8.ctmb.client.gui.MultiblockGui;
import pl.pabilo8.ctmb.common.CommonUtils;
import pl.pabilo8.ctmb.common.crafttweaker.gui.MultiblockContainer;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author Pabilo8
 * @since 04.03.2022
 */
@ZenClass("mods.ctmb.gui.component.ItemSlot")
@ZenRegister
public class GuiComponentItemSlot extends GuiComponent
{
	private final int id;
	private final int styleID;
	public GuiComponentItemSlot(int x, int y, int w, int h, String name, int id, int styleID)
	{
		super(x, y, name, w, h);
		this.id = id;
		this.styleID = styleID;
	}

	@ZenMethod()
	@ZenDoc("Creates a new Gui Component instance")
	public static GuiComponentItemSlot create(int x, int y, String name, IData data)
	{
		int styleID = 0, id=0;
		int w = 20, h = 20;

		if(CommonUtils.dataCheck(data))
		{
			Map<String, IData> map = data.asMap();

			if(map.containsKey("id"))
				id = map.get("id").asInt();

			if(map.containsKey("style_id"))
				styleID = map.get("style_id").asInt();

			if(map.containsKey("width"))
				w = map.get("width").asInt();
			if(map.containsKey("height"))
				h = map.get("height").asInt();

		}

		return new GuiComponentItemSlot(x, y, w, h, name, id, styleID);
	}

	@SideOnly(Side.CLIENT)
	@Override
	@Nullable
	public Gui provide(int id, int x, int y, MultiblockGui gui)
	{
		return null;
	}

	@Nullable
	@Override
	public Slot[] provideSlots(MultiblockContainer gui)
	{
		assert gui.inv!=null;
		return new Slot[]{new Slot(gui.inv, id, x, y)};
	}
}
