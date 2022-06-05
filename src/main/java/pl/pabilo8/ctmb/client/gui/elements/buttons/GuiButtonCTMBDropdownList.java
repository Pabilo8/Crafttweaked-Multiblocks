package pl.pabilo8.ctmb.client.gui.elements.buttons;

import crafttweaker.api.data.DataInt;
import crafttweaker.api.data.DataMap;
import crafttweaker.api.data.DataString;
import crafttweaker.api.data.IData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;
import pl.pabilo8.ctmb.client.ClientUtils;
import pl.pabilo8.ctmb.common.crafttweaker.gui.MultiblockGuiStyle;
import pl.pabilo8.ctmb.common.crafttweaker.gui.component.GuiComponent;

import java.util.Arrays;
import java.util.Map;

/**
 * @author Pabilo8
 * @since 17.09.2021
 */
public class GuiButtonCTMBDropdownList extends GuiButtonCTMB
{
	private boolean needsSlider;
	public boolean dropped = false;
	private int perPage;
	private int offset;
	private int maxOffset;

	private final String[] entries;
	private final String[] translated;

	public int selectedEntry = -1;
	private int hoveredEntry = -1;
	private int hoverTimer = 0;
	private long prevWheelNano = 0;

	private final int[] uvs;

	public GuiButtonCTMBDropdownList(GuiComponent parent, int buttonId, int x, int y, int w, int h, int styleID, MultiblockGuiStyle style, int perPage, String[] entries, String[] translated)
	{
		super(parent, x, y, w, buttonId, styleID, "", style, h);
		this.perPage = perPage;
		this.entries = entries;
		this.translated = translated;

		uvs = new int[]
				{
						128+(styleID*64), 140,
						128+(styleID*64), 152,
						128+(styleID*64), 164,
						122, 140+(18*styleID),
						122, 152+(18*styleID)
				};

		needsSlider = perPage < entries.length;
		if(needsSlider)
			maxOffset = entries.length-perPage;
	}

	/**
	 * Draws this button to the screen.
	 */
	@Override
	public void drawButton(Minecraft mc, int mx, int my, float partialTicks)
	{
		// TODO: 07.03.2022 cleanup
		FontRenderer fr = ClientUtils.mc.fontRenderer;
		if(!this.visible)
			return;

		if(!this.enabled)
			GlStateManager.color(0.5f, 0.5f, 0.5f, 1);
		else
			GlStateManager.color(1, 1, 1, 1);


		ClientUtils.bindTexture(style.getStylePath());

		final int firstX = Math.min(width, 12);
		final int topYOffset = dropped||ClientUtils.isPointInRectangle(x, y, x+width, y+height, mx, my)?uvs[3]: uvs[1];

		this.drawTexturedModalRect(x-1, y-1, uvs[0], topYOffset, firstX, 12);
		this.drawTexturedModalRect(x+1+Math.max(width-12, 0), y-1, uvs[0]+52, topYOffset, Math.min(width, 12), 12);
		for(int i = 0; i < Math.max(width+2-24, 0); i += 16)
			this.drawTexturedModalRect(x-1+firstX+i, y-1, uvs[4]+12, topYOffset, MathHelper.clamp(width+2-24-i, 0, 16), 12);

		if(dropped)
		{
			int yDropDown = y+2+fr.FONT_HEIGHT;
			int mmY = my-yDropDown;
			int strWidth = width-(needsSlider?6: 0);
			int hh = Math.min(perPage, entries.length)*fr.FONT_HEIGHT;
			//drawRect(x, yDropDown, x+width, yDropDown+hh, 0xff000000);

			for(int hpos = 12; hpos < hh+12; hpos += fr.FONT_HEIGHT)
			{
				this.drawTexturedModalRect(x-1, y-1+hpos, uvs[0], uvs[5], firstX, fr.FONT_HEIGHT);
				this.drawTexturedModalRect(x+1+Math.max(width-12, 0), y-1+hpos, uvs[0]+52, uvs[5], Math.min(width, 12), fr.FONT_HEIGHT);
				for(int i = 0; i < Math.max(width+2-24, 0); i += 16)
					this.drawTexturedModalRect(x-1+firstX+i, y-1+hpos, uvs[4]+12, uvs[5], MathHelper.clamp(width+2-24-i, 0, 16), fr.FONT_HEIGHT);
			}

			this.drawTexturedModalRect(x-1, y-1+12+hh, uvs[4], uvs[5]+12, firstX, 3);
			this.drawTexturedModalRect(x+1+Math.max(width-12, 0), y-1+12+hh, uvs[4]+52, uvs[5]+12, Math.min(width, 12), 3);
			for(int i = 0; i < Math.max(width+2-24, 0); i += 16)
				this.drawTexturedModalRect(x-1+firstX+i, y-1+12+hh, uvs[4]+12, uvs[5]+12, MathHelper.clamp(width+2-24-i, 0, 16), 3);

			//slider
			if(needsSlider)
			{
				//136 -  topYOffset
				this.drawTexturedModalRect(x+width-6, yDropDown, uvs[6], uvs[7], 6, 4);
				this.drawTexturedModalRect(x+width-6, yDropDown+hh-4, uvs[6], uvs[7]+8, 6, 4);
				for(int i = 0; i < hh-8; i += 2)
					this.drawTexturedModalRect(x+width-6, yDropDown+4+i, uvs[6], uvs[7]+8, 6, 2);

				int sliderSize = Math.max(6, hh-maxOffset*fr.FONT_HEIGHT);
				float silderShift = (hh-sliderSize)/(float)maxOffset*offset;

				this.drawTexturedModalRect(x+width-5, yDropDown+silderShift+1, uvs[8], uvs[9], 4, 2);
				this.drawTexturedModalRect(x+width-5, yDropDown+silderShift+sliderSize-4, uvs[8], uvs[9]+3, 4, 3);
				for(int i = 0; i < sliderSize-7; i++)
					this.drawTexturedModalRect(x+width-5, yDropDown+silderShift+3+i, uvs[8], uvs[9]+2, 4, 1);
			}

			GlStateManager.scale(1, 1, 1);
			this.hovered = mx >= x&&mx < x+width&&my >= yDropDown&&my < yDropDown+hh;
			boolean hasTarget = false;
			for(int i = 0; i < Math.min(perPage, entries.length); i++)
			{
				int j = offset+i;
				int col = style.getLinkColor();
				boolean selectionHover = hovered&&mmY >= i*fr.FONT_HEIGHT&&mmY < (i+1)*fr.FONT_HEIGHT;
				if(selectionHover)
				{
					hasTarget = true;
					if(hoveredEntry!=j)
					{
						hoveredEntry = j;
						hoverTimer = 0;
					}
					else
						hoverTimer++;
					col = style.getHoverColor();
				}
				if(j > entries.length-1)
					j = entries.length-1;
				String s = translated[j];
				//Thanks, Blu!
				int overLength = s.length()-fr.sizeStringToWidth(s, strWidth);
				if(overLength > 0)//String is too long
				{
					if(selectionHover&&hoverTimer > 20)
					{
						int textOffset = (hoverTimer/10)%(s.length());
						s = s.substring(textOffset)+" "+s.substring(0, textOffset);
					}
					s = fr.trimStringToWidth(s, strWidth);
				}
				float tx = x;
				float ty = yDropDown+(fr.FONT_HEIGHT*i)+1;
				GlStateManager.translate(tx, ty, 0);
				fr.drawString(s, 0, 0, col, false);
				GlStateManager.translate(-tx, -ty, 0);
			}
			GlStateManager.scale(1, 1, 1);
			if(!hasTarget)
			{
				hoveredEntry = -1;
				hoverTimer = 0;
			}

			if(pl.pabilo8.ctmb.client.ClientUtils.isPointInRectangle(x, yDropDown, x+width, yDropDown+hh, mx, my))
				handleWheel();
		}

		if(selectedEntry!=-1)
		{
			String text = translated[selectedEntry];
			int maxW = width-2-12;

			fr.drawString(fr.trimStringToWidth(text, maxW), x+1, y+1, dropped?style.getHoverColor(): (enabled?style.getLinkColor(): style.getDisabledColor()), false);
		}
		fr.drawString(dropped?"▼": "▶", x+0.5f+width-7, y+1, dropped?style.getHoverColor(): (enabled?style.getLinkColor(): style.getDisabledColor()), false);

	}

	private void handleWheel()
	{
		int mouseWheel = Mouse.getEventDWheel();
		if(mouseWheel!=0&&maxOffset > 0&&Mouse.getEventNanoseconds()!=prevWheelNano)
		{
			prevWheelNano = Mouse.getEventNanoseconds();
			if(mouseWheel < 0&&offset < maxOffset)
				offset++;
			if(mouseWheel > 0&&offset > 0)
				offset--;
		}
	}

	/**
	 * Returns true if the mouse has been pressed on this control. Equivalent of MouseListener.mousePressed(MouseEvent e).
	 */
	@Override
	public boolean mousePressed(Minecraft mc, int mx, int my)
	{
		if(!(this.enabled&&this.visible))
			return false;

		if(dropped)
		{
			FontRenderer fr = ClientUtils.mc.fontRenderer;
			int yDropDown = y+2+fr.FONT_HEIGHT;
			int hh = Math.min(perPage, entries.length)*fr.FONT_HEIGHT;
			boolean b = pl.pabilo8.ctmb.client.ClientUtils.isPointInRectangle(x, yDropDown, x+width, yDropDown+hh, mx, my);
			if(b)
			{
				int mmY = my-yDropDown;
				for(int i = 0; i < Math.min(perPage, entries.length); i++)
					if(mmY >= i*fr.FONT_HEIGHT&&mmY < (i+1)*fr.FONT_HEIGHT)
					{
						selectedEntry = offset+i;
						dropped = false;
					}
			}
			else
				dropped = false;
			return selectedEntry!=-1;
		}
		else
		{
			return this.dropped = pl.pabilo8.ctmb.client.ClientUtils.isPointInRectangle(x, y, x+width, y+height, mx, my);
		}
	}

	public String getEntry(int selectedEntry)
	{
		return selectedEntry==-1?"":entries[MathHelper.clamp(selectedEntry, 0, entries.length-1)];
	}

	@Override
	public void setData(DataMap map)
	{
		super.setData(map);
		Map<String, IData> params = map.asMap();

		if(params.containsKey("selected_entry"))
			this.selectedEntry=Arrays.asList(entries).indexOf(params.get("selected_entry").asString());
	}

	@Override
	protected Map<String, IData> getDataInternal(Map<String, IData> map)
	{
		map.put("selected_entry",new DataString(getEntry(selectedEntry)));
		map.put("hovered_entry",new DataString(getEntry(hoveredEntry)));
		map.put("hover_timer",new DataInt(hoverTimer));

		return super.getDataInternal(map);
	}
}
