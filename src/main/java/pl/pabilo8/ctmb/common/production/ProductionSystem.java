package pl.pabilo8.ctmb.common.production;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import pl.pabilo8.ctmb.common.storage.StorageSystem;
import java.util.*;

/** Master ownership and named persistence, including definitions removed by a script edit. */
public final class ProductionSystem
{
	private final StorageSystem storage;
	private final Map<String, ProductionAccess> handlers = new LinkedHashMap<>();
	private NBTTagCompound orphaned = new NBTTagCompound();

	public ProductionSystem(StorageSystem storage)
	{
		this.storage = storage;
		storage.tile.getMultiblock().productionHandlers.forEach((name, handler) -> handlers.put(name, new ProductionAccess(storage, handler)));
	}

	public ProductionAccess get(String name)
	{
		ProductionAccess p = handlers.get(name);
		if(p==null) throw new IllegalArgumentException("Unknown production handler: "+name);
		return p;
	}

	public void tick()
	{
		if(storage.isServer()) handlers.values().forEach(ProductionAccess::tick);
	}

	public NBTTagCompound save()
	{
		NBTTagCompound tag = orphaned.copy();
		handlers.forEach((name, p) -> tag.setTag(name, p.save()));
		return tag;
	}

	public void restore(NBTTagCompound tag)
	{
		orphaned = tag.copy();
		handlers.forEach((name, p) ->  {
			p.restore(tag.getCompoundTag(name)); orphaned.removeTag(name);
		});
	}

	public List<ItemStack> claimItems()
	{
		List<ItemStack> items = new ArrayList<>();
		handlers.values().forEach(p -> items.addAll(p.claimItems()));
		for(String name : orphaned.getKeySet())
		{
			net.minecraft.nbt.NBTTagList lanes = orphaned.getCompoundTag(name).getTagList("lanes", 10);
			for(int i = 0; i<lanes.tagCount(); i++) ProductionAccess.claimSaved(lanes.getCompoundTagAt(i), items);
		}
		orphaned = new NBTTagCompound();
		storage.changed();
		return items;
	}
}
