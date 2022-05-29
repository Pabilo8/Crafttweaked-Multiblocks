package pl.pabilo8.ctmb.client;

import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.obj.IEOBJLoader;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.lib.manual.IManualPage;
import blusunrize.lib.manual.ManualInstance.ManualEntry;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.client.resource.VanillaResourceType;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.pabilo8.ctmb.CTMB;
import pl.pabilo8.ctmb.common.CommonProxy;

import javax.annotation.Nullable;
import java.util.function.Predicate;

/**
 * @author Pabilo8
 * @since 29.01.2022
 */
@EventBusSubscriber(modid = CTMB.MODID, value = Side.CLIENT)
public class ClientProxy extends CommonProxy implements ISelectiveResourceReloadListener
{
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

		for(ManualEntry manual : ManualTweaker.PAGES.values())
			for(IManualPage page : manual.getPages())
				if(page instanceof CTMBManualPage)
					((CTMBManualPage)page).setManual();
		ManualHelper.ieManualInstance.manualContents.putAll(ManualTweaker.PAGES);
	}

	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent evt)
	{
		//itemblock models
		for(ItemBlockCTMBMultiblock item : itemblocks)
		{
			final ResourceLocation loc = Block.REGISTRY.getNameForObject(item.getBlock());
			ModelLoader.setCustomMeshDefinition(item, stack -> new ModelResourceLocation(loc, "inventory"));
		}
	}

	// TODO: 29.01.2022 multiblock GUIs
	@Nullable
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		return null;
	}


	@Override
	public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate)
	{
		if(resourcePredicate.test(VanillaResourceType.LANGUAGES))
		{
			ManualTweaker.PAGES.values().forEach(ctmbManualEntry -> ctmbManualEntry.loadTexts(true));
			CTMBLogger.info(I18n.format("ie.manual.entry.melter.name"));
		}
	}
}
