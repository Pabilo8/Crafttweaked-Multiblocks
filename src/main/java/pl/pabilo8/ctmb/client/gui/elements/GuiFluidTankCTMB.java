package pl.pabilo8.ctmb.client.gui.elements;

import crafttweaker.api.data.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import org.lwjgl.opengl.GL11;
import pl.pabilo8.ctmb.client.ClientUtils;
import pl.pabilo8.ctmb.common.gui.MultiblockGuiStyle;
import pl.pabilo8.ctmb.common.gui.component.GuiComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Pabilo8
 * @since 07.06.2022
 */
public class GuiFluidTankCTMB extends GuiButton implements IGuiTweakable
{
	private final GuiComponent parent;
	//A supplier, as the instance of the tank can be changed in the TileEntity
	private final Supplier<FluidTank> tank;

	private FluidStack lastFluid;

	private final int styleID;
	private final MultiblockGuiStyle style;
	private static final int CHANGE_SPEED = 50;

	public GuiFluidTankCTMB(@Nullable GuiComponent parent, int buttonId, int x, int y, int w, int h, Supplier<FluidTank> tank, MultiblockGuiStyle style, int styleID)
	{
		super(buttonId, x, y, w, h, "");
		this.parent = parent;
		this.tank = tank;
		this.style = style;
		this.styleID = styleID;
	}

	@Nullable
	@Override
	public GuiComponent getBlueprint()
	{
		return parent;
	}

	@Override
	public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks)
	{
		if(this.visible)
		{
			GlStateManager.pushMatrix();
			FluidTank t = tank.get();

			if(enabled)
			{
				this.lastFluid = tank.get().getFluid();
				this.enabled = false;
			}

			int lastAmount = lastFluid==null?0: lastFluid.amount;
			int fluidAmount = t.getFluidAmount();

			float diff = Math.signum(fluidAmount-lastAmount);
			int capacity = t.getCapacity();

			if(diff!=0)
			{
				if(lastAmount > fluidAmount||t.getFluid()==null||t.getFluid().getFluid()!=lastFluid.getFluid())
					lastFluid.amount = Math.max(lastFluid.amount-CHANGE_SPEED, fluidAmount);
				else
				{
					if(lastFluid==null)
					{
						lastFluid = t.getFluid().copy();
						lastFluid.amount = 0;
					}

					lastFluid.amount = Math.min(lastFluid.amount+CHANGE_SPEED, capacity);
				}
			}

			if(lastFluid!=null&&lastFluid.amount==0)
				lastFluid = null;

			double currentAmount = lastAmount+(diff*Math.min(Math.abs(lastAmount-fluidAmount), CHANGE_SPEED)*partialTicks);

			ClientUtils.bindTexture(style.getStylePath());
			GlStateManager.color(1, 1, 1, 1);

			Tessellator tes = Tessellator.getInstance();
			BufferBuilder buffer = tes.getBuffer();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			ClientUtils.drawFrame(buffer, x, y, width, height, 16, 16, 208+styleID*24, 74);
			tes.draw();

			if(lastFluid!=null)
			{
				int fluidHeight = (int)(height*(currentAmount/(float)capacity));
				ClientUtils.drawRepeatedFluidSprite(lastFluid, x, y+height-fluidHeight, width, fluidHeight);
			}

			ClientUtils.bindTexture(style.getStylePath());
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			ClientUtils.drawFrame(buffer, x, y, width, height, 8, 16, 208, 23);
			tes.draw();
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

		if(params.containsKey("visible"))
			this.visible = params.get("visible").asBool();

		if(params.containsKey("text"))
			this.displayString = params.get("text").asString();
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
