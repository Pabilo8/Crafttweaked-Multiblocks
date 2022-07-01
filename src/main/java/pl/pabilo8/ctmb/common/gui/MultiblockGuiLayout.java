package pl.pabilo8.ctmb.common.gui;

import crafttweaker.annotations.ZenDoc;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.player.IPlayer;
import pl.pabilo8.ctmb.common.block.crafttweaker.MultiblockTileCTWrapper;
import pl.pabilo8.ctmb.common.gui.component.GuiComponent;
import pl.pabilo8.ctmb.common.gui.rectangle.GuiRectangle;
import pl.pabilo8.ctmb.common.gui.rectangle.GuiRectangleStyled;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @author Pabilo8
 * @since 25.02.2022
 */
@ZenClass("mods.ctmb.gui.Layout")
@ZenDoc("A Layout used when displaying a GUI for a CTMB multiblock")
@ZenRegister
public class MultiblockGuiLayout
{
	public final MultiblockGuiStyle style;

	public final List<GuiRectangle> rectangles = new ArrayList<>();
	public final Map<String, GuiComponent> components = new HashMap<>();

	public IMultiblockGuiEventOnComponent onPress, onHover;

	public IMultiblockGuiEventGeneral onOpen, onClose;

	public MultiblockGuiLayout(MultiblockGuiStyle style)
	{
		this.style = style;
	}

	@ZenMethod
	@ZenDoc("Creates a simple Gui Layout using the default generated style")
	public static MultiblockGuiLayout create(int w, int h)
	{
		MultiblockGuiLayout layout = new MultiblockGuiLayout(MultiblockGuiStyle.DEFAULT_SKIN);
		layout.rectangles.add(new GuiRectangleStyled(0, 0, w, h, null));
		return layout;
	}

	@ZenMethod
	@ZenDoc("Creates a new Gui Layout")
	public static MultiblockGuiLayout create(GuiRectangle[] rectangles, @Optional String styleName)
	{
		MultiblockGuiLayout layout = new MultiblockGuiLayout((styleName!=null&&MultiblockGuiStyle.STYLES.containsKey(styleName))?
				MultiblockGuiStyle.STYLES.get(styleName):
				MultiblockGuiStyle.DEFAULT_SKIN);
		layout.rectangles.addAll(Arrays.asList(rectangles));
		return layout;
	}

	@ZenMethod
	@ZenDoc("Adds a component to the Gui Layout")
	public void addComponent(GuiComponent component)
	{
		components.put(component.name, component);
	}

	@ZenMethod
	@ZenDoc("Adds a component to the Gui Layout")
	public void addComponents(GuiComponent... components)
	{
		for(GuiComponent component : components)
			addComponent(component);
	}

	@Nullable
	@ZenMethod
	@ZenDoc("Gets a component with given id from the Gui Layout")
	public GuiComponent getComponent(String id)
	{
		return components.get(id);
	}

	@ZenMethod
	@ZenDoc("Removes a component from the Gui Layout")
	public boolean removeComponent(String id)
	{
		return components.remove(id)!=null;
	}

	@ZenMethod
	@ZenDoc("Sets the function called by a MB gui when a component was activated.")
	public void setOnPress(IMultiblockGuiEventOnComponent event)
	{
		this.onPress = event;
	}

	@ZenMethod
	@ZenDoc("Sets the function called by a MB gui when a component was hovered over.")
	public void setOnHover(IMultiblockGuiEventOnComponent event)
	{
		this.onHover = event;
	}

	@ZenMethod
	@ZenDoc("Sets the function called by a MB gui after it is initialized.")
	public void setOnOpen(IMultiblockGuiEventGeneral onOpen)
	{
		this.onOpen = onOpen;
	}

	@ZenMethod
	@ZenDoc("Sets the function called by a MB gui before closing it.")
	public void setOnClose(IMultiblockGuiEventGeneral event)
	{
		this.onClose = event;
	}

	@ZenRegister
	@ZenClass(value = "mods.ctmb.gui.IMultiblockGuiEventOnComponent")
	public interface IMultiblockGuiEventOnComponent
	{
		void execute(String component, MultiblockGuiCTWrapper gui, MultiblockTileCTWrapper mb, int mx, int my, IPlayer player);
	}

	@ZenRegister
	@ZenClass(value = "mods.ctmb.gui.IMultiblockGuiEventGeneral")
	public interface IMultiblockGuiEventGeneral
	{
		void execute(MultiblockGuiCTWrapper gui, MultiblockTileCTWrapper mb, IPlayer player);
	}

}
