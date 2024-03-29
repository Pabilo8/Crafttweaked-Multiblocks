package pl.pabilo8.ctmb.common.gui;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.DataMap;
import crafttweaker.api.data.IData;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.nbt.NBTTagCompound;
import pl.pabilo8.ctmb.client.gui.MultiblockGui;
import pl.pabilo8.ctmb.client.gui.elements.IGuiTweakable;
import pl.pabilo8.ctmb.common.util.GuiNBTData;
import pl.pabilo8.ctmb.common.util.ICTWrapper;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;

/**
 * @author Pabilo8
 * @since 03.06.2022
 */
@ZenRegister
@ZenClass(value = "mods.ctmb.gui.MultiblockGui")
public class MultiblockGuiCTWrapper implements ICTWrapper
{
	private final MultiblockGui gui;

	@Nonnull
	private NBTTagCompound data = new NBTTagCompound();

	/**
	 * Default constructor, used by a multiblock TE on load
	 */
	public MultiblockGuiCTWrapper(@Nonnull MultiblockGui gui)
	{
		this.gui = gui;
	}

	@ZenMethod
	@Override
	public boolean hasVar(String name)
	{
		return data.hasKey(name);
	}

	@ZenMethod
	@Nullable
	@Override
	public IData getVar(String name)
	{
		return CraftTweakerMC.getIData(data.getTag(name));
	}

	@Nullable
	@Override
	public IData getVarOr(String name, IData def)
	{
		if(data.hasKey(name))
			return CraftTweakerMC.getIData(data.getTag(name));
		return def;
	}

	@ZenMethod
	@Override
	public void setVar(String name, IData value)
	{
		data.setTag(name, CraftTweakerMC.getNBT(value));
	}

	@ZenMethod
	public DataMap getComponentData(String name)
	{
		IGuiTweakable comp = gui.ctComponents.getOrDefault(name, null);
		if(comp!=null)
			return comp.getData();
		return new DataMap(new HashMap<>(), true);
	}

	@ZenMethod
	public void setComponentData(String name, IData value)
	{
		IGuiTweakable comp = gui.ctComponents.getOrDefault(name, null);
		GuiNBTData map = new GuiNBTData(value);

		if(comp!=null&&map.isValid())
			comp.setData(map);
	}

	@Override
	public NBTTagCompound saveData()
	{
		return data;
	}

	@Override
	public void loadData(NBTTagCompound nbt)
	{
		data = nbt;
	}
}
