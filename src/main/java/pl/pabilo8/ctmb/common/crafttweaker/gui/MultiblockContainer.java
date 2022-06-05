package pl.pabilo8.ctmb.common.crafttweaker.gui;

import blusunrize.immersiveengineering.common.gui.ContainerIEBase;
import net.minecraft.entity.player.InventoryPlayer;
import pl.pabilo8.ctmb.common.block.TileEntityBasicMultiblock;

/**
 * @author Pabilo8
 * @since 25.02.2022
 */
public class MultiblockContainer extends ContainerIEBase<TileEntityBasicMultiblock>
{
	public MultiblockContainer(InventoryPlayer inventoryPlayer, TileEntityBasicMultiblock tile, int page)
	{
		//tile.getMultiblock()
		super(inventoryPlayer, tile);
	}
}
