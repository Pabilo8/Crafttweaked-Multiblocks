package pl.pabilo8.ctmb.common.manual;

import blusunrize.lib.manual.gui.GuiButtonManual;
import blusunrize.lib.manual.gui.GuiManual;
import crafttweaker.api.data.IData;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;

/**
 * @author Pabilo8
 * @since 22.03.2022
 */
public abstract class CTMBManualObject extends GuiButtonManual
{
	//--- Setup ---//

	private String dataSourceName = null;
	protected IData dataSource = null;

	public CTMBManualObject(ManualObjectInfo info, NBTTagCompound compound)
	{
		super(info.gui, info.id, info.x, info.y,
				compound.hasKey("w")?compound.getInteger("w"): 120,
				0,
				"");

		this.height = compound.hasKey("h")?compound.getInteger("h"): getDefaultHeight();

		if(compound.hasKey("source"))
			dataSourceName = compound.getString("source");
	}

	/**
	 * Used to add items to page (for search reference) and configure stuff based on the page
	 */
	public void postInit(CTMBManualPage page)
	{
		if(dataSourceName!=null)
			dataSource = page.getDataSource(dataSourceName);
	}

	@Override
	public void drawButton(Minecraft mc, int mx, int my, float partialTicks)
	{
		if(this.visible)
		{
			this.hovered = mx >= this.x&&mx < (this.x+this.width)&&my >= this.y&&my < (this.y+this.height);
			this.mouseDragged(mc, mx, my);
		}
	}

	protected abstract int getDefaultHeight();

	//do not extend
	@Override
	protected final void mouseDragged(@Nonnull Minecraft mc, int mouseX, int mouseY)
	{

	}

	/**
	 * Called when mouse has been dragged across the gui
	 *
	 * @param x      gui X
	 * @param y      gui Y
	 * @param clickX the initial point of drag X
	 * @param clickY the initial point of drag Y
	 * @param mx     current mouse X
	 * @param my     current mouse Y
	 * @param lastX  mouse X from last time the method was called
	 * @param lastY  mouse Y from last time the method was called
	 * @param button mouse button ID
	 */
	public abstract void mouseDragged(int x, int y, int clickX, int clickY, int mx, int my, int lastX, int lastY, int button);

	/**
	 * Used for drawing tooltips, called after drawing buttons
	 */
	public void drawTooltip(Minecraft mc, int mx, int my)
	{

	}

	public static class ManualObjectInfo
	{
		final GuiManual gui;
		final int x;
		final int y;
		final int id;

		public ManualObjectInfo(GuiManual gui, int x, int y, int id)
		{
			this.gui = gui;
			this.x = x;
			this.y = y;
			this.id = id;
		}
	}
}
