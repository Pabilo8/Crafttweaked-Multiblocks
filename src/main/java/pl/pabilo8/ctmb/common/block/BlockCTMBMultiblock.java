package pl.pabilo8.ctmb.common.block;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.DimensionBlockPos;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.model.obj.OBJModel.OBJState;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.Properties;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import pl.pabilo8.ctmb.common.crafttweaker.MultiblockBasic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * @author Pabilo8
 * @since 16.02.2022
 */
@EventBusSubscriber
@SuppressWarnings("deprecation")
public class BlockCTMBMultiblock extends Block
{
	//block properties
	public final IProperty<?>[] additionalProperties = new IProperty[]{IEProperties.FACING_HORIZONTAL, IEProperties.MULTIBLOCKSLAVE, IEProperties.BOOLEANS[0]};

	//temporary properties, for blockstate creation
	protected static IProperty<?>[] tempProperties;
	protected static IUnlistedProperty<?>[] tempUnlistedProperties;

	public final String registryName;
	public final MultiblockBasic multiblock;

	private static final Map<DimensionBlockPos, TileEntity> TEMP_TILES = new HashMap<>();

	@SubscribeEvent
	public static void onTick(TickEvent.ServerTickEvent ev)
	{
		if(ev.phase==TickEvent.Phase.END)
			TEMP_TILES.clear();
	}

	public BlockCTMBMultiblock(MultiblockBasic mb)
	{
		super(setTempProperties(mb.getMaterial(), new IProperty[]{IEProperties.FACING_HORIZONTAL, IEProperties.MULTIBLOCKSLAVE, IEProperties.BOOLEANS[0]}));
		this.multiblock = mb;
		this.registryName = "ctmb:"+mb.getFlattenedName();

		this.setDefaultState(getInitDefaultState());
		this.setUnlocalizedName(Lib.DESC_INFO+"multiblock."+mb.getUniqueName());
		CommonProxy.itemblocks.add(new ItemBlockCTMBMultiblock(this));

		//this.setCreativeTab(null);
		//this.adjustSound();

		setHardness(2f);
		setResistance(13f);

		// TODO: 16.02.2022 check
		lightOpacity = 255;
	}

	public void setBlockParams(float hardness, float resistance)
	{
		setHardness(hardness);
		setResistance(resistance);
	}

	//Tile Entity / BlockState Creation & Handling

	@Override
	public int getMetaFromState(IBlockState state)
	{
		return 0;
	}

	protected static Material setTempProperties(Material material, Object... additionalProperties)
	{
		ArrayList<IProperty<?>> propList = new ArrayList<>();
		ArrayList<IUnlistedProperty<?>> unlistedPropList = new ArrayList<>();

		for(Object o : additionalProperties)
		{
			if(o instanceof IProperty)
				propList.add((IProperty<?>)o);
			if(o instanceof IProperty[])
				Collections.addAll(propList, ((IProperty<?>[])o));
			if(o instanceof IUnlistedProperty)
				unlistedPropList.add((IUnlistedProperty<?>)o);
			if(o instanceof IUnlistedProperty[])
				Collections.addAll(unlistedPropList, ((IUnlistedProperty<?>[])o));
		}
		tempProperties = propList.toArray(new IProperty[0]);
		tempUnlistedProperties = unlistedPropList.toArray(new IUnlistedProperty[0]);


		return material;
	}

	@Override
	@Nonnull
	protected BlockStateContainer createBlockState()
	{
		if(tempUnlistedProperties.length > 0)
			return new ExtendedBlockState(this, tempProperties, tempUnlistedProperties);
		return new BlockStateContainer(this, tempProperties);
	}

	protected IBlockState getInitDefaultState()
	{
		IBlockState state = this.blockState.getBaseState();
		for(IProperty<?> property : this.additionalProperties)
			if(property!=null&&!property.getAllowedValues().isEmpty())
				state = applyProperty(state, property, property.getAllowedValues().iterator().next());

		if(state.getPropertyKeys().contains(IEProperties.FACING_HORIZONTAL))
			state = state.withProperty(IEProperties.FACING_HORIZONTAL, EnumFacing.NORTH);

		return state;
	}

	protected <V extends Comparable<V>> IBlockState applyProperty(IBlockState in, IProperty<V> prop, Object val)
	{
		return in.withProperty(prop, (V)val);
	}

	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		return true;
	}

	@Override
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items)
	{

	}

	//Block breaking behaviour

	@Nullable
	@Override
	public TileEntity createTileEntity(World world, IBlockState state)
	{
		TileEntityBasicMultiblock basic = new TileEntityBasicMultiblock(this.multiblock);

		//facing
		EnumFacing newFacing = null;
		if(state.getPropertyKeys().contains(IEProperties.FACING_HORIZONTAL))
		{
			newFacing = state.getValue(IEProperties.FACING_HORIZONTAL);
			if(newFacing.getAxis()==Axis.Y)
				newFacing = null;
		}
		if(newFacing!=null)
			basic.setFacing(newFacing);

		return basic;
	}

	//Block breaking behaviour

	public ItemStack getOriginalBlock(World world, BlockPos pos)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityMultiblockPart)
			return ((TileEntityMultiblockPart<?>)te).getOriginalBlock();
		return ItemStack.EMPTY;
	}

	// TODO: 20.02.2022 custom drops
	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
	{
		TileEntity tile = world.getTileEntity(pos);
		DimensionBlockPos dpos = new DimensionBlockPos(pos, world instanceof World?((World)world).provider.getDimension(): 0);
		if(tile==null&&TEMP_TILES.containsKey(dpos))
			tile = TEMP_TILES.get(dpos);
		if(tile!=null&&(!(tile instanceof ITileDrop)||!((ITileDrop)tile).preventInventoryDrop()))
		{
			if(tile instanceof IIEInventory&&((IIEInventory)tile).getDroppedItems()!=null)
			{
				for(ItemStack s : ((IIEInventory)tile).getDroppedItems())
					if(!s.isEmpty())
						drops.add(s);
			}
			else if(tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
			{
				IItemHandler h = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
				if(h instanceof IEInventoryHandler)
					for(int i = 0; i < h.getSlots(); i++)
						if(!h.getStackInSlot(i).isEmpty())
						{
							drops.add(h.getStackInSlot(i));
							((IEInventoryHandler)h).setStackInSlot(i, ItemStack.EMPTY);
						}
			}
		}
		if(tile instanceof ITileDrop)
		{
			NonNullList<ItemStack> s = ((ITileDrop)tile).getTileDrops(harvesters.get(), state);
			drops.addAll(s);
		}
		else
			super.getDrops(drops, world, pos, state, fortune);

		TEMP_TILES.remove(dpos);
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityBasicMultiblock)
		{
			TileEntityBasicMultiblock tile = (TileEntityBasicMultiblock)te;

			if(world.getGameRules().getBoolean("doTileDrops"))
			{
				if(!tile.formed&&tile.pos==-1&&!tile.getOriginalBlock().isEmpty())
					world.spawnEntity(new EntityItem(world, pos.getX()+.5, pos.getY()+.5, pos.getZ()+.5, tile.getOriginalBlock().copy()));

				if(tile.formed)
				{
					IIEInventory master = (IIEInventory)tile.master();
					if(master!=null&&(!(master instanceof ITileDrop)||!((ITileDrop)master).preventInventoryDrop())&&master.getDroppedItems()!=null)
						for(ItemStack s : master.getDroppedItems())
							if(!s.isEmpty())
								world.spawnEntity(new EntityItem(world, pos.getX()+.5, pos.getY()+.5, pos.getZ()+.5, s.copy()));
				}
			}
			tile.disassemble();

		}

		TEMP_TILES.put(new DimensionBlockPos(pos, world.provider.getDimension()), te);
		super.breakBlock(world, pos, state);
		world.removeTileEntity(pos);

		super.breakBlock(world, pos, state);
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
	{
		ItemStack stack = getOriginalBlock(world, pos);
		return stack.isEmpty()?super.getPickBlock(state, target, world, pos, player): stack;
	}

	//Update Stuff

	/**
	 * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
	 * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
	 * block, etc.
	 */
	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos)
	{
		if(!world.isRemote)
		{
			//Necessary to prevent ghostloading, see conversation in #immersive-engineering on Discord on 12/13 Mar 2019
			Chunk posChunk = world.getChunkFromBlockCoords(pos);
			ApiUtils.addFutureServerTask(world, () ->
			{
				if(world.isBlockLoaded(pos)&&!posChunk.unloadQueued)
				{
					TileEntity tile = world.getTileEntity(pos);
					if(tile instanceof INeighbourChangeTile&&!tile.getWorld().isRemote)
						((INeighbourChangeTile)tile).onNeighborBlockChange(fromPos);
				}
			});
		}
	}


	/**
	 * Called on server when World#addBlockEvent is called. If server returns true, then also called on the client. On the
	 * Server, this may perform additional changes to the world, like pistons replacing the block with an extended base. On
	 * the client, the update may involve replacing tile entities or effects such as sounds or particles
	 */
	@Override
	public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int eventID, int eventParam)
	{
		super.eventReceived(state, worldIn, pos, eventID, eventParam);
		TileEntity tileentity = worldIn.getTileEntity(pos);
		return tileentity!=null&&tileentity.receiveClientEvent(eventID, eventParam);
	}

	protected EnumFacing getDefaultFacing()
	{
		return EnumFacing.NORTH;
	}

	/**
	 * Get the actual Block state of this Block at the given position. This applies properties not visible in the metadata,
	 * such as fence connections.
	 */
	@Override
	@Nonnull
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		// TODO: 20.02.2022 investigate
		state = super.getActualState(state, world, pos);
		TileEntity tile = world.getTileEntity(pos);

		if(tile instanceof IAttachedIntegerProperies)
		{
			for(String s : ((IAttachedIntegerProperies)tile).getIntPropertyNames())
				state = applyProperty(state, ((IAttachedIntegerProperies)tile).getIntProperty(s), ((IAttachedIntegerProperies)tile).getIntPropertyValue(s));
		}

		if(tile instanceof IDirectionalTile&&(state.getPropertyKeys().contains(IEProperties.FACING_ALL)||state.getPropertyKeys().contains(IEProperties.FACING_HORIZONTAL)))
		{
			PropertyDirection prop = state.getPropertyKeys().contains(IEProperties.FACING_HORIZONTAL)?IEProperties.FACING_HORIZONTAL: IEProperties.FACING_ALL;
			state = applyProperty(state, prop, ((IDirectionalTile)tile).getFacing());
		}
		if(tile instanceof IActiveState)
		{
			IProperty<Boolean> boolProp = ((IActiveState)tile).getBoolProperty(IActiveState.class);
			if(state.getPropertyKeys().contains(boolProp))
				state = applyProperty(state, boolProp, ((IActiveState)tile).getIsActive());
		}

		if(tile instanceof TileEntityMultiblockPart)
			state = applyProperty(state, IEProperties.MULTIBLOCKSLAVE, ((TileEntityMultiblockPart<?>)tile).isDummy());
		else if(tile instanceof IHasDummyBlocks)
			state = applyProperty(state, IEProperties.MULTIBLOCKSLAVE, ((IHasDummyBlocks)tile).isDummy());

		if(tile instanceof IMirrorAble)
			state = applyProperty(state, ((IMirrorAble)tile).getBoolProperty(IMirrorAble.class), ((IMirrorAble)tile).getIsMirrored());

		return state;
	}

	@Override
	public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis)
	{
		TileEntity tile = world.getTileEntity(pos);
		if(tile instanceof IDirectionalTile)
		{
			if(!((IDirectionalTile)tile).canRotate(axis))
				return false;
			IBlockState state = world.getBlockState(pos);
			if(state.getPropertyKeys().contains(IEProperties.FACING_ALL)||state.getPropertyKeys().contains(IEProperties.FACING_HORIZONTAL))
			{
				PropertyDirection prop = state.getPropertyKeys().contains(IEProperties.FACING_HORIZONTAL)?IEProperties.FACING_HORIZONTAL: IEProperties.FACING_ALL;
				EnumFacing f = ((IDirectionalTile)tile).getFacing();

				f = axis.getAxisDirection()==AxisDirection.POSITIVE?f.rotateY(): f.rotateYCCW();

				if(f!=((IDirectionalTile)tile).getFacing())
				{
					EnumFacing old = ((IDirectionalTile)tile).getFacing();
					((IDirectionalTile)tile).setFacing(f);
					((IDirectionalTile)tile).afterRotation(old, f);
					state = applyProperty(state, prop, ((IDirectionalTile)tile).getFacing());
					world.setBlockState(pos, state.cycleProperty(prop));
				}
			}
		}
		return false;
	}

	@Override
	@Nonnull
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		state = super.getExtendedState(state, world, pos);
		if(state instanceof IExtendedBlockState)
		{
			IExtendedBlockState extended = (IExtendedBlockState)state;
			TileEntity te = world.getTileEntity(pos);
			if(te!=null)
			{
				if(te instanceof IAdvancedHasObjProperty)
					extended = extended.withProperty(Properties.AnimationProperty, ((IAdvancedHasObjProperty)te).getOBJState());
				else if(te instanceof IHasObjProperty)
					extended = extended.withProperty(Properties.AnimationProperty, new OBJState(((IHasObjProperty)te).compileDisplayList(), true));

				if(te instanceof IDynamicTexture)
					extended = extended.withProperty(IEProperties.OBJ_TEXTURE_REMAP, ((IDynamicTexture)te).getTextureReplacements());
				if(te instanceof IOBJModelCallback)
					extended = extended.withProperty(IOBJModelCallback.PROPERTY, (IOBJModelCallback<TileEntity>)te);

				if(te.hasCapability(CapabilityShader.SHADER_CAPABILITY, null))
					extended = extended.withProperty(CapabilityShader.BLOCKSTATE_PROPERTY, te.getCapability(CapabilityShader.SHADER_CAPABILITY, null));
			}
			state = extended;
		}

		return state;
	}

	public void onIEBlockPlacedBy(World world, BlockPos pos, IBlockState state, EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase placer, ItemStack stack)
	{
		TileEntity tile = world.getTileEntity(pos);

		if(tile instanceof TileEntityBasicMultiblock)
		{
			EnumFacing f = ((IDirectionalTile)tile).getFacingForPlacement(placer, pos, side, hitX, hitY, hitZ);
			((IDirectionalTile)tile).setFacing(f);
		}
	}

	/**
	 * Called when the block is right clicked by a player.
	 */
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		ItemStack heldItem = player.getHeldItem(hand);
		TileEntity te = world.getTileEntity(pos);

		if(te instanceof TileEntityBasicMultiblock)
		{
			TileEntityBasicMultiblock mb = (TileEntityBasicMultiblock)te;

			//hammer rotation
			if(Utils.isHammer(heldItem)&&mb.canHammerRotate(side, hitX, hitY, hitZ, player)&&!world.isRemote)
			{
				EnumFacing f = mb.getFacing();
				EnumFacing oldF = f;

				f = player.isSneaking()?f.rotateYCCW(): f.rotateY();

				mb.setFacing(f);
				mb.afterRotation(oldF, f);
				te.markDirty();
				world.notifyBlockUpdate(pos, state, state, 3);
				world.addBlockEvent(te.getPos(), te.getBlockType(), 255, 0);
				return true;
			}

			//hammer interaction
			if(Utils.isHammer(heldItem)&&!world.isRemote&&mb.hammerUseSide(side, player, hitX, hitY, hitZ))
				return true;

			//player hand interaction
			if(mb.interact(side, player, hand, heldItem, hitX, hitY, hitZ))
				return true;

			//GUI display
			if(hand==EnumHand.MAIN_HAND&&!player.isSneaking())
			{
				TileEntityBasicMultiblock master = mb.master();

				// TODO: 20.02.2022 replace commonproxy
				if(!world.isRemote&&master!=null&&master.canOpenGui(player))
				{
					//CommonProxy.openGuiForTile(player, master);
				}
				return true;
			}
		}

		return false;
	}

	//Collision

	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityBasicMultiblock)
			((TileEntityBasicMultiblock)te).onEntityCollision(world, entity);
	}

	@Override
	@Nullable
	public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof IAdvancedSelectionBounds)
		{
			List<AxisAlignedBB> list = ((IAdvancedSelectionBounds)te).getAdvancedSelectionBounds();
			if(!list.isEmpty())
			{
				RayTraceResult min = null;
				double minDist = Double.POSITIVE_INFINITY;
				for(AxisAlignedBB aabb : list)
				{
					RayTraceResult mop = this.rayTrace(pos, start, end, aabb.offset(-pos.getX(), -pos.getY(), -pos.getZ()));
					if(mop!=null)
					{
						double dist = mop.hitVec.squareDistanceTo(start);
						if(dist < minDist)
						{
							min = mop;
							minDist = dist;
						}
					}
				}
				return min;
			}
		}
		return super.collisionRayTrace(state, world, pos, start, end);
	}

	@Override
	@Nonnull
	public BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess world, @Nonnull IBlockState state, @Nonnull BlockPos pos, @Nullable EnumFacing side)
	{
		if(side!=null)
		{
			TileEntity te = world.getTileEntity(pos);
			if(te instanceof IFaceShape)
				return ((IFaceShape)te).getFaceShape(side);
			else
			{
				AxisAlignedBB bb = getBoundingBox(state, world, pos);
				double wMin = side.getAxis()==Axis.X?bb.minZ: bb.minX;
				double wMax = side.getAxis()==Axis.X?bb.maxZ: bb.maxX;
				double hMin = side.getAxis()==Axis.Y?bb.minZ: bb.minY;
				double hMax = side.getAxis()==Axis.Y?bb.maxZ: bb.maxY;
				if(wMin==0&&hMin==0&&wMax==1&&hMax==1)
					return BlockFaceShape.SOLID;
				else if(hMin==0&&hMax==1&&wMin==(1-wMax))
				{
					if(wMin > .375)
						return BlockFaceShape.MIDDLE_POLE_THIN;
					else if(wMin > .3125)
						return BlockFaceShape.MIDDLE_POLE;
					else
						return BlockFaceShape.MIDDLE_POLE_THICK;
				}
				else if(hMin==wMin&&hMax==wMax)
				{
					if(wMin > .375)
						return BlockFaceShape.CENTER_SMALL;
					else if(wMin > .3125)
						return BlockFaceShape.CENTER;
					else
						return BlockFaceShape.CENTER_BIG;
				}
				return BlockFaceShape.UNDEFINED;
			}
		}
		return super.getBlockFaceShape(world, state, pos, side);
	}

	@Override
	@Nonnull
	public AxisAlignedBB getBoundingBox(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos)
	{
		if(world.getBlockState(pos).getBlock()!=this)
			return FULL_BLOCK_AABB;
		else
		{
			TileEntity te = world.getTileEntity(pos);
			if(te instanceof IBlockBounds)
			{
				float[] bounds = ((IBlockBounds)te).getBlockBounds();
				if(bounds.length > 5)
					return new AxisAlignedBB(bounds[0], bounds[1], bounds[2], bounds[3], bounds[4], bounds[5]);
			}
		}
		return super.getBoundingBox(state, world, pos);
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB mask, List<AxisAlignedBB> list, @Nullable Entity ent, boolean isActualState)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof IAdvancedCollisionBounds)
		{
			List<AxisAlignedBB> bounds = ((IAdvancedCollisionBounds)te).getAdvancedColisionBounds();
			if(!bounds.isEmpty())
			{
				for(AxisAlignedBB aabb : bounds)
					if(aabb!=null&&mask.intersects(aabb))
						list.add(aabb);
				return;
			}
		}
		super.addCollisionBoxToList(state, world, pos, mask, list, ent, isActualState);
	}

	//Redstone

	/**
	 * Can this block provide power. Only wire currently seems to have this change based on its state.
	 */
	@Override
	public boolean canProvidePower(IBlockState state)
	{
		return true;
	}

	@Override
	public int getWeakPower(IBlockState blockState, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityBasicMultiblock)
			return ((TileEntityBasicMultiblock)te).getWeakRSOutput(blockState, side);
		return 0;
	}

	@Override
	public int getStrongPower(IBlockState blockState, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityBasicMultiblock)
			return ((TileEntityBasicMultiblock)te).getStrongRSOutput(blockState, side);
		return 0;
	}

	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityBasicMultiblock)
			return ((TileEntityBasicMultiblock)te).canConnectRedstone(state, side);
		return false;
	}

	@Override
	public boolean hasComparatorInputOverride(IBlockState state)
	{
		return true;
	}

	@Override
	public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof IEBlockInterfaces.IComparatorOverride)
			return ((IEBlockInterfaces.IComparatorOverride)te).getComparatorInputOverride();
		return 0;
	}
}
