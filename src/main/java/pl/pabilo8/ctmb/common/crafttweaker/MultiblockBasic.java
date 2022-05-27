package pl.pabilo8.ctmb.common.crafttweaker;

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
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Pabilo8
 * @since 29.01.2022
 */
@ZenClass(value = "mods.ctmb.MultiblockBasic")
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

	private HashMap<Integer, AxisAlignedBB[]> AABBs = new HashMap<>();

	@ZenProperty
	public boolean redstoneControl = false;
	@ZenProperty
	public int energyCapacity = 0;
	@ZenProperty
	public int[] energyPositions = {}, redstonePositions = {};

	//Is set only once
	public final Material material;

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

	@Override
	protected void addBlockEvent(World world, BlockPos pos)
	{

	}

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
}
