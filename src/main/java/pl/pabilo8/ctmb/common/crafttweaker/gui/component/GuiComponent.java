package pl.pabilo8.ctmb.common.crafttweaker.gui.component;

import crafttweaker.annotations.ZenRegister;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.pabilo8.ctmb.client.gui.MultiblockGui;
import pl.pabilo8.ctmb.client.gui.elements.IGuiTweakable;
import stanhebben.zenscript.annotations.ZenClass;

import javax.annotation.Nonnull;

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
	 * @param id id of the button to be created
	 * @param x
	 * @param y
	 * @param gui
	 * @return a new gui element instance to be added into a GUI
	 */
	@SideOnly(Side.CLIENT)
	@Nonnull
	public abstract Gui provide(int id, int x, int y, MultiblockGui gui);
}
