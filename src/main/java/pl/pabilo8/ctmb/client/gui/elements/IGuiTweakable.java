package pl.pabilo8.ctmb.client.gui.elements;

import crafttweaker.api.data.DataMap;
import pl.pabilo8.ctmb.common.crafttweaker.gui.component.GuiComponent;

import javax.annotation.Nullable;

/**
 * @author Pabilo8
 * @since 03.06.2022
 */
public interface IGuiTweakable
{
	@Nullable
	GuiComponent getBlueprint();

	void setData(DataMap map);

	DataMap getData();
}
