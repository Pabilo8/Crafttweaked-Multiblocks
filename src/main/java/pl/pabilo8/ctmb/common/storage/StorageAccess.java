package pl.pabilo8.ctmb.common.storage;

import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorageAdvanced;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.mc1120.liquid.MCLiquidStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.items.ItemHandlerHelper;
import pl.pabilo8.immersiveintelligence.api.crafting.DustStack;
import pl.pabilo8.immersiveintelligence.api.data.DataPacket;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;

/**
 * A typed, named handle shared by scripts, native Deco views and sided ports.
 */
@ZenRegister
@ZenClass("mods.ctmb.storage.Storage")
public final class StorageAccess
{
	public final StorageDefinition definition;
	public final StorageSystem system;
	public final int itemOffset;
	private final FluidTank fluid;
	private final CTMBDustTank dust;
	private final FluxStorageAdvanced energy;
	private final Deque<DataPacket> packets = new ArrayDeque<>();
	private int redstone;

	public StorageAccess(StorageSystem system, StorageDefinition definition, int itemOffset)
	{
		this.system = system;
		this.definition = definition;
		this.itemOffset = itemOffset;
		fluid = definition.kind==StorageDefinition.Kind.FLUID?new FluidTank(definition.size())
		{
			@Override
			protected void onContentsChanged()
			{
				system.changed();
			}
		}: null;
		dust = definition.kind==StorageDefinition.Kind.DUST?new CTMBDustTank(definition.size(), system::changed): null;
		energy = definition.kind==StorageDefinition.Kind.ENERGY?new FluxStorageAdvanced(definition.size()): null;
	}

	@ZenGetter("name")
	public String getName()
	{
		return definition.name;
	}

	@ZenGetter("type")
	public String getType()
	{
		return definition.kind.name().toLowerCase(Locale.ROOT);
	}

	@ZenGetter("size")
	public int getSize()
	{
		return definition.size();
	}

	public void require(StorageDefinition.Kind kind)
	{
		if(definition.kind!=kind)
			throw new IllegalArgumentException("Storage "+definition.name+" is "+definition.kind+", expected "+kind);
	}

	private void slot(int slot)
	{
		require(StorageDefinition.Kind.ITEM);
		if(slot < 0||slot >= getSize()) throw new IndexOutOfBoundsException(definition.name+": slot "+slot);
	}

	private void mutable()
	{
		if(!system.isServer())
			throw new IllegalStateException("Storage mutation requires the server: "+definition.name);
	}

	public FluidTank fluidTank()
	{
		require(StorageDefinition.Kind.FLUID);
		return fluid;
	}

	public CTMBDustTank dustTank()
	{
		require(StorageDefinition.Kind.DUST);
		return dust;
	}

	public FluxStorageAdvanced energy()
	{
		require(StorageDefinition.Kind.ENERGY);
		return energy;
	}

	public ItemStack item(int slot)
	{
		slot(slot);
		return system.inventory.get(itemOffset+slot);
	}

	public int itemLimit(int slot)
	{
		slot(slot);
		return 64;
	}

	public ItemStack insert(int slot, ItemStack stack, boolean simulate)
	{
		slot(slot);
		if(stack.isEmpty()) return ItemStack.EMPTY;
		ItemStack existing = item(slot);
		if(!existing.isEmpty()&&!ItemHandlerHelper.canItemStacksStack(existing, stack)) return stack.copy();
		int accepted = Math.min(stack.getCount(), Math.max(0, Math.min(itemLimit(slot), stack.getMaxStackSize())-existing.getCount()));
		if(accepted > 0&&!simulate)
		{
			if(!system.isServer()) return stack.copy();
			ItemStack result = existing.isEmpty()?stack.copy(): existing.copy();
			result.setCount(existing.getCount()+accepted);
			system.inventory.set(itemOffset+slot, result);
			system.changed();
		}
		ItemStack remaining = stack.copy();
		remaining.shrink(accepted);
		return remaining;
	}

	public ItemStack extract(int slot, int amount, boolean simulate)
	{
		slot(slot);
		if(amount <= 0||item(slot).isEmpty()||(!simulate&&!system.isServer())) return ItemStack.EMPTY;
		ItemStack existing = item(slot), result = existing.copy();
		result.setCount(Math.min(amount, existing.getCount()));
		if(!simulate)
		{
			ItemStack rest = existing.copy();
			rest.shrink(result.getCount());
			system.inventory.set(itemOffset+slot, rest);
			system.changed();
		}
		return result;
	}

	@ZenMethod
	public IItemStack getItem(int slot)
	{
		return CraftTweakerMC.getIItemStack(item(slot).copy());
	}

	@ZenMethod
	public IItemStack fillItem(int slot, IItemStack stack)
	{
		mutable();
		return CraftTweakerMC.getIItemStack(insert(slot, CraftTweakerMC.getItemStack(stack), false));
	}

	@ZenMethod
	public IItemStack extractItem(int slot, int amount)
	{
		mutable();
		return CraftTweakerMC.getIItemStack(extract(slot, amount, false));
	}

	@ZenMethod
	public void setItem(int slot, IItemStack stack)
	{
		mutable();
		slot(slot);
		ItemStack nativeStack = CraftTweakerMC.getItemStack(stack).copy();
		if(!nativeStack.isEmpty()&&(nativeStack.getCount() < 0||nativeStack.getCount() > Math.min(itemLimit(slot), nativeStack.getMaxStackSize())))
			throw new IllegalArgumentException("Oversized item stack");
		system.inventory.set(itemOffset+slot, nativeStack);
		system.changed();
	}

	@ZenMethod
	public ILiquidStack getFluid()
	{
		require(StorageDefinition.Kind.FLUID);
		return fluid.getFluid()==null?null: new MCLiquidStack(fluid.getFluid().copy());
	}

	@ZenMethod
	public int fillFluid(ILiquidStack stack)
	{
		mutable();
		require(StorageDefinition.Kind.FLUID);
		return fluid.fill(CraftTweakerMC.getLiquidStack(stack), true);
	}

	@ZenMethod
	public ILiquidStack drainFluid(int amount)
	{
		mutable();
		require(StorageDefinition.Kind.FLUID);
		FluidStack result = fluid.drain(amount, true);
		return result==null?null: new MCLiquidStack(result);
	}

	@ZenMethod
	public int getEnergy()
	{
		return energy().getEnergyStored();
	}

	@ZenMethod
	public int fillEnergy(int amount)
	{
		mutable();
		return receiveEnergy(amount, false);
	}

	@ZenMethod
	public int extractEnergy(int amount)
	{
		mutable();
		return extractEnergy(amount, false);
	}

	public int receiveEnergy(int amount, boolean simulate)
	{
		if(amount <= 0||(!simulate&&!system.isServer())) return 0;
		int accepted = energy().receiveEnergy(amount, simulate);
		if(accepted > 0&&!simulate) system.changed();
		return accepted;
	}

	public int extractEnergy(int amount, boolean simulate)
	{
		if(amount <= 0||(!simulate&&!system.isServer())) return 0;
		int extracted = energy().extractEnergy(amount, simulate);
		if(extracted > 0&&!simulate) system.changed();
		return extracted;
	}

	@ZenMethod
	public int fillDust(String name, int amount)
	{
		mutable();
		return dustTank().fill(new DustStack(name, amount), true);
	}

	@ZenMethod
	public IData drainDust(int amount)
	{
		mutable();
		return CraftTweakerMC.getIData(dustTank().drain(amount, true).serializeNBT());
	}

	@ZenMethod
	public int getRedstone()
	{
		require(StorageDefinition.Kind.REDSTONE);
		return redstone;
	}

	@ZenMethod
	public void setRedstone(int value)
	{
		mutable();
		require(StorageDefinition.Kind.REDSTONE);
		if(value < 0||value > 15) throw new IllegalArgumentException("Redstone is 0..15");
		redstone(value);
	}

	public void redstone(int value)
	{
		if(redstone!=value)
		{
			redstone = value;
			system.changed();
			system.redstoneChanged(definition);
		}
	}

	@ZenMethod
	public boolean pushPacket(IData data)
	{
		mutable();
		require(StorageDefinition.Kind.DATA);
		return offer(new DataPacket(CraftTweakerMC.getNBTCompound(data)));
	}

	@ZenMethod
	public IData peekPacket()
	{
		require(StorageDefinition.Kind.DATA);
		return packets.isEmpty()?null: CraftTweakerMC.getIData(packets.peek().serializeNBT().copy());
	}

	@ZenMethod
	public IData pollPacket()
	{
		mutable();
		IData result = peekPacket();
		if(result!=null) removePacket();
		return result;
	}

	public boolean offer(DataPacket packet)
	{
		require(StorageDefinition.Kind.DATA);
		if(packets.size() >= getSize()) return false;
		packets.add(packet.clone());
		system.changed();
		return true;
	}

	public DataPacket firstPacket()
	{
		return packets.peek();
	}

	public void removePacket()
	{
		if(!packets.isEmpty())
		{
			packets.remove();
			system.changed();
		}
	}

	@ZenMethod
	public IData getData()
	{
		NBTTagCompound tag = save();
		tag.setString("name", definition.name);
		tag.setInteger("capacity", getSize());
		return CraftTweakerMC.getIData(tag);
	}

	public NBTTagCompound save()
	{
		NBTTagCompound tag = new NBTTagCompound();
		tag.setString("type", getType());
		switch(definition.kind)
		{
			case ITEM:
				NBTTagList items = new NBTTagList();
				for(int i = 0; i < getSize(); i++)
				{
					NBTTagCompound item = item(i).writeToNBT(new NBTTagCompound());
					item.setInteger("slot", i);
					items.appendTag(item);
				}
				tag.setTag("items", items);
				break;
			case FLUID:
				if(fluid.getFluid()!=null) tag.setTag("contents", fluid.getFluid().writeToNBT(new NBTTagCompound()));
				break;
			case DUST:
				tag.setTag("contents", dust.getDustStack().serializeNBT());
				break;
			case ENERGY:
				tag.setInteger("value", energy.getEnergyStored());
				break;
			case REDSTONE:
				tag.setInteger("value", redstone);
				break;
			case DATA:
				NBTTagList list = new NBTTagList();
				for(DataPacket packet : packets) list.appendTag(packet.serializeNBT().copy());
				tag.setTag("packets", list);
				break;
		}
		return tag;
	}

	public void restore(NBTTagCompound tag)
	{
		if(!tag.getString("type").equals(getType()))
			throw new IllegalArgumentException("Saved provider type differs: "+definition.name);
		switch(definition.kind)
		{
			case ITEM:
				for(int i = 0; i < getSize(); i++) system.inventory.set(itemOffset+i, ItemStack.EMPTY);
				NBTTagList items = tag.getTagList("items", 10);
				for(int i = 0; i < items.tagCount(); i++)
				{
					NBTTagCompound value = items.getCompoundTagAt(i);
					int slot = value.getInteger("slot");
					if(slot >= 0&&slot < getSize())
					{
						ItemStack stack = new ItemStack(value);
						stack.setCount(Math.max(0, Math.min(stack.getCount(), Math.min(64, stack.getMaxStackSize()))));
						system.inventory.set(itemOffset+slot, stack);
					}
				}
				break;
			case FLUID:
				FluidStack f = FluidStack.loadFluidStackFromNBT(tag.getCompoundTag("contents"));
				if(f!=null)
				{
					f.amount = Math.max(0, Math.min(f.amount, getSize()));
					if(f.amount==0) f = null;
				}
				fluid.setFluid(f);
				break;
			case DUST:
				dust.restore(new DustStack(tag.getCompoundTag("contents")));
				break;
			case ENERGY:
				energy.setEnergy(Math.max(0, Math.min(getSize(), tag.getInteger("value"))));
				break;
			case REDSTONE:
				redstone = Math.max(0, Math.min(15, tag.getInteger("value")));
				break;
			case DATA:
				packets.clear();
				NBTTagList list = tag.getTagList("packets", 10);
				for(int i = 0; i < Math.min(getSize(), list.tagCount()); i++)
					packets.add(new DataPacket(list.getCompoundTagAt(i)));
				break;
		}
	}
}
