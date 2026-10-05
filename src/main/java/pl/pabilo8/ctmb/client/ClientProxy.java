package pl.pabilo8.ctmb.client;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.obj.IEOBJLoader;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.client.resource.VanillaResourceType;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import pl.pabilo8.ctmb.CTMB;
import pl.pabilo8.ctmb.client.gui.MultiblockGui;
import pl.pabilo8.ctmb.common.CommonProxy;
import pl.pabilo8.ctmb.common.block.ItemBlockCTMBMultiblock;
import pl.pabilo8.ctmb.common.block.TileEntityMultiblock;
import pl.pabilo8.ctmb.common.block.crafttweaker.Multiblock;
import pl.pabilo8.ctmb.common.manual.ManualTweaker;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import pl.pabilo8.ctmb.common.util.DirectoryResourcePack;
import pl.pabilo8.ctmb.client.manual.CTMBManualResourcePack;
import pl.pabilo8.ctmb.client.manual.CTMBIIManualCategory;

/**
 * @author Pabilo8
 * @since 29.01.2022
 */
@EventBusSubscriber(modid = CTMB.MODID, value = Side.CLIENT)
public class ClientProxy extends CommonProxy implements ISelectiveResourceReloadListener
{
	private final List<CTMBIIManualCategory> manualCategories = new ArrayList<>();
	private int manualCheckTicks;

	@SubscribeEvent
	public static void onClientTick(net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent event)
	{
		if(event.phase!=net.minecraftforge.fml.common.gameevent.TickEvent.Phase.END) return;
		ClientProxy proxy = (ClientProxy)CTMB.proxy;
		if(proxy.manualCheckTicks++%20==0) proxy.manualCategories.forEach(CTMBIIManualCategory::ensureRegistered);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void registerResourcePacks(File folder)
	{
		List<IResourcePack> packs = ReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(),
				"defaultResourcePacks", "field_110449_ao", "ap");
		packs.add(new DirectoryResourcePack(folder));
		packs.add(new CTMBManualResourcePack(folder));
		Minecraft.getMinecraft().refreshResources();
	}

	@Override
	public void preInit()
	{
		super.preInit();

		OBJLoader.INSTANCE.addDomain(CTMB.MODID);
		IEOBJLoader.instance.addDomain(CTMB.MODID);
	}

	@Override
	public void init()
	{
		super.init();

		//for handling languages
		((IReloadableResourceManager)ClientUtils.mc().getResourceManager()).registerReloadListener(this);
	}

	@Override
	public void postInit()
	{
		super.postInit();

		for(String name : ManualTweaker.CATEGORIES.keySet())
		{
			CTMBIIManualCategory category = new CTMBIIManualCategory(name);
			category.addPages();
			manualCategories.add(category);
		}
	}

	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent evt)
	{
		//itemblock models
		for(ItemBlockCTMBMultiblock item : ITEMBLOCKS)
		{
			final ResourceLocation loc = Block.REGISTRY.getNameForObject(item.getBlock());
			ModelLoader.setCustomMeshDefinition(item, stack -> new ModelResourceLocation(loc, "inventory"));
		}
	}

	@Nullable
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		//ID is used as a page identifier

		TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
		if(te instanceof TileEntityMultiblock)
		{
			TileEntityMultiblock master = ((TileEntityMultiblock)te).master();
			if(master==null) return null;
			Multiblock mb = master.getMultiblock();
			if(mb.getGuiLayout(ID)!=null)
				return new MultiblockGui(player.inventory, master, ID);
		}

		return null;
	}


	@Override
	public void onResourceManagerReload(@Nonnull IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate)
	{
		if(resourcePredicate.test(VanillaResourceType.LANGUAGES))
		{
			manualCategories.forEach(CTMBIIManualCategory::reload);
		}
	}
}
