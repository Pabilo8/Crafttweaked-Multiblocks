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
	private final java.util.List<GuiComponent> bars = new java.util.ArrayList<>();

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
	public static GuiComponent barGroup(@Optional int x, @Optional int y)
	{
		return new GuiComponent("bar_group", x, y);
	}

	@ZenMethod
	public static GuiComponent image(@Optional int x, @Optional int y)
	{
		return new GuiComponent("image", x, y);
	}

	@ZenMethod
	public static GuiComponent gauge(@Optional int x, @Optional int y)
	{
		return new GuiComponent("gauge", x, y);
	}

	@ZenMethod
	public GuiComponent withBar(GuiComponent bar)
	{
		return withBars(bar);
	}

	@ZenMethod
	public GuiComponent withBars(GuiComponent... children)
	{
		mutable();
		if(!type.equals("bar_group")) throw new IllegalArgumentException("withBars requires a bar group");
		for(GuiComponent child : children)
		{
			if(child==null||!(child.type.equals("bar")||child.type.equals("energy")))
				throw new IllegalArgumentException("Bar groups contain bars or energy bars");
			if(child.frozen||bars.contains(child)) throw new IllegalArgumentException("Bar is already attached");
			bars.add(child);
		}
		return this;
	}

	@ZenMethod
	public GuiComponent withImage(pl.pabilo8.ctmb.common.gui.DecoTexture texture)
	{
		mutable();
		if(!type.equals("image")||texture==null||texture.location.endsWith("/"))
			throw new IllegalArgumentException("Images require a registered texture, not a texture directory");
		options.withString("image", texture.location);
		return this;
	}

	@ZenMethod
	public GuiComponent withUV(float textureSize, float u, float v, float uu, float vv)
	{
		mutable();
		if(!type.equals("image")||!Float.isFinite(textureSize)||textureSize <= 0
				||!Float.isFinite(u)||!Float.isFinite(v)||!Float.isFinite(uu)||!Float.isFinite(vv))
			throw new IllegalArgumentException("Finite image UVs and a positive texture size required");
		options.withFloat("texture_size", textureSize).withFloat("u", u).withFloat("v", v).withFloat("uu", uu).withFloat("vv", vv);
		return this;
	}

	@ZenMethod
	public GuiComponent withRotation(float angle)
	{
		mutable();
		if(!type.equals("image")||!Float.isFinite(angle))
			throw new IllegalArgumentException("Image rotation must be finite");
		options.withFloat("rotation", angle);
		return this;
	}

	@ZenMethod
	public GuiComponent withRange(float minimum, float maximum)
	{
		mutable();
		if(!type.equals("gauge")||!Float.isFinite(minimum)||!Float.isFinite(maximum)||maximum <= minimum)
			throw new IllegalArgumentException("Gauge maximum must exceed its finite minimum");
		options.withFloat("angle_min", minimum).withFloat("angle_max", maximum);
		return this;
	}

	@ZenMethod
	public GuiComponent withAngle(float angle)
	{
		mutable();
		if(!type.equals("gauge")||!Float.isFinite(angle))
			throw new IllegalArgumentException("Gauge angle must be finite");
		options.withFloat("angle", angle);
		return this;
	}

	@ZenMethod
	public GuiComponent withInverted(boolean inverted)
	{
		mutable();
		options.withBoolean("inverted", inverted);
		return this;
	}

	@ZenMethod
	public GuiComponent withDisplayCross(boolean show)
	{
		mutable();
		options.withBoolean("display_cross", show);
		return this;
	}

	@ZenMethod
	public GuiComponent withDisplayValues(boolean show)
	{
		mutable();
		options.withBoolean("display_values", show);
		return this;
	}

	public java.util.List<GuiComponent> bars()
	{
		return java.util.Collections.unmodifiableList(bars);
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
		if(type.equals("image")&&!options.hasKey("image"))
			throw new IllegalArgumentException("Image requires withImage(<deco:...>)");
		if(type.equals("bar_group"))
		{
			if(bars.isEmpty()) throw new IllegalArgumentException("Bar group requires at least one bar");
			if(options.hasKey("w")||options.hasKey("h"))
				throw new IllegalArgumentException("Bar group size is determined by its bars");
			bars.forEach(bar -> {
				if(bar.hover!=null||bar.press!=null)
					throw new IllegalArgumentException("Attach mouse events to the bar group");
				bar.validate(tile);
			});
		}
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
		bars.forEach(GuiComponent::freeze);
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
			case "bar_group":
				return 6+bars.stream().mapToInt(GuiComponent::width).sum();
			case "gauge":
				return 64;
			case "image":
				return 16;
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
		if(type.equals("bar_group")) return bars.stream().mapToInt(GuiComponent::height).max().orElse(0);
		if(type.equals("gauge")) return 76;
		if(type.equals("image")) return 16;
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
