package pl.pabilo8.ctmb.client.gui.elements;

import crafttweaker.api.data.*;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiLabel;
import pl.pabilo8.ctmb.common.gui.component.GuiComponent;
import pl.pabilo8.ctmb.common.util.GuiNBTData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;

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
	public void drawCenteredString(FontRenderer fontRendererIn, @Nonnull String text, int x, int y, int color)
	{
		fontRendererIn.drawString(text, x-fontRendererIn.getStringWidth(text)/2f, y, color, dropShadow);
	}

	/**
	 * Renders the specified text to the screen.
	 * Without shadow.
	 */
	@Override
	public void drawString(FontRenderer fontRendererIn, @Nonnull String text, int x, int y, int color)
	{
		fontRendererIn.drawString(text, x, y, color, dropShadow);
	}

	/**
	 * For more streamlined label creation
	 */
	public GuiLabelCTMB withLine(String line)
	{
		addLine(line);
		return this;
	}

	@Nonnull
	@Override
	public GuiLabelCTMB setCentered()
	{
		return (GuiLabelCTMB)super.setCentered();
	}

	@Override
	@Nullable
	public GuiComponent getBlueprint()
	{
		return parent;
	}

	@Override
	public void setData(GuiNBTData map)
	{
		this.x = map.getX(x);
		this.y = map.getY(y);

		this.width = map.getWidth(width);
		this.height = map.getHeight(height);

		this.visible = map.getProperty("visible", visible);

		if(map.has("text"))
		{
			labels.clear();
			IData text = map.get("text");
			if(text instanceof DataString)
				labels.add(text.asString());
			else if(text instanceof DataList)
				text.asList().forEach(line -> labels.add(line.asString()));
		}
	}

	@Override
	public final DataMap getData()
	{
		HashMap<String, IData> map = new HashMap<>();

		map.put("x", new DataInt(x));
		map.put("y", new DataInt(y));

		map.put("w", new DataInt(width));
		map.put("h", new DataInt(height));

		map.put("visible", new DataBool(visible));

		return new DataMap(map, true);
	}
}
