package pl.pabilo8.ctmb.common.gui;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.player.IPlayer;
import lombok.Getter;
import pl.pabilo8.ctmb.common.block.crafttweaker.MultiblockTileCTWrapper;
import pl.pabilo8.ctmb.common.gui.component.GuiComponent;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Defines the components and server slots of a Deco multiblock GUI.
 *
 * @author Pabilo8 (pabilo@iiteam.net)
 * @since 25.02.2022
 * @updated 05.10.2026
 */
@ZenRegister
@ZenClass("mods.ctmb.gui.Layout")
@Getter
public class MultiblockGuiLayout
{
	private final int width, height;
	private final Map<String, GuiComponent> components = new LinkedHashMap<>();
	private int inventoryX, inventoryY;
	private boolean playerInventory;
	private IMultiblockGuiEventOnComponent onPress, onHover;
	private IMultiblockGuiEventGeneral onOpen, onClose;

	private MultiblockGuiLayout(int width, int height)
	{
		if(width < 1||height < 1)
			throw new IllegalArgumentException("GUI dimensions must be positive");
		this.width = width;
		this.height = height;
	}

	/** Creates a Deco layout with the specified dimensions. */
	@ZenMethod
	public static MultiblockGuiLayout create(int width, int height)
	{
		return new MultiblockGuiLayout(width, height);
	}

	@ZenMethod
	public MultiblockGuiLayout addComponent(GuiComponent component)
	{
		if(components.putIfAbsent(component.getName(), component)!=null)
			throw new IllegalArgumentException("Duplicate component: "+component.getName());
		return this;
	}

	@ZenMethod
	public MultiblockGuiLayout addComponents(GuiComponent... components)
	{
		for(GuiComponent component : components)
			addComponent(component);
		return this;
	}

	@ZenMethod
	public GuiComponent getComponent(String name)
	{
		return components.get(name);
	}

	@ZenMethod
	public boolean removeComponent(String name)
	{
		return components.remove(name)!=null;
	}

	@ZenMethod
	public MultiblockGuiLayout withPlayerInventory(int x, int y)
	{
		playerInventory = true;
		inventoryX = x;
		inventoryY = y;
		return this;
	}

	@ZenMethod
	public void setOnPress(IMultiblockGuiEventOnComponent event) { onPress = event; }
	@ZenMethod
	public void setOnHover(IMultiblockGuiEventOnComponent event) { onHover = event; }
	@ZenMethod
	public void setOnOpen(IMultiblockGuiEventGeneral event) { onOpen = event; }
	@ZenMethod
	public void setOnClose(IMultiblockGuiEventGeneral event) { onClose = event; }

	@ZenRegister
	@ZenClass("mods.ctmb.gui.IMultiblockGuiEventOnComponent")
	public interface IMultiblockGuiEventOnComponent
	{
		void execute(String component, MultiblockGuiCTWrapper gui, MultiblockTileCTWrapper mb, int mx, int my, IPlayer player);
	}

	@ZenRegister
	@ZenClass("mods.ctmb.gui.IMultiblockGuiEventGeneral")
	public interface IMultiblockGuiEventGeneral
	{
		void execute(MultiblockGuiCTWrapper gui, MultiblockTileCTWrapper mb, IPlayer player);
	}
}
