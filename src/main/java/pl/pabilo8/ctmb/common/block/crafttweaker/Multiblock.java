package pl.pabilo8.ctmb.common.block.crafttweaker;

import crafttweaker.annotations.ZenDoc;
import crafttweaker.annotations.ZenRegister;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pl.pabilo8.ctmb.common.CommonProxy;
import pl.pabilo8.ctmb.common.block.BlockCTMBMultiblock;
import pl.pabilo8.ctmb.common.block.MultiblockDefinition;
import pl.pabilo8.ctmb.common.block.MultiblockStuctureBase;
import pl.pabilo8.ctmb.common.block.TileEntityMultiblock;
import pl.pabilo8.ctmb.common.block.crafttweaker.MultiblockTileCTWrapper.*;
import pl.pabilo8.ctmb.common.gui.GuiDefinition;
import pl.pabilo8.ctmb.common.storage.StorageDefinition;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Pabilo8
 * @since 29.01.2022
 */
@ZenClass(value = "mods.ctmb.multiblock.Multiblock")
@ZenRegister
public class Multiblock extends MultiblockStuctureBase<TileEntityMultiblock>
{
	public static final Multiblock DEFAULT_MULTIBLOCK = new Multiblock("", new ResourceLocation("missingno"), Material.AIR, null);
	private static final AxisAlignedBB[] AABB_CUBE = new AxisAlignedBB[]{new AxisAlignedBB(0, 0, 0, 1, 1, 1)};

	/**
	 * The block bound to this multiblock
	 */
	@Nonnull
	private final BlockCTMBMultiblock block;

	public final MultiblockDefinition definition;
	public final Map<String, StorageDefinition> storages = new LinkedHashMap<>();
	public final Map<String, pl.pabilo8.ctmb.common.production.ProductionHandler> productionHandlers = new LinkedHashMap<>();
	private boolean frozen;
	private String animatedModel;
	private ResourceLocation tactileModel;
	public IMultiblockTactileInteractionFunction onTactileInteract;

	public IMultiblockFunction onUpdate = null;
	public IMultiblockMessageOutFunction onSendMessage = null;
	public IMultiblockMessageInFunction onReceiveMessage = null;
	public IMultiblockInteractionFunction onInteract = null;
	public IMultiblockTooltipFunction onTooltip = null;
	public boolean tooltipNixieFont = false;

	//Is set only once
	public final Material material;

	public GuiDefinition mainGui;


	protected Multiblock(String name, ResourceLocation res, Material material, MultiblockDefinition definition)
	{
		super(name, res);
		this.material = material;
		this.definition = definition;
		if(definition!=null) this.offset = definition.master;
		this.block = new BlockCTMBMultiblock(this);
	}

	//--- Init Method ---//

	@ZenMethod
	public static Multiblock create(String resource)
	{
		MultiblockDefinition definition = MultiblockDefinition.load(resource);
		if(CommonProxy.MULTIBLOCKS.stream().anyMatch(m -> m.getUniqueName().equals(definition.name)))
			throw new IllegalArgumentException("Duplicate multiblock: "+definition.name);
		String flattened = "multiblock_"+definition.name.replace(':', '_').replace('/', '_').toLowerCase(Locale.ROOT);
		if(CommonProxy.MULTIBLOCKS.stream().anyMatch(m -> m.getFlattenedName().equals(flattened)))
			throw new IllegalArgumentException("Multiblock names produce the same block registry ID: "+definition.name);
		Multiblock mb = new Multiblock(definition.name, definition.structure, definition.material, definition);
		CommonProxy.MULTIBLOCKS.add(mb);
		CommonProxy.BLOCKS.add(mb.getBlock());
		return mb;
	}

	@ZenMethod
	public Multiblock withAnimatedModel()
	{
		return withAnimatedModel("ctmb:"+getUniqueName().substring(getUniqueName().indexOf(':')+1)+".obj.ie");
	}

	@ZenMethod
	public Multiblock withAnimatedModel(String model)
	{
		if(frozen) throw new IllegalStateException("Multiblock is already registered");
		ResourceLocation resource = new ResourceLocation(model);
		if(!resource.getResourcePath().endsWith(".obj.ie"))
			throw new IllegalArgumentException("Animated models must use .obj.ie");
		animatedModel = resource.toString();
		return this;
	}

	@ZenMethod
	public Multiblock withTactileModel(String resource)
	{
		if(frozen) throw new IllegalStateException("Multiblock is already registered");
		tactileModel = new ResourceLocation(resource);
		return this;
	}

	@ZenMethod
	public void setOnTactileInteract(IMultiblockTactileInteractionFunction function)
	{
		onTactileInteract = function;
	}

	public String animatedModel()
	{
		return animatedModel;
	}

	public ResourceLocation tactileModel()
	{
		return tactileModel;
	}

	public void freeze()
	{
		storages.values().forEach(storage -> storage.freeze(definition));
		// Speed/torque cannot be combined like item slots: one provider per rotary face.
		java.util.Set<String> rotaryFaces = new java.util.HashSet<>();
		for(StorageDefinition storage : storages.values())
			if(storage.kind==StorageDefinition.Kind.ROTARY)
			{
				java.util.Set<String> providerFaces = new java.util.HashSet<>();
				for(StorageDefinition.Port port : storage.ports())
					for(int position : definition.getPOI(port.poi))
						providerFaces.add(position+":"+definition.getDirection(port.direction));
				for(String face : providerFaces)
					if(!rotaryFaces.add(face))
						throw new IllegalArgumentException("Multiple rotary providers on face "+face);
			}
		productionHandlers.values().forEach(pl.pabilo8.ctmb.common.production.ProductionHandler::freeze);
		frozen = true;
	}

	@Override
	public void updateStructure()
	{
		super.updateStructure();
		if(definition!=null)
		{
			definition.validate(getSize());
			if(getStructureManual()[offset.getY()][offset.getZ()][offset.getX()].isEmpty())
				throw new IllegalArgumentException(getUniqueName()+": JSON master must be an occupied structure block");
			for(StorageDefinition provider : storages.values())
				for(StorageDefinition.Port port : provider.ports())
					for(int position : definition.getPOI(port.poi))
					{
						int[] dimensions = getSize();
						int h = position/(dimensions[1]*dimensions[2]), l = position/dimensions[2]%dimensions[1], w = position%dimensions[2];
						if(getStructureManual()[h][l][w].isEmpty())
							throw new IllegalArgumentException(getUniqueName()+": Port "+port.poi+" refers to an empty structure block "+position);
					}
			onDefinitionLoaded(definition);
		}
	}

	/**
	 * Extension seam for Tactile/animation modules; raw JSON remains available on the definition.
	 */
	protected void onDefinitionLoaded(MultiblockDefinition definition)
	{
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
	public void addGui(GuiDefinition gui)
	{
		gui.bind(this);
	}

	@ZenMethod
	public void setMainGui(GuiDefinition gui)
	{
		if(gui==null) throw new IllegalArgumentException("A main GUI definition is required");
		gui.bind(this);
		this.mainGui = gui;
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
		return "multiblock_"+getUniqueName().replace(':', '_').replace('/', '_').toLowerCase(Locale.ROOT);
	}

	@Nullable
	public GuiDefinition getGuiLayout(int page)
	{
		if(page==0) return mainGui;
		GuiDefinition gui = GuiDefinition.forPage(page);
		return gui!=null&&gui.isBoundTo(this)?gui: null;
	}

	public int getGuiPage(String name)
	{
		GuiDefinition gui = GuiDefinition.find(name);
		return gui!=null&&gui.isBoundTo(this)?gui.page(): -1;
	}

	public java.util.List<AxisAlignedBB> getAABB(int position, net.minecraft.util.EnumFacing facing, boolean mirrored)
	{
		return definition==null?java.util.Collections.emptyList(): definition.bounds(position, facing, mirrored);
	}

	@ZenMethod
	public pl.pabilo8.ctmb.common.production.ProductionHandler setProductionHandler(String name)
	{
		if(frozen) throw new IllegalStateException("Multiblock definitions are frozen");
		if(productionHandlers.containsKey(name))
			throw new IllegalArgumentException("Duplicate production handler: "+name);
		pl.pabilo8.ctmb.common.production.ProductionHandler handler = new pl.pabilo8.ctmb.common.production.ProductionHandler(this, name);
		productionHandlers.put(name, handler);
		return handler;
	}

	@ZenMethod
	public pl.pabilo8.ctmb.common.production.ProductionHandler getProductionHandler(String name)
	{
		pl.pabilo8.ctmb.common.production.ProductionHandler handler = productionHandlers.get(name);
		if(handler==null) throw new IllegalArgumentException("Unknown production handler: "+name);
		return handler;
	}

	@ZenMethod
	public pl.pabilo8.ctmb.common.production.ProductionRecipe addProductionRecipe(crafttweaker.api.item.IIngredient input, crafttweaker.api.item.IIngredient output)
	{
		return addProductionRecipe(new crafttweaker.api.item.IIngredient[]{input, output});
	}

	@ZenMethod
	public pl.pabilo8.ctmb.common.production.ProductionRecipe addProductionRecipe(crafttweaker.api.item.IIngredient... arguments)
	{
		if(productionHandlers.size()!=1)
			throw new IllegalArgumentException("Select a production handler when the machine has more than one");
		return productionHandlers.values().iterator().next().add(arguments);
	}

	@ZenMethod
	public pl.pabilo8.ctmb.common.production.ProductionRecipe addProductionRecipe(String handler, crafttweaker.api.item.IIngredient... arguments)
	{
		return getProductionHandler(handler).add(arguments);
	}

	private StorageDefinition storage(String name, StorageDefinition.Kind kind)
	{
		if(frozen) throw new IllegalStateException("Multiblock storage definitions are frozen");
		if(storages.containsKey(name)) throw new IllegalArgumentException("Duplicate storage: "+name);
		StorageDefinition storage = new StorageDefinition(name, kind);
		storages.put(name, storage);
		return storage;
	}

	@ZenMethod
	public StorageDefinition setItemStorage(String name)
	{
		return storage(name, StorageDefinition.Kind.ITEM);
	}

	@ZenMethod
	public StorageDefinition setFluidStorage(String name)
	{
		return storage(name, StorageDefinition.Kind.FLUID);
	}

	@ZenMethod
	public StorageDefinition setDustStorage(String name)
	{
		return storage(name, StorageDefinition.Kind.DUST);
	}

	@ZenMethod
	public StorageDefinition setEnergyStorage(String name)
	{
		return storage(name, StorageDefinition.Kind.ENERGY);
	}

	@ZenMethod
	public StorageDefinition setRotaryStorage(String name)
	{
		return storage(name, StorageDefinition.Kind.ROTARY);
	}

	@ZenMethod
	public StorageDefinition setDataStorage(String name)
	{
		return storage(name, StorageDefinition.Kind.DATA);
	}

	@ZenMethod
	public StorageDefinition setRedstoneStorage(String name)
	{
		return storage(name, StorageDefinition.Kind.REDSTONE);
	}
}
