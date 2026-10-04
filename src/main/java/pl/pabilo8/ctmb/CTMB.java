package pl.pabilo8.ctmb;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import pl.pabilo8.ctmb.common.CommonProxy;

import static pl.pabilo8.ctmb.CTMB.MODID;
import static pl.pabilo8.ctmb.CTMB.VERSION;

@SuppressWarnings("unused")
@Mod(
		modid = MODID,
		name = Tags.MOD_NAME,
		version = VERSION,
		dependencies = "required-after:forge@[14.23.5.2847,);required-after:immersiveengineering@[0.12-92,);required-after:immersiveintelligence@[0.3.1,);required-after:crafttweaker@[4.1.20,)",
		acceptedMinecraftVersions = "[1.12.2]"
)
public class CTMB
{
	public static final String MODID = Tags.MOD_ID;
	public static final String VERSION = Tags.VERSION;

	@Instance(MODID)
	public static CTMB INSTANCE;

	@SidedProxy(clientSide = "pl.pabilo8.ctmb.client.ClientProxy", serverSide = "pl.pabilo8.ctmb.common.CommonProxy")
	public static CommonProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		proxy.preInit();
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, proxy);
		proxy.init();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		proxy.postInit();
	}
}
