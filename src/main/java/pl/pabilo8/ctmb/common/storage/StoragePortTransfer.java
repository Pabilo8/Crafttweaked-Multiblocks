package pl.pabilo8.ctmb.common.storage;

import blusunrize.immersiveengineering.common.util.EnergyHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import pl.pabilo8.immersiveintelligence.api.crafting.DustStack;
import pl.pabilo8.immersiveintelligence.api.rotary.CapabilityRotaryEnergy;
import pl.pabilo8.immersiveintelligence.api.rotary.IRotaryConnector;
import pl.pabilo8.immersiveintelligence.api.rotary.IRotaryEnergy;

/**
 * Adjacent transfers debit only the amount accepted by the destination.
 */
final class StoragePortTransfer
{
	private StoragePortTransfer()
	{
	}

	static int push(StorageAccess source, StorageDefinition.Port port, TileEntity target, EnumFacing face, int maximum)
	{
		if(maximum <= 0||!source.system.isServer()) return 0;
		switch(source.definition.kind)
		{
			case ITEM:
				IItemHandler items = target.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face);
				if(items==null) return 0;
				int moved = 0;
				for(int slot = 0; slot < source.getSize()&&moved < maximum; slot++)
					if(port.includes(slot))
					{
						ItemStack offered = source.extract(slot, maximum-moved, true);
						if(offered.isEmpty()) continue;
						ItemStack remainder = ItemHandlerHelper.insertItemStacked(items, offered.copy(), false);
						int accepted = accepted(offered.getCount(), offered.getCount()-remainder.getCount());
						source.extract(slot, accepted, false);
						moved += accepted;
					}
				return moved;
			case FLUID:
				IFluidHandler fluids = target.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face);
				if(fluids==null) return 0;
				FluidStack fluid = source.fluidTank().drain(maximum, false);
				if(fluid==null||fluid.amount <= 0) return 0;
				int filled = accepted(fluid.amount, fluids.fill(fluid.copy(), true));
				source.fluidTank().drain(filled, true);
				return filled;
			case DUST:
				IDustHandler dusts = target.getCapability(DustCapability.CAPABILITY, face);
				if(dusts==null) return 0;
				DustStack dust = source.dustTank().drain(maximum, false);
				if(dust.isEmpty()) return 0;
				int stored = accepted(dust.amount, dusts.fill(new DustStack(dust.name, dust.amount), true));
				source.dustTank().drain(stored, true);
				return stored;
			case ENERGY:
				int offered = source.extractEnergy(maximum, true);
				if(offered==0) return 0;
				int received = accepted(offered, EnergyHelper.insertFlux(target, face, offered, false));
				source.extractEnergy(received, false);
				return received;
			case ROTARY:
				IRotaryEnergy rotary = target.getCapability(CapabilityRotaryEnergy.ROTARY_ENERGY, face);
				if(rotary==null||!rotary.getSide(face).canInput()) return 0;
				float oldSpeed = rotary.getRotationSpeed(), oldTorque = rotary.getTorque();
				if(source.getRotationSpeed()==0||source.getTorque()==0)
				{
					rotary.setRotationSpeed(0);
					rotary.setTorque(0);
				}
				else rotary.grow(source.rotary(), 0.01f);
				if(oldSpeed!=rotary.getRotationSpeed()||oldTorque!=rotary.getTorque())
				{
					target.markDirty();
					if(target instanceof IRotaryConnector) ((IRotaryConnector)target).getNetwork().updateValues();
				}
				return 0; // Continuous speed/torque is communicated, not consumed.
			default:
				return 0;
		}
	}

	private static int accepted(int offered, int received)
	{
		return Math.max(0, Math.min(offered, received));
	}
}
