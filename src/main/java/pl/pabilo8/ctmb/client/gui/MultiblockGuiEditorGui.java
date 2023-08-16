package pl.pabilo8.ctmb.client.gui;

import crafttweaker.api.data.IData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import pl.pabilo8.ctmb.client.ClientUtils;
import pl.pabilo8.ctmb.client.gui.elements.GuiDroppedCTMB;
import pl.pabilo8.ctmb.client.gui.elements.GuiLabelCTMB;
import pl.pabilo8.ctmb.client.gui.elements.IDragAndDropGUI;
import pl.pabilo8.ctmb.client.gui.elements.buttons.GuiButtonCTMB;
import pl.pabilo8.ctmb.common.CommonUtils;
import pl.pabilo8.ctmb.common.block.TileEntityMultiblock;
import pl.pabilo8.ctmb.common.gui.MultiblockGuiLayout;
import pl.pabilo8.ctmb.common.gui.MultiblockGuiStyle;
import pl.pabilo8.ctmb.common.gui.component.*;
import pl.pabilo8.ctmb.common.gui.rectangle.GuiRectangleStyled;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Pabilo8
 * @since 03.07.2022
 */
public class MultiblockGuiEditorGui extends GuiScreen implements IComponentGui, IDragAndDropGUI
{
	//--- Editor Variables ---//

	private final MultiblockGuiStyle style = MultiblockGuiStyle.STYLES.get("vanilla");
	private GuiButtonCTMB buttonRects, buttonObjects;
	private GuiRectangleStyled[] background;
	private boolean built = false, projectBuilt = false;
	int displayList = -1, projectDisplayList = -1;
	private GuiDroppedCTMB dropped = null;

	private boolean secondTab = false;
	private int editedWidth = 32, editedHeight = 32;

	private final ArrayList<GuiButton> projectButtonList = new ArrayList<>();
	private final ArrayList<GuiLabel> projectLabelList = new ArrayList<>();
	private GuiComponent edited = null;


	//--- Project Variables ---//

	MultiblockGuiLayout editedLayout = new MultiblockGuiLayout(MultiblockGuiStyle.DEFAULT_SKIN);

	@Override
	public void initGui()
	{
		buttonList.clear();
		labelList.clear();

		background = new GuiRectangleStyled[]{
				new GuiRectangleStyled(0, 0, width, 12, null),
				new GuiRectangleStyled(0, 12, 120, height-34, null),
				new GuiRectangleStyled(width-120, 12+16, 120, height-34-16, null)
		};

		addLabel(
				new GuiLabelCTMB(null, fontRenderer, labelList.size(), 3, 3, 0, 12, style.getTitleColor(), true)
						.withLine("GUI Editor mk.1")
		);

		addLabel(
				new GuiLabelCTMB(null, fontRenderer, labelList.size(), width-120, 12+16, 120, 12, style.getTitleColor(), true)
						.withLine("Properties")
						.setCentered()
		);


		addButton(buttonRects = new GuiButtonCTMB(null, 0, 13, 60, 20, buttonList.size(), "Rects", style, 0));
		addButton(buttonObjects = new GuiButtonCTMB(null, 60, 13, 60, 20, buttonList.size(), "Objects", style, 0));
		buttonRects.setState(!secondTab);
		buttonObjects.setState(secondTab);

		int list = 0;
		for(String option : new String[]{"File", "Preview", "Grid"})
			addButton(new GuiButtonCTMB(null, 120+(list++*50), 0, 50, 16, buttonList.size(), option, style, 0));

		addButton(new GuiButtonCTMB(null, width-60, 0, 40, 16, buttonList.size(), "Reset", style, 0));

		list = 0;
		String[] draggables = secondTab?
				new String[]{"label", "button", "state_button", "tab", "checkbox", "switch", "dropdown", "bar", "energy_bar", "item_slot", "fluid_tank"}:
				new String[]{"round", "frame", "dotted", "thick"};
		for(String c : draggables)
		{
			addButton(new GuiDroppedCTMB(null, buttonList.size(), 4+
					(list%3*34),
					34+((int)Math.floor(list/3f)*34),
					32, 32, style, c, this));
			list++;
		}

		if(edited!=null)
		{
			addLabel(
					new GuiLabelCTMB(null, fontRenderer, labelList.size(), width-120+4, 12+32, 116, 12, style.getTitleColor(), true)
							.withLine("X: "+edited.x)
			);
			addLabel(
					new GuiLabelCTMB(null, fontRenderer, labelList.size(), width-120+4, 12+32+16, 116, 12, style.getTitleColor(), true)
							.withLine("Y"+edited.y)
			);
			addLabel(
					new GuiLabelCTMB(null, fontRenderer, labelList.size(), width-120+4, 12+32+32, 116, 12, style.getTitleColor(), true)
							.withLine("Width"+edited.w)
			);
			addLabel(
					new GuiLabelCTMB(null, fontRenderer, labelList.size(), width-120+4, 12+32+48, 116, 12, style.getTitleColor(), true)
							.withLine("Height"+edited.h)
			);
		}

		refreshProjectDisplay();

		/*addButton(new GuiButtonCTMBDropdownList(null, buttonList.size(), 2, 36, 116, 20, 0, style, 20,
				new String[]{""},
				new String[]{""}
		));*/
	}

	private void refreshProjectDisplay()
	{
		projectButtonList.clear();
		projectLabelList.clear();

		projectBuilt = false;
		if(projectDisplayList!=-1)
			GLAllocation.deleteDisplayLists(projectDisplayList);
		projectDisplayList = -1;

		int i = 0;
		IComponentGui project = new IComponentGui()
		{
			@Override
			public MultiblockGuiStyle getStyle()
			{
				return editedLayout.style;
			}

			@Override
			public boolean hasTile()
			{
				return false;
			}

			@Override
			public TileEntityMultiblock getTile()
			{
				return null;
			}

			@Override
			public String parseVariable(String text)
			{
				return text;
			}

			@Nonnull
			@Override
			public <T extends GuiLabel> T addLabel(@Nonnull T label)
			{
				return label;
			}

			@Nonnull
			@Override
			public <T extends GuiButton> T addButton(@Nonnull T button)
			{
				return button;
			}
		};

		for(GuiComponent component : editedLayout.components.values())
		{
			Gui gui = component.provide(i++, 128, 24, project);
			if(gui!=null)
			{
				if(gui instanceof GuiButton)
					projectButtonList.add(((GuiButton)gui));
				else if(gui instanceof GuiLabel)
					projectLabelList.add(((GuiLabel)gui));
			}

		}
	}

	//--- Drawing ---//

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		drawDefaultBackground();

		//draw editor
		drawStyledRects(false);

		buttonList.forEach(guiButton -> guiButton.drawButton(mc, mouseX, mouseY, partialTicks));
		labelList.forEach(guiButton -> guiButton.drawLabel(mc, mouseX, mouseY));

		//draw project
		GlStateManager.pushMatrix();
		GlStateManager.translate(128, 24, 0);
		drawStyledRects(true);
		GlStateManager.popMatrix();

		projectButtonList.forEach(guiButton -> guiButton.drawButton(mc, mouseX, mouseY, partialTicks));
		projectLabelList.forEach(guiButton -> guiButton.drawLabel(mc, mouseX, mouseY));

		//draw currently dropped
		if(dropped!=null)
		{
			dropped.drawButton(mc, mouseX, mouseY, partialTicks);
			dropped.width = editedWidth;
			dropped.height = editedHeight;
		}

		if(mc.frameTimer.getIndex()%5==0)
			checkHeldKeys();

	}

	public void drawStyledRects(boolean project)
	{
		GlStateManager.pushMatrix();

		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buffer = tess.getBuffer();
		//ClientUtils.bindTexture(style.getStylePath());
		// TODO: 07.07.2022 fix gui rect rendering
		ClientUtils.bindTexture(project?editedLayout.style.getStylePath(): style.getStylePath());

		boolean built = project?this.projectBuilt: this.built;

		if(!built)
		{
			int list;
			GuiRectangleStyled[] background = project?
					this.editedLayout.rectangles.stream()
							.filter(g -> g instanceof GuiRectangleStyled)
							.toArray(GuiRectangleStyled[]::new):
					this.background;
			GlStateManager.glNewList(list = GLAllocation.generateDisplayLists(1), GL11.GL_COMPILE);

			//Mask
			GL11.glEnable(GL11.GL_STENCIL_TEST);
			GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
			GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
			GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			StyledGuiUtils.drawBackgroundRoundedMask(background, buffer, 0, 0);
			tess.draw();

			//Background
			GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
			GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			StyledGuiUtils.drawBackgroundBlock(background, buffer);
			tess.draw();

			GL11.glDisable(GL11.GL_STENCIL_TEST);

			GlStateManager.enableBlend();
			GlStateManager.blendFunc(SourceFactor.DST_COLOR, DestFactor.SRC_COLOR);
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			StyledGuiUtils.drawBackgroundRoundedMask(background, buffer, 0, 0);
			tess.draw();

			GlStateManager.color(1f, 1f, 1f, 1f);

			GlStateManager.blendFunc(SourceFactor.SRC_COLOR, DestFactor.DST_COLOR);
			GlStateManager.disableBlend();

			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			StyledGuiUtils.drawBorderAround(background, buffer);
			tess.draw();

			GlStateManager.glEndList();
			if(project)
			{
				this.projectDisplayList = list;
				this.projectBuilt = true;
			}
			else
			{
				this.displayList = list;
				this.built = true;
			}
		}
		else
			GlStateManager.callList(project?projectDisplayList: displayList);

		GlStateManager.blendFunc(SourceFactor.SRC_COLOR, DestFactor.DST_COLOR);
		GlStateManager.popMatrix();

	}

	//--- Handling Input ---//

	@Override
	public void onGuiClosed()
	{
		unloadEditorGui();
		if(projectBuilt)
			GLAllocation.deleteDisplayLists(projectDisplayList);
	}

	private void checkHeldKeys()
	{
		if(Keyboard.isKeyDown(Keyboard.KEY_LBRACKET))
		{
			if(isShiftKeyDown())
				editedHeight = Math.max(8, editedHeight-8);
			else
				editedWidth = Math.max(8, editedWidth-8);
		}
		else if(Keyboard.isKeyDown(Keyboard.KEY_RBRACKET))
		{
			if(isShiftKeyDown())
				editedHeight = Math.min(256, editedHeight+8);
			else
				editedWidth = Math.min(256, editedWidth+8);
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException
	{
		super.keyTyped(typedChar, keyCode);
		/*switch(keyCode)
		{
			default:
				super.keyTyped(typedChar, keyCode);
				break;
		}*/
	}

	@Override
	protected void actionPerformed(@Nonnull GuiButton button)
	{
		if(button instanceof GuiDroppedCTMB)
		{
			if(this.dropped==null)
				this.dropped = ((GuiDroppedCTMB)button);
		}
		else if(button==buttonRects||button==buttonObjects)
		{
			secondTab = button==buttonObjects;
			initGui();

		}
		else if(button.displayString.equals("Reset"))
		{
			unloadEditorGui();
			initGui();
		}

	}

	private void unloadEditorGui()
	{
		if(displayList!=-1)
			GLAllocation.deleteDisplayLists(displayList);
		displayList = -1;
		built = false;
	}

	@Override
	public void setWorldAndResolution(@Nonnull Minecraft mc, int width, int height)
	{
		super.setWorldAndResolution(mc, width, height);
		unloadEditorGui();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
	{
		// TODO: 09.07.2022 selecting element

		if(mouseButton==1&&dropped!=null)
			dropped.mouseReleased(dropped.x = dropped.y = 0, 0);
		/*else if(mouseButton==0&&dropped==null) //select component from project
		{

			if(secondTab) //objects
			{
				editedLayout.components.values().stream()
						.filter(gc -> ClientUtils.isPointInRectangle(gc.x, gc.y, gc.x+gc.w, gc.y+gc.h, mouseX, mouseY))
						.findFirst();
			}
			else //rects
			{

			}

			//initGui();
		}
		else*/
			super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	//--- IDragAndDropGUI ---//

	@Override
	public boolean dropOnto(GuiDroppedCTMB dropped, int mouseX, int mouseY)
	{
		dropped.width = 32;
		dropped.height = 32;

		if(mouseX >= 128&&mouseY >= 24&&mouseX+editedWidth < width-120&&mouseY+editedHeight < height-8)
		{
			int xx = mouseX-128;
			int yy = mouseY-24;

			switch(dropped.displayString)
			{
				case "round":
					editedLayout.rectangles.add(new GuiRectangleStyled(xx, yy, editedWidth, editedHeight, CommonUtils.getDataFromString("{}")));
					break;
				case "frame":
					editedLayout.rectangles.add(new GuiRectangleStyled(xx, yy, editedWidth, editedHeight, CommonUtils.getDataFromString("{border_id:0, margin:2}")));
					break;
				case "dotted":
					editedLayout.rectangles.add(new GuiRectangleStyled(xx, yy, editedWidth, editedHeight, CommonUtils.getDataFromString("{border_id:1, margin:2}")));
					break;
				case "thick":
					editedLayout.rectangles.add(new GuiRectangleStyled(xx, yy, editedWidth, editedHeight, CommonUtils.getDataFromString("{border_id:2, margin:6}")));
					break;

				//"energy_bar", "item_slot", "fluid_tank"
				case "label":
					editedLayout.addComponent(edited = GuiComponentLabel.create(xx, yy,
							getNewComponentName(), getNewComponentData()));
					break;
				case "button":
					editedLayout.addComponent(edited = GuiComponentButton.create(xx, yy,
							getNewComponentName(), getNewComponentData()));
					break;
				case "state_button":
					editedLayout.addComponent(edited = GuiComponentButtonState.create(xx, yy,
							getNewComponentName(), getNewComponentData()));
					break;
				case "tab":
					editedLayout.addComponent(edited = GuiComponentTab.create(xx, yy,
							getNewComponentName(), getNewComponentData()));
					break;
				case "checkbox":
					editedLayout.addComponent(edited = GuiComponentCheckbox.create(xx, yy,
							getNewComponentName(), getNewComponentData()));
					break;
				case "switch":
					editedLayout.addComponent(edited = GuiComponentSwitch.create(xx, yy,
							getNewComponentName(), getNewComponentData()));
					break;
				case "dropdown":
					editedLayout.addComponent(edited = GuiComponentDropdown.create(xx, yy,
							getNewComponentName(), getNewComponentData()));
					break;
				case "bar":
					editedLayout.addComponent(edited = GuiComponentBar.create(xx, yy,
							getNewComponentName(), getNewComponentData()));
					break;
			}
			refreshProjectDisplay();
			initGui();
		}

		this.dropped = null;
		return true;
	}

	private String getNewComponentName()
	{
		return "object_"+editedLayout.components.size();
	}

	private IData getNewComponentData()
	{
		return CommonUtils.getDataFromString("{w:"+editedWidth+", h:"+editedHeight+"}");
	}

	@Override
	public int getXGridSize(GuiDroppedCTMB dropped, int mouseX, int mouseY)
	{
		return (mouseX >= 128&&mouseY >= 24&&mouseX+editedWidth < width-120&&mouseY+editedHeight < height-8)?8: 1;
	}

	@Override
	public int getYGridSize(GuiDroppedCTMB dropped, int mouseX, int mouseY)
	{
		return (mouseX >= 128&&mouseY >= 24&&mouseX+editedWidth < width-120&&mouseY+editedHeight < height-8)?8: 1;
	}

	@Override
	public boolean hasDragFocus()
	{
		return false;
	}

	//--- IComponentGUI ---//

	@Override
	public MultiblockGuiStyle getStyle()
	{
		return style;
	}

	@Override
	public boolean hasTile()
	{
		return false;
	}

	@Override
	public TileEntityMultiblock getTile()
	{
		return null;
	}

	@Override
	public String parseVariable(String text)
	{
		return text;
	}

	@Nonnull
	@Override
	public <T extends GuiLabel> T addLabel(@Nonnull T label)
	{
		labelList.add(label);
		return label;
	}

	@Nonnull
	@Override
	public <T extends GuiButton> T addButton(@Nonnull T button)
	{
		return super.addButton(button);
	}
}
