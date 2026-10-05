package pl.pabilo8.ctmb.common.manual;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registers side-neutral definitions for the II manual.
 *
 * @author Pabilo8 (pabilo@iiteam.net)
 * @since 20.03.2022
 * @updated 05.10.2026
 */
@ZenRegister
@ZenClass("mods.ctmb.manual.ManualTweaker")
public final class ManualTweaker
{
	public static final Map<String, CTMBManualCategory> CATEGORIES = new LinkedHashMap<>();
	public static final Map<String, CTMBManualEntry> ENTRIES = new LinkedHashMap<>();

	private ManualTweaker() {}

	@ZenMethod
	public static CTMBManualCategory addCategory(String name)
	{
		validatePath(name);
		if(name.contains("/")) throw new IllegalArgumentException("Category names must not contain slashes");
		return CATEGORIES.computeIfAbsent(name, CTMBManualCategory::new);
	}

	/** Registers an entry and creates folders from its path. */
	@ZenMethod
	public static CTMBManualEntry addEntry(String name, String category, CTMBManualPage... pages)
	{
		validatePath(name);
		addCategory(category);
		String key = category+"/"+name;
		if(ENTRIES.containsKey(key)) throw new IllegalArgumentException("Duplicate manual entry: "+key);
		CTMBManualEntry entry = new CTMBManualEntry(name, category, pages);
		ENTRIES.put(key, entry);
		return entry;
	}

	@ZenMethod
	public static void addDataSource(CTMBManualEntry entry, String name, IData value) { entry.addSource(name, value); }

	private static void validatePath(String path)
	{
		if(path==null||!path.matches("[a-z0-9_]+(/[a-z0-9_]+)*"))
			throw new IllegalArgumentException("Invalid manual path: "+path);
	}
}
