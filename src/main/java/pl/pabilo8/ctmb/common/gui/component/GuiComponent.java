package pl.pabilo8.ctmb.common.gui.component;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import crafttweaker.api.minecraft.CraftTweakerMC;
import lombok.Getter;
import pl.pabilo8.immersiveintelligence.common.util.easynbt.EasyNBT;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Stores a side-neutral definition for a Deco component.
 *
 * @author Pabilo8 (pabilo@iiteam.net)
 * @since 25.02.2022
 * @updated 05.10.2026
 */
@ZenRegister
@ZenClass("mods.ctmb.gui.Component")
@Getter
public class GuiComponent
{
	private static final Set<String> TYPES = new HashSet<>(Arrays.asList(
			"button", "checkbox", "switch", "slider", "dropdown", "text", "label", "bar", "energy", "fluid", "slot"));
	private final String name, type;
	private final int x, y;
	private final EasyNBT options;

	private GuiComponent(String type, String name, int x, int y, IData data)
	{
		if(!TYPES.contains(type))
			throw new IllegalArgumentException("Unknown Deco component type: "+type);
		if(name==null||name.isEmpty())
			throw new IllegalArgumentException("A component needs a name");
		this.type = type;
		this.name = name;
		this.x = x;
		this.y = y;
		this.options = EasyNBT.wrapNBT(CraftTweakerMC.getNBTCompound(data).copy());
		if((options.hasKey("w")&&options.getInt("w") < 1)||(options.hasKey("h")&&options.getInt("h") < 1))
			throw new IllegalArgumentException("Component dimensions must be positive");
	}

	/** Creates a component definition from NBT options. */
	@ZenMethod
	public static GuiComponent create(String type, String name, int x, int y, IData data)
	{
		return new GuiComponent(type, name, x, y, data);
	}

	@ZenMethod
	public GuiComponent withSize(int width, int height)
	{
		if(width < 1||height < 1)
			throw new IllegalArgumentException("Component dimensions must be positive");
		options.withInt("w", width).withInt("h", height);
		return this;
	}

	@ZenMethod
	public GuiComponent withText(String text)
	{
		options.withString("text", text);
		return this;
	}
}
