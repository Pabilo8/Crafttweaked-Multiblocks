package pl.pabilo8.ctmb.common.gui.rectangle;

import crafttweaker.annotations.ZenDoc;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import pl.pabilo8.ctmb.common.CommonUtils;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author Pabilo8
 * @since 25.02.2022
 */
@ZenClass("mods.ctmb.gui.Rectangle")
@ZenRegister
public abstract class GuiRectangle
{
	/**
	 * X, Y, Width, Height of the rectangle
	 */
	public final int x, y, w, h;

	/**
	 * Foreground and background color, used instead of texture
	 */
	public int fgColor = -1, bgColor = -1;
	/**
	 * Outer Margin of the rectangle
	 */
	public int[] margin = new int[]{0, 0, 0, 0};
	/**
	 * Borderlines layout, if true a border will be drawn on the side<br>
	 * Sides are listed clockwise: up, right, down, left
	 */
	public boolean[] border = new boolean[]{true, true, true, true};

	public GuiRectangle(int x, int y, int w, int h, @Nullable IData data)
	{
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;

		if(CommonUtils.dataCheck(data))
		{
			Map<String, IData> map = data.asMap();

			if(map.containsKey("bgcolor"))
				this.bgColor = CommonUtils.getColorFromData(map.get("bgcolor"));
			if(map.containsKey("fgcolor"))
				this.fgColor = CommonUtils.getColorFromData(map.get("fgcolor"));

			if(map.containsKey("margin"))
				this.margin = CommonUtils.get4ParIntArrayFromData(map.get("margin"));

			if(map.containsKey("border"))
				this.border = CommonUtils.get4ParBoolArrayFromData(map.get("borderline"));
		}
	}

	@ZenMethod()
	@ZenDoc("Creates a styled rectangle with default parameters")
	public static GuiRectangle create(int x, int y, int w, int h)
	{
		return new GuiRectangleStyled(x, y, w, h, null);
	}

	@ZenMethod()
	@ZenDoc("Creates a styled rectangle with default parameters")
	public static GuiRectangle createStyled(int x, int y, int w, int h, @Optional IData data)
	{
		return new GuiRectangleStyled(x, y, w, h, data);
	}

	@ZenMethod()
	@ZenDoc("Creates a custom rectangle with default parameters")
	public static GuiRectangle createCustom(int x, int y, int w, int h, @Optional IData data)
	{
		return new GuiRectangleCustom(x, y, w, h, data);
	}
}
