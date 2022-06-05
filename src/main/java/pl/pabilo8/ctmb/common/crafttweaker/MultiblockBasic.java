package pl.pabilo8.ctmb.common.crafttweaker;

import com.google.common.collect.HashMultimap;
import crafttweaker.annotations.ZenDoc;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.world.IBlockPos;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import pl.pabilo8.ctmb.common.block.BlockCTMBMultiblock;
import pl.pabilo8.ctmb.common.block.MultiblockStuctureBase;
import pl.pabilo8.ctmb.common.block.TileEntityBasicMultiblock;
import pl.pabilo8.ctmb.common.crafttweaker.MultiblockTileCTWrapper.IMultiblockFunction;
import pl.pabilo8.ctmb.common.crafttweaker.MultiblockTileCTWrapper.IMultiblockMessageInFunction;
import pl.pabilo8.ctmb.common.crafttweaker.MultiblockTileCTWrapper.IMultiblockMessageOutFunction;
import pl.pabilo8.ctmb.common.crafttweaker.gui.MultiblockGuiLayout;
import pl.pabilo8.ctmb.common.crafttweaker.storage.MultiblockEnergyInfo;
import pl.pabilo8.ctmb.common.crafttweaker.storage.MultiblockFluidTankInfo;
import pl.pabilo8.ctmb.common.crafttweaker.storage.MultiblockInventoryInfo;
import pl.pabilo8.ctmb.common.crafttweaker.storage.MultiblockStorageInfo;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * @author Pabilo8
 * @since 29.01.2022
 */
@ZenClass(value = "mods.ctmb.multiblock.MultiblockBasic")
@ZenRegister
@SuppressWarnings("unused")
public class MultiblockBasic extends MultiblockStuctureBase<TileEntityBasicMultiblock>
{
	public static final MultiblockBasic DEFAULT_MULTIBLOCK = new MultiblockBasic("", new ResourceLocation("missingno"), Material.AIR);
	private static final AxisAlignedBB[] AABB_CUBE = new AxisAlignedBB[]{new AxisAlignedBB(0, 0, 0, 1, 1, 1)};

	/**
	 * The block bound to this multiblock
	 */
	@Nonnull
	private final BlockCTMBMultiblock block;

	private final HashMap<Integer, AxisAlignedBB[]> AABBs = new HashMap<>();

	private final ArrayList<MultiblockStorageInfo<?>> caps = new ArrayList<>();
	private final HashMultimap<Integer, MultiblockFluidTankInfo> tanks = HashMultimap.create();
	private final HashMultimap<Integer, MultiblockInventoryInfo> inventory = HashMultimap.create();
	private final HashMultimap<Integer, MultiblockEnergyInfo> energy = HashMultimap.create();

	public int[] redstonePositions = {}, dataPositions = {};

	public IMultiblockFunction onUpdate = null;
	public IMultiblockMessageOutFunction onSendMessage = null;
	public IMultiblockMessageInFunction onReceiveMessage = null;

	//Is set only once
	public final Material material;

	public MultiblockGuiLayout mainGui;
	public final LinkedHashMap<String, MultiblockGuiLayout> assignedGuis = new LinkedHashMap<>();

	public MultiblockBasic(String name, ResourceLocation res, Material material)
	{
		super(name, res);
		this.material = material;
		this.block = new BlockCTMBMultiblock(this);
	}

	@ZenMethod
	@ZenDoc("Sets offset of the main multiblock tile used to form it with a hammer")
	public void setOffset(int x, int y, int z)
	{
		this.offset = new Vec3i(x, y, z);
	}

	@ZenMethod
	@ZenDoc("")
	public void setOffset(IBlockPos pos)
	{
		setOffset(pos.getX(), pos.getY(), pos.getZ());
	}

	@ZenMethod
	@ZenDoc("otak")
	public void setManualScale(float manualScale)
	{
		this.manualScale = manualScale;
	}

	@ZenMethod
	@ZenDoc("otak")
	public void setBlockParams(float hardness, float resistance)
	{
		block.setBlockParams(hardness, resistance);
	}

	@ZenMethod
	@ZenDoc("Adds a gui to the multiblock GUI list")
	public void addGui(String name, MultiblockGuiLayout layout)
	{
		this.assignedGuis.put(name, layout);
	}

	@ZenMethod
	@ZenDoc("Sets the GUI displayed on multiblock interaction")
	public void setMainGui(String name)
	{
		this.mainGui = this.assignedGuis.get(name);
	}

	@Override
	protected void addBlockEvent(World world, BlockPos pos)
	{

	}

	@ZenMethod
	@ZenDoc("Sets the function called by MB every tick.")
	public void setOnUpdate(IMultiblockFunction function)
	{
		this.onUpdate = function;
	}

	@ZenMethod
	@ZenDoc("Sets the function called when an NBT message is sent.")
	public void setOnSendMessage(IMultiblockMessageOutFunction function)
	{
		this.onSendMessage = function;
	}

	@ZenMethod
	@ZenDoc("Sets the function called when an NBT message is received.")
	public void setOnReceiveMessage(IMultiblockMessageInFunction function)
	{
		this.onReceiveMessage = function;
	}

	//--- Block Handling (non-CT) ---//

	@Nullable
	@Override
	protected TileEntityBasicMultiblock placeTile(World world, BlockPos pos)
	{
		world.setBlockState(pos, block.getDefaultState());
		return (TileEntityBasicMultiblock)world.getTileEntity(pos);
	}

	public Material getMaterial()
	{
		return material;
	}

	@Nonnull
	@Override
	public BlockCTMBMultiblock getBlock()
	{
		return block;
	}

	/**
	 * @return a flattened version of the name, i.e. IE:Mixer turns into multiblock_ie_mixer
	 */
	public String getFlattenedName()
	{
		return "multiblock_"+getUniqueName().replace(':', '_');
	}

	//--- AABB ---//

	@ZenMethod
	@ZenDoc("Adds an AABB to the multiblock block of given id")
	public void addAABB(int[] pos, double[]... vectors)
	{
		AxisAlignedBB[] array = Arrays.stream(vectors)
				.map(vector -> new AxisAlignedBB(vector[0], vector[1], vector[2], vector[3], vector[4], vector[5]))
				.toArray(AxisAlignedBB[]::new);
		for(int p : pos)
			AABBs.put(p, array);
	}

	public AxisAlignedBB[] getAABB(int pos)
	{
		return AABBs.getOrDefault(pos, AABB_CUBE);
	}

	//--- Storage ---//

	@ZenMethod
	public MultiblockFluidTankInfo setTank(int id, int capacity, int[] pos, boolean input)
	{
		MultiblockFluidTankInfo info = tanks.values().stream()
				.filter(value -> value.id==id)
				.findFirst()
				.orElse(new MultiblockFluidTankInfo(id, capacity, input));

		if(!caps.contains(info))
			caps.add(info);

		for(int p : pos)
			tanks.put(p, info);

		return info;
	}

	@ZenMethod
	public MultiblockEnergyInfo setEnergyStorage(int id, int capacity, int[] pos, boolean input)
	{
		MultiblockEnergyInfo info = energy.values().stream()
				.filter(value -> value.id==id)
				.findFirst()
				.orElse(new MultiblockEnergyInfo(id, capacity, input));

		if(!caps.contains(info))
			caps.add(info);

		for(int p : pos)
			energy.put(p, info);

		return info;
	}

	@ZenMethod
	public MultiblockInventoryInfo setInventory(int id, int capacity, int[] pos, boolean input)
	{
		MultiblockInventoryInfo info = inventory.values().stream()
				.filter(value -> value.id==id)
				.findFirst()
				.orElse(new MultiblockInventoryInfo(id, capacity, input));

		if(!caps.contains(info))
			caps.add(info);

		for(int p : pos)
			inventory.put(p, info);

		return info;
	}

	@ZenMethod
	public void setRedstonePort(int id, int[] pos, boolean input)
	{

	}

	@ZenMethod
	public void setDataPort(int id, int[] pos, boolean input)
	{

	}
}
