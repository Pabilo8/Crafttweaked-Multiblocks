package pl.pabilo8.ctmb.common.gui.component;

import crafttweaker.annotations.ZenRegister;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.pabilo8.ctmb.client.gui.IComponentGui;
import pl.pabilo8.ctmb.client.gui.StyledGuiUtils;
import pl.pabilo8.ctmb.common.gui.IEditablePart;
import pl.pabilo8.ctmb.common.gui.MultiblockContainer;
import stanhebben.zenscript.annotations.ZenClass;

import javax.annotation.Nullable;

/**
 * @author Pabilo8
 * @since 25.02.2022
 */
@ZenClass("mods.ctmb.gui.Component")
@ZenRegister
public abstract class GuiComponent implements IEditablePart
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
	 * @param id  id of the component to be created
	 * @param x   the horizontal position of the component
	 * @param y   the vertical position of the component
	 * @param gui the gui containing this component
	 * @return a new gui element instance to be added into a GUI
	 */
	@SideOnly(Side.CLIENT)
	@Nullable
	public abstract Gui provide(int id, int x, int y, IComponentGui gui);

	@Nullable
	public Slot[] provideSlots(MultiblockContainer gui, InventoryPlayer inventoryPlayer)
	{
		return null;
	}

	@SideOnly(Side.CLIENT)
	protected final String getTranslation(boolean translated, IComponentGui gui, String text)
	{
		return translated?I18n.format(StyledGuiUtils.processText(gui, text)): StyledGuiUtils.processText(gui, text);
	}

	@Override
	public int getX()
	{
		return x;
	}

	@Override
	public int getY()
	{
		return y;
	}

	@Override
	public int getW()
	{
		return w;
	}

	@Override
	public int getH()
	{
		return h;
	}
}
