package pl.pabilo8.ctmb.client.gui.elements.buttons;

import crafttweaker.api.data.DataMap;
import crafttweaker.api.data.IData;
import net.minecraft.client.Minecraft;
import pl.pabilo8.ctmb.common.crafttweaker.gui.MultiblockGuiStyle;
import pl.pabilo8.ctmb.common.crafttweaker.gui.component.GuiComponent;

import java.util.Map;

/**
 * @author Pabilo8
 * @since 04.03.2022
 */
public class GuiButtonCTMBState extends GuiButtonCTMB
{
	private final String textOn;

	public GuiButtonCTMBState(GuiComponent parent, int buttonId, int x, int y, int w, int h, int styleID, String textOn, String textOff, MultiblockGuiStyle style, boolean state)
	{
		super(parent, x, y, w, h, buttonId, textOff, style, styleID);
		this.state = state;
		this.textOn = textOn;
	}

	/**
	 * Returns true if the mouse has been pressed on this control. Equivalent of MouseListener.mousePressed(MouseEvent e).
	 */
	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
	{
		boolean b = super.mousePressed(mc, mouseX, mouseY);
		if(b)
			this.state = !state;
		return b;
	}

	@Override
	protected String getText()
	{
		return state?displayString: textOn;
	}

	@Override
	public void setData(DataMap map)
	{
		super.setData(map);
		Map<String, IData> params = map.asMap();

		if(params.containsKey("activated"))
			this.state=params.get("activated").asBool();
	}
}
