package pl.pabilo8.ctmb.common.util;

import crafttweaker.CraftTweakerAPI;

/**
 * @author Pabilo8
 * @since 19.02.2022
 */
public class CTMBLogger
{
	public static void error(Object object)
	{
		CraftTweakerAPI.logError("[CTMB] "+object.toString());
	}

	public static void info(Object object)
	{
		CraftTweakerAPI.logInfo("[CTMB] "+object.toString());
	}

	public static void warn(Object object)
	{
		CraftTweakerAPI.logWarning("[CTMB] "+object.toString());
	}
}
