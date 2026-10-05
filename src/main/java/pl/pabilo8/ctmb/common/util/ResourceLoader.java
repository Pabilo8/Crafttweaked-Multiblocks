package pl.pabilo8.ctmb.common.util;

import com.google.gson.*;
import crafttweaker.mc1120.util.CraftTweakerHacks;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Loader;
import pl.pabilo8.ctmb.CTMB;
import pl.pabilo8.ctmb.common.CommonProxy;
import pl.pabilo8.ctmb.common.block.crafttweaker.Multiblock;
import pl.pabilo8.ctmb.common.manual.CTMBManualEntry;
import pl.pabilo8.ctmb.common.manual.CTMBManualPage;
import pl.pabilo8.ctmb.common.manual.ManualTweaker;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Rightfully stolen from Contenttweaker/B.A.S.E.<br>
 * <a href="https://github.com/The-Acronym-Coders/BASE/blob/develop/1.12.0/src/main/java/com/teamacronymcoders/base/util/files/ResourceLoader.java">https://github.com/The-Acronym-Coders/BASE/blob/develop/1.12.0/src/main/java/com/teamacronymcoders/base/util/files/ResourceLoader.java</a>
 *
 * @since 19.02.2022
 */

public class ResourceLoader
{
	private File resourceFolder;
	private final Gson gson;

	public ResourceLoader()
	{
		gson = new GsonBuilder().setPrettyPrinting().create();
	}

	public void setup()
	{
		Object minecraftDirObject = CraftTweakerHacks.getPrivateStaticObject(Loader.class, "minecraftDir");

		if(minecraftDirObject instanceof File)
		{
			File minecraftDir = (File)minecraftDirObject;
			this.resourceFolder = new File(minecraftDir, "resources");
			CTMBFileUtils.createFolder(this.resourceFolder);

		}

		createPackMcMeta();
		prepareResources();
	}

	public File getResourceFolder()
	{
		return resourceFolder;
	}

	private void prepareResources()
	{
		fixLangFolder();
	}

	private void fixLangFolder()
	{
		File[] modFolders = this.resourceFolder.listFiles(File::isDirectory);
		if(Objects.nonNull(modFolders))
		{
			Arrays.stream(modFolders)
					.map(modFolder -> new File(modFolder, "lang"))
					.filter(File::exists)
					.filter(File::isDirectory)
					.map(File::listFiles)
					.filter(Objects::nonNull)
					.flatMap(Arrays::stream)
					.filter(File::isFile)
					.forEach(this::fixLangFile);
		}

	}

	final Pattern pattern = Pattern.compile(" += +");

	private void fixLangFile(File file)
	{
		String fileString = CTMBFileUtils.readFileToString(file);
		fileString = pattern.matcher(fileString).replaceAll("=");
		CTMBFileUtils.writeStringToFile(fileString, file);
	}

	private void createPackMcMeta()
	{
		String mcMeta = "{\"pack\":{\"pack_format\":3,\"description\":\"CTMB External Resources\"}}";
		CTMBFileUtils.writeStringToFile(mcMeta, new File(this.resourceFolder, "pack.mcmeta"));
	}

	public void createFolders()
	{
		File modFolder = new File(resourceFolder, CTMB.MODID);
		CTMBFileUtils.createFolder(modFolder);
		File lang = new File(modFolder, "lang");
		CTMBFileUtils.createFolder(lang);
		File enUsLang = new File(lang, "en_us.lang");
		if(!enUsLang.exists())
		{
			CTMBFileUtils.writeStringToFile("", enUsLang);
		}
		CTMBFileUtils.createFolder(new File(modFolder, "ie_manual"));

		File textures = new File(modFolder, "textures");
		CTMBFileUtils.createFolder(textures);
		CTMBFileUtils.createFolder(new File(textures, "blocks"));

		CTMBFileUtils.createFolder(new File(modFolder, "blockstates"));

		CTMBFileUtils.createFolder(new File(modFolder, "structures"));

		File models = new File(modFolder, "models");
		CTMBFileUtils.createFolder(models);
		CTMBFileUtils.createFolder(new File(models, "block"));
	}

	public void autoGenerateFiles()
	{
		File modFolder = new File(resourceFolder, CTMB.MODID);
		File blockstates = new File(modFolder, "blockstates");
		File manualEntries = new File(new File(modFolder, "ie_manual"), "en_us");

		for(Multiblock mb : CommonProxy.MULTIBLOCKS)
		{
			File stateFile = new File(blockstates, mb.getFlattenedName()+".json");
			if(!stateFile.exists())
			{
				CTMBFileUtils.createFile(stateFile);
				JsonObject state = new JsonObject();
				state.addProperty("forge_marker", 1);

				JsonObject defaults = new JsonObject();
				JsonObject custom = new JsonObject();
				JsonObject textures = new JsonObject();
				JsonObject variants = new JsonObject();

				//defaults

				custom.addProperty("flip-v", true);
				defaults.add("custom", custom);

				textures.addProperty("particle", "immersiveengineering:blocks/storage_steel");
				defaults.add("textures", textures);

				state.add("defaults", defaults);

				//variants
				JsonObject facing = new JsonObject();
				for(EnumFacing f : EnumFacing.HORIZONTALS)
				{
					JsonObject fac = new JsonObject();
					JsonObject transform = new JsonObject();
					JsonObject rotation = new JsonObject();
					rotation.addProperty("y", (int)f.getHorizontalAngle());
					transform.add("rotation", rotation);
					fac.add("transform", transform);

					facing.add(f.getName(), fac);
				}

				String modelName = mb.getUniqueName().substring(mb.getUniqueName().indexOf(":")+1);

				JsonObject inventory = new JsonObject();
				inventory.addProperty("model", "ctmb:"+modelName+".obj");

				JsonObject transform = new JsonObject();
				transform.addProperty("scale", 0.1875);
				transform.add("rotation", jsonArrayOf(
						getIntProperty("x", 20),
						getIntProperty("y", -45)
				));

				inventory.add("transform", transform);
				variants.add("inventory", jsonArrayOf(inventory));

				variants.add("facing", facing);


				variants.add("_0multiblockslave", getBooleanProperty(
						getNamedProperty("model", "immersiveengineering:ie_empty"),
						null
				));
				variants.add("boolean0", getBooleanProperty(
						getNamedProperty("model", "ctmb:"+modelName+"_mirrored.obj"),
						getNamedProperty("model", "ctmb:"+modelName+".obj")
				));

				state.add("variants", variants);

				CTMBFileUtils.writeStringToFile(gson.toJson(state), stateFile);
			}
		}

		for(CTMBManualEntry entry : ManualTweaker.ENTRIES.values())
		{
			String defaultResource = CTMB.MODID+":ie_manual/{lang}/"+entry.getCategory()+"/"+entry.getName()+".md";
			if(!entry.getResource().equals(defaultResource)) continue;
			File entryFile = new File(manualEntries, entry.getCategory()+"/"+entry.getName()+".md");
			if(entryFile.exists()) continue;
			StringBuilder text = new StringBuilder("#meta\nTitle\nSubtitle\n");
			CTMBManualPage[] pages = entry.getPages();
			if(pages.length==0) pages = new CTMBManualPage[]{CTMBManualPage.create("overview")};
			for(CTMBManualPage page : pages)
				text.append("\n#").append(page.getName()).append("\nAdd manual text here.\n");
			CTMBFileUtils.writeStringToFile(text.toString(), entryFile);
		}
	}

	private JsonObject getIntProperty(String name, int value)
	{
		JsonObject o = new JsonObject();
		o.addProperty(name, value);
		return o;
	}

	public JsonObject getBooleanProperty(@Nullable JsonElement t, @Nullable JsonElement f)
	{
		JsonObject o = new JsonObject();
		o.add("false", f==null?new JsonObject(): f);
		o.add("true", t==null?new JsonObject(): t);
		return o;
	}

	public JsonObject getNamedProperty(String name, String value)
	{
		JsonObject o = new JsonObject();
		o.addProperty(name, value);
		return o;
	}

	public JsonArray jsonArrayOf(JsonElement... elements)
	{
		JsonArray array = new JsonArray();
		for(JsonElement element : elements)
			array.add(element);
		return array;
	}

}