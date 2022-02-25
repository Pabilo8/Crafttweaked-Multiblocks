package pl.pabilo8.ctmb.client;

import blusunrize.immersiveengineering.client.models.obj.IEOBJLoader;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.pabilo8.ctmb.CTMB;
import pl.pabilo8.ctmb.common.CommonProxy;

import javax.annotation.Nullable;

/**
 * @author Pabilo8
 * @since 29.01.2022
 */
public class ClientProxy extends CommonProxy
{
	@Override
	public void preInit()
	{
		super.preInit();

		OBJLoader.INSTANCE.addDomain(CTMB.MODID);
		IEOBJLoader.instance.addDomain(CTMB.MODID);
	}

	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent evt)
	{

	}

	// TODO: 29.01.2022 multiblock GUIs
	@Nullable
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		return null;
	}


}
