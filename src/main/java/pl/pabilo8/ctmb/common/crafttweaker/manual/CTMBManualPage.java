package pl.pabilo8.ctmb.common.crafttweaker.manual;

import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.ManualPages;
import blusunrize.lib.manual.ManualUtils;
import blusunrize.lib.manual.gui.GuiManual;
import crafttweaker.annotations.ZenDoc;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.pabilo8.ctmb.common.crafttweaker.manual.CTMBManualObject.ManualObjectInfo;
import pl.pabilo8.ctmb.common.crafttweaker.manual.objects.*;
import pl.pabilo8.ctmb.common.util.CTMBLogger;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Pabilo8
 * @since 20.03.2022
 */
@ZenClass(value = "mods.ctmb.manual.Page")
@ZenRegister
public class CTMBManualPage extends ManualPages
{
	//--- Statics and Internals ---//

	private static final int WIDTH = 120; //width of a single manual page
	public static HashMap<String, BiFunction<ManualObjectInfo, NBTTagCompound, CTMBManualObject>> registeredObjects = new HashMap<>();
	private final ArrayList<CTMBManualObject> manualObjects = new ArrayList<>();

	static
	{
		registeredObjects.put("image", CTMBManualImage::new);
		registeredObjects.put("multiblock", CTMBManualMultiblock::new);
		registeredObjects.put("crafting", CTMBManualCraftingRecipe::new);
		registeredObjects.put("item_display", CTMBManualItemDisplay::new);
		registeredObjects.put("table", CTMBManualTable::new);
	}

	private CTMBManualEntry entry;

	public CTMBManualPage(ManualInstance manual, String text)
	{
		super(manual, text);
	}

	@ZenMethod
	@ZenDoc("Creates a page instance")
	public static CTMBManualPage create(String text)
	{
		return new CTMBManualPage(null, text);
	}

	public void setParent(CTMBManualEntry entry)
	{
		this.entry = entry;
	}

	public void setManual()
	{
		manual = ManualHelper.getManual();
	}

	public String getPageName()
	{
		return text;
	}

	//--- Initialization on Opening ---//

	@SideOnly(Side.CLIENT)
	@Override
	public void initPage(GuiManual gui, int x, int y, List<GuiButton> pageButtons)
	{
		highlighted = ItemStack.EMPTY;
		String file = entry.fetchPage(text); //get text for this page

		if(file!=null&&!file.isEmpty())
		{
			//turn markdown into IE tags

			final Pattern patternObject = Pattern.compile("\\|(.+?)\\|");
			final Pattern patternLink = Pattern.compile("\\[(.+?)]\\((.+?)\\)");

			final Pattern patternHighlight = Pattern.compile("\\[(.+?)]");
			final Pattern patternBold = Pattern.compile("\\*\\*(.+?)\\*\\*");
			final Pattern patternItalic = Pattern.compile("\\*(.+?)\\*");
			final Pattern patternUnderline = Pattern.compile("__(.+?)__");
			final Pattern patternStrikethrough = Pattern.compile("~~(.+?)~~");

			//object
			manualObjects.clear();
			file = addObjects(file, patternObject, x, y, pageButtons, gui);

			//link
			file = matchReplace(patternLink, file, (stringBuilder, matcher) ->
					stringBuilder
							.append("<link;")
							.append(matcher.group(2))
							.append(";")
							.append(Arrays.stream(matcher.group(1).split(" "))
									.map(s -> TextFormatting.ITALIC.toString()+TextFormatting.UNDERLINE+s+" ")
									.collect(Collectors.joining())
							)
							.append(TextFormatting.RESET)
							.append(">")
			);

			//text types
			file = matchReplaceSimple(patternHighlight, file, TextFormatting.BOLD, TextFormatting.GOLD); //highlight
			file = matchReplaceSimple(patternBold, file, TextFormatting.BOLD); //bold
			file = matchReplaceSimple(patternItalic, file, TextFormatting.ITALIC); //italic
			file = matchReplaceSimple(patternUnderline, file, TextFormatting.UNDERLINE); //underline
			file = matchReplaceSimple(patternStrikethrough, file, TextFormatting.STRIKETHROUGH); //strikethrough

			//process the text IE way
			boolean uni = manual.fontRenderer.getUnicodeFlag();
			manual.fontRenderer.setUnicodeFlag(true);
			this.localizedText = manual.formatText(file);
			this.localizedText = addLinks(manual, gui, this.localizedText, x, y, WIDTH, pageButtons);

			if(this.localizedText==null)
				this.localizedText = "";

			manual.fontRenderer.setUnicodeFlag(uni);
		}
	}

	//--- GUI Rendering and Element Handling ---//

	@SideOnly(Side.CLIENT)
	@Override
	public void renderPage(GuiManual gui, int x, int y, int mx, int my)
	{
		if(localizedText!=null&&!localizedText.isEmpty())
			ManualUtils.drawSplitString(manual.fontRenderer, localizedText, x, y, WIDTH, manual.getTextColour());

		GlStateManager.enableBlend();

		for(CTMBManualObject object : manualObjects)
			object.drawTooltip(gui.mc, mx, my);

	}

	@Override
	public void buttonPressed(GuiManual gui, GuiButton button)
	{
		super.buttonPressed(gui, button);
	}

	@Override
	public void mouseDragged(int x, int y, int clickX, int clickY, int mx, int my, int lastX, int lastY, int button)
	{
		for(CTMBManualObject obj : manualObjects)
			obj.mouseDragged(x, y, clickX, clickY, mx, my, lastX, lastY, button);
	}

	@Override
	public boolean listForSearch(String searchTag)
	{
		return false;
	}

	//--- Private Methods (for parsing) ---//

	private String matchReplace(Pattern pattern, String text, BiConsumer<StringBuilder, Matcher> operation)
	{
		//iterate, skip fragments with no significance
		StringBuilder builder = new StringBuilder();
		Matcher matcher = pattern.matcher(text);
		int i = 0;
		while(matcher.find())
		{
			//replace from marker
			builder.append(text, i, matcher.start());
			operation.accept(builder, matcher);
			//move the marker
			i = matcher.end();
		}
		//build
		builder.append(text.substring(i));
		return builder.toString();
	}

	private String matchReplaceSimple(Pattern pattern, String text, TextFormatting... formats)
	{
		return matchReplace(pattern, text, (stringBuilder, matcher) ->
				{
					for(TextFormatting format : formats)
						stringBuilder.append(format);
					stringBuilder
							.append(matcher.group(1))
							.append(TextFormatting.RESET);
				}
		);
	}

	private String addObjects(String file, Pattern patternObject, int x, int y, List<GuiButton> pageButtons, GuiManual gui)
	{
		StringBuilder builder = new StringBuilder();
		String[] split = file.split("\n");
		final int[] lines = {0}; //wiurd intellij fix... but it works
		for(String line : split)
		{
			line = matchReplace(patternObject, line, (stringBuilder, matcher) ->
					{
						String text = matcher.group(1);
						CTMBManualObject object = parseObject(gui, pageButtons, text, x, y+lines[0]*manual.fontRenderer.FONT_HEIGHT);

						if(object!=null)
						{
							object.postInit(this);
							manualObjects.add(object);
							pageButtons.add(object);
							for(int i = 0; i < (object.height/manual.fontRenderer.FONT_HEIGHT); i++)
							{
								lines[0]++;
								stringBuilder.append("<br>");
							}
						}
					}
			);
			builder.append(line).append("\n");
			lines[0]++;
		}

		return builder.toString();
	}

	@Nullable
	private CTMBManualObject parseObject(GuiManual gui, List<GuiButton> pageButtons, String text, int x, int y)
	{
		final Matcher matcherName = Pattern.compile("\\[(.+?)]").matcher(text);
		final Matcher matcherTag = Pattern.compile("\\{(.+?)}\\|").matcher(text+"|");

		String objectID = matcherName.find()?matcherName.group(1): "";
		String objectTag = matcherTag.find()?String.format("{%s}", matcherTag.group(1)): "";

		if(objectID.isEmpty())
			return null;

		BiFunction<ManualObjectInfo, NBTTagCompound, CTMBManualObject> fun = registeredObjects.getOrDefault(objectID, null);
		NBTTagCompound tag = null;

		if(objectTag!=null)
			try
			{
				tag = JsonToNBT.getTagFromJson(objectTag);
			}
			catch(NBTException ignored)
			{
			}

		if(fun!=null)
			return fun.apply(getInfoForNext(gui, pageButtons, x, y), tag==null?new NBTTagCompound(): tag);

		return null;
	}

	private ManualObjectInfo getInfoForNext(GuiManual gui, List<GuiButton> pageButtons, int x, int y)
	{
		return new ManualObjectInfo(gui, x, y, pageButtons.size()+100);
	}

	@Nullable
	public IData getDataSource(String name)
	{
		return entry.getSource(name);
	}
}
