package pl.pabilo8.ctmb.client.gui.elements;

import crafttweaker.api.data.DataInt;
import crafttweaker.api.data.IData;
import net.minecraft.client.Minecraft;
import pl.pabilo8.ctmb.client.gui.elements.buttons.GuiButtonCTMB;
import pl.pabilo8.ctmb.common.gui.MultiblockGuiStyle;
import pl.pabilo8.ctmb.common.gui.component.GuiComponent;
import pl.pabilo8.ctmb.common.util.GuiNBTData;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author Pabilo8
 * @since 05.07.2022
 */
public class GuiDroppedCTMB extends GuiButtonCTMB
{
	private int homeX;
	private int homeY;
	private final IDragAndDropGUI dnd;

	public GuiDroppedCTMB(@Nullable GuiComponent parent, int buttonId, int x, int y, int w, int h, MultiblockGuiStyle style, String display, IDragAndDropGUI dnd)
	{
		super(parent, x, y, w, h, buttonId, display, style, 0);
		this.homeX = x;
		this.homeY = y;
		this.dnd = dnd;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
	{
		super.drawButton(mc, mouseX, mouseY, partialTicks);
	}

	@Override
	protected void mouseDragged(Minecraft mc, int mouseX, int mouseY)
	{
		if(visible&&state)
		{
			this.x = alignToGrid(mouseX, this.dnd.getXGridSize(this, mouseX, mouseY));
			this.y = alignToGrid(mouseY, this.dnd.getYGridSize(this, mouseX, mouseY));
		}
	}

	private int alignToGrid(int value, int grid)
	{
		return (int)Math.floor(value/(float)grid)*grid;
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY)
	{
		if(visible&&dnd!=null&&hovered)
		{
			if(state&&dnd.dropOnto(this, x, y))
			{
				this.x = homeX;
				this.y = homeY;
			}
			state = !state;

		}
	}

	@Override
	public void setData(GuiNBTData map)
	{
		super.setData(map);

		this.homeX = map.getX(homeX);
		this.homeY = map.getY(homeY);
	}

	@Override
	protected Map<String, IData> getDataInternal(Map<String, IData> map)
	{
		super.getDataInternal(map);

		map.put("x", new DataInt(homeX));
		map.put("y", new DataInt(homeY));

		return map;
	}
}
