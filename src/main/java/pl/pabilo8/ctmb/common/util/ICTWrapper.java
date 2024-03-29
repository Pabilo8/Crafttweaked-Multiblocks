package pl.pabilo8.ctmb.common.util;

import crafttweaker.api.data.IData;
import net.minecraft.nbt.NBTTagCompound;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nullable;

/**
 * A Crafttweaker wrapper around a minecraft class
 * All wrappers should support setting and getting temporary variables
 *
 * @author Pabilo8
 * @since 03.06.2022
 */
public interface ICTWrapper
{
	@ZenMethod
	boolean hasVar(String name);

	@ZenMethod
	@Nullable
	IData getVar(String name);

	@ZenMethod
	@Nullable
	IData getVarOr(String name, IData def);

	@ZenMethod
	void setVar(String name, IData value);

	NBTTagCompound saveData();

	void loadData(NBTTagCompound nbt);
}
