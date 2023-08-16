package pl.pabilo8.ctmb.client.gui.elements;

/**
 * @author Pabilo8
 * @since 05.07.2022
 */
public interface IDragAndDropGUI
{
	boolean dropOnto(GuiDroppedCTMB dropped, int mouseX, int mouseY);

	int getXGridSize(GuiDroppedCTMB dropped, int mouseX, int mouseY);

	int getYGridSize(GuiDroppedCTMB dropped, int mouseX, int mouseY);

	boolean hasDragFocus();
}
