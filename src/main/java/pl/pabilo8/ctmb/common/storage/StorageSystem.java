package pl.pabilo8.ctmb.common.storage;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import pl.pabilo8.ctmb.common.block.TileEntityMultiblock;
import pl.pabilo8.immersiveintelligence.api.data.DataPacket;
import pl.pabilo8.immersiveintelligence.api.data.IIDataHandlingUtils;
import pl.pabilo8.immersiveintelligence.api.rotary.CapabilityRotaryEnergy;
import pl.pabilo8.immersiveintelligence.api.rotary.IRotaryEnergy;

import java.util.*;

/**
 * The master tile owns one instance of each provider; names are its persistence keys.
 */
public final class StorageSystem
{
	public final TileEntityMultiblock tile;
	public final NonNullList<ItemStack> inventory;
	private final Map<String, StorageAccess> providers = new LinkedHashMap<>();
	private final Map<String, StoragePortView> views = new HashMap<>();
	private final Map<String, Integer> publishedRedstone = new HashMap<>();
	private boolean outputting;

	public StorageSystem(TileEntityMultiblock tile)
	{
		this.tile = tile;
		int total = tile.getMultiblock().storages.values().stream().filter(d -> d.kind==StorageDefinition.Kind.ITEM).mapToInt(StorageDefinition::size).reduce(0, Math::addExact);
		inventory = NonNullList.withSize(total, ItemStack.EMPTY);
		int offset = 0;
		for(StorageDefinition definition : tile.getMultiblock().storages.values())
		{
			providers.put(definition.name, new StorageAccess(this, definition, offset));
			if(definition.kind==StorageDefinition.Kind.ITEM) offset += definition.size();
		}
	}

	public Collection<StorageAccess> providers()
	{
		return Collections.unmodifiableCollection(providers.values());
	}

	public StorageAccess get(String name)
	{
		StorageAccess result = providers.get(name);
		if(result==null)
			throw new IllegalArgumentException(tile.getMultiblock().getUniqueName()+": Unknown storage "+name);
		return result;
	}

	public boolean isServer()
	{
		return tile.hasWorld()&&!tile.getWorld().isRemote;
	}

	public void changed()
	{
		if(isServer()) tile.forceUpdate();
	}

	public NBTTagCompound save()
	{
		NBTTagCompound tag = new NBTTagCompound();
		providers.forEach((name, provider) -> tag.setTag(name, provider.save()));
		return tag;
	}

	public void restore(NBTTagCompound tag)
	{
		publishedRedstone.clear();
		providers.forEach((name, provider) -> {
			if(tag.hasKey(name, 10)) provider.restore(tag.getCompoundTag(name));
		});
	}

	public StoragePortView view(int position, EnumFacing side)
	{
		String key = position+":"+side+":"+tile.facing+":"+tile.mirrored;
		return views.computeIfAbsent(key, k -> new StoragePortView(this, position, side));
	}

	public List<StorageAccess> items()
	{
		List<StorageAccess> result = new ArrayList<>();
		for(StorageAccess provider : providers.values())
			if(provider.definition.kind==StorageDefinition.Kind.ITEM) result.add(provider);
		return result;
	}

	public int flatSlot(String name, int slot)
	{
		StorageAccess provider = get(name);
		provider.require(StorageDefinition.Kind.ITEM);
		if(slot < 0||slot >= provider.getSize()) throw new IllegalArgumentException(name+": Invalid GUI slot "+slot);
		return provider.itemOffset+slot;
	}

	public void receive(DataPacket packet, int position, EnumFacing side)
	{
		if(!isServer()) return;
		for(StorageAccess provider : providers.values())
			if(provider.definition.kind==StorageDefinition.Kind.DATA)
				for(StorageDefinition.Port port : provider.definition.ports())
					if(port.input&&matches(port, position, side))
					{
						provider.offer(packet);
						break;
					}
	}

	public boolean matches(StorageDefinition.Port port, int position, EnumFacing side)
	{
		return side!=null&&tile.getMultiblock().definition.isPOI(port.poi, position)&&tile.getMultiblock().definition.direction(port.direction, tile.facing, tile.mirrored)==side;
	}

	public void tick()
	{
		if(!isServer()) return;
		for(StorageAccess provider : providers.values())
		{
			if(provider.definition.kind==StorageDefinition.Kind.REDSTONE)
			{
				int signal = 0;
				boolean input = false;
				for(StorageDefinition.Port port : provider.definition.ports())
					if(port.input)
					{
						input = true;
						EnumFacing side = tile.getMultiblock().definition.direction(port.direction, tile.facing, tile.mirrored);
						for(int position : tile.getMultiblock().definition.getPOI(port.poi))
						{
							BlockPos at = tile.getBlockPosForPos(position).offset(side);
							if(tile.getWorld().isBlockLoaded(at))
								signal = Math.max(signal, tile.getWorld().getRedstonePower(at, side));
						}
					}
				if(input) provider.redstone(signal);
			}
			else if(provider.definition.kind==StorageDefinition.Kind.ROTARY) tickRotaryInput(provider);
		}
	}

	private EnumFacing direction(StorageDefinition.Port port)
	{
		return tile.getMultiblock().definition.direction(port.direction, tile.facing, tile.mirrored);
	}

	private TileEntity neighbour(int position, EnumFacing side)
	{
		BlockPos at = tile.getBlockPosForPos(position).offset(side);
		if(!tile.getWorld().isBlockLoaded(at)) return null;
		TileEntity target = tile.getWorld().getTileEntity(at);
		// Never circulate resources or rotary power into this machine's own parts.
		return target instanceof TileEntityMultiblock&&((TileEntityMultiblock)target).master()==tile?null: target;
	}

	private void tickRotaryInput(StorageAccess provider)
	{
		boolean input = false;
		float speed = 0, torque = 0;
		double bestPower = -1;
		for(StorageDefinition.Port port : provider.definition.ports())
			if(port.input)
			{
				input = true;
				EnumFacing side = direction(port), otherSide = side.getOpposite();
				for(int position : tile.getMultiblock().definition.getPOI(port.poi))
				{
					TileEntity target = neighbour(position, side);
					IRotaryEnergy source = target==null?null: target.getCapability(CapabilityRotaryEnergy.ROTARY_ENERGY, otherSide);
					if(source==null||!source.getSide(otherSide).canOutput()) continue;
					float s = source.getOutputRotationSpeed(), t = source.getOutputTorque();
					if(!Float.isFinite(s)||!Float.isFinite(t)||s < 0||t < 0) continue;
					double power = (double)s*t;
					if(power > bestPower)
					{
						bestPower = power;
						speed = s;
						torque = t;
					}
				}
			}
		if(!input) return; // Output-only providers can be driven by a generator script.
		if(speed==0||torque==0)
		{
			provider.rotary().setRotationSpeed(0);
			provider.rotary().setTorque(0);
		}
		else provider.rotary().grow(speed, torque, 0.01f);
	}

	/**
	 * Called once by the master after production and script updates.
	 */
	public void tickOutputs()
	{
		if(!isServer()||outputting) return;
		outputting = true;
		try
		{
			for(StorageAccess provider : providers.values())
			{
				if(provider.definition.kind==StorageDefinition.Kind.REDSTONE)
				{
					if(!Objects.equals(publishedRedstone.get(provider.definition.name), provider.getRedstone()))
						redstoneChanged(provider.definition);
					continue;
				}
				if(!provider.definition.autoOutput()) continue;
				if(provider.definition.kind==StorageDefinition.Kind.DATA)
				{
					outputData(provider);
					continue;
				}
				int remaining = provider.definition.outputRate();
				Set<String> visited = new HashSet<>();
				for(StorageDefinition.Port port : provider.definition.ports())
					if(!port.input)
					{
						EnumFacing side = direction(port);
						for(int position : tile.getMultiblock().definition.getPOI(port.poi))
						{
							// Item declarations may select different slots on the same face.
							String key = position+":"+side;
							if(remaining==0||(provider.definition.kind!=StorageDefinition.Kind.ITEM&&!visited.add(key)))
								continue;
							TileEntity target = neighbour(position, side);
							if(target!=null)
								remaining -= StoragePortTransfer.push(provider, port, target, side.getOpposite(), remaining);
						}
					}
			}
		} finally {outputting = false;}
	}

	private void outputData(StorageAccess provider)
	{
		if(provider.firstPacket()!=null)
		{
			boolean sent = false;
			for(StorageDefinition.Port port : provider.definition.ports())
				if(!port.input)
				{
					EnumFacing side = direction(port);
					for(int position : tile.getMultiblock().definition.getPOI(port.poi))
						if(neighbour(position, side)!=null)
							sent |= IIDataHandlingUtils.sendPacketAdjacently(provider.firstPacket().clone(), tile.getWorld(), tile.getBlockPosForPos(position), side);
				}
			if(sent) provider.removePacket();
		}
	}

	public void redstoneChanged(StorageDefinition definition)
	{
		if(!isServer()) return;
		publishedRedstone.put(definition.name, get(definition.name).getRedstone());
		for(StorageDefinition.Port port : definition.ports())
			if(!port.input)
				for(int position : tile.getMultiblock().definition.getPOI(port.poi))
				{
					BlockPos at = tile.getBlockPosForPos(position);
					if(tile.getWorld().isBlockLoaded(at))
						tile.getWorld().notifyNeighborsOfStateChange(at, tile.getBlockType(), false);
				}
	}

	public int redstone(int position, EnumFacing side)
	{
		int signal = 0;
		for(StorageAccess provider : providers.values())
			if(provider.definition.kind==StorageDefinition.Kind.REDSTONE)
				for(StorageDefinition.Port port : provider.definition.ports())
					if(!port.input&&matches(port, position, side)) signal = Math.max(signal, provider.getRedstone());
		return signal;
	}

	public boolean redstonePort(int position, EnumFacing side)
	{
		for(StorageAccess provider : providers.values())
			if(provider.definition.kind==StorageDefinition.Kind.REDSTONE)
				for(StorageDefinition.Port port : provider.definition.ports())
					if(matches(port, position, side)) return true;
		return false;
	}
}
