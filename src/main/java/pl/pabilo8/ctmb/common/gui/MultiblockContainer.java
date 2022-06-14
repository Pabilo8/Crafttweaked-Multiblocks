package pl.pabilo8.ctmb.common.gui;

import blusunrize.immersiveengineering.common.gui.ContainerIEBase;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import pl.pabilo8.ctmb.common.CommonUtils;
import pl.pabilo8.ctmb.common.block.TileEntityMultiblock;
import pl.pabilo8.ctmb.common.block.crafttweaker.Multiblock;
import pl.pabilo8.ctmb.common.gui.component.GuiComponent;

import java.util.Arrays;

/**
 * @author Pabilo8
 * @since 25.02.2022
 */
public class MultiblockContainer extends ContainerIEBase<TileEntityMultiblock>
{
	private final MultiblockGuiLayout layout;

	public MultiblockContainer(InventoryPlayer inventoryPlayer, TileEntityMultiblock tile, int page)
	{
		super(inventoryPlayer, tile);
		Multiblock mb = tile.getMultiblock();
		this.layout = page==0?mb.mainGui: CommonUtils.getMapElement(mb.assignedGuis, page);

		initSlots(inventoryPlayer);
	}

	private void initSlots(InventoryPlayer inventoryPlayer)
	{
		this.slotCount = tile.getInventory().size();
		for(GuiComponent comp : layout.components.values())
		{
			Slot[] slots = comp.provideSlots(this, inventoryPlayer);
			if(slots!=null)
				Arrays.stream(slots).forEach(this::addSlotToContainer);
		}
	}

}
