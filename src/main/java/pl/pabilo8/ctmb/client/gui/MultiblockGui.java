package pl.pabilo8.ctmb.client.gui;

import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import pl.pabilo8.ctmb.client.gui.deco.*;
import pl.pabilo8.ctmb.common.block.TileEntityMultiblock;
import pl.pabilo8.ctmb.common.gui.*;
import pl.pabilo8.ctmb.common.gui.component.GuiComponent;
import pl.pabilo8.ctmb.common.util.CTMBLogger;
import pl.pabilo8.immersiveintelligence.client.gui.deco.DecoGui;
import pl.pabilo8.immersiveintelligence.client.gui.deco.component.DecoComponent;
import pl.pabilo8.immersiveintelligence.client.gui.deco.util.DecoBackgroundBuilder;
import pl.pabilo8.immersiveintelligence.common.util.ResLoc;
import pl.pabilo8.immersiveintelligence.common.util.easynbt.EasyNBT;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;

/**
 * Native Deco screen built from a common, server-verifiable container plan.
 */
public class MultiblockGui extends DecoGui<TileEntityMultiblock, MultiblockContainer>
{
	private final MultiblockGuiLayout layout;
	private final Map<String, DecoComponentAccess> components = new LinkedHashMap<>();
	private final MultiblockGuiCTWrapper wrapper = new MultiblockGuiCTWrapper(components);
	private final Queue<String> selectionEvents = new ArrayDeque<>();
	private EasyNBT localData = EasyNBT.newNBT();
	private boolean layoutConfirmed;

	public MultiblockGui(InventoryPlayer player, TileEntityMultiblock tile, int page)
	{
		super(player.player, new MultiblockContainer(player, tile, page), tile, null);
		layout = container.layout;
	}

	@Override
	public void onInit()
	{
		Map<String, crafttweaker.api.data.IData> previous = new LinkedHashMap<>();
		components.forEach((name, component) -> previous.put(name, component.getData()));
		components.clear();
		selectionEvents.clear();
		DecoBackgroundBuilder<TileEntityMultiblock, MultiblockContainer> background = startBackground();
		for(MultiblockGuiLayout.BackgroundBox box : layout.boxes)
		{
			background.withBox(ResLoc.of(box.texture.location), ResLoc.of(box.mask.location), box.x, box.y, box.width, box.height);
			if("@machine".equals(box.title)) background.withTitleBar(context);
			else if(box.title!=null) background.withTitleBar(box.title);
			if(box.inventoryTitle) background.withInventoryTitleBar();
		}
		for(Slot slot : container.inventorySlots)
			background.withInventorySlots(
					pl.pabilo8.immersiveintelligence.client.gui.deco.util.SlotStyle.valueOf(slot instanceof CTMBSlot?((CTMBSlot)slot).getStyle(): layout.inventoryStyle), slot);
		background.build();
		for(Map.Entry<String, GuiComponent> entry : layout.components.entrySet())
			buildComponent(entry.getKey(), entry.getValue());
		for(GuiDefinition link : layout.links)
		{
			CTMBDecoTab tab = new CTMBDecoTab();
			tab.withIcon(new ResourceLocation(link.icon().location)).withSelected(link==layout.definition)
					.withTranslatedTooltip(link.title());
			tab.withOnLMBPressed(() -> {
				if(link!=layout.definition)
					context.getMbWrapper().openGUI(link.name, CraftTweakerMC.getIPlayer(playerContainer.player));
			});
			addComponents(tab);
		}
		// Preserve user-entered control state on resizing; bound storage views keep native suppliers.
		previous.forEach((name, data) -> {
			GuiComponent definition = layout.components.get(name);
			if(definition!=null&&definition.source()==null&&components.containsKey(name))
				components.get(name).setData(data);
		});
	}

	@Override
	protected void onInitStandardAddons()
	{
		super.onInitStandardAddons();
		addWidget(new pl.pabilo8.immersiveintelligence.client.gui.deco.component.widget.DecoManualWidget());
	}

	private void buildComponent(String name, GuiComponent definition)
	{
		EasyNBT data = definition.getOptions();
		int x = definition.getX(), y = definition.getY();
		DecoComponent<?> component;
		switch(definition.getType())
		{
			case "label":
				CTMBDecoLabel label = new CTMBDecoLabel(x, y);
				label.setData(CraftTweakerMC.getIData(data.unwrap()));
				label.withSize(definition.width(), definition.height());
				addLabel(label);
				components.put(name, label);
				return;
			case "checkbox":
				component = new CTMBDecoCheckbox(x, y).withOnToggle(value -> activated(name));
				break;
			case "switch":
				component = new CTMBDecoSwitch(x, y).withOnToggle(value -> activated(name));
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
			case "bar":
			case "energy":
				component = new CTMBDecoBar(x, y);
				break;
			case "fluid":
				CTMBDecoFluidTank tank = new CTMBDecoFluidTank(x, y);
				tank.bind(definition.source());
				component = tank;
				break;
			case "dust":
				CTMBDecoDustTank dust = new CTMBDecoDustTank(x, y);
				dust.bind(definition.source());
				component = dust;
				break;
			case "tab":
				component = new CTMBDecoTab().withOnLMBPressed(() -> activated(name));
				break;
			default:
				component = new CTMBDecoButton(x, y).withOnLMBPressed(() -> activated(name));
		}
		component.withSize(definition.width(), definition.height());
		DecoComponentAccess access = (DecoComponentAccess)component;
		access.setData(CraftTweakerMC.getIData(data.unwrap()));
		if(definition.getType().equals("energy")) ((CTMBDecoBar)component).bindEnergy(definition.source());
		component.withOnHovered((widget, button, mx, my) -> {
			if(definition.hover()!=null)
				definition.hover().execute(access, wrapper, context.getMbWrapper(), mx-getScreenLeft(), my-getScreenTop(), CraftTweakerMC.getIPlayer(playerContainer.player));
			return false;
		});
		components.put(name, access);
		addComponents(component);
	}

	private void activated(String name)
	{
		GuiComponent definition = layout.components.get(name);
		if(definition!=null&&definition.press()!=null)
		{
			Minecraft minecraft = Minecraft.getMinecraft();
			int mx = Mouse.getX()*width/minecraft.displayWidth-getScreenLeft();
			int my = height-Mouse.getY()*height/minecraft.displayHeight-1-getScreenTop();
			definition.press().execute(components.get(name), wrapper, context.getMbWrapper(), mx, my, CraftTweakerMC.getIPlayer(playerContainer.player));
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		while(!selectionEvents.isEmpty()) activated(selectionEvents.remove());
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void handleMouseClick(Slot slot, int slotId, int mouseButton, ClickType type)
	{
		if(layoutConfirmed) super.handleMouseClick(slot, slotId, mouseButton, type);
	}

	public void confirmLayout(TileEntityMultiblock tile, String signature, int window)
	{
		if(tile!=context||window!=container.windowId) return;
		layoutConfirmed = layout.signature().equals(signature);
		if(!layoutConfirmed)
		{
			CTMBLogger.error("CTMB GUI initializer built different server/client slots: "+layout.definition.name);
			Minecraft.getMinecraft().player.closeScreen();
		}
	}

	@Override
	protected EasyNBT loadGuiData()
	{
		return localData;
	}

	@Override
	protected EasyNBT createGuiDataTag()
	{
		return localData = EasyNBT.newNBT();
	}
}
