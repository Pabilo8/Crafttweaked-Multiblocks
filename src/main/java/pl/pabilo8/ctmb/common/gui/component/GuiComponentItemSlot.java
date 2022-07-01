package pl.pabilo8.ctmb.common.gui.component;

import crafttweaker.annotations.ZenDoc;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.pabilo8.ctmb.client.gui.MultiblockGui;
import pl.pabilo8.ctmb.common.gui.CTMBSlot;
import pl.pabilo8.ctmb.common.gui.MultiblockContainer;
import pl.pabilo8.ctmb.common.util.GuiNBTData;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nullable;

/**
 * @author Pabilo8
 * @since 04.03.2022
 */
@ZenClass("mods.ctmb.gui.component.ItemSlot")
@ZenRegister
public class GuiComponentItemSlot extends GuiComponent
{
	//inventoryID - -1 is the player, 0... is the TileEntity
	private final int id, inventoryID;
	//0 - default, 1 - framed, 2 - input, 3 - output
	private final int styleID;

	public GuiComponentItemSlot(int x, int y, String name, int inventoryID, int id, int styleID)
	{
		super(x, y, name, 20, 20);
		this.inventoryID = inventoryID;
		this.id = id;
		this.styleID = styleID;
	}

	@ZenMethod()
	@ZenDoc("Creates a new Gui Component instance")
	public static GuiComponentItemSlot create(int x, int y, String name, IData data)
	{
		GuiNBTData map = new GuiNBTData(data);

		return new GuiComponentItemSlot(x, y,
				name,
				map.getInt("inv_id", 0),
				map.getID(),
				map.getStyle()
		);
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
	public Slot[] provideSlots(MultiblockContainer gui, InventoryPlayer inventoryPlayer)
	{
		assert gui.inv!=null;

		return new Slot[]{
				new CTMBSlot(inventoryID==-1?inventoryPlayer: gui.inv,
						inventoryID!=-1?(gui.tile.getMultiblock().inventory.get(inventoryID).getOffset()+id): id,
						x, y, styleID)
		};
	}
}
