package pl.pabilo8.ctmb.common.manual;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import crafttweaker.api.minecraft.CraftTweakerMC;
import lombok.Getter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import pl.pabilo8.ctmb.CTMB;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Stores the resource and NBT sources for an II manual entry.
 *
 * @author Pabilo8 (pabilo@iiteam.net)
 * @since 21.03.2022
 * @updated 05.10.2026
 */
@ZenRegister
@ZenClass("mods.ctmb.manual.Entry")
@Getter
public class CTMBManualEntry
{
	private final String name, category;
	private final CTMBManualPage[] pages;
	private final Map<String, NBTTagCompound> sources = new LinkedHashMap<>();
	private String resource;

	CTMBManualEntry(String name, String category, CTMBManualPage[] pages)
	{
		this.name = name;
		this.category = category;
		this.pages = pages.clone();
		resource = CTMB.MODID+":ie_manual/{lang}/"+category+"/"+name+".md";
	}

	/** Sets a resource path with an optional {lang} token. */
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
	public CTMBManualPage[] getPages() { return pages.clone(); }
}
