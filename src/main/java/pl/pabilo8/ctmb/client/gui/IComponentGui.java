package pl.pabilo8.ctmb.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import pl.pabilo8.ctmb.client.gui.elements.GuiLabelCTMB;
import pl.pabilo8.ctmb.common.block.TileEntityMultiblock;
import pl.pabilo8.ctmb.common.gui.MultiblockGuiStyle;

import javax.annotation.Nonnull;

/**
 * @author Pabilo8
 * @since 08.07.2022
 */
public interface IComponentGui
{
	MultiblockGuiStyle getStyle();

	boolean hasTile();

	TileEntityMultiblock getTile();

	String parseVariable(String text);

	/**
	 * Adds a label to the GUI and returns it
	 *
	 * @param label to be added
	 * @return the label added
	 */
	@Nonnull
	<T extends GuiLabel> T addLabel(@Nonnull T label);

	/**
	 * Adds a button to the GUI and returns it
	 *
	 * @param button to be added
	 * @return the button added
	 */
	@Nonnull
	<T extends GuiButton> T addButton(@Nonnull T button);
}
