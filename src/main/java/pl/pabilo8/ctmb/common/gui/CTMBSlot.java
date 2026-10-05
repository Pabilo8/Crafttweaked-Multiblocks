package pl.pabilo8.ctmb.common.gui;

import lombok.Getter;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

/**
 * Stores a Deco slot style without loading client classes.
 *
 * @author Pabilo8 (pabilo@iiteam.net)
 * @since 08.06.2022
 * @updated 05.10.2026
 */
@Getter
public class CTMBSlot extends Slot
{
	private final String style;

	public CTMBSlot(IInventory inventory, int index, int x, int y, String style)
	{
		super(inventory, index, x, y);
		this.style = style;
	}
}
