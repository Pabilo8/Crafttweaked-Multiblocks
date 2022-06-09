package pl.pabilo8.ctmb.common.crafttweaker.storage;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

/**
 * @author Pabilo8
 * @since 08.06.2022
 */
public class CTMBSlot extends Slot
{
	private final int style;

	public CTMBSlot(IInventory inventoryIn, int index, int xPosition, int yPosition, int style)
	{
		super(inventoryIn, index, xPosition, yPosition);
		this.style = style;
	}

	public int getStyle()
	{
		return style;
	}
}
