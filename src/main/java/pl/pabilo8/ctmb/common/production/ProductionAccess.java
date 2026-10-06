package pl.pabilo8.ctmb.common.production;

import crafttweaker.annotations.ZenRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import pl.pabilo8.ctmb.common.storage.*;
import stanhebben.zenscript.annotations.*;
import java.util.*;

/** Server-owned lanes. GUI suppliers read this same synchronised state. */
@ZenRegister
@ZenClass("mods.ctmb.production.Production")
public final class ProductionAccess
{
	public final StorageSystem storage;
	public final ProductionHandler handler;
	private final Lane[] lanes;
	// Retain removed lanes verbatim so edits to scripts do not silently erase reserved items.
	private final List<NBTTagCompound> retired = new ArrayList<>();
	ProductionAccess(StorageSystem storage, ProductionHandler handler)
	{
		this.storage = storage;
		this.handler = handler;
		lanes = new Lane[handler.lanes()];
		for(int i = 0; i<lanes.length; i++) lanes[i] = new Lane();
	}

	@ZenMethod
	public double getProgress()
	{
		return getLaneProgress(0);
	}

	@ZenMethod
	public double getLaneProgress(int lane)
	{
		Lane l = lane(lane);
		return l.active==null?0 : (double) l.active.progress/l.active.time;
	}

	@ZenMethod
	public int getLaneCount()
	{
		return lanes.length;
	}

	@ZenMethod
	public String getState()
	{
		return getLaneState(0);
	}

	@ZenMethod
	public String getLaneState(int lane)
	{
		return lane(lane).state;
	}

	public int progressValue()
	{
		return (int) Math.round(getProgress()*10000);
	}

	private Lane lane(int index)
	{
		if(index<0||index>=lanes.length) throw new IndexOutOfBoundsException("Invalid production lane: "+index);
		return lanes[index];
	}
	void tick()
	{
		if(!storage.isServer()) return;
		for(Lane lane : lanes)
		{
			String before = lane.state;
			if(handler.redstone()!=null&&storage.get(handler.redstone()).getRedstone()>0) lane.state = "redstone";
			else tick(lane);
			if(!before.equals(lane.state)) storage.changed();
		}
	}

	private void tick(Lane lane)
	{
		if(lane.blocked!=null)
		{
			lane.state = "configuration_changed";
			return;
		}
		if(handler.rotary()!=null)
		{
			StorageAccess rotary = storage.get(handler.rotary());
			if(rotary.getRotationSpeed()<handler.minSpeed()||rotary.getRotationSpeed()>handler.maxSpeed()||rotary.getTorque()<handler.torque())
			{
				lane.state = "no_rotary_power";
				return;
			}
		}
		if(lane.active==null)
		{
			lane.state = "idle";
			for(ProductionRecipe recipe : handler.recipes())
			{
				ProductionTransaction input = new ProductionTransaction(storage);
				if(!input.apply(handler.inputs(), recipe.inputs, false)) continue;
				if(!input.fork().apply(handler.outputs(), recipe.outputs, true))
				{
					lane.state = "output_blocked";
					continue;
				}
				int firstCost = energyForTick(recipe.getTotalProcessEnergy(), recipe.getTotalProcessTime(), 0);
				if(!hasEnergy(firstCost))
				{
					lane.state = "no_power";
					continue;
				}
				Process active = new Process(recipe, input.escrow);
				input.commit();
				lane.active = active;
				recipe.used();
				break;
			}
			if(lane.active==null) return;
		}
		Process p = lane.active;
		if(!handler.bindingSignature().equals(p.signature))
		{
			lane.state = "configuration_changed";
			return;
		}
		ProductionTransaction output = new ProductionTransaction(storage);
		if(!output.apply(handler.outputs(), p.outputs, true))
		{
			lane.state = "output_blocked";
			return;
		}
		if(p.progress<p.time)
		{
			int cost = energyForTick(p.energy, p.time, p.progress);
			if(!hasEnergy(cost))
			{
				lane.state = "no_power";
				return;
			}
			if(cost>0) storage.get(handler.energy()).extractEnergy(cost, false);
			p.progress++;
			storage.changed();
		}
		lane.state = "running";
		if(p.progress==p.time)
		{
			output.commit();
			lane.active = null;
			lane.state = "idle";
		}
	}

	private boolean hasEnergy(int cost)
	{
		return cost==0||(handler.energy()!=null&&storage.get(handler.energy()).extractEnergy(cost, true)==cost);
	}
	static int energyForTick(int total, int time, int progress)
	{
		return (int)((long) total*(progress+1)/time-(long) total*progress/time);
	}
	NBTTagCompound save()
	{
		NBTTagCompound tag = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		for(Lane lane : lanes)
		{
			NBTTagCompound n = lane.blocked!=null?lane.blocked.copy() : lane.active==null?new NBTTagCompound() : lane.active.save();
			n.setString("state", lane.state);
			list.appendTag(n);
		}
		for(NBTTagCompound n : retired) list.appendTag(n.copy());
		tag.setTag("lanes", list);
		return tag;
	}
	void restore(NBTTagCompound tag)
	{
		NBTTagList list = tag.getTagList("lanes", 10);
		retired.clear();
		for(int i = 0; i<lanes.length; i++)
		{
			Lane lane = lanes[i];
			lane.active = null;
			lane.blocked = null;
			lane.state = "idle";
			if(i>=list.tagCount()) continue;
			NBTTagCompound n = list.getCompoundTagAt(i);
			lane.state = n.getString("state");
			if(n.hasKey("recipe")) try
			{
				lane.active = new Process(n);
			}
			catch(IllegalArgumentException error)
			{
				lane.blocked = n.copy();
				lane.state = "configuration_changed";
			}
		}
		for(int i = lanes.length; i<list.tagCount(); i++) retired.add(list.getCompoundTagAt(i).copy());
	}
	List<ItemStack> claimItems()
	{
		List<ItemStack> items = new ArrayList<>();
		for(Lane lane : lanes)
		{
			if(lane.active!=null) for(ItemStack item : lane.active.escrow) items.add(item.copy());
			if(lane.blocked!=null) claimSaved(lane.blocked, items);
			lane.active = null;
			lane.blocked = null;
			lane.state = "idle";
		}
		for(NBTTagCompound n : retired) claimSaved(n, items);
		retired.clear();
		return items;
	}
	static void claimSaved(NBTTagCompound tag, List<ItemStack> items)
	{
		NBTTagList list = tag.getTagList("escrow", 10);
		for(int i = 0; i<list.tagCount(); i++)
		{
			ItemStack item = new ItemStack(list.getCompoundTagAt(i));
			if(!item.isEmpty()) items.add(item);
		}
	}
	private static final class Lane
	{
		Process active;
		NBTTagCompound blocked;
		String state = "idle";
	}
	private static final class Process
	{
		final String recipe, signature;
		final int time, energy;
		int progress;
		final List<RecipeValue> outputs = new ArrayList<>();
		final List<ItemStack> escrow = new ArrayList<>();
		Process(ProductionRecipe recipe, List<ItemStack> claimed)
		{
			this.recipe = recipe.getName();
			signature = recipe.handler.bindingSignature();
			time = recipe.getTotalProcessTime();
			energy = recipe.getTotalProcessEnergy();
			for(RecipeValue value : recipe.outputs) outputs.add(RecipeValue.load(value.save()));
			for(ItemStack item : claimed) escrow.add(item.copy());
		}
		Process(NBTTagCompound tag)
		{
			recipe = tag.getString("recipe");
			signature = tag.getString("bindings");
			time = tag.getInteger("time");
			energy = tag.getInteger("energy");
			progress = tag.getInteger("progress");
			if(time<=0||energy<0||progress<0||progress> time) throw new IllegalArgumentException("Invalid saved process");
			NBTTagList list = tag.getTagList("outputs", 10);
			for(int i = 0; i<list.tagCount(); i++) outputs.add(RecipeValue.load(list.getCompoundTagAt(i)));
			claimSaved(tag, escrow);
		}
		NBTTagCompound save()
		{
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString("recipe", recipe);
			tag.setString("bindings", signature);
			tag.setInteger("time", time);
			tag.setInteger("energy", energy);
			tag.setInteger("progress", progress);
			NBTTagList out = new NBTTagList(), in = new NBTTagList();
			for(RecipeValue v : outputs) out.appendTag(v.save());
			for(ItemStack item : escrow) in.appendTag(item.writeToNBT(new NBTTagCompound()));
			tag.setTag("outputs", out);
			tag.setTag("escrow", in);
			return tag;
		}
	}
}
