package pl.pabilo8.ctmb.common;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import crafttweaker.CraftTweakerAPI;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import pl.pabilo8.ctmb.CTMB;
import pl.pabilo8.ctmb.common.block.BlockCTMBMultiblock;
import pl.pabilo8.ctmb.common.block.ItemBlockCTMBMultiblock;
import pl.pabilo8.ctmb.common.block.TileEntityMultiblock;
import pl.pabilo8.ctmb.common.block.crafttweaker.Multiblock;
import pl.pabilo8.ctmb.common.gui.MultiblockContainer;
import pl.pabilo8.ctmb.common.util.ResourceLoader;

import javax.annotation.Nullable;
import java.util.ArrayList;

/**
 * @author Pabilo8
 * @since 29.01.2022
 */
@EventBusSubscriber(modid = CTMB.MODID)
public class CommonProxy implements IGuiHandler
{
	public static final ArrayList<Multiblock> MULTIBLOCKS = new ArrayList<>();
	public static final ArrayList<BlockCTMBMultiblock> BLOCKS = new ArrayList<>();
	public static final ArrayList<ItemBlockCTMBMultiblock> ITEMBLOCKS = new ArrayList<>();

	public static final ResourceLoader RESOURCE_LOADER = new ResourceLoader();

	public void preInit()
	{
		RESOURCE_LOADER.setup();
		RESOURCE_LOADER.createFolders();
	}

	public void init()
	{
		CommonUtils.registerTile(TileEntityMultiblock.class);

		for(Multiblock mb : MULTIBLOCKS)
			MultiblockHandler.registerMultiblock(mb);

		RESOURCE_LOADER.autoGenerateFiles();
	}

	@SubscribeEvent
	public static void onBlockRegister(RegistryEvent.Register<Block> event)
	{
		CraftTweakerAPI.tweaker.loadScript(false, "ctmb");

		for(BlockCTMBMultiblock block : BLOCKS)
			event.getRegistry().register(block.setRegistryName(block.registryName));

	}

	@SubscribeEvent
	public static void onItemRegister(RegistryEvent.Register<Item> event)
	{
		for(ItemBlockCTMBMultiblock item : ITEMBLOCKS)
			event.getRegistry().register(item.setRegistryName(item.getBlock().registryName));

	}

	public void postInit()
	{
		for(Multiblock mb : MULTIBLOCKS)
			mb.updateStructure();
	}

	@Nullable
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		//ID is used as a page identifier

		TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
		if(te instanceof TileEntityMultiblock)
		{
			Multiblock mb = ((TileEntityMultiblock)te).getMultiblock();
			if((ID==0&&mb.mainGui!=null)||(mb.assignedGuis.size() >= ID))
				return new MultiblockContainer(player.inventory, ((TileEntityMultiblock)te), ID);
		}
		return null;
	}

	@Nullable
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		return null;
	}
}
