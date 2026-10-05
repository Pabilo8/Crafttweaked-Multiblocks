package pl.pabilo8.ctmb.common.gui;

import crafttweaker.annotations.ZenRegister;
import net.minecraft.util.math.MathHelper;
import pl.pabilo8.ctmb.common.block.TileEntityMultiblock;
import pl.pabilo8.ctmb.common.gui.component.GuiComponent;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.*;

/**
 * Side-neutral per-container plan produced by Gui.onInit.
 */
@ZenRegister
@ZenClass("mods.ctmb.gui.Builder")
public final class MultiblockGuiLayout
{
	public final GuiDefinition definition;
	public final TileEntityMultiblock tile;
	public final List<BackgroundBox> boxes = new ArrayList<>();
	public final List<SlotDefinition> slots = new ArrayList<>();
	public final Map<String, GuiComponent> components = new LinkedHashMap<>();
	public final List<GuiDefinition> links = new ArrayList<>();
	public boolean playerInventory;
	public int inventoryX, inventoryY;
	public String inventoryStyle = SlotStyle.VANILLA;
	private boolean finished;
	private int width, height;

	MultiblockGuiLayout(GuiDefinition definition, TileEntityMultiblock tile)
	{
		this.definition = definition;
		this.tile = tile;
	}

	@ZenMethod
	public GuiBackgroundBuilder addBackground()
	{
		mutable();
		return new GuiBackgroundBuilder(this);
	}

	@ZenMethod
	public MultiblockGuiLayout addComponents(GuiComponent... definitions)
	{
		mutable();
		for(GuiComponent component : definitions)
		{
			String name = component.getName().isEmpty()?"@"+components.size(): component.getName();
			if(components.putIfAbsent(name, component)!=null)
				throw new IllegalArgumentException("Duplicate component ID "+name);
			component.validate(tile);
			component.freeze();
		}
		return this;
	}

	@ZenMethod
	public MultiblockGuiLayout addLinkTab(GuiDefinition target)
	{
		mutable();
		if(target==null) throw new IllegalArgumentException("Unknown linked GUI");
		target.bind(tile.getMultiblock());
		if(links.contains(target)) throw new IllegalArgumentException("Duplicate GUI link "+target.name);
		links.add(target);
		return this;
	}

	public void finish()
	{
		if(boxes.isEmpty())
			throw new IllegalArgumentException(definition.name+": At least one background box is required");
		Set<Integer> used = new HashSet<>();
		for(SlotDefinition slot : slots)
			if(!used.add(tile.getStorageSystem().flatSlot(slot.storage, slot.slot)))
				throw new IllegalArgumentException("Duplicate GUI inventory slot");
		int minX = boxes.stream().mapToInt(b -> b.x).min().getAsInt(), minY = boxes.stream().mapToInt(b -> b.y).min().getAsInt();
		// Native Deco has no origin-offset field: normalise the whole plan to its background extent.
		for(BackgroundBox box : boxes)
		{
			box.x -= minX;
			box.y -= minY;
			width = Math.max(width, box.x+box.width);
			height = Math.max(height, box.y+box.height);
		}
		for(SlotDefinition slot : slots)
		{
			slot.x -= minX;
			slot.y -= minY;
			checkInside(slot.x, slot.y, 16, 16, "slot");
		}
		inventoryX -= minX;
		inventoryY -= minY;
		if(playerInventory) checkInside(inventoryX, inventoryY, 162, 76, "player inventory");
		for(GuiComponent component : components.values())
		{
			component.translate(-minX, -minY);
			checkInside(component.getX(), component.getY(), component.width(), component.height(), "component");
		}
		finished = true;
	}

	private void checkInside(int x, int y, int w, int h, String name)
	{
		if(x < 0||y < 0||x+w > width||y+h > height)
			throw new IllegalArgumentException(definition.name+": "+name+" falls outside the background boxes");
	}

	public int getWidth()
	{
		return width;
	}

	public int getHeight()
	{
		return height;
	}

	public Map<String, GuiComponent> getComponents()
	{
		return Collections.unmodifiableMap(components);
	}

	public void mutable()
	{
		if(finished) throw new IllegalStateException("The GUI plan is already built");
	}

	/**
	 * Structural signature excludes live values, but includes every inventory mapping and coordinate.
	 */
	public String signature()
	{
		StringBuilder out = new StringBuilder(definition.name).append('|').append(width).append(',').append(height);
		for(SlotDefinition slot : slots)
			out.append('|').append(slot.storage).append(':').append(slot.slot).append(':').append(slot.x).append(',').append(slot.y);
		out.append('|').append(playerInventory).append(':').append(inventoryX).append(',').append(inventoryY);
		return out.toString();
	}

	public static final class BackgroundBox
	{
		public final DecoTexture texture, mask;
		public int x, y;
		public final int width, height;
		public String title;
		public boolean inventoryTitle;

		BackgroundBox(DecoTexture texture, DecoTexture mask, int x, int y, int width, int height)
		{
			if(width <= 0||height <= 0) throw new IllegalArgumentException("Positive background dimensions required");
			this.texture = texture;
			this.mask = mask;
			this.x = x;
			this.y = y;
			this.width = MathHelper.ceil(width/8.0)*8;
			this.height = MathHelper.ceil(height/8.0)*8;
		}
	}

	public static final class SlotDefinition
	{
		public final String storage, style;
		public final int slot;
		public int x, y;

		SlotDefinition(String storage, int slot, String style, int x, int y)
		{
			this.storage = storage;
			this.slot = slot;
			this.style = SlotStyle.validate(style);
			this.x = x;
			this.y = y;
		}
	}
}
