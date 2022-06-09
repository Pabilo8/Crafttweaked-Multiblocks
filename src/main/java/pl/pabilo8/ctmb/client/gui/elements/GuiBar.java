package pl.pabilo8.ctmb.client.gui.elements;

import crafttweaker.api.data.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fluids.FluidTank;
import org.lwjgl.opengl.GL11;
import pl.pabilo8.ctmb.client.ClientUtils;
import pl.pabilo8.ctmb.common.crafttweaker.gui.MultiblockGuiStyle;
import pl.pabilo8.ctmb.common.crafttweaker.gui.component.GuiComponent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Pabilo8
 * @since 07.06.2022
 */
public class GuiBar extends GuiButton implements IGuiTweakable
{
	private MultiblockGuiStyle style;
	private final GuiComponent parent;
	private final int styleID;
	private final int color1, color2;
	private float bar = 1.0f;

	public GuiBar(@Nullable GuiComponent parent, int buttonId, int x, int y, int w, int h, MultiblockGuiStyle style, int styleID, int color1, int color2)
	{
		super(buttonId, x, y, w, h, "");
		this.parent = parent;
		this.style = style;
		this.styleID = styleID;

		this.color1 = color1;
		this.color2 = color2;
	}

	@Nullable
	@Override
	public GuiComponent getBlueprint()
	{
		return parent;
	}

	public float getBar()
	{
		return bar;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
	{
		if(this.visible)
		{
			GlStateManager.pushMatrix();
			ClientUtils.bindTexture(style.getStylePath());
			GlStateManager.color(1, 1, 1, 1);

			Tessellator tes = Tessellator.getInstance();
			BufferBuilder buffer = tes.getBuffer();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			ClientUtils.drawFrame(buffer, x, y, width, height, 4, 32, 184+styleID*28, 16);
			tes.draw();

			boolean horizontal = width > height;
			float bar = getBar();

			int ww = (int)(width*(horizontal?bar: 1f));
			int hh = (int)(height*(!horizontal?bar: 1f));

			ClientUtils.drawGradientRect(x+2+(width-ww), y+2+(height-hh), x-2+width, y-2+height, color1, color2, horizontal);

			GlStateManager.popMatrix();

		}
	}

	@Override
	public void setData(DataMap map)
	{
		Map<String, IData> params = map.asMap();

		if(params.containsKey("x"))
			this.x = params.get("x").asInt();
		if(params.containsKey("y"))
			this.x = params.get("y").asInt();

		if(params.containsKey("w"))
			this.width = params.get("w").asInt();
		if(params.containsKey("h"))
			this.height = params.get("h").asInt();

		if(params.containsKey("bar"))
			this.bar = params.get("bar").asFloat();

		if(params.containsKey("visible"))
			this.visible = params.get("visible").asBool();
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

		map.put("bar", new DataFloat(bar));

		map.put("visible", new DataBool(visible));
		map.put("hovered", new DataBool(hovered));


		return map;
	}

	@Override
	public final DataMap getData()
	{
		return new DataMap(getDataInternal(new HashMap<>()), true);
	}
}