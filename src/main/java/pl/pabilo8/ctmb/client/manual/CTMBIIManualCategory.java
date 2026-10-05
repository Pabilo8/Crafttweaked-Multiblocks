package pl.pabilo8.ctmb.client.manual;

import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.lib.manual.ManualInstance.ManualEntry;
import pl.pabilo8.ctmb.common.manual.CTMBManualEntry;
import pl.pabilo8.ctmb.common.manual.CTMBManualPage;
import pl.pabilo8.ctmb.common.manual.ManualTweaker;
import pl.pabilo8.immersiveintelligence.client.manual.IIManualCategory;
import pl.pabilo8.immersiveintelligence.client.manual.IIManualEntry;
import pl.pabilo8.immersiveintelligence.client.manual.IIManualPage;
import pl.pabilo8.immersiveintelligence.common.util.easynbt.EasyNBT;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Registers scripted entries in an II manual category.
 *
 * @author Pabilo8 (pabilo@iiteam.net)
 * @since 05.10.2026
 */
public class CTMBIIManualCategory extends IIManualCategory
{
	private final String name;
	private final Map<String, IIManualEntry> registered = new LinkedHashMap<>();
	private final Map<String, String> folderTitles = new LinkedHashMap<>();

	public CTMBIIManualCategory(String name) { this.name = name; }

	@Override
	public String getCategory() { return name; }

	@Override
	public void addPages()
	{
		//II clears a category in addPages. Preserve entries in existing categories.
		if(!ManualHelper.getManual().manualContents.containsKey(name)) super.addPages();
		Map<String, CTMBIIManualFolder> folders = new LinkedHashMap<>();
		for(CTMBManualEntry definition : ManualTweaker.ENTRIES.values())
		{
			if(!definition.getCategory().equals(name)) continue;
			String[] path = definition.getName().split("/");
			String leaf = path[path.length-1];
			for(ManualEntry existing : ManualHelper.getManual().manualContents.values())
				if(existing.getName().equals(leaf))
					throw new IllegalArgumentException("Manual entry names must be unique: "+leaf);
			CTMBIIManualFolder folder = null;
			StringBuilder fullPath = new StringBuilder();
			for(int i = 0; i < path.length-1; i++)
			{
				if(i > 0) fullPath.append('/');
				fullPath.append(path[i]);
				CTMBIIManualFolder next = folders.get(fullPath.toString());
				if(next==null)
				{
					String id = "ctmb_folder."+name+"."+fullPath.toString().replace('/', '.');
					next = folder==null?new CTMBIIManualFolder(id, name):new CTMBIIManualFolder(id, folder);
					folderTitles.put(id, path[i]);
					updateFolderTitle(id, path[i]);
					folders.put(fullPath.toString(), next);
				}
				folder = next;
			}
			IIManualEntry entry = new CTMBIIManualEntry(definition);
			applyDefinition(entry, definition);
			if(folder==null) ManualHelper.getManual().manualContents.put(name, entry);
			else folder.addEntry(entry);
			registered.put(definition.getName(), entry);
		}
	}

	/** Restores entries removed by II's manual reload command. */
	public void ensureRegistered()
	{
		if(ManualHelper.getManual().manualContents.values().containsAll(registered.values())) return;
		String prefix = "ctmb_folder."+name+".";
		ManualHelper.getManual().manualContents.entries().removeIf(pair ->
				registered.containsValue(pair.getValue())||pair.getValue().getName().startsWith(prefix)
						||pair.getValue().getName().startsWith("folder:"+prefix));
		registered.clear();
		folderTitles.clear();
		addPages();
	}

	public void reload()
	{
		folderTitles.forEach(this::updateFolderTitle);
		registered.forEach((path, entry) -> {
			entry.loadTexts(true);
			applyDefinition(entry, ManualTweaker.ENTRIES.get(name+"/"+path));
		});
	}

	private void updateFolderTitle(String id, String folder)
	{
		String key = "ie.manual.folder."+folder;
		String title = net.minecraft.client.resources.I18n.hasKey(key)?net.minecraft.client.resources.I18n.format(key):folder;
		net.minecraft.client.resources.I18n.i18nLocale.properties.put("ie.manual.entry."+id+".name",
				pl.pabilo8.immersiveintelligence.common.util.IIReference.CHARICON_FOLDER+" "+title);
	}

	private void applyDefinition(IIManualEntry entry, CTMBManualEntry definition)
	{
		definition.getSources().forEach((key, source) -> entry.addSource(key, EasyNBT.wrapNBT(source.copy())));
		CTMBManualPage[] sections = definition.getPages();
		if(sections.length==0) return;
		List<IIManualPage> pages = new ArrayList<>();
		for(CTMBManualPage section : sections)
		{
			IIManualPage page = new IIManualPage(section.getName());
			page.setManual();
			page.setParent(entry);
			pages.add(page);
		}
		entry.setPages(pages.toArray(new IIManualPage[0]));
	}
}
