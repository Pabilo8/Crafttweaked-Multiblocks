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
import pl.pabilo8.ctmb.client.gui.IComponentGui;
import pl.pabilo8.ctmb.client.gui.StyledGuiUtils;
import pl.pabilo8.ctmb.client.gui.elements.GuiLabelCTMB;
import pl.pabilo8.ctmb.common.util.GuiNBTData;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nullable;

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
		GuiNBTData map = new GuiNBTData(data);

		return new GuiComponentLabel(x, y,
				map.getWidth(200), map.getHeight(11),
				name,
				map.getText(name),
				map.getTranslated(),
				map.getColor(-1),
				map.getProperty("drop_shadow"),
				map.getProperty("center")
		);
	}

	@SideOnly(Side.CLIENT)
	@Override
	@Nullable
	public Gui provide(int id, int x, int y, IComponentGui gui)
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
