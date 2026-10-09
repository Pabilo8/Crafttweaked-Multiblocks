package pl.pabilo8.ctmb.client.render;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.client.ClientUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.opengl.GL11;
import pl.pabilo8.ctmb.common.amt.CTMBAMTHeader;
import pl.pabilo8.ctmb.common.block.TileEntityMultiblock;
import pl.pabilo8.ctmb.common.util.CTMBLogger;
import pl.pabilo8.immersiveintelligence.client.util.amt.AMTLoader;
import pl.pabilo8.immersiveintelligence.client.util.amt.animation.IIAnimationCompiledMap;
import pl.pabilo8.immersiveintelligence.client.util.amt.models.AMTModel;
import pl.pabilo8.immersiveintelligence.common.util.ResLoc;

import java.util.*;

/**
 * Only a blockstate's explicit .obj.ie dynamic variant supplies an animated model.
 */
public final class CTMBAMTRenderer extends TileEntitySpecialRenderer<TileEntityMultiblock>
{
	public static final CTMBAMTRenderer INSTANCE = new CTMBAMTRenderer();
	private final Map<TileEntityMultiblock, Entry> models = new IdentityHashMap<>();

	@Override
	public void render(TileEntityMultiblock tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
	{
		if(tile==null||!tile.hasWorld()||!tile.formed||tile.isDummy()) return;
		Entry entry = models.computeIfAbsent(tile, this::load);
		if(entry.model==null) return;
		entry.model.defaultize();
		entry.header.applyHierarchy(Arrays.asList(entry.model.getChildrenRecursive()));
		tile.getAMTState().samples().forEach((animation, progress) -> {
			if(!entry.animations.containsKey(animation))
				try {entry.animations.put(animation, IIAnimationCompiledMap.create(entry.model, animation));} catch(
						RuntimeException exception)
				{
					entry.animations.put(animation, null);
					CTMBLogger.error("Cannot load CTMB render animation "+animation+": "+exception.getMessage());
				}
			IIAnimationCompiledMap map = entry.animations.get(animation);
			if(map!=null) map.apply(progress);
		});
		GlStateManager.pushMatrix();
		try
		{
			GlStateManager.translate(x+0.5, y+0.5, z+0.5);
			GlStateManager.rotate(tile.facing.getOpposite().getHorizontalAngle(), 0, 1, 0);
			if(tile.mirrored)
			{
				GlStateManager.scale(-1, 1, 1);
				GlStateManager.cullFace(GlStateManager.CullFace.FRONT);
			}
			GlStateManager.translate(-0.5, -0.5, -0.5);
			GlStateManager.color(1, 1, 1, alpha);
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			ClientUtils.bindAtlas();
			Tessellator tessellator = Tessellator.getInstance();
			entry.model.render(tessellator, tessellator.getBuffer());
		} finally
		{
			GlStateManager.cullFace(GlStateManager.CullFace.BACK);
			GlStateManager.disableBlend();
			GlStateManager.color(1, 1, 1, 1);
			GlStateManager.popMatrix();
		}
	}

	@SuppressWarnings("deprecation")
	private Entry load(TileEntityMultiblock tile)
	{
		try
		{
			IBlockState state = tile.getWorld().getBlockState(tile.getPos());
			state = state.getBlock().getActualState(state, tile.getWorld(), tile.getPos()).withProperty(IEProperties.DYNAMICRENDER, true);
			IBakedModel baked = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(state);
			if(!(baked instanceof OBJModel.OBJBakedModel)) return new Entry(null, null);
			OBJModel model = ((OBJModel.OBJBakedModel)baked).getModel();
			ResourceLocation location = ReflectionHelper.getPrivateValue(OBJModel.class, model, "modelLocation");
			// An unchanged static variant must never be rendered a second time by the TESR.
			if(location==null||!location.getResourcePath().endsWith(".obj.ie")) return new Entry(null, null);
			ResourceLocation headerLocation = ResLoc.of(location).withExtension(ResLoc.EXT_OBJAMT);
			CTMBAMTHeader header = new CTMBAMTHeader(AMTLoader.readFileToJSON(headerLocation, "CTMB model header"));
			return new Entry(CTMBAMTParts.create(model, header), header);
		} catch(RuntimeException exception)
		{
			CTMBLogger.error("Cannot load CTMB animated model "+tile.getMultiblock().getUniqueName()+": "+exception.getMessage());
			return new Entry(null, null);
		}
	}

	public void reload()
	{
		models.values().forEach(Entry::dispose);
		models.clear();
	}

	public void prune()
	{
		Iterator<Map.Entry<TileEntityMultiblock, Entry>> iterator = models.entrySet().iterator();
		while(iterator.hasNext())
		{
			Map.Entry<TileEntityMultiblock, Entry> entry = iterator.next();
			TileEntityMultiblock tile = entry.getKey();
			if(tile.isInvalid()||!tile.formed||tile.getWorld()!=Minecraft.getMinecraft().world||!tile.getWorld().isBlockLoaded(tile.getPos()))
			{
				entry.getValue().dispose();
				iterator.remove();
			}
		}
	}

	private static final class Entry
	{
		final AMTModel model;
		final CTMBAMTHeader header;
		final Map<ResourceLocation, IIAnimationCompiledMap> animations = new HashMap<>();

		Entry(AMTModel model, CTMBAMTHeader header)
		{
			this.model = model;
			this.header = header;
		}

		void dispose()
		{
			if(model!=null) model.disposeOf();
		}
	}
}
