package pl.pabilo8.ctmb.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import pl.pabilo8.ctmb.common.CommonUtils;

import java.util.Random;

/**
 * @author Pabilo8
 * @since 29.01.2022
 */
@SideOnly(Side.CLIENT)
public class ClientUtils
{
	@SideOnly(Side.CLIENT)
	public static Minecraft mc = Minecraft.getMinecraft();
	public static final Random RAND = new Random();
	public static float zLevel = 0;

	public static void drawRectangle(int x, int y, int w, int h, int color)
	{
		drawRectangle(x, y, w, h, color, GL11.GL_QUADS);
	}

	public static void drawRectangle(int x, int y, int w, int h, int color, int mode)
	{
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();

		float[] rgb = CommonUtils.rgbIntToRGB(color);

		bufferBuilder.begin(mode, DefaultVertexFormats.POSITION_COLOR);
		bufferBuilder.pos(x, y+h, 0.0D).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
		bufferBuilder.pos(x+w, y+h, 0.0D).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
		bufferBuilder.pos(x+w, y, 0.0D).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
		bufferBuilder.pos(x, y, 0.0D).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
		tessellator.draw();
	}

	public static void drawTexturedRect(BufferBuilder bufferBuilder, int x, int y, int textureX, int textureY, int width, int height)
	{
		bufferBuilder.pos(x, y+height, zLevel).tex((float)(textureX)*0.00390625F, (float)(textureY+height)*0.00390625F).endVertex();
		bufferBuilder.pos(x+width, y+height, zLevel).tex((float)(textureX+width)*0.00390625F, (float)(textureY+height)*0.00390625F).endVertex();
		bufferBuilder.pos(x+width, y, zLevel).tex((float)(textureX+width)*0.00390625F, (float)(textureY)*0.00390625F).endVertex();
		bufferBuilder.pos(x, y, zLevel).tex((float)(textureX)*0.00390625F, (float)(textureY)*0.00390625F).endVertex();
	}

	public static void drawTexturedRect(int x, int y, int textureX, int textureY, int width, int height)
	{
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		drawTexturedRect(bufferBuilder, x, y, textureX, textureY, width, height);
		tessellator.draw();
	}

	public static void drawTexturedRectScaled(int x, int y, int width, int height, float u, float v, float uu, float vv)
	{
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		bufferBuilder.pos(x, y+height, zLevel).tex(u, vv).endVertex();
		bufferBuilder.pos(x+width, y+height, zLevel).tex(uu, vv).endVertex();
		bufferBuilder.pos(x+width, y, zLevel).tex(uu, v).endVertex();
		bufferBuilder.pos(x, y, zLevel).tex(u, v).endVertex();
		tessellator.draw();
	}

	public static TextureManager getRenderEngine()
	{
		return mc.renderEngine;
	}

	public static boolean isPointInRectangle(double x, double y, double xx, double yy, double px, double py)
	{
		return px >= x&&px < xx&&py >= y&&py < yy;
	}

	public static float[] medColour(float[] colour1, float[] colour2, float progress)
	{
		float rev = 1f-progress;
		return new float[]{
				(colour1[0]*rev+colour2[0]*progress),
				(colour1[1]*rev+colour2[1]*progress),
				(colour1[2]*rev+colour2[2]*progress)
		};
	}

	public static void drawGradientRect(int x0, int y0, int x1, int y1, int colour0, int colour1, boolean horizontal)
	{
		float[] f0 = CommonUtils.rgbIntToRGB(colour0);
		float[] f1 = CommonUtils.rgbIntToRGB(colour1);

		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldrenderer = tessellator.getBuffer();
		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

		if(horizontal)
		{
			worldrenderer.pos(x1, y0, 0).color(f1[0], f1[1], f1[2], 1f).endVertex();
			worldrenderer.pos(x0, y0, 0).color(f0[0], f0[1], f0[2], 1f).endVertex();
			worldrenderer.pos(x0, y1, 0).color(f0[0], f0[1], f0[2], 1f).endVertex();
			worldrenderer.pos(x1, y1, 0).color(f1[0], f1[1], f1[2], 1f).endVertex();

		}
		else
		{
			worldrenderer.pos(x1, y0, 0).color(f0[0], f0[1], f0[2], 1f).endVertex();
			worldrenderer.pos(x0, y0, 0).color(f0[0], f0[1], f0[2], 1f).endVertex();
			worldrenderer.pos(x0, y1, 0).color(f1[0], f1[1], f1[2], 1f).endVertex();
			worldrenderer.pos(x1, y1, 0).color(f1[0], f1[1], f1[2], 1f).endVertex();
		}


		tessellator.draw();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableTexture2D();
	}

	public static void drawRepeatedFluidSprite(FluidStack fluid, float x, float y, float w, float h)
	{
		bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		TextureAtlasSprite sprite = mc.getTextureMapBlocks().getAtlasSprite(fluid.getFluid().getStill(fluid).toString());

		int col = fluid.getFluid().getColor(fluid);
		GlStateManager.color((col>>16&255)/255.0f, (col>>8&255)/255.0f, (col&255)/255.0f, 1);
		int iW = sprite.getIconWidth();
		int iH = sprite.getIconHeight();
		if(iW > 0&&iH > 0)
			blusunrize.immersiveengineering.client.ClientUtils.drawRepeatedSprite(x, y, w, h, iW, iH, sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV());
	}

	public static void drawFrame(BufferBuilder buffer, int x, int y, int w, int h, int frameW, int frameH, int u, int v)
	{
		int eX = Math.min(frameW, w/2), eY = Math.min(frameH, h/2);
		int ww = w-(2*eX), hh = h-(2*eY);

		//top
		ClientUtils.drawTexturedRect(buffer, x, y, u, v, eX, eY);
		ClientUtils.drawTexturedRect(buffer, x+ww+eX, y, u+(2*frameW), v, eX, eY);
		for(int j = 0; j < ww; j += frameW)
			ClientUtils.drawTexturedRect(buffer, x+eX+j, y, u+frameW, v, Math.min(ww-j, frameW), eY);

		//bottom
		ClientUtils.drawTexturedRect(buffer, x, y+hh+eY, u, v+(2*frameH), eX, eY);
		ClientUtils.drawTexturedRect(buffer, x+ww+eX, y+hh+eY, u+(2*frameW), v+(2*frameH), eX, eY);

		for(int j = 0; j < ww; j += frameW)
			ClientUtils.drawTexturedRect(buffer, x+eX+j, y+hh+eY, u+frameW, v+(2*frameH), Math.min(ww-j, frameW), eY);

		for(int i = 0; i < hh; i += frameH)
		{
			int aH = Math.min(hh-i, frameH); //actual height

			ClientUtils.drawTexturedRect(buffer, x, y+eY+i, u, v+frameW, eX, aH);
			ClientUtils.drawTexturedRect(buffer, x+ww+eX, y+eY+i, u+(2*frameW), v+frameH, eX, aH);

			for(int j = 0; j < ww; j += frameW)
				ClientUtils.drawTexturedRect(buffer, x+eX+j, y+eY+i, u+frameW, v+frameH, Math.min(ww-j, frameW), aH);
		}
	}

	public static void bindTexture(ResourceLocation resLoc)
	{
		ClientUtils.getRenderEngine().bindTexture(resLoc);
	}

	public static void bindAtlas()
	{
		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
	}

	public static void setzLevel(float zLevel)
	{
		ClientUtils.zLevel = zLevel;
	}
}
