package pl.pabilo8.ctmb.common.storage;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import pl.pabilo8.immersiveintelligence.api.crafting.DustStack;

public final class DustCapability
{
	@CapabilityInject(IDustHandler.class)
	public static Capability<IDustHandler> CAPABILITY;

	private DustCapability()
	{
	}

	public static void register()
	{
		CapabilityManager.INSTANCE.register(IDustHandler.class, new Capability.IStorage<IDustHandler>()
		{
			@Override
			public NBTBase writeNBT(Capability<IDustHandler> capability, IDustHandler instance, EnumFacing side)
			{
				throw new UnsupportedOperationException("CTMB providers own persistence, not their port views");
			}

			@Override
			public void readNBT(Capability<IDustHandler> capability, IDustHandler instance, EnumFacing side, NBTBase nbt)
			{
				throw new UnsupportedOperationException("CTMB providers own persistence, not their port views");
			}
		}, () -> new IDustHandler()
		{
			@Override
			public int fill(DustStack stack, boolean execute)
			{
				return 0;
			}

			@Override
			public DustStack drain(DustStack stack, boolean execute)
			{
				return DustStack.getEmptyStack();
			}

			@Override
			public DustStack drainDust(int amount, boolean execute)
			{
				return DustStack.getEmptyStack();
			}
		});
	}
}
