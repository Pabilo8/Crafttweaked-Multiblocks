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
import pl.pabilo8.ctmb.common.gui.MultiblockGuiStyle;
import pl.pabilo8.ctmb.common.gui.component.GuiComponent;
import pl.pabilo8.ctmb.common.util.GuiNBTData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
		// TODO: 13.06.2022 finish
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
	}

	@Override
	public void setData(GuiNBTData map)
	{
		super.setData(map);

		if(map.has("display"))
			this.displayType = this.findType(map.get("display"));
	}

	enum Type
	{
		NONE,
		ITEM,
		STRING,
		ICON
	}
}
