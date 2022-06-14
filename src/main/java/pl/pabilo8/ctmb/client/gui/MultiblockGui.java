package pl.pabilo8.ctmb.client.gui;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.gui.GuiIEContainerBase;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import org.lwjgl.opengl.GL11;
import pl.pabilo8.ctmb.client.ClientUtils;
import pl.pabilo8.ctmb.client.gui.elements.IGuiTweakable;
import pl.pabilo8.ctmb.common.CommonUtils;
import pl.pabilo8.ctmb.common.block.TileEntityMultiblock;
import pl.pabilo8.ctmb.common.block.crafttweaker.Multiblock;
import pl.pabilo8.ctmb.common.gui.*;
import pl.pabilo8.ctmb.common.gui.component.GuiComponent;
import pl.pabilo8.ctmb.common.gui.rectangle.GuiRectangle;
import pl.pabilo8.ctmb.common.gui.rectangle.GuiRectangleCustom;
import pl.pabilo8.ctmb.common.gui.rectangle.GuiRectangleStyled;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.HashMap;

import static pl.pabilo8.ctmb.client.gui.StyledGuiUtils.drawItemSlot;

/**
 * @author Pabilo8
 * @since 27.02.2022
 */
public class MultiblockGui extends GuiIEContainerBase
{
	private final TileEntityMultiblock tile;
	private final MultiblockContainer container;
	private final Multiblock mb;
	private final MultiblockGuiLayout layout;
	private final MultiblockGuiCTWrapper ctWrapper;

	private final GuiRectangleCustom[] customRects;
	private final GuiRectangleStyled[] styledRects;
	public final HashMap<String, IGuiTweakable> ctComponents = new HashMap<>();

	private final int firstX, firstY;

	private int lastMX, lastMY;

	public boolean built = false;
	int displayList = -1;

	static
	{
		StyledGuiUtils.VARIABLES.put("player_name", gui -> ClientUtils.mc.player.getName());
		StyledGuiUtils.VARIABLES.put("player_slots", gui -> String.valueOf(ClientUtils.mc.player.inventory.getSizeInventory()));
		StyledGuiUtils.VARIABLES.put("mb_name", gui -> I18n.format(Lib.DESC_INFO+"multiblock."+gui.mb.getUniqueName()));
		StyledGuiUtils.VARIABLES.put("mb_slots", gui -> String.valueOf(gui.container.inventorySlots.size()));
	}

	public MultiblockGui(InventoryPlayer inventoryPlayer, TileEntityMultiblock tile, int page)
	{
		super(new MultiblockContainer(inventoryPlayer, tile, page));
		this.tile = tile;
		this.container = ((MultiblockContainer)inventorySlots);
		this.mb = tile.getMultiblock();
		this.layout = page==0?mb.mainGui: CommonUtils.getMapElement(mb.assignedGuis, page);
		this.ctWrapper = new MultiblockGuiCTWrapper(this);

		assert this.layout!=null;

		int firstX = 0, firstY = 0, lastX = 0, lastY = 0;
		for(GuiRectangle rectangle : this.layout.rectangles)
		{
			if(rectangle.x-rectangle.margin[0] < firstX)
				firstX = rectangle.x;
			if(rectangle.y-rectangle.margin[1] < firstY)
				firstY = rectangle.y;
			if(rectangle.x+rectangle.margin[2]+rectangle.w > lastX)
				lastX = rectangle.x+rectangle.w;
			if(rectangle.y+rectangle.margin[3]+rectangle.h > lastY)
				lastY = rectangle.y+rectangle.h;
		}

		this.xSize = Math.abs(lastX-firstX);
		this.ySize = Math.abs(lastY-firstY);
		this.firstX = firstX;
		this.firstY = firstY;

		customRects = layout.rectangles.stream().filter(r -> r instanceof GuiRectangleCustom).toArray(GuiRectangleCustom[]::new);
		styledRects = layout.rectangles.stream().filter(r -> r instanceof GuiRectangleStyled).toArray(GuiRectangleStyled[]::new);

	}

	@Override
	public void initGui()
	{
		super.initGui();
		layout.rectangles.forEach(rect -> rect.validate(layout));

		buttonList.clear();
		labelList.clear();
		ctComponents.clear();

		int i = 0;
		for(GuiComponent component : layout.components.values())
		{
			Gui gui = component.provide(i++, guiLeft, guiTop, this);
			if(gui!=null)
			{
				if(gui instanceof IGuiTweakable)
					ctComponents.put(component.name, ((IGuiTweakable)gui));

				if(gui instanceof GuiButton)
					buttonList.add(((GuiButton)gui));
				else if(gui instanceof GuiLabel)
					labelList.add(((GuiLabel)gui));
			}

		}

		if(layout.onOpen!=null)
			layout.onOpen.execute(ctWrapper, tile.getMbWrapper(), CraftTweakerMC.getIPlayer(ClientUtils.mc.player));
	}

	@Override
	public void onGuiClosed()
	{
		super.onGuiClosed();
		layout.rectangles.forEach(rect -> rect.invalidate(layout));
		if(built)
			GLAllocation.deleteDisplayLists(displayList);
		if(layout.onClose!=null)
			layout.onClose.execute(ctWrapper, tile.getMbWrapper(), CraftTweakerMC.getIPlayer(ClientUtils.mc.player));
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
	{
		lastMX = mouseX;
		lastMY = mouseY;

		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void actionPerformed(@Nonnull GuiButton button) throws IOException
	{
		super.actionPerformed(button);
		if(button instanceof IGuiTweakable)
		{
			GuiComponent blueprint = ((IGuiTweakable)button).getBlueprint();
			if(layout.onPress!=null&&blueprint!=null)
				layout.onPress.execute(blueprint.name, ctWrapper, tile.getMbWrapper(), lastMX, lastMY, CraftTweakerMC.getIPlayer(ClientUtils.mc.player));
		}

	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
	{
		GlStateManager.pushMatrix();
		//layout.rectangles.forEach(rect -> rect.draw(layout));

		drawStyledRects();
		for(GuiRectangleCustom rect : customRects)
		{
			int gL = guiLeft+rect.x, gT = guiTop+rect.y;
			if(rect.texture==null)
				drawRect(gL, gT, gL+rect.w, gT+rect.h, 0xff000000+rect.bgColor);
		}

		GlStateManager.popMatrix();

		if(layout.onHover!=null)
			for(GuiButton b : buttonList)
			{
				if(b.isMouseOver()&&b instanceof IGuiTweakable)
				{
					GuiComponent blueprint = ((IGuiTweakable)b).getBlueprint();
					if(blueprint!=null)
						layout.onHover.execute(blueprint.name, ctWrapper, tile.getMbWrapper(), mouseX, mouseY, CraftTweakerMC.getIPlayer(ClientUtils.mc.player));
				}
			}

	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{

	}

	public void drawStyledRects()
	{
		GlStateManager.pushMatrix();

		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buffer = tess.getBuffer();
		ClientUtils.bindTexture(layout.style.getStylePath());

		if(!built)
		{
			GlStateManager.glNewList(displayList = GLAllocation.generateDisplayLists(1), GL11.GL_COMPILE);

			//Mask
			GL11.glEnable(GL11.GL_STENCIL_TEST);
			GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
			GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			StyledGuiUtils.drawBackgroundRoundedMask(styledRects, buffer, firstX, firstY);
			tess.draw();

			//Background
			GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
			GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			StyledGuiUtils.drawBackgroundBlock(styledRects, buffer);
			tess.draw();

			GL11.glDisable(GL11.GL_STENCIL_TEST);

			GlStateManager.enableBlend();
			GlStateManager.blendFunc(SourceFactor.DST_COLOR, DestFactor.SRC_COLOR);
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			StyledGuiUtils.drawBackgroundRoundedMask(styledRects, buffer, firstX, firstY);

			for(Slot slot : container.inventorySlots)
				if(!(slot instanceof CTMBSlot)||((CTMBSlot)slot).getStyle()==0)
					drawItemSlot(slot.xPos, slot.yPos, buffer);

			tess.draw();

			GlStateManager.color(1f, 1f, 1f, 1f);

			GlStateManager.blendFunc(SourceFactor.SRC_COLOR, DestFactor.DST_COLOR);
			GlStateManager.disableBlend();

			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			StyledGuiUtils.drawBorderAround(styledRects, buffer);
			tess.draw();

			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			for(Slot slot : container.inventorySlots)
				if(slot instanceof CTMBSlot)
				{
					CTMBSlot s = (CTMBSlot)slot;
					switch(s.getStyle())
					{
						default:
						case 0:
							break;
						case 1:
						{
							ClientUtils.drawTexturedRect(buffer, slot.xPos-1, slot.yPos-1, 238, 122, 18, 18);
						}
						break;
						case 2:
						{
							ClientUtils.drawTexturedRect(buffer, slot.xPos-1, slot.yPos-1, 238, 122, 18, 18);
							ClientUtils.drawTexturedRect(buffer, slot.xPos-2, slot.yPos-2, 208, 3, 20, 20);
							ClientUtils.drawTexturedRect(buffer, slot.xPos-2+8, slot.yPos-2-3, 220, 0, 4, 3);
						}
						break;
						case 3:
						{
							ClientUtils.drawTexturedRect(buffer, slot.xPos-1, slot.yPos-1, 238, 122, 18, 18);
							ClientUtils.drawTexturedRect(buffer, slot.xPos-2, slot.yPos-2, 208, 3, 20, 20);
							ClientUtils.drawTexturedRect(buffer, slot.xPos-2+8, slot.yPos-2-3, 224, 0, 4, 3);
						}
						break;
					}
				}
			tess.draw();

			GlStateManager.glEndList();
			built = true;
		}
		else
		{
			GlStateManager.translate(guiLeft, guiTop, 0);
			GlStateManager.callList(displayList);
		}

		GlStateManager.blendFunc(SourceFactor.SRC_COLOR, DestFactor.DST_COLOR);
		GlStateManager.popMatrix();

	}

	public MultiblockGuiStyle getStyle()
	{
		return layout.style;
	}

	public TileEntityMultiblock getTile()
	{
		return tile;
	}
}
