package pl.pabilo8.ctmb.client.gui.elements.buttons;

import crafttweaker.api.data.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import pl.pabilo8.ctmb.client.ClientUtils;
import pl.pabilo8.ctmb.client.gui.elements.IGuiTweakable;
import pl.pabilo8.ctmb.common.gui.MultiblockGuiStyle;
import pl.pabilo8.ctmb.common.gui.component.GuiComponent;

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

			drawWithOffset(this.enabled?(state?3: (this.hovered?2: 1)): 0);
		}
	}

	protected void drawWithOffset(int offset)
	{
		int wwCorner = MathHelper.clamp((width/2), 0, 20); //first/last part width
		int ww = width-(2*wwCorner); //remaining width
		int texY = 112+(offset*20); //offset by index

		drawTexturedModalRect(x, y, texX, texY, wwCorner, 20); //draw beginning
		for(int xx = 0; xx < ww; xx += 20)
		{
			drawTexturedModalRect(x+wwCorner+xx, y, texX+20, texY, Math.min(ww-xx, 20), 20); //draw middle
		}
		drawTexturedModalRect(x+ww+wwCorner, y, texX+40+(20-wwCorner), texY, wwCorner, 20); //draw end

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

	@Override
	public void setData(DataMap map)
	{
		Map<String, IData> params = map.asMap();

		if(params.containsKey("x"))
			this.x=params.get("x").asInt();
		if(params.containsKey("y"))
			this.x=params.get("y").asInt();

		if(params.containsKey("w"))
			this.width=params.get("w").asInt();
		if(params.containsKey("h"))
			this.height=params.get("h").asInt();

		if(params.containsKey("enabled"))
			this.enabled=params.get("enabled").asBool();
		if(params.containsKey("visible"))
			this.visible=params.get("visible").asBool();

		if(params.containsKey("text"))
			this.displayString=params.get("text").asString();
	}

	/**
	 * Overriden by children classes, for adding entries in an easier way
	 * @return parent's map + own values
	 */
	protected Map<String,IData> getDataInternal(Map<String, IData> map)
	{
		map.put("x",new DataInt(x));
		map.put("y",new DataInt(y));

		map.put("w",new DataInt(width));
		map.put("h",new DataInt(height));

		map.put("text",new DataString(displayString));

		map.put("enabled",new DataBool(enabled));
		map.put("visible",new DataBool(visible));
		map.put("hovered",new DataBool(hovered));
		map.put("activated",new DataBool(state));

		return map;
	}

	@Override
	public final DataMap getData()
	{
		return new DataMap(getDataInternal(new HashMap<>()), true);
	}
}
