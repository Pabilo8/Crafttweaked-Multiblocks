package pl.pabilo8.ctmb.common.storage;

import net.minecraft.nbt.NBTTagCompound;
import pl.pabilo8.immersiveintelligence.api.rotary.RotaryStorage;

/** II's continuous rotary state, bounded and synchronised with its owning provider. */
public final class CTMBRotaryStorage extends RotaryStorage
{
	private final StorageSystem system;
	private final StorageDefinition definition;

	CTMBRotaryStorage(StorageSystem system, StorageDefinition definition)
	{
		this.system = system;
		this.definition = definition;
	}

	private static float bounded(float value, float maximum)
	{
		return Float.isFinite(value)?Math.max(0, Math.min(value, maximum)): 0;
	}

	@Override
	public void setRotationSpeed(float value)
	{
		if(!system.isServer()) return;
		value = bounded(value, definition.maxSpeed());
		if(speed!=value) {speed = value; system.changed();}
	}

	@Override
	public void setTorque(float value)
	{
		if(!system.isServer()) return;
		value = bounded(value, definition.maxTorque());
		if(torque!=value) {torque = value; system.changed();}
	}

	@Override
	public void deserializeNBT(NBTTagCompound tag)
	{
		// Loading/synchronising state also runs before a world is attached and on clients.
		speed = bounded(tag.getFloat("speed"), definition.maxSpeed());
		torque = bounded(tag.getFloat("torque"), definition.maxTorque());
	}
}
