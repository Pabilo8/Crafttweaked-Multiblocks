package pl.pabilo8.ctmb.common.gui.component;

import crafttweaker.annotations.ZenDoc;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.pabilo8.ctmb.client.ClientUtils;
import pl.pabilo8.ctmb.client.gui.MultiblockGui;
import pl.pabilo8.ctmb.client.gui.StyledGuiUtils;
import pl.pabilo8.ctmb.client.gui.elements.GuiLabelCTMB;
import pl.pabilo8.ctmb.common.CommonUtils;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author Pabilo8
 * @since 04.03.2022
 */
@ZenClass("mods.ctmb.gui.component.Label")
@ZenRegister
public class GuiComponentLabel extends GuiComponent
{
	private final String text;
	private final boolean translated, dropShadow, center;
	private final int textColor;

	public GuiComponentLabel(int x, int y, int w, int h, String name, String text, boolean translated, int textColor, boolean dropShadow, boolean center)
	{
		super(x, y, name, w, h);

		this.text = text;
		this.translated = translated;
		this.dropShadow = dropShadow;
		this.textColor = textColor;
		this.center = center;
	}

	@ZenMethod()
	@ZenDoc("Creates a new Gui Component instance")
	public static GuiComponentLabel create(int x, int y, String name, IData data)
	{
		int textColor = -1;
		boolean translated = false, dropShadow = false, center = false;
		String text = name;
		int w = 200, h = 11;

		if(CommonUtils.dataCheck(data))
		{
			Map<String, IData> map = data.asMap();

			if(map.containsKey("w"))
				w = map.get("w").asInt();
			if(map.containsKey("h"))
				h = map.get("h").asInt();

			if(map.containsKey("text"))
				text = map.get("text").asString();

			if(map.containsKey("color"))
				textColor = CommonUtils.getColorFromData(map.get("color"));
			if(map.containsKey("translated"))
				translated = map.get("translated").asBool();
			if(map.containsKey("drop_shadow"))
				dropShadow = map.get("drop_shadow").asBool();
			if(map.containsKey("center"))
				center = map.get("center").asBool();
		}

		return new GuiComponentLabel(x, y, w, h, name, text, translated, textColor, dropShadow, center);
	}

	@SideOnly(Side.CLIENT)
	@Override
	@Nullable
	public Gui provide(int id, int x, int y, MultiblockGui gui)
	{
		GuiLabel label = new GuiLabelCTMB(this, ClientUtils.mc.fontRenderer, id, this.x+x, this.y+y, w, h, textColor==-1?gui.getStyle().getMainColor(): textColor, dropShadow);

		if(center)
			label.setCentered();
		for(String s : text.split("\\n"))
			if(translated)
				label.addLine(I18n.format(StyledGuiUtils.processText(gui, s)));
			else
				label.addLine(StyledGuiUtils.processText(gui, s));

		return label;
	}
}
