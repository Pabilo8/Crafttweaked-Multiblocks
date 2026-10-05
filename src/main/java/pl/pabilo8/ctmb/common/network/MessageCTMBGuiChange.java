package pl.pabilo8.ctmb.common.network;

import crafttweaker.api.minecraft.CraftTweakerMC;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.pabilo8.ctmb.common.gui.MultiblockContainer;
import pl.pabilo8.immersiveintelligence.common.network.IIMessage;

/** Requests a named CTMB GUI using the packet sender's current container. */
public class MessageCTMBGuiChange extends IIMessage
{
	private BlockPos pos;
	private String guiName;
	private int windowId;

	public MessageCTMBGuiChange() {}

	public MessageCTMBGuiChange(BlockPos pos, String guiName, int windowId)
	{
		this.pos = pos;
		this.guiName = guiName;
		this.windowId = windowId;
	}

	@Override
	protected void onServerReceive(WorldServer world, NetHandlerPlayServer handler)
	{
		if(!(handler.player.openContainer instanceof MultiblockContainer)) return;
		MultiblockContainer container = (MultiblockContainer)handler.player.openContainer;
		if(container.windowId!=windowId||container.tile.getWorld()!=world
				||!container.tile.getPos().equals(pos)||!container.canInteractWith(handler.player)) return;
		container.tile.getMbWrapper().openGUI(guiName, CraftTweakerMC.getIPlayer(handler.player));
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected void onClientReceive(WorldClient world, NetHandlerPlayClient handler) {}

	@Override
	public void toBytes(ByteBuf buf)
	{
		writePos(buf, pos);
		writeString(buf, guiName);
		buf.writeInt(windowId);
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		pos = readPos(buf);
		guiName = readString(buf);
		windowId = buf.readInt();
	}
}
