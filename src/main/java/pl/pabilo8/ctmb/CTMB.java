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
		name = "Crafttweaked Multiblocks",
		version = VERSION,
		dependencies = "required-after:forge@[14.23.5.2820,);required-after:crafttweaker@[4.1.8,);required:crafttweaker@[4.1.8,);required-after:immersiveengineering@[0.12,);after:immersiveengineering@[0.12,)"
)
public class CTMB
{
	public static final String MODID = "ctmb";
	public static final String VERSION = "@VERSION@";

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
