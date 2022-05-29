package pl.pabilo8.ctmb.common.crafttweaker.manual;

import com.google.common.collect.HashMultimap;
import crafttweaker.annotations.ZenDoc;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.value.IAny;

import javax.annotation.Nullable;

/**
 * @author Pabilo8
 * @since 20.03.2022
 */
@ZenClass(value = "mods.ctmb.manual.ManualTweaker")
@ZenRegister
@SuppressWarnings("unused")
public class ManualTweaker
{
	public static HashMultimap<String, CTMBManualEntry> PAGES = HashMultimap.create();

	@ZenMethod
	@ZenDoc("Adds entries to IE Manual")
	@Nullable
	public static CTMBManualEntry addEntry(String name, String category, CTMBManualPage... pages)
	{
		CTMBManualEntry entry = new CTMBManualEntry(name, category, pages);
		PAGES.put(name, entry);
		return entry;
	}

	@ZenMethod
	@ZenDoc("Adds data sources to Manual entries")
	@Nullable
	public static void addDataSource(CTMBManualEntry entry, String sourceName, IData value)
	{
		entry.addSource(sourceName, value);
	}

}