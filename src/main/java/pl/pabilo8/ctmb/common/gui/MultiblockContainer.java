package pl.pabilo8.ctmb.common.gui;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.util.network.MessageTileSync;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.nbt.NBTTagCompound;
import pl.pabilo8.ctmb.common.block.TileEntityMultiblock;
import pl.pabilo8.immersiveintelligence.common.util.gui.ContainerIITileBase;

/**
 * The common initializer supplies the exact slots used by both server and native Deco GUI.
 */
public class MultiblockContainer extends ContainerIITileBase<TileEntityMultiblock>
{
	public final MultiblockGuiLayout layout;
	private NBTTagCompound lastStorage;
	private int syncTicks;

	public MultiblockContainer(InventoryPlayer player, TileEntityMultiblock tile, int page)
	{
		super(player.player, tile);
		GuiDefinition definition = tile.getMultiblock().getGuiLayout(page);
		if(definition==null) throw new IllegalArgumentException("Unknown CTMB GUI page "+page);
		layout = definition.build(tile, CraftTweakerMC.getIPlayer(player.player));
		for(MultiblockGuiLayout.SlotDefinition slot : layout.slots)
			addSlotToContainer(new CTMBSlot(inv, tile.getStorageSystem().flatSlot(slot.storage, slot.slot), slot.x, slot.y, slot.style));
		slotCount = inventorySlots.size();
		if(layout.playerInventory) addPlayerInventory(player, layout.inventoryX, layout.inventoryY);
	}

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
		int tick = syncTicks++;
		if(tile.getWorld().isRemote||tick%5!=0) return;
		NBTTagCompound data = tile.getGuiStorageData();
		// Repeat the signature periodically: the first tile packet may precede the open-screen packet.
		if(data.equals(lastStorage)&&tick%20!=0) return;
		lastStorage = data;
		for(IContainerListener listener : listeners) sendStorage(listener, data);
	}

	private void sendStorage(IContainerListener listener, NBTTagCompound storage)
	{
		if(listener instanceof EntityPlayerMP)
		{
			NBTTagCompound message = new NBTTagCompound();
			message.setTag("ctmbGuiStorage", storage);
			message.setString("ctmbGuiLayout", layout.signature());
			message.setInteger("ctmbWindow", windowId);
			ImmersiveEngineering.packetHandler.sendTo(new MessageTileSync(tile, message), (EntityPlayerMP)listener);
		}
	}
}
