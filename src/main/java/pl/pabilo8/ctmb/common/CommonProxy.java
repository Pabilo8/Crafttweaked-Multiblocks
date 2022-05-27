package pl.pabilo8.ctmb.common;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import crafttweaker.CraftTweakerAPI;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import pl.pabilo8.ctmb.CTMB;
import pl.pabilo8.ctmb.common.block.BlockCTMBMultiblock;
import pl.pabilo8.ctmb.common.block.ItemBlockCTMBMultiblock;
import pl.pabilo8.ctmb.common.block.TileEntityBasicMultiblock;
import pl.pabilo8.ctmb.common.crafttweaker.MultiblockBasic;
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
	public static ArrayList<MultiblockBasic> multiblocks = new ArrayList<>();
	public static ArrayList<BlockCTMBMultiblock> blocks = new ArrayList<>();
	public static ArrayList<ItemBlockCTMBMultiblock> itemblocks = new ArrayList<>();

	public static ResourceLoader resourceLoader = new ResourceLoader();

	public void preInit()
	{
		try
		{
			resourceLoader.setup();
			resourceLoader.createFolders();
		}
		catch(NoSuchFieldException|IllegalAccessException ignored)
		{

		}
	}

	public void init()
	{
		CommonUtils.registerTile(TileEntityBasicMultiblock.class);

		for(MultiblockBasic mb : multiblocks)
			MultiblockHandler.registerMultiblock(mb);

		resourceLoader.autoGenerateFiles();
	}

	@SubscribeEvent
	public static void onBlockRegister(RegistryEvent.Register<Block> event)
	{
		CraftTweakerAPI.tweaker.loadScript(false, "ctmb");

		for(BlockCTMBMultiblock block : blocks)
			event.getRegistry().register(block.setRegistryName(block.registryName));

	}

	@SubscribeEvent
	public static void onItemRegister(RegistryEvent.Register<Item> event)
	{
		for(ItemBlockCTMBMultiblock item : itemblocks)
			event.getRegistry().register(item.setRegistryName(item.getBlock().registryName));

	}

	public void postInit()
	{
		for(MultiblockBasic mb : multiblocks)
			mb.updateStructure();
	}

	// TODO: 29.01.2022 multiblock GUIs
	@Nullable
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		return null;
	}

	@Nullable
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		return null;
	}
}
