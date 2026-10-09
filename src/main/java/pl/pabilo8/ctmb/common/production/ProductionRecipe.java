package pl.pabilo8.ctmb.common.production;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import pl.pabilo8.immersiveintelligence.api.crafting.recipe.IIMultiblockRecipe;
import pl.pabilo8.immersiveintelligence.api.crafting.recipe.IIRecipeLayout;
import pl.pabilo8.immersiveintelligence.api.crafting.recipe.RotaryMachineRecipe;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Registered directly in II 0.3.1; handler identity separates machines and production lanes.
 */
@ZenRegister
@ZenClass("mods.ctmb.production.Recipe")
public final class ProductionRecipe extends IIMultiblockRecipe implements RotaryMachineRecipe
{
	public final ProductionHandler handler;
	final List<RecipeValue> inputs, outputs;
	private boolean used;

	ProductionRecipe(ProductionHandler handler, String id, List<RecipeValue> inputs, List<RecipeValue> outputs, int time, int energy)
	{
		super(id);
		this.handler = handler;
		this.inputs = Collections.unmodifiableList(inputs);
		this.outputs = Collections.unmodifiableList(outputs);
		setTimeAndEnergy(time, energy);
		inputList = new ArrayList<>();
		outputList = NonNullList.create();
		fluidInputList = new ArrayList<>();
		fluidOutputList = new ArrayList<>();
		for(RecipeValue v : inputs)
		{
			if(v.ingredient!=null) inputList.add(v.ingredient);
			if(v.fluid!=null) fluidInputList.add(v.fluid.copy());
		}
		for(RecipeValue v : outputs)
		{
			if(!v.item.isEmpty()) outputList.add(v.item.copy());
			if(v.fluid!=null) fluidOutputList.add(v.fluid.copy());
		}
	}

	@ZenMethod
	public ProductionRecipe withName(String name)
	{
		if(used) throw new IllegalStateException("Cannot rename an active recipe");
		if(name==null||!name.matches("[a-z0-9_.-]+")) throw new IllegalArgumentException("Invalid recipe name");
		String id = handler.uid()+"/"+name;
		if(!id.equals(getName())&&ProductionHandler.exists(id))
			throw new IllegalArgumentException("Duplicate recipe: "+id);
		setName(id);
		return this;
	}

	@ZenMethod
	public ProductionRecipe withTimeAndEnergy(int ticks, int totalIF)
	{
		if(used) throw new IllegalStateException("Cannot edit an active recipe");
		if(ticks <= 0||totalIF < 0||(totalIF > 0&&handler.energy()==null))
			throw new IllegalArgumentException("Invalid production time/energy provider");
		setTimeAndEnergy(ticks, totalIF);
		return this;
	}

	@ZenMethod
	public IData getManualSource()
	{
		NBTTagCompound tag = new NBTTagCompound();
		tag.setString("type", IIMultiblockRecipe.getRecipeClassName(ProductionRecipe.class));
		tag.setString("recipe", getName());
		return CraftTweakerMC.getIData(tag);
	}

	void used()
	{
		used = true;
	}

	@Override
	public int getMinSpeed()
	{
		return handler.minSpeed();
	}

	@Override
	public int getMaxSpeed()
	{
		return handler.maxSpeed();
	}

	@Override
	public int getTorque()
	{
		return handler.torque();
	}

	@Override
	protected IIRecipeLayout initRecipeLayout()
	{
		return handler.buildLayout(this);
	}

	@Override
	public boolean matchesSubCategory(String name)
	{
		return handler.uid().equals(name);
	}
}
