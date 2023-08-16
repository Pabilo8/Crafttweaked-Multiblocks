package pl.pabilo8.ctmb.client.gui;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.pabilo8.ctmb.client.ClientUtils;
import pl.pabilo8.ctmb.common.gui.rectangle.GuiRectangleStyled;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Pabilo8
 * @since 03.03.2022
 */
@SideOnly(Side.CLIENT)
public class StyledGuiUtils
{
	public static void drawBackgroundRoundedMask(GuiRectangleStyled[] rects, BufferBuilder buffer, int minXOffset, int minYOffset)
	{
		byte[][] outline = getBoxesOutline(rects, 8, minXOffset, minYOffset);

		for(int x = 0; x < outline.length; x++)
			for(int y = 0; y < outline[x].length; y++)
			{
				if(outline[x][y]==1)
				{
					boolean hasRight, hasLeft, hasTop, hasBottom;
					hasRight = x!=outline.length-1&&outline[x+1][y]==1;
					hasLeft = x!=0&&outline[x-1][y]==1;
					hasBottom = y!=outline[x].length-1&&outline[x][y+1]==1;
					hasTop = y!=0&&outline[x][y-1]==1;

					if(hasRight&&hasLeft&&hasTop&&hasBottom)
					{
						boolean hasTL, hasTR, hasBL, hasBR;
						hasTL = outline[x-1][y-1]==1;
						hasTR = outline[x+1][y-1]==1;
						hasBL = outline[x-1][y+1]==1;
						hasBR = outline[x+1][y+1]==1;

						if(hasTL&&hasTR&&hasBL&&hasBR)
							ClientUtils.drawTexturedRect(buffer, minXOffset+x*8, minYOffset+y*8, 8, 216, 8, 8);
						else if(hasBL&&hasBR)
							ClientUtils.drawTexturedRect(buffer, minXOffset+x*8, minYOffset+y*8, hasTR?24: 32, 208, 8, 8);
						else if(hasTL&&hasTR)
							ClientUtils.drawTexturedRect(buffer, minXOffset+x*8, minYOffset+y*8, hasBR?24: 32, 216, 8, 8);
					}

					else if(hasRight&&hasBottom&&hasTop)
						ClientUtils.drawTexturedRect(buffer, minXOffset+x*8, minYOffset+y*8, 0, 216, 8, 8);
					else if(hasLeft&&hasBottom&&hasTop)
						ClientUtils.drawTexturedRect(buffer, minXOffset+x*8, minYOffset+y*8, 16, 216, 8, 8);
					else if(hasBottom&&hasLeft&&hasRight)
						ClientUtils.drawTexturedRect(buffer, minXOffset+x*8, minYOffset+y*8, 8, 208, 8, 8);
					else if(hasTop&&hasLeft&&hasRight)
						ClientUtils.drawTexturedRect(buffer, minXOffset+x*8, minYOffset+y*8, 8, 224, 8, 8);


					else if(hasRight&&hasBottom)
						ClientUtils.drawTexturedRect(buffer, minXOffset+x*8, minYOffset+y*8, 0, 208, 8, 8);
					else if(hasLeft&&hasBottom)
						ClientUtils.drawTexturedRect(buffer, minXOffset+x*8, minYOffset+y*8, 16, 208, 8, 8);
					else if(hasRight&&hasTop)
						ClientUtils.drawTexturedRect(buffer, minXOffset+x*8, minYOffset+y*8, 0, 224, 8, 8);
					else if(hasLeft&&hasTop)
						ClientUtils.drawTexturedRect(buffer, minXOffset+x*8, minYOffset+y*8, 16, 224, 8, 8);
				}
			}
	}

	public static byte[][] getBoxesOutline(GuiRectangleStyled[] rects, int unit, int minXOffset, int minYOffset)
	{
		if(rects.length==0)
			return new byte[0][0];

		int xx, yy;

		GuiRectangleStyled b = Arrays.stream(rects).min((o1, o2) -> o2.x+o2.w-(o1.x+o1.w)).orElse(null);
		xx = b.x+b.w-minXOffset;
		b = Arrays.stream(rects).min((o1, o2) -> o2.y+o2.h-(o1.y+o1.h)).orElse(null);
		yy = b.y+b.h-minYOffset;

		xx /= unit;
		yy /= unit;

		byte[][] fillmap = new byte[xx+1][yy+1];

		//fill box map with 0
		for(int i = 0; i <= xx; i++)
			for(int j = 0; j <= yy; j++)
				fillmap[i][j] = 0;


		//fill box occupied spaces with 1
		for(GuiRectangleStyled rect : rects)
		{
			for(int x = rect.x; x < rect.x+rect.w; x += unit)
				for(int y = rect.y; y < rect.y+rect.h; y += unit)
					fillmap[(x-minXOffset)/unit][(y-minYOffset)/unit] = 1;
		}

		return fillmap;

	}

	/*public static void drawTooltip(int x, int y, int w, int h)
	{
		GlStateManager.translate(0, 0, 300);
		int j1 = -267386864;
		ClientUtils.drawGradientRect(x-3, y-4, x+w+3, y-3, j1, j1, false);
		ClientUtils.drawGradientRect(x-3, y+h+3, x+w+3, y+h+4, j1, j1, false);
		ClientUtils.drawGradientRect(x-3, y-3, x+w+3, y+h+3, j1, j1, false);
		ClientUtils.drawGradientRect(x-4, y-3, x-3, y+h+3, j1, j1, false);
		ClientUtils.drawGradientRect(x+w+3, y-3, x+w+4, y+h+3, j1, j1, false);
		int k1 = 1347420415;
		int l1 = (k1&16711422)>>1|k1&-16777216;
		ClientUtils.drawGradientRect(x-3, y-3+1, x-3+1, y+h+3-1, k1, l1, false);
		ClientUtils.drawGradientRect(x+w+2, y-3+1, x+w+3, y+h+3-1, k1, l1, false);
		ClientUtils.drawGradientRect(x-3, y-3, x+w+3, y-3+1, k1, k1, false);
		ClientUtils.drawGradientRect(x-3, y+h+2, x+w+3, y+h+3, l1, l1, false);
		GlStateManager.translate(0, 0, -300);
	}*/

	public static void drawBackgroundBlock(GuiRectangleStyled[] rects, BufferBuilder buffer)
	{
		for(GuiRectangleStyled rect : rects)
			for(int yy = 0; yy < rect.h; yy += 32)
				for(int xx = 0; xx < rect.w; xx += 32)
					ClientUtils.drawTexturedRect(buffer, ((int)Math.floor(rect.x/8f)*8)+xx, ((int)Math.floor(rect.y/8f)*8)+yy, rect.styleID*32, 32, Math.min(rect.w-xx, 32), Math.min(rect.h-yy, 32));
	}

	public static void drawBorderAround(GuiRectangleStyled[] rects, BufferBuilder buffer)
	{
		for(GuiRectangleStyled rect : rects)
		{
			if(rect.borderID!=-1)
			{
				int x = ((int)Math.floor(rect.x/8f)*8)-rect.margin[3];
				int y = ((int)Math.floor(rect.y/8f)*8)-rect.margin[0];
				int w = rect.w+rect.margin[3]+rect.margin[1];
				int h = rect.h+rect.margin[0]+rect.margin[2];
				int texBegin = rect.borderID*48;

				int ww = Math.min(w/2, 16);
				int hh = Math.min(h/2, 16);

				if(rect.border[0]&&rect.border[1]) //top right
					ClientUtils.drawTexturedRect(buffer, x, y, texBegin, 64, ww, hh);

				if(rect.border[0]&&rect.border[3]) //top left
					ClientUtils.drawTexturedRect(buffer, x+w-ww, y, texBegin+48-ww, 64, ww, hh);

				if(rect.border[2]&&rect.border[1]) //bottom right
					ClientUtils.drawTexturedRect(buffer, x, y+h-hh, texBegin, 112-ww, ww, hh);

				if(rect.border[2]&&rect.border[3]) //bottom left
					ClientUtils.drawTexturedRect(buffer, x+w-ww, y+h-hh, texBegin+48-ww, 112-ww, ww, hh);

				if(w-ww > 0&&h-hh > 0)
				{
					if(rect.border[0])
						for(int i = 16; i < w-16; i += 16)
							ClientUtils.drawTexturedRect(buffer, x+i, y, texBegin+16, 64, Math.min(16, w-16-i), hh);
					if(rect.border[2])
						for(int i = 16; i < w-16; i += 16)
							ClientUtils.drawTexturedRect(buffer, x+i, y+h-hh, texBegin+16, 96, Math.min(16, w-16-i), hh);

					if(rect.border[1])
						for(int i = 16; i < h-16; i += 16)
							ClientUtils.drawTexturedRect(buffer, x, y+i, texBegin, 80, ww, Math.min(16, h-16-i));
					if(rect.border[3])
						for(int i = 16; i < h-16; i += 16)
							ClientUtils.drawTexturedRect(buffer, x+w-16, y+i, texBegin+32, 80, ww, Math.min(16, h-16-i));
				}

			}

		}
	}

	public static void drawItemSlot(int x, int y, BufferBuilder buffer)
	{
		ClientUtils.drawTexturedRect(buffer,
				x-1, y-1,
				40, 208,
				18, 18);
	}

	public static String processText(IComponentGui gui, String text)
	{
		Pattern pattern = Pattern.compile("\\$\\{(.+?)}");
		Matcher matcher = pattern.matcher(text);

		//populate the replacements map ...
		StringBuilder builder = new StringBuilder();
		int i = 0;
		while(matcher.find())
		{
			String replacement = gui.parseVariable(matcher.group(1));
			builder.append(text, i, matcher.start());
			if(replacement==null)
				builder.append(matcher.group(0));
			else
				builder.append(replacement);
			i = matcher.end();
		}
		builder.append(text.substring(i));
		return builder.toString();
	}
}
