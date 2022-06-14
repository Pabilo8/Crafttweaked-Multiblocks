package pl.pabilo8.ctmb.common.manual;

import blusunrize.lib.manual.ManualInstance.ManualEntry;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import crafttweaker.mc1120.util.CraftTweakerHacks;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.Language;
import net.minecraft.client.resources.Locale;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.pabilo8.ctmb.CTMB;
import pl.pabilo8.ctmb.client.ClientUtils;
import pl.pabilo8.ctmb.common.CommonProxy;
import pl.pabilo8.ctmb.common.util.CTMBFileUtils;
import stanhebben.zenscript.annotations.ZenClass;

import javax.annotation.Nullable;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Pabilo8
 * @since 21.03.2022
 */
@ZenClass(value = "mods.ctmb.manual.Entry")
@ZenRegister
public class CTMBManualEntry extends ManualEntry
{
	private final HashMap<String, String> texts = new HashMap<>();
	private final HashMap<String, IData> dataSources = new HashMap<>();

	public CTMBManualEntry(String name, String category, CTMBManualPage... pages)
	{
		super(name, category, pages);
		for(CTMBManualPage page : pages)
			page.setParent(this);
	}

	@SideOnly(Side.CLIENT)
	@SuppressWarnings("deprecation")
	public void loadTexts(boolean updateMeta)
	{
		Language l = ClientUtils.mc.getLanguageManager().getCurrentLanguage();
		boolean nonEnglish = !l.getLanguageCode().equalsIgnoreCase("en_us");

		File manualEntries = new File(new File(new File(CommonProxy.RESOURCE_LOADER.getResourceFolder(), CTMB.MODID), "ie_manual"), l.getLanguageCode().toLowerCase());
		File stateFile = new File(manualEntries, getName()+".md"); //i.e. ie_manual/pl_pl/melter.md

		if(nonEnglish&&!stateFile.exists())
		{
			manualEntries = new File(new File(new File(CommonProxy.RESOURCE_LOADER.getResourceFolder(), CTMB.MODID), "ie_manual"), "en_us");
			stateFile = new File(manualEntries, getName()+".md"); //i.e. ie_manual/en_us/melter.md
		}

		if(stateFile.exists())
		{
			String file = CTMBFileUtils.readFileToString(stateFile);

			texts.clear();
			String[] split = file.split("#"); //separate sections
			System.arraycopy(split, 1, split, 0, split.length-1);

			for(String s : split)
			{
				String[] lines = s.split("\n"); //separate with newline, for iteration

				if(s.startsWith("meta")&&updateMeta&&lines.length > 1) //metadata
				{
					//you wish you know how much I struggled to get this to work... xD

					Locale locale = CraftTweakerHacks.getPrivateStaticObject(I18n.class, "i18nLocale");
					Map<String, String> properties = ReflectionHelper.getPrivateValue(Locale.class, locale, "properties");

					properties.put("ie.manual.entry."+getName()+".name", lines[1]); //title
					if(lines.length > 2)
						properties.put("ie.manual.entry."+getName()+".subtext", lines[2]); //subtitle
				}
				else //process section
				{
					StringBuilder builder = new StringBuilder();
					for(int i = 1; i < lines.length; i++)
						builder.append(lines[i]).append("\n"); //add all lines aside page id

					texts.put(lines[0], builder.toString());
				}
			}

		}
	}

	public void addSource(String name, IData source)
	{
		dataSources.put(name, source);
	}

	public String fetchPage(String text)
	{
		// TODO: 21.03.2022 debug mode for auto-reload when editing a page
		loadTexts(false);

		return texts.getOrDefault(text, null);
	}

	@Nullable
	public IData getSource(String name)
	{
		return dataSources.getOrDefault(name, null);
	}
}
