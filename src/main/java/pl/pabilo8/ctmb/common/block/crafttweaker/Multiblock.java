package pl.pabilo8.ctmb.common.block.crafttweaker;

import crafttweaker.annotations.ZenDoc;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.block.IMaterial;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.world.IBlockPos;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import pl.pabilo8.ctmb.common.CommonProxy;
import pl.pabilo8.ctmb.common.block.BlockCTMBMultiblock;
import pl.pabilo8.ctmb.common.block.MultiblockStuctureBase;
import pl.pabilo8.ctmb.common.block.TileEntityMultiblock;
import pl.pabilo8.ctmb.common.block.crafttweaker.MultiblockTileCTWrapper.*;
import pl.pabilo8.ctmb.common.block.crafttweaker.storage.MultiblockEnergyInfo;
import pl.pabilo8.ctmb.common.block.crafttweaker.storage.MultiblockFluidTankInfo;
import pl.pabilo8.ctmb.common.block.crafttweaker.storage.MultiblockInventoryInfo;
import pl.pabilo8.ctmb.common.gui.MultiblockGuiLayout;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

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
@ZenClass(value = "mods.ctmb.multiblock.Multiblock")
@ZenRegister
public class Multiblock extends MultiblockStuctureBase<TileEntityMultiblock>
{
	public static final Multiblock DEFAULT_MULTIBLOCK = new Multiblock("", new ResourceLocation("missingno"), Material.AIR);
	private static final AxisAlignedBB[] AABB_CUBE = new AxisAlignedBB[]{new AxisAlignedBB(0, 0, 0, 1, 1, 1)};

	/**
	 * The block bound to this multiblock
	 */
	@Nonnull
	private final BlockCTMBMultiblock block;

	private final HashMap<Integer, AxisAlignedBB[]> AABBs = new HashMap<>();

	public final ArrayList<MultiblockFluidTankInfo> tanks = new ArrayList<>();
	public final ArrayList<MultiblockInventoryInfo> inventory = new ArrayList<>();
	public final ArrayList<MultiblockEnergyInfo> energy = new ArrayList<>();

	public int[] redstonePositions = {}, dataPositions = {};

	public IMultiblockFunction onUpdate = null;
	public IMultiblockMessageOutFunction onSendMessage = null;
	public IMultiblockMessageInFunction onReceiveMessage = null;
	public IMultiblockInteractionFunction onInteract = null;
	public IMultiblockTooltipFunction onTooltip = null;
	public boolean tooltipNixieFont = false;

	//Is set only once
	public final Material material;

	public MultiblockGuiLayout mainGui;
	public final LinkedHashMap<String, MultiblockGuiLayout> assignedGuis = new LinkedHashMap<>();

	public Multiblock(String name, ResourceLocation res, Material material)
	{
		super(name, res);
		this.material = material;
		this.block = new BlockCTMBMultiblock(this);
	}

	//--- Init Method ---//

	@ZenMethod
	public static Multiblock create(String name, String res, IMaterial material)
	{
		Material mat = CraftTweakerMC.getMaterial(material);
		Multiblock mb = new Multiblock(name, new ResourceLocation(res), mat);
		CommonProxy.MULTIBLOCKS.add(mb);
		CommonProxy.BLOCKS.add(mb.getBlock());

		return mb;
	}

	//--- Crafttweaker Methods ---//
	@ZenMethod
	@ZenDoc("Sets offset of the main multiblock tile used to form it with a hammer")
	public void setOffset(int x, int y, int z)
	{
		this.offset = new Vec3i(x, y, z);
	}

	@ZenMethod
	@ZenDoc("Sets offset of the main multiblock tile used to form it with a hammer")
	public void setOffset(IBlockPos pos)
	{
		setOffset(pos.getX(), pos.getY(), pos.getZ());
	}

	@ZenMethod
	@ZenDoc("Sets display scale of the main multiblock inside the manual preview")
	public void setManualScale(float manualScale)
	{
		this.manualScale = manualScale;
	}

	@ZenMethod
	@ZenDoc("Sets hardness and blast resistance of the multiblock block")
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
		world.addBlockEvent(pos, getBlock(), 255, 0);
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

	@ZenMethod
	@ZenDoc("Sets the function called when an NBT message is received.")
	public void setOnInteract(IMultiblockInteractionFunction function)
	{
		this.onInteract = function;
	}

	@ZenMethod
	@ZenDoc("Sets the function called when an NBT message is received.")
	public void setOnTooltip(IMultiblockTooltipFunction function, @Optional boolean tooltipNixieFont)
	{
		this.onTooltip = function;
		this.tooltipNixieFont = tooltipNixieFont;
	}

	//--- Block Handling (non-CT) ---//

	@Nullable
	@Override
	protected TileEntityMultiblock placeTile(World world, BlockPos pos)
	{
		world.setBlockState(pos, block.getDefaultState());
		return (TileEntityMultiblock)world.getTileEntity(pos);
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
	@ZenDoc("Creates a fluid storage of given capacity. It can be accessed later using the ID.")
	public MultiblockFluidTankInfo setTank(int id, int capacity)
	{
		MultiblockFluidTankInfo info = new MultiblockFluidTankInfo(id, capacity);
		tanks.add(info);
		return info;
	}

	@ZenMethod
	@ZenDoc("Creates an energy storage of given capacity. It can be accessed later using the ID.")
	public MultiblockEnergyInfo setEnergyStorage(int id, int capacity)
	{
		MultiblockEnergyInfo info = new MultiblockEnergyInfo(id, capacity);
		energy.add(info);
		return info;
	}

	@ZenMethod
	@ZenDoc("Creates an item inventory of given capacity. It can be accessed later using the ID.")
	public MultiblockInventoryInfo setInventory(int id, int capacity)
	{
		int offset = inventory.stream().mapToInt(MultiblockInventoryInfo::getOffset).sum();

		MultiblockInventoryInfo info = new MultiblockInventoryInfo(id, capacity, offset);
		inventory.add(info);
		return info;
	}

	@ZenMethod
	@ZenDoc("Sets the input or output port for redstone signal.")
	public void setRedstonePort(int id, int[] pos, boolean input)
	{

	}

	@ZenMethod
	@ZenDoc("Sets the input or output port for Immersive Intelligence's Data.")
	public void setDataPort(int id, int[] pos, boolean input)
	{

	}
}
