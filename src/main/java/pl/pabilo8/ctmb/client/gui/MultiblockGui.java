package pl.pabilo8.ctmb.client.gui;

import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import org.lwjgl.input.Mouse;
import pl.pabilo8.ctmb.client.gui.deco.*;
import pl.pabilo8.ctmb.common.block.TileEntityMultiblock;
import pl.pabilo8.ctmb.common.gui.*;
import pl.pabilo8.ctmb.common.gui.component.GuiComponent;
import pl.pabilo8.immersiveintelligence.client.gui.deco.DecoGui;
import pl.pabilo8.immersiveintelligence.client.gui.deco.component.DecoComponent;
import pl.pabilo8.immersiveintelligence.client.gui.deco.util.DecoBackgroundBuilder;
import pl.pabilo8.immersiveintelligence.client.gui.deco.util.SlotStyle;
import pl.pabilo8.immersiveintelligence.common.util.easynbt.EasyNBT;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Builds a fresh native Deco GUI from a CTMB layout.
 *
 * @author Pabilo8 (pabilo@iiteam.net)
 * @since 27.02.2022
 * @updated 05.10.2026
 */
public class MultiblockGui extends DecoGui<TileEntityMultiblock, MultiblockContainer>
{
	private final MultiblockGuiLayout layout;
	private final Map<String, DecoComponentAccess> components = new LinkedHashMap<>();
	private final MultiblockGuiCTWrapper wrapper = new MultiblockGuiCTWrapper(components);
	private EasyNBT localData = EasyNBT.newNBT();
	private final java.util.Queue<String> selectionEvents = new java.util.ArrayDeque<>();

	public MultiblockGui(InventoryPlayer player, TileEntityMultiblock tile, int page)
	{
		super(player.player, new MultiblockContainer(player, tile, page), tile, null);
		layout = tile.getMultiblock().getGuiLayout(page);
	}

	@Override
	public void onInit()
	{
		components.clear();
		selectionEvents.clear();
		xSize = layout.getWidth();
		ySize = layout.getHeight();
		DecoBackgroundBuilder<TileEntityMultiblock, MultiblockContainer> background = startBackground().withBox(0, 0, xSize, ySize);
		for(Slot slot : container.inventorySlots)
			background.withInventorySlots(slot instanceof CTMBSlot?
					SlotStyle.valueOf(((CTMBSlot)slot).getStyle().toUpperCase(Locale.ROOT)):SlotStyle.IE, slot);
		for(GuiComponent definition : layout.getComponents().values())
			buildComponent(definition);
		if(layout.getOnOpen()!=null)
			layout.getOnOpen().execute(wrapper, context.getMbWrapper(), CraftTweakerMC.getIPlayer(playerContainer.player));
	}

	@Override
	protected void onInitStandardAddons()
	{
		super.onInitStandardAddons();
		addWidget(new pl.pabilo8.immersiveintelligence.client.gui.deco.component.widget.DecoManualWidget());
	}

	private void buildComponent(GuiComponent definition)
	{
		String name = definition.getName();
		EasyNBT data = definition.getOptions();
		int x = definition.getX(), y = definition.getY();
		DecoComponent<?> component;
		switch(definition.getType())
		{
			case "slot": return;
			case "label":
				CTMBDecoLabel label = new CTMBDecoLabel(x, y);
				label.setData(CraftTweakerMC.getIData(data.unwrap()));
				if(data.hasKey("w")) label.withWidth(data.getInt("w"));
				if(data.hasKey("h")) label.withHeight(data.getInt("h"));
				addLabel(label);
				components.put(name, label);
				return;
			case "checkbox":
				component = new CTMBDecoCheckbox(x, y).withOnToggle(value -> activated(name));
				break;
			case "switch":
				component = new CTMBDecoSwitch(x, y).withOnToggle(value -> activated(name));
				break;
			case "slider":
				float min = data.hasKey("min")?data.getFloat("min"):0;
				float max = data.hasKey("max")?data.getFloat("max"):1;
				if(max <= min) throw new IllegalArgumentException("Slider maximum must exceed minimum: "+name);
				component = new CTMBDecoSlider(x, y).withRange(min, max)
						.withIntegersOnly(data.getBoolean("integer")).withOnValueChanged(value -> activated(name));
				break;
			case "dropdown":
				CTMBDecoDropdown dropdown = new CTMBDecoDropdown(x, y);
				dropdown.withEntries(data.streamList(net.minecraft.nbt.NBTTagString.class, "entries").map(net.minecraft.nbt.NBTTagString::getString).toArray(String[]::new));
				dropdown.withOnSelectedEntry((oldValue, newValue) -> selectionEvents.add(name));
				component = dropdown;
				break;
			case "text":
				component = new CTMBDecoTextField(x, y).withOnTextChanged(value -> activated(name));
				break;
			case "bar": case "energy":
				component = new CTMBDecoBar(x, y);
				break;
			case "fluid":
				int tank = data.getInt("id");
				if(context.tanks==null||tank < 0||tank >= context.tanks.length)
					throw new IllegalArgumentException("Invalid fluid tank: "+name);
				component = new CTMBDecoFluidTank(x, y).withFluidTank(context.tanks[tank]);
				break;
			default:
				component = new CTMBDecoButton(x, y).withOnLMBPressed(() -> activated(name));
		}
		component.withSize(data.hasKey("w")?data.getInt("w"):component.width,
				data.hasKey("h")?data.getInt("h"):component.height);
		DecoComponentAccess access = (DecoComponentAccess)component;
		access.setData(CraftTweakerMC.getIData(data.unwrap()));
		if(definition.getType().equals("energy"))
		{
			int energy = data.getInt("id");
			if(context.energy==null||energy < 0||energy >= context.energy.length)
				throw new IllegalArgumentException("Invalid energy storage: "+name);
			((CTMBDecoBar)component).withLimits(0, Math.max(1, context.energy[energy].getMaxEnergyStored()),
					() -> context.energy[energy].getEnergyStored());
		}
		component.withOnHovered((widget, button, mx, my) -> {
			if(layout.getOnHover()!=null)
				layout.getOnHover().execute(name, wrapper, context.getMbWrapper(), mx-getScreenLeft(), my-getScreenTop(),
						CraftTweakerMC.getIPlayer(playerContainer.player));
			return false;
		});
		components.put(name, access);
		addComponents(component);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		//II reports dropdown changes before it stores the selected index.
		while(!selectionEvents.isEmpty()) activated(selectionEvents.remove());
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	private void activated(String name)
	{
		if(layout.getOnPress()!=null)
		{
			Minecraft minecraft = Minecraft.getMinecraft();
			int mx = Mouse.getX()*width/minecraft.displayWidth-getScreenLeft();
			int my = height-Mouse.getY()*height/minecraft.displayHeight-1-getScreenTop();
			layout.getOnPress().execute(name, wrapper, context.getMbWrapper(), mx, my,
					CraftTweakerMC.getIPlayer(playerContainer.player));
		}
	}

	@Override
	protected EasyNBT loadGuiData() { return localData; }
	@Override
	protected EasyNBT createGuiDataTag() { return localData = EasyNBT.newNBT(); }
	@Override
	protected void onGuiClosedWithoutTransition()
	{
		if(layout.getOnClose()!=null)
			layout.getOnClose().execute(wrapper, context.getMbWrapper(), CraftTweakerMC.getIPlayer(playerContainer.player));
	}
}
