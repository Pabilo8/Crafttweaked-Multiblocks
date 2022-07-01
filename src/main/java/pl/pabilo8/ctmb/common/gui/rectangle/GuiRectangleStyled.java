package pl.pabilo8.ctmb.common.gui.rectangle;

import crafttweaker.api.data.IData;
import pl.pabilo8.ctmb.common.CommonUtils;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author Pabilo8
 * @since 01.03.2022
 */
public class GuiRectangleStyled extends GuiRectangle
{
	/**
	 * Style ID determines which texture is used as the rectangle's background.<br>
	 * Border ID determines the border style:
	 * <ul>
	 *     <li>-1 is the default (round corner) border</li>
	 *     <li>0 is a thin border</li>
	 *     <li>1 is a dotted border</li>
	 *     <li>2 is thick border</li>
	 * </ul>
	 */
	public int styleID = 0, borderID = -1;

	public GuiRectangleStyled(int x, int y, int w, int h, @Nullable IData data)
	{
		super(x, y, w, h, data);

		if(CommonUtils.dataCheck(data))
		{
			Map<String, IData> map = data.asMap();

			if(map.containsKey("style_id"))
				this.styleID = map.get("style_id").asInt();
			if(map.containsKey("border_id"))
				this.borderID = map.get("border_id").asInt();
		}
	}
}
