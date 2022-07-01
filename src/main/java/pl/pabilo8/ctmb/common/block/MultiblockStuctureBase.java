package pl.pabilo8.ctmb.common.block;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.Template.BlockInfo;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.io.IOUtils;
import pl.pabilo8.ctmb.common.CommonProxy;
import pl.pabilo8.ctmb.common.CommonUtils;
import pl.pabilo8.ctmb.common.util.CTMBLogger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;


/**
 * @author Pabilo8
 * @since 31.05.2021
 */
public abstract class MultiblockStuctureBase<T extends TileEntityMultiblockPart<T>> implements IMultiblock
{
	static final DataFixer FIXER = DataFixesManager.createFixer();
	static final TemplateManager RES_LOC_TEMPLATE_MANAGER = new TemplateManager("Same as in II ^^", FIXER);

	//the resLoc for the .nbt file
	private final ResourceLocation res;
	//the name generated from resLoc
	private final String name;

	//stacks for manual list
	private IngredientStack[] materials = null;
	//stacks for manual block display
	private ItemStack[][][] structure = null;
	//check array for blockstates
	private IngredientStack[][][] checkStructure = null;
	//offset for trigger block
	protected Vec3i offset = Vec3i.NULL_VECTOR;
	//multiblock dimensions
	private Vec3i size = Vec3i.NULL_VECTOR;
	//scale for manual display
	protected float manualScale = 0;

	@SuppressWarnings("unused")
	public MultiblockStuctureBase(ResourceLocation res)
	{
		this(nameFromResLoc(res), res);
	}

	public MultiblockStuctureBase(String name, ResourceLocation res)
	{
		this.name = name;
		this.res = res;
	}

	private static String nameFromResLoc(ResourceLocation loc)
	{
		String[] split = loc.getResourcePath().split("/");
		return loc.getResourceDomain()+":"+CommonUtils.toCamelCase(split[split.length-1], false);
	}

	public void updateStructure()
	{
		//the .nbt file
		Template template = RES_LOC_TEMPLATE_MANAGER.getTemplate(null, res);
		if(template.blocks.isEmpty())
			template = readTemplateFromResources(res);

		if(template==null)
		{
			CTMBLogger.warn("Couldn't initialize multiblock template for "+res);
			return;
		}

		size = template.getSize();

		//sets manual display scale
		this.manualScale = 10f/(Math.max(Math.max(size.getX(), size.getZ()), size.getY())/7f);

		structure = new ItemStack[size.getY()][size.getZ()][size.getX()];
		for(int x = 0; x < size.getX(); x++)
			for(int y = 0; y < size.getY(); y++)
				for(int z = 0; z < size.getZ(); z++)
					structure[y][z][x] = ItemStack.EMPTY;

		checkStructure = new IngredientStack[size.getY()][size.getZ()][size.getX()];
		List<BlockInfo> blocks = template.blocks;
		Set<IngredientStack> matsSet = new HashSet<>();

		for(BlockInfo info : blocks)
		{
			IngredientStack here = getIngredientStackForBlockInfo(info);
			if(!here.getExampleStack().isEmpty())
			{
				structure[info.pos.getY()][info.pos.getZ()][info.pos.getX()] = here.getExampleStack();
				checkStructure[info.pos.getY()][info.pos.getZ()][info.pos.getX()] = here;
				Optional<IngredientStack> match = matsSet.stream().filter(here::equals).findAny();
				if(match.isPresent())
					match.get().inputSize++;
				else
					matsSet.add(here);
			}
		}
		materials = matsSet.toArray(new IngredientStack[0]);
	}

	@Override
	public String getUniqueName()
	{
		return name;
	}

	@Override
	public boolean isBlockTrigger(IBlockState state)
	{
		return checkState(state, checkStructure[offset.getY()][offset.getZ()][offset.getX()], null, null);
	}

	@Override
	public boolean createStructure(World world, BlockPos startPos, EnumFacing side, EntityPlayer player)
	{
		side = side.getOpposite();
		if(side==EnumFacing.UP||side==EnumFacing.DOWN)
		{
			side = EnumFacing.fromAngle(player.rotationYaw);
		}

		boolean mirrored = false;
		boolean b = structureCheck(world, startPos, side, false);
		if(!b)
		{
			mirrored = true;
			b = structureCheck(world, startPos, side, true);
		}
		if(!b)
			return false;

		ItemStack hammer = player.getHeldItemMainhand().getItem().getToolClasses(player.getHeldItemMainhand()).contains(Lib.TOOL_HAMMER)?player.getHeldItemMainhand(): player.getHeldItemOffhand();
		if(MultiblockHandler.fireMultiblockFormationEventPost(player, this, startPos, hammer).isCanceled())
			return false;

		for(int h = -offset.getY(); h < size.getY()-offset.getY(); h++)
			for(int l = -offset.getZ(); l < size.getZ()-offset.getZ(); l++)
				for(int w = -offset.getX(); w < size.getX()-offset.getX(); w++)
				{
					if(structure[h+offset.getY()][l+offset.getZ()][w+offset.getX()].isEmpty())
						continue;

					int ww = mirrored?-w: w;
					BlockPos pos2 = startPos.offset(side, l).offset(side.rotateY(), ww).add(0, h, 0);

					T tile = placeTile(world, pos2);
					if(tile!=null)
					{
						tile.facing = side;
						tile.mirrored = mirrored;
						tile.formed = true;

						tile.pos = (h+offset.getY())*size.getZ()*size.getX()+
								(l+offset.getZ())*size.getX()+
								w+offset.getX()
						;

						//BlockPos oPos = BlockPos.ORIGIN.offset(side, l).offset(side.rotateY(), ww).add(0, h, 0);

						tile.offset = new int[]{(side==EnumFacing.WEST?-l+1: side==EnumFacing.EAST?l-1: side==EnumFacing.NORTH?ww: -ww), h, (side==EnumFacing.NORTH?-l+1: side==EnumFacing.SOUTH?l-1: side==EnumFacing.EAST?ww: -ww)};
						tile.markDirty();
						addBlockEvent(world, pos2);
					}
				}
		return true;
	}

	protected boolean structureCheck(World world, BlockPos startPos, EnumFacing dir, boolean mirror)
	{
		for(int h = -offset.getY(); h < size.getY()-offset.getY(); h++)
			for(int l = -offset.getZ(); l < size.getZ()-offset.getZ(); l++)
				for(int w = -offset.getX(); w < size.getX()-offset.getX(); w++)
				{
					if(structure[h+offset.getY()][l+offset.getZ()][w+offset.getX()].isEmpty())
						continue;

					int ww = mirror?-w: w;
					BlockPos pos = startPos.offset(dir, l).offset(dir.rotateY(), ww).add(0, h, 0);

					if(!checkState(world.getBlockState(pos), checkStructure[h+offset.getY()][l+offset.getZ()][w+offset.getX()], world, pos))
						return false;
				}
		return true;
	}

	protected abstract void addBlockEvent(World world, BlockPos pos);

	@Nullable
	protected abstract T placeTile(World world, BlockPos pos);

	@Override
	public ItemStack[][][] getStructureManual()
	{
		return structure;
	}

	@Override
	public IngredientStack[] getTotalMaterials()
	{
		return materials;
	}

	@Override
	public boolean overwriteBlockRender(ItemStack stack, int iterator)
	{
		if(stack.getItem() instanceof ItemBlockIEBase&&((ItemBlockIEBase)stack.getItem()).getBlock()==IEContent.blockConveyor)
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(0.5, 0.5, 0.5);
			GlStateManager.rotate(90, 0, 1, 0);
			ClientUtils.mc().getRenderItem().renderItem(stack, TransformType.NONE);
			GlStateManager.popMatrix();
			return true;
		}

		return false;
	}

	@Override
	public float getManualScale()
	{
		return manualScale*0.85f;
	}

	@Override
	public boolean canRenderFormedStructure()
	{
		return true;
	}

	static ItemStack renderStack = ItemStack.EMPTY;

	protected abstract Block getBlock();

	@Override
	@SideOnly(Side.CLIENT)
	public void renderFormedStructure()
	{
		if(renderStack.isEmpty())
			renderStack = new ItemStack(getBlock(), 1);

		GlStateManager.pushMatrix();
		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.translate(
				size.getX()/2f-offset.getX(),
				size.getY()/2f-offset.getY(),
				size.getZ()/2f-offset.getZ());
		GlStateManager.rotate(90, 0, 1, 0);
		GlStateManager.translate(-0.5, 0.5, 0);

		GlStateManager.disableCull();
		ClientUtils.mc().getRenderItem().renderItem(renderStack, TransformType.NONE);
		GlStateManager.enableCull();
		RenderHelper.disableStandardItemLighting();

		GlStateManager.popMatrix();
	}

	/**
	 * Checks state using IngredientStack
	 *
	 * @param state blockstate
	 * @param stack to be compared to, uses stack's logic (ore/itemstack)
	 * @param world the multiblock is checked in
	 * @param pos   of the block
	 * @return whether is equal
	 */
	private boolean checkState(IBlockState state, IngredientStack stack, @Nullable World world, @Nullable BlockPos pos)
	{
		if(stack.stack.getItem() instanceof ItemBlockIEBase&&((ItemBlockIEBase)stack.stack.getItem()).getBlock()==IEContent.blockConveyor)
		{
			if(world!=null)
				return ConveyorHandler.isConveyor(world, pos, ItemNBTHelper.getString(stack.stack, "conveyorType"), null);
			else
				return state.getBlock()==IEContent.blockConveyor;
		}

		return stack.matchesItemStackIgnoringSize(new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state)));
	}

	/**
	 * @param info structure file information about the block
	 * @return an item representation (with additional NBT if required (mainly for facing))
	 */
	private IngredientStack getIngredientStackForBlockInfo(BlockInfo info)
	{
		IBlockState state = info.blockState;

		if(state.getBlock()==IEContent.blockConveyor)
		{
			ItemStack conveyorStack = ConveyorHandler.getConveyorStack(info.tileentityData.getString("conveyorBeltSubtype"));
			ItemNBTHelper.setInt(conveyorStack, "conveyorFacing", info.tileentityData.getInteger("facing"));
			return new IngredientStack(conveyorStack).setUseNBT(true);
		}

		int meta = state.getBlock().getMetaFromState(state);
		ItemStack stack = new ItemStack(state.getBlock(), 1, meta);

		try
		{
			int[] oids = OreDictionary.getOreIDs(stack);
			if(oids.length > 0)
				return new IngredientStack(OreDictionary.getOreName(oids[0]));
		} catch(Exception ignored)
		{

		}

		return new IngredientStack(stack);
	}

	/**
	 * @return size in int array, values are swapped to HLW
	 */
	public int[] getSize()
	{
		return new int[]{size.getY(), size.getZ(), size.getX()};
	}

	/**
	 * @param h height (y)
	 * @param l length (z)
	 * @param w width (x)
	 * @return resource location in string format if tile is a conveyor or empty string
	 */
	// TODO: 01.07.2022 Immersive Petroleum integration
	public Tuple<ResourceLocation, EnumFacing> getConveyorKey(int h, int l, int w, EnumFacing facing)
	{
		IngredientStack is = checkStructure[h][l][w];
		return getConveyorKey(is.stack, facing);
	}

	public Tuple<ResourceLocation, EnumFacing> getConveyorKey(ItemStack stack, EnumFacing facing)
	{
		ResourceLocation rl = new ResourceLocation(ItemNBTHelper.getString(stack, "conveyorType"));
		EnumFacing sf = EnumFacing.getFront(ItemNBTHelper.getInt(stack, "conveyorFacing"));
		EnumFacing ff = EnumFacing.getHorizontal(sf.getHorizontalIndex()+facing.getHorizontalIndex()); //
		return new Tuple<>(rl, ff);
	}

	/**
	 * Reads a template from the "resources" folder
	 */
	@SuppressWarnings("deprecation")
	private Template readTemplateFromResources(ResourceLocation res)
	{
		InputStream stream = null;
		try
		{
			//file input stream from resources/
			File file1 = new File(CommonProxy.RESOURCE_LOADER.getResourceFolder(), res.getResourceDomain()+"/structures/"+res.getResourcePath()+".nbt");
			stream = Files.newInputStream(file1.toPath());
			NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(stream);

			//version shenanigans, it's in vanilla so I left it
			if(!nbttagcompound.hasKey("DataVersion", 99))
				nbttagcompound.setInteger("DataVersion", 500);

			Template template = new Template();

			//add to template manager
			template.read(FIXER.process(FixTypes.STRUCTURE, nbttagcompound));
			Map<String, Template> list = ReflectionHelper.getPrivateValue(TemplateManager.class, RES_LOC_TEMPLATE_MANAGER, "templates");
			list.remove(res.toString());
			list.put(res.toString(), template);

			//ah, yes, is  t e m p l a t e
			return template;
		} catch(Throwable ignored)
		{
		} finally
		{
			//yuo shall not use stream endlessly
			IOUtils.closeQuietly(stream);
		}

		return null;
	}
}
