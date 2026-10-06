package pl.pabilo8.ctmb.common.gui.component;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.player.IPlayer;
import pl.pabilo8.ctmb.common.block.TileEntityMultiblock;
import pl.pabilo8.ctmb.common.block.crafttweaker.Multiblock;
import pl.pabilo8.ctmb.common.block.crafttweaker.MultiblockTileCTWrapper;
import pl.pabilo8.ctmb.common.gui.DecoComponentAccess;
import pl.pabilo8.ctmb.common.gui.MultiblockGuiCTWrapper;
import pl.pabilo8.ctmb.common.storage.StorageAccess;
import pl.pabilo8.ctmb.common.storage.StorageDefinition;
import pl.pabilo8.immersiveintelligence.common.util.easynbt.EasyNBT;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * Fluent, common-side Deco component definition; native components are per-screen instances.
 */
@ZenRegister
@ZenClass("mods.ctmb.gui.Component")
public final class GuiComponent
{
	private String name = "";
	private final String type;
	private int x, y;
	private final EasyNBT options = EasyNBT.newNBT();
	private StorageAccess source;
	private pl.pabilo8.ctmb.common.production.ProductionAccess production;
	private Event hover, press;
	private boolean frozen;

	private GuiComponent(String type, int x, int y)
	{
		this.type = type;
		this.x = x;
		this.y = y;
	}

	@ZenMethod
	public static GuiComponent label(@Optional int x, @Optional int y)
	{
		return new GuiComponent("label", x, y);
	}

	@ZenMethod
	public static GuiComponent button(@Optional int x, @Optional int y)
	{
		return new GuiComponent("button", x, y);
	}

	@ZenMethod
	public static GuiComponent tab(@Optional int x, @Optional int y)
	{
		return new GuiComponent("tab", x, y);
	}

	@ZenMethod
	public static GuiComponent checkbox(@Optional int x, @Optional int y)
	{
		return new GuiComponent("checkbox", x, y);
	}

	@ZenMethod("switch")
	public static GuiComponent toggle(@Optional int x, @Optional int y)
	{
		return new GuiComponent("switch", x, y);
	}

	@ZenMethod
	public static GuiComponent dropdown(@Optional int x, @Optional int y)
	{
		return new GuiComponent("dropdown", x, y);
	}

	@ZenMethod
	public static GuiComponent text(@Optional int x, @Optional int y)
	{
		return new GuiComponent("text", x, y);
	}

	@ZenMethod
	public static GuiComponent fluidTank(@Optional int x, @Optional int y)
	{
		return new GuiComponent("fluid", x, y);
	}

	@ZenMethod
	public static GuiComponent dustTank(@Optional int x, @Optional int y)
	{
		return new GuiComponent("dust", x, y);
	}

	@ZenMethod
	public static GuiComponent energyBar(@Optional int x, @Optional int y)
	{
		return new GuiComponent("energy", x, y);
	}

	@ZenMethod
	public static GuiComponent bar(@Optional int x, @Optional int y)
	{
		return new GuiComponent("bar", x, y);
	}

	@ZenMethod
	public GuiComponent withID(String name)
	{
		mutable();
		if(name==null||name.isEmpty()) throw new IllegalArgumentException("Empty component ID");
		this.name = name;
		return this;
	}

	@ZenMethod
	public GuiComponent withSize(int width, int height)
	{
		mutable();
		if(width < 1||height < 1) throw new IllegalArgumentException("Positive component dimensions required");
		options.withInt("w", width).withInt("h", height);
		return this;
	}

	@ZenMethod
	public GuiComponent withText(String text)
	{
		mutable();
		options.withString("text", text).withBoolean("translated", true);
		return this;
	}

	@ZenMethod
	public GuiComponent withText(Multiblock mb)
	{
		return withText("desc.immersiveengineering.info.multiblock."+mb.getUniqueName());
	}

	@ZenMethod
	public GuiComponent withText(IItemStack stack)
	{
		mutable();
		options.withItemStack("text_item", CraftTweakerMC.getItemStack(stack));
		return this;
	}

	@ZenMethod
	public GuiComponent withRawText(String text)
	{
		mutable();
		options.withString("text", text).withBoolean("translated", false);
		return this;
	}

	@ZenMethod
	public GuiComponent withEntries(String... entries)
	{
		mutable();
		if(!type.equals("dropdown")) throw new IllegalArgumentException("Entries require a dropdown");
		options.withList("entries", (Object[])entries);
		return this;
	}

	@ZenMethod
	public GuiComponent withSelected(int selected)
	{
		mutable();
		options.withInt("selected", selected);
		return this;
	}

	@ZenMethod
	public GuiComponent withState(boolean state)
	{
		mutable();
		options.withBoolean("state", state).withBoolean("checked", state);
		return this;
	}

	@ZenMethod
	public GuiComponent withLimits(int min, int max)
	{
		mutable();
		if(max <= min) throw new IllegalArgumentException("Maximum must exceed minimum");
		options.withInt("min", min).withInt("max", max).withInt("value", min);
		return this;
	}

	@ZenMethod
	public GuiComponent withValue(int value)
	{
		mutable();
		options.withInt("value", value);
		return this;
	}

	@ZenMethod
	public GuiComponent withColour(int rgb)
	{
		mutable();
		options.withInt("color", rgb);
		return this;
	}

	@ZenMethod
	public GuiComponent withDataSource(StorageAccess source)
	{
		mutable();
		StorageDefinition.Kind expected;
		switch(type)
		{
			case "fluid":
				expected = StorageDefinition.Kind.FLUID;
				break;
			case "dust":
				expected = StorageDefinition.Kind.DUST;
				break;
			case "energy":
				expected = StorageDefinition.Kind.ENERGY;
				break;
			default:
				throw new IllegalArgumentException("Component "+type+" does not accept a storage data source");
		}
		source.require(expected);
		this.source = source;
		return this;
	}

	@ZenMethod
	public GuiComponent withDataSource(pl.pabilo8.ctmb.common.production.ProductionAccess production)
	{
		mutable();
		if(!type.equals("bar")||production==null)
			throw new IllegalArgumentException("Production sources require a progress bar");
		this.production = production;
		return this;
	}

	public pl.pabilo8.ctmb.common.production.ProductionAccess production()
	{
		return production;
	}

	@ZenMethod
	public GuiComponent withOnHover(Event event)
	{
		mutable();
		hover = event;
		return this;
	}

	@ZenMethod
	public GuiComponent withOnPress(Event event)
	{
		mutable();
		press = event;
		return this;
	}

	public void validate(TileEntityMultiblock tile)
	{
		if((type.equals("fluid")||type.equals("dust")||type.equals("energy"))&&source==null)
			throw new IllegalArgumentException(type+" requires withDataSource(storage)");
		if(source!=null&&source.system!=tile.getStorageSystem())
			throw new IllegalArgumentException("Component source belongs to another machine");
		if(production!=null&&production.storage!=tile.getStorageSystem())
			throw new IllegalArgumentException("Production source belongs to another machine");
		if(type.equals("bar")&&!options.hasKey("max"))
		{
			options.withInt("min", 0).withInt("max", 100);
			if(!options.hasKey("value")) options.withInt("value", 0);
		}
	}

	public void freeze()
	{
		frozen = true;
	}

	private void mutable()
	{
		if(frozen) throw new IllegalStateException("Component is already part of a GUI plan");
	}

	public String getName()
	{
		return name;
	}

	public String getType()
	{
		return type;
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	public void translate(int x, int y)
	{
		this.x += x;
		this.y += y;
	}

	public EasyNBT getOptions()
	{
		return options;
	}

	public StorageAccess source()
	{
		return source;
	}

	public Event hover()
	{
		return hover;
	}

	public Event press()
	{
		return press;
	}

	public int width()
	{
		if(options.hasKey("w")) return options.getInt("w");
		switch(type)
		{
			case "fluid":
			case "dust":
				return 16;
			case "energy":
			case "bar":
				return 12;
			case "label":
				return 120;
			default:
				return 120;
		}
	}

	public int height()
	{
		if(options.hasKey("h")) return options.getInt("h");
		switch(type)
		{
			case "fluid":
			case "dust":
			case "energy":
			case "bar":
				return 64;
			case "label":
			case "switch":
			case "checkbox":
				return 11;
			default:
				return 16;
		}
	}

	@ZenRegister
	@ZenClass("mods.ctmb.gui.ComponentEvent")
	public interface Event
	{
		void execute(DecoComponentAccess component, MultiblockGuiCTWrapper gui, MultiblockTileCTWrapper mb, int mx, int my, IPlayer player);
	}
}
