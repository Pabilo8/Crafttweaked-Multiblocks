package pl.pabilo8.ctmb.common.crafttweaker;

import blusunrize.immersiveengineering.api.Lib;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.block.IMaterial;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.mc1120.game.MCGame;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;
import pl.pabilo8.ctmb.common.CommonProxy;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nullable;

/**
 * @author Pabilo8
 * @since 29.01.2022
 */
@ZenClass(value = "mods.ctmb.MultiblockFactory")
@ZenRegister
@SuppressWarnings("unused")
public class MultiblockFactory
{
	@ZenMethod
	@Nullable
	public static MultiblockBasic createMultiblock(String name, String res, IMaterial material)
	{
		CraftTweakerAPI.logInfo("Created a multiblock!");
		Material mat = CraftTweakerMC.getMaterial(material);
		MultiblockBasic mb = new MultiblockBasic(name, new ResourceLocation(res), mat);
		CommonProxy.multiblocks.add(mb);
		CommonProxy.blocks.add(mb.getBlock());

		return mb;
	}

	/**
	 * The lazy way
	 */
	@ZenMethod
	public static void setLocale(MultiblockBasic mb, String locale)
	{
		MCGame.INSTANCE.setLocalization(Lib.DESC_INFO+"multiblock."+mb.getUniqueName(), locale);
	}
}
