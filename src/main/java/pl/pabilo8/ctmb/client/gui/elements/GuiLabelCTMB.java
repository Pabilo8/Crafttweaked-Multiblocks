package pl.pabilo8.ctmb.client.gui.elements;

import crafttweaker.api.data.*;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiLabel;
import pl.pabilo8.ctmb.common.crafttweaker.gui.component.GuiComponent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Pabilo8
 * @since 16.07.2021
 */
public class GuiLabelCTMB extends GuiLabel implements IGuiTweakable
{
	@Nullable
	private final GuiComponent parent;
	private final boolean dropShadow;

	public GuiLabelCTMB(@Nullable GuiComponent parent, FontRenderer fontRendererObj, int id, int x, int y, int w, int h, int textColor, boolean dropShadow)
	{
		super(fontRendererObj, id, x, y, w, h, textColor);
		this.parent = parent;
		this.dropShadow = dropShadow;
	}

	/**
	 * Renders the specified text to the screen, center-aligned.
	 * Without shadow.
	 */
	@Override
	public void drawCenteredString(FontRenderer fontRendererIn, String text, int x, int y, int color)
	{
		fontRendererIn.drawString(text, x-fontRendererIn.getStringWidth(text)/2, y, color, dropShadow);
	}

	/**
	 * Renders the specified text to the screen.
	 * Without shadow.
	 */
	@Override
	public void drawString(FontRenderer fontRendererIn, String text, int x, int y, int color)
	{
		fontRendererIn.drawString(text, x, y, color, dropShadow);
	}

	@Override
	@Nullable
	public GuiComponent getBlueprint()
	{
		return parent;
	}

	@Override
	public void setData(DataMap map)
	{
		Map<String, IData> params = map.asMap();

		if(params.containsKey("x"))
			this.x=params.get("x").asInt();
		if(params.containsKey("y"))
			this.x=params.get("y").asInt();

		if(params.containsKey("w"))
			this.width=params.get("w").asInt();
		if(params.containsKey("h"))
			this.height=params.get("h").asInt();

		if(params.containsKey("visible"))
			this.visible=params.get("visible").asBool();

		// TODO: 03.06.2022 change text on a label
		/*if(params.containsKey("text"))
			this.label=params.get("text").asString();*/
	}

	@Override
	public DataMap getData()
	{
		Map<String, IData> map = new HashMap<>();

		map.put("x",new DataInt(x));
		map.put("y",new DataInt(y));

		map.put("w",new DataInt(width));
		map.put("h",new DataInt(height));

		map.put("visible",new DataBool(visible));

		return new DataMap(map, true);
	}
}
