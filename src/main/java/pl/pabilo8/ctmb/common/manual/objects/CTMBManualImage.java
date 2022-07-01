package pl.pabilo8.ctmb.common.manual.objects;

import blusunrize.lib.manual.ManualUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.*;
import net.minecraft.util.ResourceLocation;
import pl.pabilo8.ctmb.client.ClientUtils;
import pl.pabilo8.ctmb.common.manual.CTMBManualObject;

/**
 * @author Pabilo8
 * @since 22.05.2022
 */
public class CTMBManualImage extends CTMBManualObject
{
	ResourceLocation res = null;
	String alt = "", tooltip = "";
	float[] uv = new float[]{0, 0, 1, 1};
	boolean frame = false;

	//--- Setup ---//

	public CTMBManualImage(ManualObjectInfo info, NBTTagCompound compound)
	{
		super(info, compound);
		if(compound.hasKey("img"))
		{
			//image
			NBTBase img = compound.getTag("img");
			if(img instanceof NBTTagString)
				res = new ResourceLocation(((NBTTagString)img).getString()+".png");

			//dimensions
			if(res!=null&&compound.hasKey("uv")&&compound.getTag("uv") instanceof NBTTagList)
			{
				NBTTagList uv = (NBTTagList)compound.getTag("uv");
				this.uv = new float[]{uv.getFloatAt(0), uv.getFloatAt(1), uv.getFloatAt(2), uv.getFloatAt(3)};
			}

			if(compound.getBoolean("frame"))
				frame = true;
		}
		if(compound.hasKey("alt"))
			alt = compound.getString("alt");
		if(compound.hasKey("tooltip"))
			tooltip = compound.getString("tooltip");
	}

	//--- Rendering, Reaction ---//

	@Override
	public void drawButton(Minecraft mc, int mx, int my, float partialTicks)
	{
		super.drawButton(mc, mx, my, partialTicks);

		if(res!=null)
		{
			if(frame)
			{
				gui.drawGradientRect(x-2, y-2, x+width+2, y+height+2, 0xffeaa74c, 0xfff6b059);
				gui.drawGradientRect(x-1, y-1, x+width+1, y+height+1, 0xffc68e46, 0xffbe8844);
			}

			ClientUtils.bindTexture(res);
			GlStateManager.color(1, 1, 1, 1);
			GlStateManager.enableBlend();
			ManualUtils.drawTexturedRect(x, y, width, height, uv[0], uv[2], uv[1], uv[3]);
		}
		else if(!alt.isEmpty())
		{
			drawString(gui.getManual().fontRenderer, alt, x, y, 0xffeaa74c);
		}

	}

	@Override
	protected int getDefaultHeight()
	{
		return height;
	}

	@Override
	public void mouseDragged(int x, int y, int clickX, int clickY, int mx, int my, int lastX, int lastY, int button)
	{

	}

	@Override
	public void drawTooltip(Minecraft mc, int mx, int my)
	{
		if(hovered&&!tooltip.isEmpty())
			gui.drawHoveringText(tooltip, mx, my);
	}
}
