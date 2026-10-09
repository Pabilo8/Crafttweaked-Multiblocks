package pl.pabilo8.ctmb.common.production;

import crafttweaker.annotations.ZenRegister;
import pl.pabilo8.ctmb.common.storage.StorageDefinition.Kind;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * Coordinates refer to recipe channels, independently of inventory slot numbers.
 */
@ZenRegister
@ZenClass("mods.ctmb.production.RecipeLayout")
public final class RecipeLayout
{
	public final int id, x, y;
	public final Kind kind;
	public final boolean output;

	private RecipeLayout(int id, int x, int y, Kind kind, boolean output)
	{
		if(id < 0||x < 0||y < 0||x > 512||y > 512)
			throw new IllegalArgumentException("Invalid recipe layout coordinates/channel");
		this.id = id;
		this.x = x;
		this.y = y;
		this.kind = kind;
		this.output = output;
	}

	@ZenMethod
	public static RecipeLayout slot(int id, int x, int y)
	{
		return new RecipeLayout(id, x, y, Kind.ITEM, false);
	}

	@ZenMethod
	public static RecipeLayout outputSlot(int id, int x, int y)
	{
		return new RecipeLayout(id, x, y, Kind.ITEM, true);
	}

	@ZenMethod
	public static RecipeLayout fluidTank(int id, int x, int y)
	{
		return new RecipeLayout(id, x, y, Kind.FLUID, false);
	}

	@ZenMethod
	public static RecipeLayout outputFluidTank(int id, int x, int y)
	{
		return new RecipeLayout(id, x, y, Kind.FLUID, true);
	}

	@ZenMethod
	public static RecipeLayout dustTank(int id, int x, int y)
	{
		return new RecipeLayout(id, x, y, Kind.DUST, false);
	}

	@ZenMethod
	public static RecipeLayout outputDustTank(int id, int x, int y)
	{
		return new RecipeLayout(id, x, y, Kind.DUST, true);
	}
}
