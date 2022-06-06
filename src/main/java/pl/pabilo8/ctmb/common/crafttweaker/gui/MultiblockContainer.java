package pl.pabilo8.ctmb.common.crafttweaker.gui;

import blusunrize.immersiveengineering.common.gui.ContainerIEBase;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import pl.pabilo8.ctmb.common.CommonUtils;
import pl.pabilo8.ctmb.common.block.TileEntityBasicMultiblock;
import pl.pabilo8.ctmb.common.crafttweaker.MultiblockBasic;
import pl.pabilo8.ctmb.common.crafttweaker.gui.component.GuiComponent;

import java.util.Arrays;

/**
 * @author Pabilo8
 * @since 25.02.2022
 */
public class MultiblockContainer extends ContainerIEBase<TileEntityBasicMultiblock>
{
	private final MultiblockBasic mb;
	private final MultiblockGuiLayout layout;

	public MultiblockContainer(InventoryPlayer inventoryPlayer, TileEntityBasicMultiblock tile, int page)
	{
		super(inventoryPlayer, tile);
		this.mb = tile.getMultiblock();
		this.layout = page==0?mb.mainGui: CommonUtils.getMapElement(mb.assignedGuis, page);

		initSlots();
	}

	private void initSlots()
	{
		this.slotCount = tile.getInventory().size();
		for(GuiComponent comp : layout.components.values())
		{
			Slot[] slots = comp.provideSlots(this);
			if(slots!=null)
				Arrays.stream(slots).forEach(this::addSlotToContainer);
		}
	}

}
