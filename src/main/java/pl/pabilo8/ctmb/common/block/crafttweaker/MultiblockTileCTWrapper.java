package pl.pabilo8.ctmb.common.block.crafttweaker;

import crafttweaker.annotations.ZenDoc;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.player.IPlayer;
import crafttweaker.api.world.IBlockPos;
import crafttweaker.api.world.IWorld;
import crafttweaker.mc1120.world.MCVector3d;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import pl.pabilo8.ctmb.CTMB;
import pl.pabilo8.ctmb.common.block.Direction;
import pl.pabilo8.ctmb.common.block.TileEntityMultiblock;
import pl.pabilo8.ctmb.common.gui.MultiblockContainer;
import pl.pabilo8.ctmb.common.network.MessageCTMBGuiChange;
import pl.pabilo8.ctmb.common.storage.StorageAccess;
import pl.pabilo8.ctmb.common.util.ICTWrapper;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Pabilo8
 * @since 01.06.2022
 */
@ZenRegister
@ZenClass(value = "mods.ctmb.multiblock.MultiblockTile")
public class MultiblockTileCTWrapper implements ICTWrapper
{
	@Nonnull
	public final TileEntityMultiblock te;
	/**
	 * This represents the custom variables stored in the mb
	 */
	@Nonnull
	NBTTagCompound data = new NBTTagCompound();

	/**
	 * Default constructor, used by a multiblock TE on load
	 */
	public MultiblockTileCTWrapper(@Nonnull TileEntityMultiblock te)
	{
		this.te = te;
	}

	//--- Custom Variables ---//

	@ZenMethod
	@Override
	public boolean hasVar(String name)
	{
		return data.hasKey(name);
	}

	@ZenMethod
	@Nullable
	@Override
	public IData getVar(String name)
	{
		return CraftTweakerMC.getIData(data.getTag(name));
	}

	@ZenMethod
	@Nullable
	@Override
	public IData getVarOr(String name, IData def)
	{
		if(data.hasKey(name))
			return CraftTweakerMC.getIData(data.getTag(name));
		return def;
	}

	@ZenMethod
	@Override
	public void setVar(String name, IData value)
	{
		data.setTag(name, CraftTweakerMC.getNBT(value));
	}

	@Override
	public NBTTagCompound saveData()
	{
		return data;
	}

	@Override
	public void loadData(NBTTagCompound nbt)
	{
		data = nbt;
	}

	@ZenMethod
	@ZenDoc("Forces the TileEntity to send an update message.")
	public void forceUpdate()
	{
		te.forceUpdate();
	}

	@ZenMethod
	public StorageAccess getStorage(String name)
	{
		return te.getStorageSystem().get(name);
	}

	@ZenMethod
	public pl.pabilo8.ctmb.common.production.ProductionAccess getProduction(String name)
	{
		return te.getProductionSystem().get(name);
	}

	@ZenMethod
	public boolean isPOI(String name, int position)
	{
		return te.getMultiblock().definition.isPOI(name, position);
	}

	@ZenMethod
	public Direction getDirection(String name)
	{
		return te.getMultiblock().definition.getDirection(name);
	}

	@ZenMethod
	public String getPortFacing(String name)
	{
		return te.getMultiblock().definition.direction(name, te.facing, te.mirrored).getName();
	}

	//--- Miscellaneous ---//

	@ZenMethod
	public IBlockPos getBlockPos()
	{
		return CraftTweakerMC.getIBlockPos(te.getPos());
	}

	@ZenMethod
	public int getMBPos()
	{
		return te.pos;
	}

	@ZenMethod
	public IWorld getWorld()
	{
		return CraftTweakerMC.getIWorld(te.getWorld());
	}

	//--- Sending Messages ---//

	@ZenMethod
	@ZenDoc("Sends an NBT sync message to the server")
	public void sendMessageToServer(IData message)
	{
		te.sendMessageServer(CraftTweakerMC.getNBTCompound(message));
	}

	@ZenMethod
	@ZenDoc("Sends an NBT sync message to all players in range")
	public void sendMessageToClients(IData message, int range)
	{
		te.sendMessageClients(CraftTweakerMC.getNBTCompound(message), range);
	}

	//--- GUI Opening ---//

	@ZenMethod
	@ZenDoc("Opens a GUI on the server; client callbacks request a transition for the current container.")
	public void openGUI(String guiName, IPlayer player)
	{
		TileEntityMultiblock master = te.master();
		if(master==null||master.getMultiblock().getGuiPage(guiName) < 0) return;
		EntityPlayer nativePlayer = CraftTweakerMC.getPlayer(player);
		if(master.getWorld().isRemote)
		{
			if(nativePlayer.openContainer instanceof MultiblockContainer
					&&((MultiblockContainer)nativePlayer.openContainer).tile==master)
				CTMB.GUI_NETWORK.sendToServer(new MessageCTMBGuiChange(master.getPos(), guiName, nativePlayer.openContainer.windowId));
			return;
		}
		if(nativePlayer.getEntityWorld()!=master.getWorld()) return;
		int page = master.getMultiblock().getGuiPage(guiName);
		master.forceUpdate();
		BlockPos pos = master.getPos();
		nativePlayer.openGui(CTMB.INSTANCE, page, master.getWorld(), pos.getX(), pos.getY(), pos.getZ());
	}

	//--- CT Function Interfaces ---//

	@ZenRegister
	@ZenClass(value = "mods.ctmb.multiblock.IMultiblockFunction")
	public interface IMultiblockFunction
	{
		void execute(MultiblockTileCTWrapper mb);
	}

	@ZenRegister
	@ZenClass(value = "mods.ctmb.multiblock.IMultiblockInteractionFunction")
	public interface IMultiblockInteractionFunction
	{
		boolean execute(MultiblockTileCTWrapper mb, int pos, IPlayer player, boolean hand, MCVector3d hitVec);
	}

	@ZenRegister
	@ZenClass(value = "mods.ctmb.multiblock.IMultiblockTooltipFunction")
	public interface IMultiblockTooltipFunction
	{
		String[] execute(MultiblockTileCTWrapper mb, int pos, IPlayer player, boolean hammer);
	}

	@ZenRegister
	@ZenClass(value = "mods.ctmb.multiblock.IMultiblockMessageInFunction")
	public interface IMultiblockMessageInFunction
	{
		void execute(MultiblockTileCTWrapper mb, IData message, boolean client);
	}

	@ZenRegister
	@ZenClass(value = "mods.ctmb.multiblock.IMultiblockMessageOutFunction")
	public interface IMultiblockMessageOutFunction
	{
		IData execute(MultiblockTileCTWrapper mb, IData message, boolean client);
	}
}
