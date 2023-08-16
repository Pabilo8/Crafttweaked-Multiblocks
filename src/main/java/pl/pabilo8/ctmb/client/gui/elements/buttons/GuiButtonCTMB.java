package pl.pabilo8.ctmb.client.gui.elements.buttons;

import crafttweaker.api.data.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import pl.pabilo8.ctmb.client.ClientUtils;
import pl.pabilo8.ctmb.client.gui.elements.IGuiTweakable;
import pl.pabilo8.ctmb.common.CommonUtils;
import pl.pabilo8.ctmb.common.gui.MultiblockGuiStyle;
import pl.pabilo8.ctmb.common.gui.component.GuiComponent;
import pl.pabilo8.ctmb.common.util.GuiNBTData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Pabilo8
 * @since 07-07-2019
 */
public class GuiButtonCTMB extends GuiButton implements IGuiTweakable
{
	private final GuiComponent parent;
	private final int texX;
	protected boolean state = false;
	protected final MultiblockGuiStyle style;

	public GuiButtonCTMB(@Nullable GuiComponent parent, int x, int y, int w, int h, int buttonId, String text, MultiblockGuiStyle style, int styleID)
	{
		super(buttonId, x, y, w, h, text);
		this.parent = parent;
		this.texX = styleID*60;
		this.style = style;
	}

	public boolean canClick(Minecraft mc, int mouseX, int mouseY)
	{
		return this.enabled&&this.visible&&mouseX >= this.x&&mouseY >= this.y&&mouseX < this.x+this.width&&mouseY < this.y+this.height;
	}

	/**
	 * Returns true if the mouse has been pressed on this control. Equivalent of MouseListener.mousePressed(MouseEvent e).
	 */
	@Override
	public boolean mousePressed(@Nonnull Minecraft mc, int mouseX, int mouseY)
	{
		return canClick(mc, mouseX, mouseY);
	}

	/**
	 * Draws this button to the screen.
	 */
	@Override
	public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks)
	{
		if(this.visible)
		{
			this.hovered = canClick(mc, mouseX, mouseY);
			this.mouseDragged(mc, mouseX, mouseY);

			ClientUtils.bindTexture(style.getStylePath());
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
			GlStateManager.blendFunc(770, 771);

			drawWithOffset(this.enabled?((this.hovered?2: (state?3:1))): 0);

		}
	}

	protected void drawWithOffset(int offset)
	{
		int texY = 112+(offset*24); //offset by index

		Tessellator tes = Tessellator.getInstance();
		BufferBuilder buffer = tes.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		ClientUtils.drawFrame(buffer, x,y, width, height, 20, 8, texX, texY);
		tes.draw();

		if(displayString!=null&&!displayString.isEmpty()) //draw text
		{
			this.drawCenteredString(ClientUtils.mc.fontRenderer, getText(),
					this.x+this.width/2, this.y+(this.height-8)/2,
					this.enabled?(this.hovered?style.getHoverColor(): style.getLinkColor()): style.getDisabledColor()
			);
		}

	}

	protected String getText()
	{
		return this.displayString;
	}

	@Override
	@Nullable
	public GuiComponent getBlueprint()
	{
		return parent;
	}

	public void setState(boolean state)
	{
		this.state = state;
	}

	@Override
	public void setData(GuiNBTData map)
	{
		this.x = map.getX(x);
		this.y = map.getY(y);

		this.width = map.getWidth(width);
		this.height = map.getHeight(height);

		this.enabled = map.getProperty("enabled", enabled);
		this.visible = map.getProperty("visible", visible);

		this.displayString = map.getText(displayString);
	}

	/**
	 * Overriden by children classes, for adding entries in an easier way
	 *
	 * @return parent's map + own values
	 */
	protected Map<String, IData> getDataInternal(Map<String, IData> map)
	{
		map.put("x", new DataInt(x));
		map.put("y", new DataInt(y));

		map.put("w", new DataInt(width));
		map.put("h", new DataInt(height));

		map.put("text", new DataString(displayString));

		map.put("enabled", new DataBool(enabled));
		map.put("visible", new DataBool(visible));
		map.put("hovered", new DataBool(hovered));
		map.put("activated", new DataBool(state));

		return map;
	}

	@Override
	public final DataMap getData()
	{
		return new DataMap(getDataInternal(new HashMap<>()), true);
	}
}
