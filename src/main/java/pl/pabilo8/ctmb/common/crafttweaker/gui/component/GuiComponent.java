package pl.pabilo8.ctmb.common.crafttweaker.gui.component;

import crafttweaker.annotations.ZenRegister;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.pabilo8.ctmb.client.gui.MultiblockGui;
import pl.pabilo8.ctmb.common.crafttweaker.gui.MultiblockContainer;
import stanhebben.zenscript.annotations.ZenClass;

import javax.annotation.Nullable;

/**
 * @author Pabilo8
 * @since 25.02.2022
 */
@ZenClass("mods.ctmb.gui.Component")
@ZenRegister
public abstract class GuiComponent
{
	public final int x, y;
	public final int w, h;
	public final String name;

	public GuiComponent(int x, int y, String name, int w, int h)
	{
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.name = name;
	}

	/**
	 * @param id  id of the button to be created
	 * @param x
	 * @param y
	 * @param gui
	 * @return a new gui element instance to be added into a GUI
	 */
	@SideOnly(Side.CLIENT)
	@Nullable
	public abstract Gui provide(int id, int x, int y, MultiblockGui gui);

	@Nullable
	public Slot[] provideSlots(MultiblockContainer gui, InventoryPlayer inventoryPlayer)
	{
		return null;
	}
}
