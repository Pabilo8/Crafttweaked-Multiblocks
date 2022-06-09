package pl.pabilo8.ctmb.client.gui.elements;

import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import pl.pabilo8.ctmb.common.crafttweaker.gui.MultiblockGuiStyle;
import pl.pabilo8.ctmb.common.crafttweaker.gui.component.GuiComponent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * @author Pabilo8
 * @since 08.06.2022
 */
public class GuiEnergyBar extends GuiBar
{
	private final Supplier<FluxStorage> energy;

	public GuiEnergyBar(@Nullable GuiComponent parent, int buttonId, int x, int y, int w, int h, MultiblockGuiStyle style, Supplier<FluxStorage> energy, int styleID)
	{
		super(parent, buttonId, x, y, w, h, style, styleID, 0xb51500, 0x600b00);
		this.energy = energy;
	}

	@Override
	public float getBar()
	{
		FluxStorage storage = energy.get();
		return storage.getEnergyStored()/(float)storage.getMaxEnergyStored();
	}
}
