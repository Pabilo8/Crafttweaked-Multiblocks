package pl.pabilo8.ctmb.common.manual.objects;

import blusunrize.lib.manual.ManualUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants.NBT;
import pl.pabilo8.ctmb.common.manual.CTMBManualObject;

/**
 * @author Pabilo8
 * @since 22.05.2022
 */
public class CTMBManualItemDisplay extends CTMBManualObject
{
	final NonNullList<ItemStack> stacks;
	ItemStack highlighted = ItemStack.EMPTY;

	public CTMBManualItemDisplay(ManualObjectInfo info, NBTTagCompound compound)
	{
		super(info, compound);
		if(compound.hasKey("item"))
		{
			stacks = NonNullList.from(ItemStack.EMPTY, new ItemStack(compound.getCompoundTag("item")));
		}
		else if(compound.hasKey("items"))
		{
			NBTTagList items = compound.getTagList("items", NBT.TAG_COMPOUND);
			stacks = NonNullList.create();

			items.tagList.stream()
					.filter(nbt -> nbt instanceof NBTTagCompound)
					.map(nbt -> ((NBTTagCompound)nbt))
					.map(ItemStack::new)
					.forEach(stacks::add);
		}
		else
			stacks = NonNullList.create();
	}

	//--- Rendering, Reaction ---//

	@Override
	public void drawButton(Minecraft mc, int mx, int my, float partialTicks)
	{
		super.drawButton(mc, mx, my, partialTicks);

		GlStateManager.enableRescaleNormal();
		RenderHelper.enableGUIStandardItemLighting();
		highlighted = ItemStack.EMPTY;
		int length = stacks.size();
		if(length > 0)
		{
			float scale = length > 8?1f: length > 3?1.5f: 2f;
			int line0 = (int)(7.5/scale);
			int line1 = line0-1;
			int lineSum = line0+line1;
			int lines = (length/lineSum*2)+(length%lineSum/line0)+(length%lineSum%line0 > 0?1: 0);
			float equalPerLine = length/(float)lines;
			line1 = (int)Math.floor(equalPerLine);
			line0 = (int)Math.ceil(equalPerLine);
			lineSum = line0+line1;
			int lastLines = length%lineSum;
			int lastLine = lastLines==line0?line0: lastLines==0?line1: lastLines%line0;
			GlStateManager.scale(scale, scale, scale);

			for(int line = 0; line < lines; line++)
			{
				int perLine = line==lines-1?lastLine: line%2==0?line0: line1;
				if(line==0&&perLine > length)
					perLine = length;
				int w2 = perLine*(int)(18*scale)/2;
				for(int i = 0; i < perLine; i++)
				{
					int item = line/2*lineSum+line%2*line0+i;
					if(item >= length)
						break;
					int xx = x+60-w2+(int)(i*18*scale);
					int yy = y+(lines < 2?4: 0)+line*(int)(18*scale);
					ManualUtils.renderItem().renderItemAndEffectIntoGUI(stacks.get(item), (int)(xx/scale), (int)(yy/scale));
					if(mx >= xx&&mx < xx+(16*scale)&&my >= yy&&my < yy+(16*scale))
						highlighted = stacks.get(item);
				}
			}
			GlStateManager.scale(1/scale, 1/scale, 1/scale);
		}
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableRescaleNormal();
		RenderHelper.disableStandardItemLighting();

	}

	@Override
	protected int getDefaultHeight()
	{
		return 36;
	}

	@Override
	public void mouseDragged(int x, int y, int clickX, int clickY, int mx, int my, int lastX, int lastY, int button)
	{

	}

	@Override
	public void drawTooltip(Minecraft mc, int mx, int my)
	{
		gui.getManual().fontRenderer.setUnicodeFlag(false);
		if(!highlighted.isEmpty())
			gui.renderToolTip(highlighted, mx, my);

		RenderHelper.disableStandardItemLighting();
	}
}
