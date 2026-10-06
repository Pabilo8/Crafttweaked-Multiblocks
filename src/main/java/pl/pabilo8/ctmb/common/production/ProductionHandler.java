package pl.pabilo8.ctmb.common.production;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import pl.pabilo8.ctmb.common.block.crafttweaker.Multiblock;
import pl.pabilo8.ctmb.common.storage.StorageDefinition;
import pl.pabilo8.ctmb.common.storage.StorageDefinition.Kind;
import pl.pabilo8.immersiveintelligence.api.crafting.recipe.*;
import java.util.*;
import java.util.stream.Collectors;
import stanhebben.zenscript.annotations.*;

/** Frozen channel configuration; recipes subsequently enter II's native registry. */
@ZenRegister
@ZenClass("mods.ctmb.production.ProductionHandler")
public final class ProductionHandler
{
	public final Multiblock multiblock;
	public final String name;
	private final SortedMap<Integer, Binding> inputs = new TreeMap<>(), outputs = new TreeMap<>();
	private RecipeLayout[] layout;
	private String energy, redstone, rotary;
	private int minSpeed, maxSpeed = Integer.MAX_VALUE, torque;
	private int time = 200, cost = 1600, lanes = 1, sequence;
	private boolean frozen;

	public ProductionHandler(Multiblock multiblock, String name)
	{
		if(name==null||!name.matches("[a-z0-9_.-]+")) throw new IllegalArgumentException("Invalid production name");
		this.multiblock = multiblock;
		this.name = name;
	}

	private void mutable()
	{
		if(frozen) throw new IllegalStateException("Production handler is frozen: "+name);
	}

	@ZenMethod
	public ProductionHandler withInput(int id, String storage)
	{
		return withInput(id, storage, new int[0]);
	}

	@ZenMethod
	public ProductionHandler withInput(int id, String storage, int[] slots)
	{
		bind(inputs, id, storage, slots);
		return this;
	}

	@ZenMethod
	public ProductionHandler withOutput(int id, String storage)
	{
		return withOutput(id, storage, new int[0]);
	}

	@ZenMethod
	public ProductionHandler withOutput(int id, String storage, int[] slots)
	{
		bind(outputs, id, storage, slots);
		return this;
	}

	private void bind(SortedMap<Integer, Binding> bindings, int id, String storage, int[] slots)
	{
		mutable();
		if(id<0||bindings.containsKey(id)) throw new IllegalArgumentException("Invalid or duplicate recipe channel: "+id);
		bindings.put(id, new Binding(id, storage, slots.clone()));
	}

	@ZenMethod
	public ProductionHandler withEnergyStorage(String storage)
	{
		mutable();
		energy = storage;
		return this;
	}

	@ZenMethod
	public ProductionHandler withRotaryStorage(String storage)
	{
		mutable();
		rotary = storage;
		return this;
	}

	@ZenMethod
	public ProductionHandler withRotaryPower(int speed, int torque)
	{
		return withRotaryPower(speed, Integer.MAX_VALUE, torque);
	}

	/** A continuous operating range in D/t and minimum torque in IT. */
	@ZenMethod
	public ProductionHandler withRotaryPower(int minimumSpeed, int maximumSpeed, int torque)
	{
		mutable();
		if(minimumSpeed<=0||maximumSpeed<minimumSpeed||torque<=0)
			throw new IllegalArgumentException("Positive rotary speed/torque and an ordered speed range required");
		minSpeed = minimumSpeed;
		maxSpeed = maximumSpeed;
		this.torque = torque;
		return this;
	}

	@ZenMethod
	public ProductionHandler withRedstoneReaction(String storage)
	{
		mutable();
		redstone = storage;
		return this;
	}

	@ZenMethod
	public ProductionHandler withTime(int ticks)
	{
		mutable();
		if(ticks<=0) throw new IllegalArgumentException("Positive recipe time required");
		time = ticks;
		return this;
	}

	@ZenMethod
	public ProductionHandler withEnergy(int totalIF)
	{
		mutable();
		if(totalIF<0) throw new IllegalArgumentException("Non-negative energy required");
		cost = totalIF;
		return this;
	}

	@ZenMethod
	public ProductionHandler withLanes(int count)
	{
		mutable();
		if(count<1||count>64) throw new IllegalArgumentException("Production lanes must be 1..64");
		lanes = count;
		return this;
	}

	@ZenMethod
	public ProductionHandler withRecipeLayout(RecipeLayout... layout)
	{
		mutable();
		this.layout = layout.clone();
		return this;
	}

	public void freeze()
	{
		if(frozen) return;
		if(inputs.isEmpty()||outputs.isEmpty()) throw new IllegalArgumentException("Production requires input and output channels: "+name);
		for(Binding b : allBindings())
		{
			StorageDefinition d = storage(b.storage);
			if(d.kind!=Kind.ITEM&&d.kind!=Kind.FLUID&&d.kind!=Kind.DUST) throw new IllegalArgumentException("Recipe channel requires item/fluid/dust storage");
			if(d.kind!=Kind.ITEM&&b.slots.length>0) throw new IllegalArgumentException("Only item channels accept slot selectors");
			Set<Integer> seen = new HashSet<>();
			for(int slot : b.slots) if(slot<0||slot>=d.size()||!seen.add(slot)) throw new IllegalArgumentException("Invalid or duplicate recipe slot: "+slot);
		}
		if(energy==null)
		{
			List<StorageDefinition> candidates = multiblock.storages.values().stream().filter(d -> d.kind==Kind.ENERGY).collect(Collectors.toList());
			if(candidates.size()==1) energy = candidates.get(0).name;
			else if(cost>0) throw new IllegalArgumentException("Select an energy provider for "+name);
		}
		if(energy!=null&&storage(energy).kind!=Kind.ENERGY) throw new IllegalArgumentException("Expected energy storage");
		if(minSpeed>0&&rotary==null)
		{
			List<StorageDefinition> candidates = multiblock.storages.values().stream().filter(d -> d.kind==Kind.ROTARY).collect(Collectors.toList());
			if(candidates.size()!=1) throw new IllegalArgumentException("Select a rotary provider for "+name);
			rotary = candidates.get(0).name;
		}
		if(rotary!=null&&(storage(rotary).kind!=Kind.ROTARY||minSpeed==0))
			throw new IllegalArgumentException("Rotary production requires a rotary provider and withRotaryPower");
		if(redstone!=null&&storage(redstone).kind!=Kind.REDSTONE) throw new IllegalArgumentException("Expected redstone storage");
		if(layout==null)
		{
			List<RecipeLayout> automatic = new ArrayList<>();
			int x = 8;
			for(Binding b : inputs.values())
			{
				automatic.add(component(b, x, false));
				x+=26;
			}
			x+=24;
			for(Binding b : outputs.values())
			{
				automatic.add(component(b, x, true));
				x+=26;
			}
			layout = automatic.toArray(new RecipeLayout[0]);
		}
		Set<String> seen = new HashSet<>();
		for(RecipeLayout c : layout)
		{
			Binding b = (c.output?outputs : inputs).get(c.id);
			if(b==null||storage(b.storage).kind!=c.kind||!seen.add(c.output+":"+c.id)) throw new IllegalArgumentException("Duplicate, missing or mistyped recipe layout channel");
		}
		if(seen.size()!=inputs.size()+outputs.size()) throw new IllegalArgumentException("Recipe layout must show each input and output once");
		frozen = true;
	}

	private RecipeLayout component(Binding b, int x, boolean output)
	{
		switch(storage(b.storage).kind)
		{
			case ITEM : return output?RecipeLayout.outputSlot(b.id, x, 10) : RecipeLayout.slot(b.id, x, 10);
			case FLUID : return output?RecipeLayout.outputFluidTank(b.id, x, 10) : RecipeLayout.fluidTank(b.id, x, 10);
			default : return output?RecipeLayout.outputDustTank(b.id, x, 10) : RecipeLayout.dustTank(b.id, x, 10);
		}
	}

	private StorageDefinition storage(String name)
	{
		StorageDefinition d = multiblock.storages.get(name);
		if(d==null) throw new IllegalArgumentException("Unknown recipe storage: "+name);
		return d;
	}

	public List<Binding> inputs()
	{
		return Collections.unmodifiableList(new ArrayList<>(inputs.values()));
	}

	public List<Binding> outputs()
	{
		return Collections.unmodifiableList(new ArrayList<>(outputs.values()));
	}

	private List<Binding> allBindings()
	{
		List<Binding> all = new ArrayList<>(inputs.values());
		all.addAll(outputs.values());
		return all;
	}

	public String energy()
	{
		return energy;
	}

	public String redstone()
	{
		return redstone;
	}

	public String rotary() {return rotary;}
	public int minSpeed() {return minSpeed;}
	public int maxSpeed() {return maxSpeed;}
	public int torque() {return torque;}

	public int lanes()
	{
		return lanes;
	}

	public String uid()
	{
		return "ctmb."+multiblock.getUniqueName()+"/"+name;
	}

	public String bindingSignature()
	{
		StringBuilder s = new StringBuilder(uid());
		for(Binding b : allBindings()) s.append('|').append(b.id).append(':').append(b.storage).append(':').append(storage(b.storage).kind).append(Arrays.toString(b.slots));
		s.append("|inputs=").append(inputs.size()).append("|energy=").append(energy);
		if(rotary!=null) s.append("|rotary=").append(rotary).append(':').append(minSpeed).append(':').append(maxSpeed).append(':').append(torque);
		return s.toString();
	}

	public List<ProductionRecipe> recipes()
	{
		return IIMultiblockRecipe.streamRecipes(ProductionRecipe.class).filter(r -> r.handler==this).collect(Collectors.toList());
	}

	@ZenMethod
	public ProductionRecipe getRecipe(String name)
	{
		return recipes().stream().filter(r -> r.getName().equals(uid()+"/"+name)||r.getName().equals(name)).findFirst().orElseThrow(() -> new IllegalArgumentException("Unknown production recipe: "+name));
	}

	public ProductionRecipe add(IIngredient[] arguments)
	{
		if(!frozen) throw new IllegalStateException("Register recipes using the default loader after CTMB definitions");
		if(arguments.length!=inputs.size()+outputs.size()) throw new IllegalArgumentException(name+": expected "+(inputs.size()+outputs.size())+" recipe arguments (inputs then outputs, each in channel ID order)");
		List<RecipeValue> in = new ArrayList<>(), out = new ArrayList<>();
		int index = 0;
		for(Binding b : inputs.values()) in.add(RecipeValue.convert(arguments[index++], storage(b.storage).kind, false));
		for(Binding b : outputs.values()) out.add(RecipeValue.convert(arguments[index++], storage(b.storage).kind, true));
		String id;
		do
		{
			id = uid()+"/recipe_"+(++sequence);
		}
		while(exists(id));
		return new ProductionRecipe(this, id, in, out, time, cost);
	}

	public static boolean exists(String id)
	{
		return IIMultiblockRecipe.streamRecipes(ProductionRecipe.class).anyMatch(r -> r.getName().equals(id));
	}

	public int layoutWidth()
	{
		int width = rotary==null?156: energy==null?280: 360;
		for(RecipeLayout c : layout)
			width = Math.max(width, c.x+26);
		return width;
	}

	public int layoutHeight()
	{
		int height = 60;
		for(RecipeLayout c : layout)
			height = Math.max(height, c.y+(c.kind==Kind.ITEM?18 : 47)+20);
		return height;
	}

	public IIRecipeLayout buildLayout(ProductionRecipe recipe)
	{
		IIRecipeLayoutBuilder builder = new IIRecipeLayoutBuilder(layoutWidth(), layoutHeight());
		for(RecipeLayout c : layout)
		{
			List<Binding> channels = c.output?outputs() : inputs();
			int index = 0;
			while(channels.get(index).id!=c.id) index++;
			RecipeValue value = (c.output?recipe.outputs : recipe.inputs).get(index);
			IIRecipeLayout.IOType io = c.output?IIRecipeLayout.IOType.OUTPUT : IIRecipeLayout.IOType.INPUT;
			switch(c.kind)
			{
				case ITEM : if(c.output) builder.withSlot(c.x, c.y, value.item, io, "frame");
				else builder.withSlot(c.x, c.y, value.displayIngredient(), io, "frame");
				break;
				case FLUID : builder.withFluidTank(c.x, c.y, value.fluid, io);
				break;
				case DUST : builder.withDustTank(c.x, c.y, value.dust, io);
				break;
				default : throw new IllegalStateException("Invalid layout kind");
			}
		}
		builder.withTimeInfo();
		if(energy!=null)
		{
			if(rotary==null) builder.withPowerInfo();
			else builder.withBottomBarDisplay(80, "power");
		}
		if(rotary!=null) builder.withBottomBarDisplay(energy==null?90: 200, "mechanical_power");
		return builder.build();
	}
	public static final class Binding
	{
		public final int id;
		public final String storage;
		private final int[] slots;
		Binding(int id, String storage, int[] slots)
		{
			this.id = id;
			this.storage = storage;
			this.slots = slots;
		}
		public int[] slots(int size)
		{
			return slots.length==0?java.util.stream.IntStream.range(0, size).toArray() : slots.clone();
		}
	}
}
