package pl.pabilo8.ctmb.common.production;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemHandlerHelper;
import pl.pabilo8.ctmb.common.storage.StorageAccess;
import pl.pabilo8.ctmb.common.storage.StorageSystem;
import pl.pabilo8.immersiveintelligence.api.crafting.DustStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Plan all channels against shared copies, then commit once on the server.
 */
final class ProductionTransaction
{
	private final StorageSystem system;
	private final Map<StorageAccess, Snapshot> snapshots = new LinkedHashMap<>();
	final List<ItemStack> escrow = new ArrayList<>();

	ProductionTransaction(StorageSystem system)
	{
		this.system = system;
	}

	ProductionTransaction fork()
	{
		ProductionTransaction copy = new ProductionTransaction(system);
		snapshots.forEach((provider, state) -> copy.snapshots.put(provider, state.copy()));
		return copy;
	}

	boolean apply(List<ProductionHandler.Binding> bindings, List<RecipeValue> values, boolean output)
	{
		if(bindings.size()!=values.size()) return false;
		for(int i = 0; i < bindings.size(); i++)
		{
			ProductionHandler.Binding binding = bindings.get(i);
			RecipeValue value = values.get(i);
			StorageAccess provider = system.get(binding.storage);
			if(provider.definition.kind!=value.kind) return false;
			Snapshot state = snapshots.computeIfAbsent(provider, Snapshot::new);
			switch(value.kind)
			{
				case ITEM:
					int remaining = output?value.item.getCount(): value.ingredient.inputSize;
					int[] slots = binding.slots(provider.getSize());
					if(output)
					{
						// Merge first, then use empty slots; every channel shares the same snapshot.
						for(int pass = 0; pass < 2&&remaining > 0; pass++)
							for(int slot : slots)
							{
								ItemStack at = state.items.get(slot);
								if((pass==0&&at.isEmpty())||(pass==1&&!at.isEmpty())) continue;
								if(!at.isEmpty()&&!ItemHandlerHelper.canItemStacksStack(at, value.item)) continue;
								int amount = Math.min(remaining, Math.max(0, Math.min(provider.itemLimit(slot), value.item.getMaxStackSize())-at.getCount()));
								if(amount > 0)
								{
									ItemStack next = at.isEmpty()?value.item.copy(): at.copy();
									next.setCount(at.getCount()+amount);
									state.items.set(slot, next);
									remaining -= amount;
								}
							}
					}
					else for(int slot : slots)
					{
						ItemStack at = state.items.get(slot);
						if(at.isEmpty()||!value.ingredient.matchesItemStackIgnoringSize(at)) continue;
						int amount = Math.min(remaining, at.getCount());
						if(amount > 0)
						{
							ItemStack claimed = at.copy();
							claimed.setCount(amount);
							escrow.add(claimed);
							at.shrink(amount);
							remaining -= amount;
						}
						if(remaining==0) break;
					}
					if(remaining > 0) return false;
					break;
				case FLUID:
					FluidStack fluid = value.fluid;
					if(output)
					{
						if(state.fluid!=null&&!state.fluid.isFluidEqual(fluid)) return false;
						int amount = state.fluid==null?0: state.fluid.amount;
						if(fluid.amount > provider.getSize()-amount) return false;
						if(state.fluid==null) state.fluid = fluid.copy();
						else state.fluid.amount += fluid.amount;
					}
					else
					{
						if(state.fluid==null||!state.fluid.isFluidEqual(fluid)||state.fluid.amount < fluid.amount)
							return false;
						state.fluid.amount -= fluid.amount;
						if(state.fluid.amount==0) state.fluid = null;
					}
					break;
				case DUST:
					DustStack dust = value.dust;
					if(output)
					{
						if(!state.dust.canMergeWith(dust)||dust.amount > provider.getSize()-state.dust.amount)
							return false;
						state.dust = state.dust.mergeWith(dust);
					}
					else
					{
						if(!state.dust.name.equals(dust.name)||state.dust.amount < dust.amount) return false;
						state.dust = state.dust.subtract(dust);
					}
					break;
				default:
					return false;
			}
		}
		return true;
	}

	void commit()
	{
		if(!system.isServer()) throw new IllegalStateException("Production commits require the server");
		snapshots.forEach((provider, state) -> {
			switch(provider.definition.kind)
			{
				case ITEM:
					for(int i = 0; i < state.items.size(); i++)
						system.inventory.set(provider.itemOffset+i, state.items.get(i).copy());
					break;
				case FLUID:
					provider.fluidTank().setFluid(state.fluid==null?null: state.fluid.copy());
					break;
				case DUST:
					provider.dustTank().restore(state.dust.copy());
					break;
				default:
					throw new IllegalStateException();
			}
		});
		system.changed();
	}

	private static final class Snapshot
	{
		List<ItemStack> items;
		FluidStack fluid;
		DustStack dust;

		Snapshot(StorageAccess provider)
		{
			switch(provider.definition.kind)
			{
				case ITEM:
					items = new ArrayList<>();
					for(int i = 0; i < provider.getSize(); i++) items.add(provider.item(i).copy());
					break;
				case FLUID:
					FluidStack f = provider.fluidTank().getFluid();
					fluid = f==null?null: f.copy();
					break;
				case DUST:
					dust = provider.dustTank().getDustStack().copy();
					break;
				default:
					throw new IllegalStateException();
			}
		}

		private Snapshot()
		{
		}

		Snapshot copy()
		{
			Snapshot s = new Snapshot();
			if(items!=null)
			{
				s.items = new ArrayList<>();
				for(ItemStack item : items) s.items.add(item.copy());
			}
			s.fluid = fluid==null?null: fluid.copy();
			s.dust = dust==null?null: dust.copy();
			return s;
		}
	}
}
