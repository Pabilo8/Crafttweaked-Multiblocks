package pl.pabilo8.ctmb.common.manual;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import lombok.Getter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import pl.pabilo8.immersiveintelligence.common.util.easynbt.EasyNBT;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Stores the resource and NBT sources for an II manual entry.
 *
 * @author Pabilo8 (pabilo@iiteam.net)
 * @updated 05.10.2026
 * @since 21.03.2022
 */
@ZenRegister
@ZenClass("mods.ctmb.manual.Entry")
@Getter
public class CTMBManualEntry
{
	private final String name, category, resourceId, path;
	private final CTMBManualPage[] pages;
	private final Map<String, NBTTagCompound> sources = new LinkedHashMap<>();
	private String resource;

	CTMBManualEntry(String name, String category, CTMBManualPage[] pages)
	{
		ResourceLocation id = new ResourceLocation(name);
		this.resourceId = id.toString();
		this.path = id.getResourcePath();
		this.name = referenceFor(resourceId);
		this.category = category;
		this.pages = pages.clone();
		resource = id.getResourceDomain()+":ie_manual/{lang}/"+path+".md";
	}

	/**
	 * Escapes every separator (including underscores), so folder and namespace paths cannot collide.
	 */
	public static String referenceFor(String id)
	{
		StringBuilder reference = new StringBuilder("ctmb_");
		for(char c : id.toCharArray())
		{
			if(c >= 'a'&&c <= 'z'||c >= '0'&&c <= '9') reference.append(c);
			else reference.append('_').append(Integer.toHexString(c)).append('_');
		}
		return reference.toString();
	}

	@ZenGetter("reference")
	public String reference()
	{
		return name;
	}

	/**
	 * Sets a resource path with an optional {lang} token.
	 */
	@ZenMethod
	public CTMBManualEntry setResource(String resource)
	{
		ResourceLocation location = new ResourceLocation(resource.replace("{lang}", "en_us"));
		if(location.getResourceDomain().equals("immersiveintelligence"))
			throw new IllegalArgumentException("CTMB manual resources need a separate resource domain");
		this.resource = resource;
		return this;
	}

	@ZenMethod
	public CTMBManualEntry addSource(String name, IData value)
	{
		sources.put(name, CraftTweakerMC.getNBTCompound(value).copy());
		return this;
	}

	@ZenMethod
	public CTMBManualEntry addSource(String name, IItemStack item)
	{
		sources.put(name, EasyNBT.newNBT().withItemStack("item", CraftTweakerMC.getItemStack(item)).unwrap());
		return this;
	}

	@ZenMethod
	public CTMBManualEntry addSource(String name, IItemStack[] items)
	{
		Object[] nativeItems = java.util.Arrays.stream(items).map(CraftTweakerMC::getItemStack).toArray();
		sources.put(name, EasyNBT.newNBT().withList("items", nativeItems).unwrap());
		return this;
	}

	@ZenMethod
	public CTMBManualPage[] getPages()
	{
		return pages.clone();
	}
}
