package pl.pabilo8.ctmb.common.production;

import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.common.util.compat.crafttweaker.CraftTweakerHelper;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import pl.pabilo8.ctmb.common.storage.StorageDefinition.Kind;
import pl.pabilo8.immersiveintelligence.api.crafting.DustStack;
import pl.pabilo8.immersiveintelligence.api.crafting.DustUtils;

/**
 * Native quantities. Persisted process outputs are snapshots, independent of later recipe changes.
 */
public final class RecipeValue
{
	public final Kind kind;
	public IngredientStack ingredient;
	public ItemStack item = ItemStack.EMPTY;
	public FluidStack fluid;
	public DustStack dust;

	private RecipeValue(Kind kind)
	{
		this.kind = kind;
	}

	public static RecipeValue convert(IIngredient argument, Kind kind, boolean output)
	{
		if(argument==null||argument.getAmount() <= 0)
			throw new IllegalArgumentException("Positive recipe ingredient required");
		RecipeValue value = new RecipeValue(kind);
		switch(kind)
		{
			case ITEM:
				if(argument instanceof ILiquidStack)
					throw new IllegalArgumentException("Item channel cannot accept a fluid");
				if(output)
				{
					if(!(argument instanceof IItemStack))
						throw new IllegalArgumentException("Item outputs must be concrete item stacks");
					value.item = CraftTweakerMC.getItemStack((IItemStack)argument).copy();
					if(value.item.isEmpty()) throw new IllegalArgumentException("Empty recipe output");
				}
				else
				{
					value.ingredient = CraftTweakerHelper.toIEIngredientStack(argument);
					if(value.ingredient==null)
						throw new IllegalArgumentException("Unsupported item ingredient (use an item or ore entry)");
					value.ingredient.inputSize = argument.getAmount();
				}
				break;
			case FLUID:
				if(!(argument instanceof ILiquidStack))
					throw new IllegalArgumentException("Fluid channel requires a liquid stack");
				FluidStack f = CraftTweakerMC.getLiquidStack((ILiquidStack)argument);
				if(f==null||f.amount <= 0) throw new IllegalArgumentException("Invalid fluid quantity");
				value.fluid = f.copy();
				break;
			case DUST:
				if(!(argument instanceof IItemStack))
					throw new IllegalArgumentException("Dust channel requires an II-registered dust item");
				value.dust = DustUtils.fromItemStack(CraftTweakerMC.getItemStack((IItemStack)argument));
				if(value.dust==null||value.dust.isEmpty()||value.dust.amount <= 0)
					throw new IllegalArgumentException("Item is not a registered II dust");
				value.dust = value.dust.copy();
				break;
			default:
				throw new IllegalArgumentException("Unsupported production channel: "+kind);
		}
		return value;
	}

	public IngredientStack displayIngredient()
	{
		java.util.List<ItemStack> stacks = new java.util.ArrayList<>();
		for(ItemStack stack : ingredient.getStackList())
		{
			ItemStack copy = stack.copy();
			copy.setCount(ingredient.inputSize);
			stacks.add(copy);
		}
		// Released II's JEI wrapper uses getStackList directly; preserve ore quantities in the shared layout.
		return new IngredientStack(stacks, ingredient.inputSize);
	}

	public NBTTagCompound save()
	{
		NBTTagCompound tag = new NBTTagCompound();
		tag.setString("kind", kind.name());
		switch(kind)
		{
			case ITEM:
				tag.setTag("value", item.writeToNBT(new NBTTagCompound()));
				tag.setInteger("amount", item.getCount());
				break;
			case FLUID:
				tag.setTag("value", fluid.writeToNBT(new NBTTagCompound()));
				break;
			case DUST:
				tag.setTag("value", dust.serializeNBT());
				break;
			default:
				throw new IllegalStateException();
		}
		return tag;
	}

	public static RecipeValue load(NBTTagCompound tag)
	{
		RecipeValue v = new RecipeValue(Kind.valueOf(tag.getString("kind")));
		NBTTagCompound n = tag.getCompoundTag("value");
		switch(v.kind)
		{
			case ITEM:
				v.item = new ItemStack(n);
				v.item.setCount(tag.getInteger("amount"));
				if(v.item.isEmpty()||v.item.getCount() <= 0)
					throw new IllegalArgumentException("Invalid saved item output");
				break;
			case FLUID:
				v.fluid = FluidStack.loadFluidStackFromNBT(n);
				if(v.fluid==null||v.fluid.amount <= 0) throw new IllegalArgumentException("Invalid saved fluid output");
				break;
			case DUST:
				v.dust = new DustStack(n);
				if(v.dust.isEmpty()||v.dust.amount <= 0)
					throw new IllegalArgumentException("Invalid saved dust output");
				break;
			default:
				throw new IllegalArgumentException("Invalid saved output kind");
		}
		return v;
	}
}
