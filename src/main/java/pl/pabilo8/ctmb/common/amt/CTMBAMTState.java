package pl.pabilo8.ctmb.common.amt;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Ordered, persisted animation samples shared by render and collision maps.
 */
public final class CTMBAMTState
{
	private final Map<ResourceLocation, Float> samples = new LinkedHashMap<>();

	public Map<ResourceLocation, Float> samples()
	{
		return Collections.unmodifiableMap(samples);
	}

	public boolean set(String animation, float progress)
	{
		if(!Float.isFinite(progress)) throw new IllegalArgumentException("Animation progress must be finite");
		ResourceLocation id = new ResourceLocation(animation);
		float value = Math.max(0, Math.min(1, progress));
		return !Objects.equals(samples.put(id, value), value);
	}

	public boolean clear(String animation)
	{
		return samples.remove(new ResourceLocation(animation))!=null;
	}

	public boolean clear()
	{
		boolean changed = !samples.isEmpty();
		samples.clear();
		return changed;
	}

	public NBTTagCompound save()
	{
		NBTTagCompound tag = new NBTTagCompound();
		int index = 0;
		for(Map.Entry<ResourceLocation, Float> sample : samples.entrySet())
		{
			NBTTagCompound entry = new NBTTagCompound();
			entry.setString("animation", sample.getKey().toString());
			entry.setFloat("progress", sample.getValue());
			tag.setTag(Integer.toString(index++), entry);
		}
		tag.setInteger("count", index);
		return tag;
	}

	public void restore(NBTTagCompound tag)
	{
		samples.clear();
		int count = tag.getInteger("count");
		if(count < 0||count > 4096) throw new IllegalArgumentException("Invalid AMT sample count");
		for(int index = 0; index < count; index++)
		{
			NBTTagCompound sample = tag.getCompoundTag(Integer.toString(index));
			set(sample.getString("animation"), sample.getFloat("progress"));
		}
	}
}
