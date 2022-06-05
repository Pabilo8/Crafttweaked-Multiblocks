package pl.pabilo8.ctmb.common.crafttweaker.gui.rectangle;

import crafttweaker.api.data.DataMap;
import crafttweaker.api.data.IData;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.pabilo8.ctmb.common.CommonUtils;
import pl.pabilo8.ctmb.common.crafttweaker.gui.MultiblockGuiLayout;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author Pabilo8
 * @since 01.03.2022
 */
public class GuiRectangleCustom extends GuiRectangle
{
	/**
	 * UV Map Coordinates of the rectangle
	 */
	public float u = 0, v = 0, uu = 1, vv = 1;
	/**
	 * Padding (inner margin) and Borderline thickness of the rectangle
	 */
	public int[] padding = new int[]{0, 0, 0, 0}, borderLine = new int[]{1, 1, 1, 1};
	/**
	 * Texture used to fill the background<br>
	 * Is null when a color is used
	 */
	@Nullable
	public ResourceLocation texture;

	public GuiRectangleCustom(int x, int y, int w, int h, @Nullable IData data)
	{
		super(x, y, w, h, data);

		if(CommonUtils.dataCheck(data))
		{
			Map<String, IData> map = data.asMap();

			if(map.containsKey("texture"))
			{
				IData tag = map.get("texture");
				if(tag instanceof DataMap)
				{
					Map<String, IData> mm = tag.asMap();
					int texW = mm.get("texw").asInt();
					int texH = mm.get("texH").asInt();

					this.u = mm.get("u").asInt()/((float)texW);
					this.v = mm.get("v").asInt()/((float)texH);

					this.uu = this.u+(mm.get("w").asInt()/((float)texW));
					this.vv = this.v+(mm.get("h").asInt()/((float)texH));

				}
			}
			if(map.containsKey("padding"))
				this.padding = CommonUtils.get4ParIntArrayFromData(map.get("padding"));
			if(map.containsKey("border_width"))
				this.borderLine = CommonUtils.get4ParIntArrayFromData(map.get("border_width"));
		}
	}

	/**
	 * Called when the GUI is initialised, used to update draw call lists
	 */
	@SideOnly(Side.CLIENT)
	@Override
	public void validate(MultiblockGuiLayout layout)
	{

	}

	/**
	 * Called when the GUI is closed, used to remove draw call lists
	 */
	@SideOnly(Side.CLIENT)
	@Override
	public void invalidate(MultiblockGuiLayout layout)
	{

	}
}
