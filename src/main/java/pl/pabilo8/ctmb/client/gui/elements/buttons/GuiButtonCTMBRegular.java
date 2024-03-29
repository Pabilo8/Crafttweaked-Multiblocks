package pl.pabilo8.ctmb.client.gui.elements.buttons;

import net.minecraft.client.Minecraft;
import pl.pabilo8.ctmb.common.gui.MultiblockGuiStyle;
import pl.pabilo8.ctmb.common.gui.component.GuiComponent;

import javax.annotation.Nonnull;

/**
 * @author Pabilo8
 * @since 04.03.2022
 */
public class GuiButtonCTMBRegular extends GuiButtonCTMB
{
	public GuiButtonCTMBRegular(GuiComponent parent, int buttonId, int x, int y, int w, int h, int styleID, String text, MultiblockGuiStyle style)
	{
		super(parent, x, y, w, h, buttonId, text, style, styleID);
	}

	@Override
	public boolean mousePressed(@Nonnull Minecraft mc, int mouseX, int mouseY)
	{
		return state = super.mousePressed(mc, mouseX, mouseY);
	}

	@Override
	public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks)
	{
		super.drawButton(mc, mouseX, mouseY, partialTicks);
		state = false;
	}
}
