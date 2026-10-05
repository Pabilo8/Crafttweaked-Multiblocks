package pl.pabilo8.ctmb.common.gui;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.player.IPlayer;
import pl.pabilo8.ctmb.common.block.TileEntityMultiblock;
import pl.pabilo8.ctmb.common.block.crafttweaker.Multiblock;
import pl.pabilo8.ctmb.common.block.crafttweaker.MultiblockTileCTWrapper;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Registered named GUI; its common initializer creates a fresh plan for every container.
 */
@ZenRegister
@ZenClass("mods.ctmb.gui.Gui")
public final class GuiDefinition
{
	private static final Map<String, GuiDefinition> REGISTRY = new LinkedHashMap<>();
	public final String name;
	private final int page;
	private Multiblock owner;
	private Init init;
	private String title;
	private DecoTexture icon = DecoTextures.ICON_STORAGE;

	private GuiDefinition(String name)
	{
		this.name = new net.minecraft.util.ResourceLocation(name).toString();
		page = REGISTRY.size()+1;
		title = this.name;
	}

	@ZenMethod
	public static GuiDefinition create(String name)
	{
		GuiDefinition gui = new GuiDefinition(name);
		if(REGISTRY.putIfAbsent(gui.name, gui)!=null) throw new IllegalArgumentException("Duplicate GUI "+name);
		return gui;
	}

	@ZenMethod
	public GuiDefinition onInit(Init init)
	{
		this.init = Objects.requireNonNull(init);
		return this;
	}

	@ZenMethod
	public GuiDefinition withTitle(String title)
	{
		this.title = title;
		return this;
	}

	@ZenMethod
	public GuiDefinition withIcon(DecoTexture icon)
	{
		this.icon = Objects.requireNonNull(icon);
		return this;
	}

	public String title()
	{
		return title;
	}

	public DecoTexture icon()
	{
		return icon;
	}

	public int page()
	{
		return page;
	}

	public void bind(Multiblock multiblock)
	{
		if(owner!=null&&owner!=multiblock)
			throw new IllegalArgumentException("GUI "+name+" already belongs to "+owner.getUniqueName());
		owner = multiblock;
	}

	public boolean isBoundTo(Multiblock multiblock)
	{
		return owner==multiblock;
	}

	public MultiblockGuiLayout build(TileEntityMultiblock tile, IPlayer player)
	{
		if(init==null) throw new IllegalStateException("GUI "+name+" has no onInit callback");
		MultiblockGuiLayout layout = new MultiblockGuiLayout(this, tile);
		init.execute(layout, tile.getMbWrapper(), player);
		layout.finish();
		return layout;
	}

	public static GuiDefinition find(String name)
	{
		return REGISTRY.get(name);
	}

	public static GuiDefinition require(String name)
	{
		GuiDefinition gui = find(name);
		if(gui==null) throw new IllegalArgumentException("Unknown GUI "+name);
		return gui;
	}

	public static GuiDefinition forPage(int page)
	{
		for(GuiDefinition gui : REGISTRY.values()) if(gui.page==page) return gui;
		return null;
	}

	@ZenRegister
	@ZenClass("mods.ctmb.gui.Init")
	public interface Init
	{
		void execute(MultiblockGuiLayout gui, MultiblockTileCTWrapper mb, IPlayer player);
	}
}
