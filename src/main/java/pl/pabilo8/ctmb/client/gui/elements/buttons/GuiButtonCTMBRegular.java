package pl.pabilo8.ctmb.client.gui.elements.buttons;

import net.minecraft.client.Minecraft;
import pl.pabilo8.ctmb.common.crafttweaker.gui.MultiblockGuiStyle;
import pl.pabilo8.ctmb.common.crafttweaker.gui.component.GuiComponent;

/**
 * @author Pabilo8
 * @since 04.03.2022
 */
public class GuiButtonCTMBRegular extends GuiButtonCTMB
{
	public GuiButtonCTMBRegular(GuiComponent parent, int buttonId, int x, int y, int w, int h, int styleID, String text, MultiblockGuiStyle style)
	{
		super(parent, x, y, w, buttonId, styleID, text, style, h);
	}

	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
	{
		return state = super.mousePressed(mc, mouseX, mouseY);
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
	{
		super.drawButton(mc, mouseX, mouseY, partialTicks);
		state = false;
	}
}
