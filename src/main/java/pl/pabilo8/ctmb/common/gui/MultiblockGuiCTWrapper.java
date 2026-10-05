package pl.pabilo8.ctmb.common.gui;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.nbt.NBTTagCompound;
import pl.pabilo8.ctmb.common.util.ICTWrapper;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.Map;

/**
 * Provides side-neutral script access to an open multiblock GUI.
 *
 * @author Pabilo8 (pabilo@iiteam.net)
 * @since 03.06.2022
 * @updated 05.10.2026
 */
@ZenRegister
@ZenClass("mods.ctmb.gui.MultiblockGui")
public class MultiblockGuiCTWrapper implements ICTWrapper
{
	private final Map<String, DecoComponentAccess> components;
	private NBTTagCompound data = new NBTTagCompound();

	public MultiblockGuiCTWrapper(Map<String, DecoComponentAccess> components)
	{
		this.components = components;
	}

	@ZenMethod
	public DecoComponentAccess getComponent(String name) { return components.get(name); }

	@ZenMethod
	public IData getComponentData(String name)
	{
		DecoComponentAccess component = components.get(name);
		return component==null?CraftTweakerMC.getIData(new NBTTagCompound()): component.getData();
	}

	@ZenMethod
	public void setComponentData(String name, IData value)
	{
		DecoComponentAccess component = components.get(name);
		if(component!=null)
			component.setData(value);
	}

	@ZenMethod
	@Override
	public boolean hasVar(String name) { return data.hasKey(name); }
	@ZenMethod
	@Override
	public IData getVar(String name) { return data.hasKey(name)?CraftTweakerMC.getIData(data.getTag(name)):null; }
	@ZenMethod
	@Override
	public IData getVarOr(String name, IData def) { return hasVar(name)?getVar(name):def; }
	@ZenMethod
	@Override
	public void setVar(String name, IData value) { data.setTag(name, CraftTweakerMC.getNBT(value)); }
	@Override
	public NBTTagCompound saveData() { return data.copy(); }
	@Override
	public void loadData(NBTTagCompound nbt) { data = nbt.copy(); }
}
