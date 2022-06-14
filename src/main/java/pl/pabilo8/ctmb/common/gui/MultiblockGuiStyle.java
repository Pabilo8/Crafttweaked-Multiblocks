package pl.pabilo8.ctmb.common.gui;

import crafttweaker.annotations.ZenRegister;
import net.minecraft.util.ResourceLocation;
import pl.pabilo8.ctmb.CTMB;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.HashMap;

/**
 * @author Pabilo8
 * @since 08.03.2022
 */
@ZenRegister
@ZenClass("mods.ctmb.gui.Style")
@SuppressWarnings("unused")
public class MultiblockGuiStyle
{
	public static final HashMap<String, MultiblockGuiStyle> STYLES = new HashMap<>();
	public static final MultiblockGuiStyle DEFAULT_SKIN;

	static
	{
		DEFAULT_SKIN = new MultiblockGuiStyle(
				"royale", new int[]{0xf78034, 0x0a0a0a, 0x1a1a1a},
				0xE0E0E0,
				0xf78034,
				0
		);

		new MultiblockGuiStyle(
				"steelworks", new int[]{0xf78034, 0x0a0a0a, 0x1a1a1a},
				0xE0E0E0,
				0xf78034,
				0
		);

		new MultiblockGuiStyle(
				"futur", new int[]{0xf78034, 0x0a0a0a, 0x1a1a1a},
				0xE0E0E0,
				0x3493f7,
				0
		);

		new MultiblockGuiStyle(
				"vanilla", new int[]{14737632, 14737632, 14737632},
				0xE0E0E0,
				0xFFFFA0,
				0
		);
	}

	private final ResourceLocation stylePath;
	private final int[] color = new int[3];
	private final int colorLink;
	private final int colorHover;
	private final int colorDisabled;

	public MultiblockGuiStyle(String styleName, int[] color, int colorLink, int colorHover, int colorDisabled)
	{
		this.stylePath = new ResourceLocation(CTMB.MODID, "textures/gui/templates/"+styleName+".png");
		this.colorLink = colorLink;
		this.colorHover = colorHover;
		this.colorDisabled = colorDisabled;
		System.arraycopy(color, 0, this.color, 0, 3);

		STYLES.put(styleName, this);
	}

	@ZenMethod
	public static MultiblockGuiStyle create(String styleName, int[] color, int colorLink, int colorHover, int colorDisabled)
	{
		return new MultiblockGuiStyle(styleName, color, colorLink, colorHover, colorDisabled);
	}

	@ZenMethod
	public ResourceLocation getStylePath()
	{
		return stylePath;
	}

	@ZenMethod
	public int getTitleColor()
	{
		return color[0];
	}

	@ZenMethod
	public int getMainColor()
	{
		return color[1];
	}

	@ZenMethod
	public int getSecondColor()
	{
		return color[2];
	}

	@ZenMethod
	public int getLinkColor()
	{
		return colorLink;
	}

	@ZenMethod
	public int getHoverColor()
	{
		return colorHover;
	}

	@ZenMethod
	public int getDisabledColor()
	{
		return colorDisabled;
	}
}
