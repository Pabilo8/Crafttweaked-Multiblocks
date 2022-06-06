package pl.pabilo8.ctmb.common.crafttweaker;

import crafttweaker.annotations.ZenDoc;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.player.IPlayer;
import crafttweaker.api.world.IBlockPos;
import crafttweaker.api.world.IWorld;
import crafttweaker.mc1120.liquid.MCLiquidStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.items.ItemHandlerHelper;
import pl.pabilo8.ctmb.CTMB;
import pl.pabilo8.ctmb.common.block.TileEntityBasicMultiblock;
import pl.pabilo8.ctmb.common.crafttweaker.storage.MultiblockInventoryInfo;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Pabilo8
 * @since 01.06.2022
 */
@SuppressWarnings("unused")
@ZenRegister
@ZenClass(value = "mods.ctmb.multiblock.MultiblockTile")
public class MultiblockTileCTWrapper implements ICTWrapper
{
	@Nonnull
	private final TileEntityBasicMultiblock te;
	/**
	 * This represents the custom variables stored in the mb
	 */
	@Nonnull
	NBTTagCompound data = new NBTTagCompound();

	/**
	 * Default constructor, used by a multiblock TE on load
	 */
	public MultiblockTileCTWrapper(@Nonnull TileEntityBasicMultiblock te)
	{
		this.te = te;
	}

	//--- Custom Variables ---//

	@ZenMethod
	@Nullable
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

	@ZenMethod
	@Override
	public void setVar(String name, IData value)
	{
		data.setTag(name, CraftTweakerMC.getNBT(value));
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

	//--- Fluid Storage ---//

	@ZenMethod
	@ZenDoc("Returns the fluid stored in tank.")
	@Nullable
	public ILiquidStack getTank(int id)
	{
		return new MCLiquidStack(te.tanks[id].getFluid());
	}

	@ZenMethod
	@ZenDoc("Sets the fluid stored in tank.")
	public void setTank(int id, ILiquidStack to)
	{
		te.tanks[id].setFluid(CraftTweakerMC.getLiquidStack(to));
	}

	@ZenMethod
	@ZenDoc("Extracts fluid from tank, abides to the limits set.")
	public ILiquidStack extractTank(int id, int amount)
	{
		return new MCLiquidStack(te.tanks[id].drain(amount, true));
	}

	@ZenMethod
	@ZenDoc("Fills tank with fluid, abides to the limits set. Returns the remains.")
	public ILiquidStack fillTank(int id, ILiquidStack with)
	{
		return with.withAmount(with.getAmount()-te.tanks[id].fill(CraftTweakerMC.getLiquidStack(with), true));
	}

	//--- Energy Storage ---//

	@ZenMethod
	@ZenDoc("Returns the energy stored.")
	public int getEnergy(int id)
	{
		return te.energy[id].getEnergyStored();
	}

	@ZenMethod
	@ZenDoc("Sets the energy stored.")
	public void setEnergy(int id, int to)
	{
		te.energy[id].setEnergy(to);
	}


	@ZenMethod
	@ZenDoc("Extracts energy, abides to the limits set.")
	public int extractEnergy(int id, int amount)
	{
		return te.energy[id].extractEnergy(amount, false);
	}


	@ZenMethod
	@ZenDoc("Fills with energy, abides to the limits set. Returns the remains.")
	public int fillEnergy(int id, int with)
	{
		return te.energy[id].receiveEnergy(with, false);
	}


	//--- Item Storage ---//

	@ZenMethod
	@ZenDoc("Returns the items stored in an inventory.")
	public IItemStack[] getInventory(int id)
	{
		MultiblockInventoryInfo inv = te.getMultiblock().inventory.get(id);
		IItemStack[] array = new IItemStack[inv.capacity];

		for(int i = inv.getOffset(); i < inv.capacity; i++)
			array[i] = CraftTweakerMC.getIItemStack(te.inventory.get(id));

		return array;
	}

	@ZenMethod
	@ZenDoc("Returns the item stored in inventory.")
	public IItemStack getItem(int id, int slot)
	{
		MultiblockInventoryInfo inv = te.getMultiblock().inventory.get(id);
		return CraftTweakerMC.getIItemStack(te.inventory.get(inv.getOffset()+slot));
	}

	@ZenMethod
	@ZenDoc("Sets the item stored in inventory.")
	public void setItem(int id, int slot, IItemStack to)
	{
		MultiblockInventoryInfo inv = te.getMultiblock().inventory.get(id);
		te.inventory.set(inv.getOffset()+slot, CraftTweakerMC.getItemStack(to));
	}

	@ZenMethod
	@ZenDoc("Extracts item, abides to the limits set.")
	public IItemStack extractItem(int id, int slot)
	{
		MultiblockInventoryInfo inv = te.getMultiblock().inventory.get(id);
		IItemStack stack = CraftTweakerMC.getIItemStack(te.inventory.get(inv.getOffset()+slot));
		te.inventory.set(inv.getOffset()+slot, ItemStack.EMPTY);
		return stack;
	}

	@ZenMethod
	@ZenDoc("Tries to put (merge) an item into the slot, abides to the limits set. Returns the remains that couldn't be put.")
	public IItemStack fillItem(int id, int slot, IItemStack with)
	{
		MultiblockInventoryInfo inv = te.getMultiblock().inventory.get(id);
		ItemStack stack = te.inventory.get(inv.getOffset()+slot).copy();
		ItemStack stacked = CraftTweakerMC.getItemStack(with);

		if(ItemHandlerHelper.canItemStacksStack(stack, stacked))
		{
			int added = MathHelper.clamp(
					stack.getMaxStackSize()-(stack.getCount()+stacked.getCount()),
					0,
					stacked.getCount()
			);

			stack.grow(added);
			te.inventory.set(inv.getOffset()+slot, stack);

			return with.withAmount(with.getAmount()-added);
		}
		return with;
	}

	//--- Redstone Interaction ---//

	// TODO: 01.06.2022 add

	//--- Data Interaction ---//

	// TODO: 01.06.2022 add

	//--- Miscellaneous ---//

	@ZenMethod
	public IBlockPos getBlockPos()
	{
		return CraftTweakerMC.getIBlockPos(te.getPos());
	}

	@ZenMethod
	public int getMBPos()
	{
		return te.pos;
	}

	@ZenMethod
	public IWorld getWorld()
	{
		return CraftTweakerMC.getIWorld(te.getWorld());
	}

	//--- Sending Messages ---//

	@ZenMethod
	@ZenDoc("Sends an NBT sync message to the server")
	public void sendMessageToServer(IData message)
	{
		te.sendMessageServer(CraftTweakerMC.getNBTCompound(message));
	}

	@ZenMethod
	@ZenDoc("Sends an NBT sync message to all players in range")
	public void sendMessageToClients(IData message, int range)
	{
		te.sendMessageClients(CraftTweakerMC.getNBTCompound(message), range);
	}

	//--- GUI Opening ---//

	@ZenMethod
	@ZenDoc("Opens a GUI")
	public void openGUI(String guiName, IPlayer player)
	{
		if(te.getMultiblock().assignedGuis.containsKey(guiName))
		{
			int i = 0;
			for(String s : te.getMultiblock().assignedGuis.keySet())
				if(!s.equals(guiName))
					i++;
				else
					break;

			BlockPos pos = te.getPos();
			CraftTweakerMC.getPlayer(player).openGui(CTMB.INSTANCE, i, te.getWorld(), pos.getX(),
					pos.getY(), pos.getZ());
		}
	}

	//--- CT Function Interfaces ---//

	@ZenRegister
	@ZenClass(value = "mods.ctmb.multiblock.IMultiblockFunction")
	public interface IMultiblockFunction
	{
		void execute(MultiblockTileCTWrapper mb);
	}

	@ZenRegister
	@ZenClass(value = "mods.ctmb.multiblock.IMultiblockMessageInFunction")
	public interface IMultiblockMessageInFunction
	{
		void execute(MultiblockTileCTWrapper mb, IData message, boolean client);
	}

	@ZenRegister
	@ZenClass(value = "mods.ctmb.multiblock.IMultiblockMessageOutFunction")
	public interface IMultiblockMessageOutFunction
	{
		IData execute(MultiblockTileCTWrapper mb, IData message, boolean client);
	}
}
