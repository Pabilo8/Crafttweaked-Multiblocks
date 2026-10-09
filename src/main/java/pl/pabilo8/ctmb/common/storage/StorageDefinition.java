package pl.pabilo8.ctmb.common.storage;

import crafttweaker.annotations.ZenRegister;
import pl.pabilo8.ctmb.common.block.MultiblockDefinition;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Named provider configuration; frozen after registry scripts finish.
 */
@ZenRegister
@ZenClass("mods.ctmb.storage.Provider")
public final class StorageDefinition
{
	public enum Kind
	{ITEM, FLUID, DUST, ENERGY, ROTARY, DATA, REDSTONE}

	public final String name;
	public final Kind kind;
	private int size = 1;
	private int outputRate = Integer.MAX_VALUE;
	private boolean autoOutput = true;
	private float maxSpeed = Float.MAX_VALUE, maxTorque = Float.MAX_VALUE;
	private boolean frozen;
	private final List<Port> ports = new ArrayList<>();

	public StorageDefinition(String name, Kind kind)
	{
		if(name==null||!name.matches("[a-z0-9_]+")) throw new IllegalArgumentException("Invalid storage name: "+name);
		this.name = name;
		this.kind = kind;
	}

	@ZenMethod
	public StorageDefinition withSize(int size)
	{
		mutable();
		if(size < 1||((kind==Kind.REDSTONE||kind==Kind.ROTARY)&&size!=1))
			throw new IllegalArgumentException("Invalid "+kind+" storage size: "+size);
		this.size = size;
		return this;
	}

	/**
	 * Maximum items, fluid/dust mB or IF pushed per provider per tick, shared by its ports.
	 */
	@ZenMethod
	public StorageDefinition withOutputRate(int amount)
	{
		mutable();
		if(amount <= 0||(kind!=Kind.ITEM&&kind!=Kind.FLUID&&kind!=Kind.DUST&&kind!=Kind.ENERGY))
			throw new IllegalArgumentException("Output rate requires item, fluid, dust or energy storage and a positive amount");
		outputRate = amount;
		return this;
	}

	@ZenMethod
	public StorageDefinition withAutoOutput(boolean enabled)
	{
		mutable();
		autoOutput = enabled;
		return this;
	}

	@ZenMethod
	public StorageDefinition withRotaryLimits(float speed, float torque)
	{
		mutable();
		if(kind!=Kind.ROTARY||!Float.isFinite(speed)||!Float.isFinite(torque)||speed <= 0||torque <= 0)
			throw new IllegalArgumentException("Rotary limits require positive finite D/t and IT values");
		maxSpeed = speed;
		maxTorque = torque;
		return this;
	}

	public int outputRate()
	{
		return outputRate;
	}

	public boolean autoOutput()
	{
		return autoOutput;
	}

	public float maxSpeed()
	{
		return maxSpeed;
	}

	public float maxTorque()
	{
		return maxTorque;
	}

	@ZenMethod
	public StorageDefinition withInputPort(String poi)
	{
		return port(poi, poi, null, true);
	}

	@ZenMethod
	public StorageDefinition withInputPort(String poi, int[] slots)
	{
		return port(poi, poi, slots, true);
	}

	@ZenMethod
	public StorageDefinition withInputPort(String poi, String direction)
	{
		return port(poi, direction, null, true);
	}

	@ZenMethod
	public StorageDefinition withInputPort(String poi, String direction, int[] slots)
	{
		return port(poi, direction, slots, true);
	}

	@ZenMethod
	public StorageDefinition withOutputPort(String poi)
	{
		return port(poi, poi, null, false);
	}

	@ZenMethod
	public StorageDefinition withOutputPort(String poi, int[] slots)
	{
		return port(poi, poi, slots, false);
	}

	@ZenMethod
	public StorageDefinition withOutputPort(String poi, String direction)
	{
		return port(poi, direction, null, false);
	}

	@ZenMethod
	public StorageDefinition withOutputPort(String poi, String direction, int[] slots)
	{
		return port(poi, direction, slots, false);
	}

	private StorageDefinition port(String poi, String direction, int[] slots, boolean input)
	{
		mutable();
		if(poi==null||direction==null) throw new IllegalArgumentException("A port needs a POI and direction name");
		if(slots!=null&&kind!=Kind.ITEM) throw new IllegalArgumentException("Slot selectors require an item provider");
		if(slots!=null&&slots.length==0) throw new IllegalArgumentException("An empty slot selector exposes no slots");
		ports.add(new Port(poi, direction, slots, input));
		return this;
	}

	public void freeze(MultiblockDefinition definition)
	{
		if(frozen) return;
		for(Port port : ports)
		{
			if(!definition.hasPOI(port.poi)) throw new IllegalArgumentException(name+": Missing/empty POI "+port.poi);
			if(!definition.hasDirection(port.direction))
				throw new IllegalArgumentException(name+": Missing direction "+port.direction);
			if(port.slots!=null) for(int slot : port.slots)
				if(slot < 0||slot >= size) throw new IllegalArgumentException(name+": Invalid slot "+slot);
		}
		frozen = true;
	}

	public int size()
	{
		return size;
	}

	public List<Port> ports()
	{
		return Collections.unmodifiableList(ports);
	}

	private void mutable()
	{
		if(frozen) throw new IllegalStateException("Storage definitions cannot change after registration: "+name);
	}

	public static final class Port
	{
		public final String poi, direction;
		public final boolean input;
		private final int[] slots;

		private Port(String poi, String direction, int[] slots, boolean input)
		{
			this.poi = poi;
			this.direction = direction;
			this.slots = slots==null?null: Arrays.stream(slots).distinct().sorted().toArray();
			this.input = input;
		}

		public boolean includes(int slot)
		{
			return slots==null||Arrays.binarySearch(slots, slot) >= 0;
		}
	}
}
