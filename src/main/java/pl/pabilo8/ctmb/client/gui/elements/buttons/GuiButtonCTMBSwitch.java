package pl.pabilo8.ctmb.client.gui.elements.buttons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import pl.pabilo8.ctmb.client.ClientUtils;
import pl.pabilo8.ctmb.common.CommonUtils;
import pl.pabilo8.ctmb.common.gui.MultiblockGuiStyle;
import pl.pabilo8.ctmb.common.gui.component.GuiComponent;

import javax.annotation.Nonnull;

/**
 * @author Pabilo8
 * @since 16.07.2021
 */
public class GuiButtonCTMBSwitch extends GuiButtonCTMBState
{
	private final int textColor;
	private final int bx, by, fx, fy, textWidth;

	final float[] color1, color2;
	int timer;
	final int MAX_SWITCH_TICKS = 20;

	public GuiButtonCTMBSwitch(GuiComponent parent, int buttonId, int x, int y, int textWidth, int styleID, String on, String off, MultiblockGuiStyle texture, boolean state, int textColor, int color1, int color2)
	{
		super(parent, buttonId, x, y, 18, 9, styleID, on, off, texture, state);
		this.textColor = textColor;

		this.textWidth = textWidth;

		this.color1 = CommonUtils.rgbIntToRGB(color1);
		this.color2 = CommonUtils.rgbIntToRGB(color2);

		this.bx = 186;
		this.by = 122+9*styleID;
		this.fx = 190+9*styleID;
		this.fy = 112;

		this.timer = state?0: MAX_SWITCH_TICKS;
	}

	@Override
	public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks)
	{
		if(this.visible)
		{
			ClientUtils.bindTexture(style.getStylePath());

			this.hovered = canClick(mc, mouseX, mouseY);
			GlStateManager.color(1f, 1f, 1f, 1f);
			this.drawTexturedModalRect(x, y, bx, by, 18, 9);

			timer = MathHelper.clamp(timer+(state?-1: 1), 0, MAX_SWITCH_TICKS);

			float progress = 1f-(MathHelper.clamp((timer+(this.state?partialTicks: -partialTicks)), 0, MAX_SWITCH_TICKS)/MAX_SWITCH_TICKS);
			float[] c = ClientUtils.medColour(color1, color2, progress);
			int offset = (int)(progress*10);
			GlStateManager.color(c[0], c[1], c[2], 1f);
			this.drawTexturedModalRect(x+offset, y, fx, fy, 9, 9); //176, 98, 8, 9
			GlStateManager.color(1f, 1f, 1f, 1f);

			this.mouseDragged(mc, mouseX, mouseY);
			if(displayString!=null&&!displayString.isEmpty())
			{
				//textColor
				mc.fontRenderer.drawSplitString(displayString, x+20, y+1, textWidth, textColor);
			}
		}
	}

	/**
	 * Renders the specified text to the screen, center-aligned.
	 * Without shadow.
	 */
	@Override
	public void drawCenteredString(FontRenderer fontRendererIn, @Nonnull String text, int x, int y, int color)
	{
		fontRendererIn.drawString(text, x-fontRendererIn.getStringWidth(text)/2, y, color);
	}

	/**
	 * Renders the specified text to the screen.
	 * Without shadow.
	 */
	@Override
	public void drawString(FontRenderer fontRendererIn, @Nonnull String text, int x, int y, int color)
	{
		fontRendererIn.drawString(text, x, y, color);
	}
}
