package pl.pabilo8.ctmb.common.gui;

import pl.pabilo8.immersiveintelligence.common.util.gui.ContainerIITileBase;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.util.network.MessageTileSync;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import pl.pabilo8.ctmb.common.block.TileEntityMultiblock;
import pl.pabilo8.ctmb.common.gui.component.GuiComponent;
import pl.pabilo8.immersiveintelligence.common.util.easynbt.EasyNBT;

import java.util.HashSet;
import java.util.Set;

/**
 * Creates the server inventory slots for a scripted Deco layout.
 *
 * @author Pabilo8 (pabilo@iiteam.net)
 * @since 25.02.2022
 * @updated 05.10.2026
 */
public class MultiblockContainer extends ContainerIITileBase<TileEntityMultiblock>
{
	public MultiblockContainer(InventoryPlayer player, TileEntityMultiblock tile, int page)
	{
		super(player.player, tile);
		MultiblockGuiLayout layout = tile.getMultiblock().getGuiLayout(page);
		if(layout==null)
			throw new IllegalArgumentException("Unknown GUI page: "+page);
		Set<Integer> usedSlots = new HashSet<>();
		for(GuiComponent component : layout.getComponents().values())
			if(component.getType().equals("slot"))
			{
				EasyNBT data = component.getOptions();
				int inventory = data.getInt("inv_id"), index = data.getInt("id");
				if(inventory < 0||inventory >= tile.getMultiblock().inventory.size()
						||index < 0||index >= tile.getMultiblock().inventory.get(inventory).capacity)
					throw new IllegalArgumentException("Invalid inventory slot: "+component.getName());
				index = tile.getInvOffset(inventory, index);
				if(!usedSlots.add(index))
					throw new IllegalArgumentException("Duplicate inventory slot: "+index);
				addSlotToContainer(new CTMBSlot(inv, index, component.getX(), component.getY(),
						data.hasKey("style")?data.getString("style"):"IE"));
			}
		slotCount = inventorySlots.size();
		if(layout.isPlayerInventory()) addPlayerInventory(player, layout.getInventoryX(), layout.getInventoryY());
	}

	private NBTTagCompound lastStorage;
	private int syncTicks;

	@Override
	public void addListener(IContainerListener listener)
	{
		super.addListener(listener);
		sendStorage(listener, tile.getGuiStorageData());
	}

	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();
		if(tile.getWorld().isRemote||syncTicks++%5!=0) return;
		NBTTagCompound storage = tile.getGuiStorageData();
		if(storage.equals(lastStorage)) return;
		lastStorage = storage;
		for(IContainerListener listener : listeners) sendStorage(listener, storage);
	}

	private void sendStorage(IContainerListener listener, NBTTagCompound storage)
	{
		if(listener instanceof EntityPlayerMP)
		{
			NBTTagCompound message = new NBTTagCompound();
			message.setTag("ctmbGuiStorage", storage);
			ImmersiveEngineering.packetHandler.sendTo(new MessageTileSync(tile, message), (EntityPlayerMP)listener);
		}
	}
}
