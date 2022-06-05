package pl.pabilo8.ctmb.client.gui.elements.buttons;

import net.minecraft.client.gui.FontRenderer;
import pl.pabilo8.ctmb.client.ClientUtils;
import pl.pabilo8.ctmb.common.crafttweaker.gui.MultiblockGuiStyle;
import pl.pabilo8.ctmb.common.crafttweaker.gui.component.GuiComponent;

/**
 * @author Pabilo8
 * @since 18.07.2021
 */
public class GuiButtonCTMBCheckbox extends GuiButtonCTMBState
{
	private final int u, v;

	public GuiButtonCTMBCheckbox(GuiComponent parent, int buttonId, int x, int y, int styleID, String textOn, String textOff, MultiblockGuiStyle style, boolean state)
	{
		super(parent, buttonId, x, y, 9, 9, styleID, textOn, textOff, style, state);
		u = 177;
		v = 122+(styleID*9);
	}

	@Override
	protected void drawWithOffset(int offset)
	{
		this.drawTexturedModalRect(x, y, u, v, width, height);
		if(displayString!=null&&!displayString.isEmpty())
		{
			this.drawString(ClientUtils.mc.fontRenderer, displayString, x+9+2, y+1, style.getMainColor());
		}

		if(state)
			this.drawCenteredString(ClientUtils.mc.fontRenderer, "\u2714", x+width/2+2, y-1, style.getHoverColor());
	}

	/**
	 * Renders the specified text to the screen, center-aligned.
	 * Without shadow.
	 */
	@Override
	public void drawCenteredString(FontRenderer fontRendererIn, String text, int x, int y, int color)
	{
		fontRendererIn.drawString(text, x-fontRendererIn.getStringWidth(text)/2, y, color);
	}

	/**
	 * Renders the specified text to the screen.
	 * Without shadow.
	 */
	@Override
	public void drawString(FontRenderer fontRendererIn, String text, int x, int y, int color)
	{
		fontRendererIn.drawString(text, x, y, color);
	}
}
