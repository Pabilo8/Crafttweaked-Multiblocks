package pl.pabilo8.ctmb.common.storage;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandler;
import pl.pabilo8.immersiveintelligence.api.crafting.DustStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A capability view exposes the union of permitted provider slots at one physical face.
 */
public final class StoragePortView implements IItemHandler, IFluidHandler, IEnergyStorage, IDustHandler
{
	private final StorageSystem system;
	private final List<ItemRef> slots = new ArrayList<>();
	private final Map<StorageAccess, Access> providers = new LinkedHashMap<>();

	StoragePortView(StorageSystem system, int position, EnumFacing side)
	{
		this.system = system;
		for(StorageAccess storage : system.providers())
		{
			Access access = new Access();
			for(StorageDefinition.Port port : storage.definition.ports())
				if(system.matches(port, position, side))
				{
					access.input |= port.input;
					access.output |= !port.input;
					if(storage.definition.kind==StorageDefinition.Kind.ITEM)
						for(int slot = 0; slot < storage.getSize(); slot++)
							if(port.includes(slot))
							{
								ItemRef found = null;
								for(ItemRef ref : slots)
									if(ref.storage==storage&&ref.slot==slot)
									{
										found = ref;
										break;
									}
								if(found==null)
								{
									found = new ItemRef(storage, slot);
									slots.add(found);
								}
								found.input |= port.input;
								found.output |= !port.input;
							}
				}
			if(access.input||access.output) providers.put(storage, access);
		}
	}

	public boolean has(StorageDefinition.Kind kind)
	{
		return providers.keySet().stream().anyMatch(s -> s.definition.kind==kind);
	}

	private ItemRef slot(int index)
	{
		if(index < 0||index >= slots.size()) throw new IndexOutOfBoundsException("Port slot "+index);
		return slots.get(index);
	}

	@Override
	public int getSlots()
	{
		return slots.size();
	}

	@Override
	public ItemStack getStackInSlot(int index)
	{
		ItemRef ref = slot(index);
		return ref.storage.item(ref.slot).copy();
	}

	@Override
	public ItemStack insertItem(int index, ItemStack stack, boolean simulate)
	{
		ItemRef ref = slot(index);
		return ref.input?ref.storage.insert(ref.slot, stack, simulate): stack.copy();
	}

	@Override
	public ItemStack extractItem(int index, int amount, boolean simulate)
	{
		ItemRef ref = slot(index);
		return ref.output?ref.storage.extract(ref.slot, amount, simulate): ItemStack.EMPTY;
	}

	@Override
	public int getSlotLimit(int index)
	{
		ItemRef ref = slot(index);
		return ref.storage.itemLimit(ref.slot);
	}

	private List<StorageAccess> matching(StorageDefinition.Kind kind, boolean input)
	{
		List<StorageAccess> result = new ArrayList<>();
		providers.forEach((storage, access) -> {
			if(storage.definition.kind==kind&&(input?access.input: access.output)) result.add(storage);
		});
		return result;
	}

	@Override
	public IFluidTankProperties[] getTankProperties()
	{
		List<IFluidTankProperties> result = new ArrayList<>();
		providers.forEach((storage, access) -> {
			if(storage.definition.kind==StorageDefinition.Kind.FLUID)
			{
				FluidStack fluid = storage.fluidTank().getFluid();
				result.add(new FluidTankProperties(fluid==null?null: fluid.copy(), storage.getSize(), access.input, access.output));
			}
		});
		return result.toArray(new IFluidTankProperties[0]);
	}

	@Override
	public int fill(FluidStack resource, boolean doFill)
	{
		if(resource==null||resource.amount <= 0||(doFill&&!system.isServer())) return 0;
		int accepted = 0;
		for(StorageAccess storage : matching(StorageDefinition.Kind.FLUID, true))
		{
			FluidStack rest = resource.copy();
			rest.amount = resource.amount-accepted;
			accepted += storage.fluidTank().fill(rest, doFill);
			if(accepted==resource.amount) break;
		}
		return accepted;
	}

	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain)
	{
		if(resource==null||resource.amount <= 0||(doDrain&&!system.isServer())) return null;
		FluidStack result = null;
		for(StorageAccess storage : matching(StorageDefinition.Kind.FLUID, false))
		{
			FluidStack rest = resource.copy();
			rest.amount = resource.amount-(result==null?0: result.amount);
			FluidStack drained = storage.fluidTank().drain(rest, doDrain);
			if(drained!=null)
			{
				if(result==null) result = drained;
				else result.amount += drained.amount;
			}
			if(result!=null&&result.amount==resource.amount) break;
		}
		return result;
	}

	@Override
	public FluidStack drain(int amount, boolean doDrain)
	{
		if(amount <= 0) return null;
		for(StorageAccess storage : matching(StorageDefinition.Kind.FLUID, false))
		{
			FluidStack fluid = storage.fluidTank().getFluid();
			if(fluid!=null&&fluid.amount > 0)
			{
				FluidStack requested = fluid.copy();
				requested.amount = amount;
				return drain(requested, doDrain);
			}
		}
		return null;
	}

	@Override
	public int receiveEnergy(int amount, boolean simulate)
	{
		if(amount <= 0) return 0;
		int accepted = 0;
		for(StorageAccess storage : matching(StorageDefinition.Kind.ENERGY, true))
		{
			accepted += storage.receiveEnergy(amount-accepted, simulate);
			if(accepted==amount) break;
		}
		return accepted;
	}

	@Override
	public int extractEnergy(int amount, boolean simulate)
	{
		if(amount <= 0) return 0;
		int extracted = 0;
		for(StorageAccess storage : matching(StorageDefinition.Kind.ENERGY, false))
		{
			extracted += storage.extractEnergy(amount-extracted, simulate);
			if(extracted==amount) break;
		}
		return extracted;
	}

	@Override
	public int getEnergyStored()
	{
		return (int)Math.min(Integer.MAX_VALUE, providers.keySet().stream().filter(s -> s.definition.kind==StorageDefinition.Kind.ENERGY).mapToLong(StorageAccess::getEnergy).sum());
	}

	@Override
	public int getMaxEnergyStored()
	{
		return (int)Math.min(Integer.MAX_VALUE, providers.keySet().stream().filter(s -> s.definition.kind==StorageDefinition.Kind.ENERGY).mapToLong(StorageAccess::getSize).sum());
	}

	@Override
	public boolean canReceive()
	{
		return !matching(StorageDefinition.Kind.ENERGY, true).isEmpty();
	}

	@Override
	public boolean canExtract()
	{
		return !matching(StorageDefinition.Kind.ENERGY, false).isEmpty();
	}

	@Override
	public int fill(DustStack resource, boolean doFill)
	{
		if(resource==null||resource.isEmpty()||resource.amount <= 0||(doFill&&!system.isServer())) return 0;
		int accepted = 0;
		for(StorageAccess storage : matching(StorageDefinition.Kind.DUST, true))
		{
			DustStack rest = new DustStack(resource.name, resource.amount-accepted);
			accepted += storage.dustTank().fill(rest, doFill);
			if(accepted==resource.amount) break;
		}
		return accepted;
	}

	@Override
	public DustStack drain(DustStack resource, boolean doDrain)
	{
		if(resource==null||resource.isEmpty()||resource.amount <= 0||(doDrain&&!system.isServer()))
			return DustStack.getEmptyStack();
		int amount = 0;
		for(StorageAccess storage : matching(StorageDefinition.Kind.DUST, false))
		{
			DustStack rest = new DustStack(resource.name, resource.amount-amount);
			amount += storage.dustTank().drain(rest, doDrain).amount;
			if(amount==resource.amount) break;
		}
		return amount==0?DustStack.getEmptyStack(): new DustStack(resource.name, amount);
	}

	// IDustHandler's amount-drain has a distinct name because IFluidHandler.drain has the same parameters.
	@Override
	public DustStack drainDust(int amount, boolean doDrain)
	{
		if(amount <= 0) return DustStack.getEmptyStack();
		for(StorageAccess storage : matching(StorageDefinition.Kind.DUST, false))
		{
			DustStack dust = storage.dustTank().getDustStack();
			if(!dust.isEmpty()) return drain(new DustStack(dust.name, amount), doDrain);
		}
		return DustStack.getEmptyStack();
	}

	private static final class Access
	{
		boolean input, output;
	}

	private static final class ItemRef
	{
		final StorageAccess storage;
		final int slot;
		boolean input, output;

		ItemRef(StorageAccess storage, int slot)
		{
			this.storage = storage;
			this.slot = slot;
		}
	}
}
