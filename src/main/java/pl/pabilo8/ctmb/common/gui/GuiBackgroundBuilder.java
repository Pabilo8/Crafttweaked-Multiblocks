package pl.pabilo8.ctmb.common.gui;

import crafttweaker.annotations.ZenRegister;
import pl.pabilo8.ctmb.common.storage.StorageAccess;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.List;

@ZenRegister
@ZenClass("mods.ctmb.gui.BackgroundBuilder")
public final class GuiBackgroundBuilder
{
	private final MultiblockGuiLayout layout;
	private MultiblockGuiLayout.BackgroundBox last;

	GuiBackgroundBuilder(MultiblockGuiLayout layout)
	{
		this.layout = layout;
	}

	@ZenMethod
	public GuiBackgroundBuilder withBox(DecoTexture texture, int x, int y, int width, int height)
	{
		return withBox(texture, DecoTextures.TEMPLATE_ROUND, x, y, width, height);
	}

	@ZenMethod
	public GuiBackgroundBuilder withBox(DecoTexture texture, DecoTexture mask, int x, int y, int width, int height)
	{
		layout.mutable();
		last = new MultiblockGuiLayout.BackgroundBox(texture, mask, x, y, width, height);
		layout.boxes.add(last);
		return this;
	}

	private MultiblockGuiLayout.BackgroundBox box()
	{
		layout.mutable();
		if(last==null) throw new IllegalStateException("Add a background box first");
		return last;
	}

	@ZenMethod
	public GuiBackgroundBuilder withSlot(int slot, SlotStyle style, int x, int y)
	{
		List<StorageAccess> items = layout.tile.getStorageSystem().items();
		if(items.size()!=1)
			throw new IllegalArgumentException("withSlot shorthand requires exactly one item provider; use the storage overload");
		return withSlot(items.get(0), slot, style, x, y);
	}

	@ZenMethod
	public GuiBackgroundBuilder withSlot(StorageAccess storage, int slot, SlotStyle style, int x, int y)
	{
		MultiblockGuiLayout.BackgroundBox box = box();
		if(storage.system!=layout.tile.getStorageSystem())
			throw new IllegalArgumentException("GUI slot storage belongs to a different machine");
		layout.tile.getStorageSystem().flatSlot(storage.getName(), slot);
		layout.slots.add(new MultiblockGuiLayout.SlotDefinition(storage.getName(), slot, style.nativeName(), box.x+x, box.y+y));
		return this;
	}

	@ZenMethod
	public GuiBackgroundBuilder withPlayerInventory(SlotStyle style, int x, int y)
	{
		MultiblockGuiLayout.BackgroundBox box = box();
		if(layout.playerInventory) throw new IllegalArgumentException("Player inventory already added");
		layout.playerInventory = true;
		layout.inventoryStyle = style.nativeName();
		layout.inventoryX = box.x+x;
		layout.inventoryY = box.y+y;
		return this;
	}

	@ZenMethod
	public GuiBackgroundBuilder withTitleBar()
	{
		box().title = "@machine";
		return this;
	}

	@ZenMethod
	public GuiBackgroundBuilder withTitleBar(String title)
	{
		box().title = title;
		return this;
	}

	@ZenMethod
	public GuiBackgroundBuilder withInventoryTitleBar()
	{
		box().inventoryTitle = true;
		return this;
	}

	@ZenMethod
	public void build()
	{
		box();
	}
}
