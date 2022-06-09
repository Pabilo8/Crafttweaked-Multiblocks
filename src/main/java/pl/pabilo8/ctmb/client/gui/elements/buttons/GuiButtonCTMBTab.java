package pl.pabilo8.ctmb.client.gui.elements.buttons;

import blusunrize.immersiveengineering.common.IEContent;
import crafttweaker.api.data.DataMap;
import crafttweaker.api.data.DataString;
import crafttweaker.api.data.IData;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import pl.pabilo8.ctmb.client.ClientUtils;
import pl.pabilo8.ctmb.common.crafttweaker.gui.MultiblockGuiStyle;
import pl.pabilo8.ctmb.common.crafttweaker.gui.component.GuiComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author Pabilo8
 * @since 04.03.2022
 */
public class GuiButtonCTMBTab extends GuiButtonCTMB
{
	private Object displayedObject;
	private Type displayType;

	public GuiButtonCTMBTab(@Nullable GuiComponent parent, int buttonId, int x, int y, int w, int h, MultiblockGuiStyle style, int styleID, @Nullable IData displayed)
	{
		super(parent, x, y, w, h, buttonId, "", style, styleID);
		this.displayedObject = displayed;
		displayType = findType(displayed);
	}

	@Nonnull
	private Type findType(IData displayed)
	{
		if(displayed==null)
			displayed = CraftTweakerMC.getIData(new ItemStack(IEContent.itemTool, 1).serializeNBT());

		if(displayed instanceof DataMap)
		{
			displayedObject = new ItemStack(CraftTweakerMC.getNBTCompound(displayed));
			return Type.ITEM;
		}
		else if(displayed instanceof DataString)
		{
			displayedObject = displayed.asString();
			return Type.STRING;
		}

		return Type.NONE;
	}

	protected void drawWithOffset(int offset)
	{
		drawTexturedModalRect(x, y, 116, 227, 28, 24);

		switch(displayType)
		{
			case ITEM:
			{
				RenderHelper.enableGUIStandardItemLighting();
				GlStateManager.pushMatrix();
				ClientUtils.mc.getRenderItem().renderItemAndEffectIntoGUI(((ItemStack)displayedObject), x+7, y+4);
				GlStateManager.popMatrix();
			}
			break;
			case STRING:
			{
				drawString(ClientUtils.mc.fontRenderer, ((String)displayedObject),x,y,style.getLinkColor());
			}
			break;
			default:
			case NONE:
				break;
		}

		/*int wwCorner = MathHelper.clamp((width/2), 0, 20); //first/last part width
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
		}*/
	}

	@Override
	public void setData(DataMap map)
	{
		super.setData(map);
		Map<String, IData> params = map.asMap();

		if(params.containsKey("display"))
			this.displayType = this.findType(params.get("display"));
	}

	enum Type
	{
		NONE,
		ITEM,
		STRING,
		ICON
	}
}
