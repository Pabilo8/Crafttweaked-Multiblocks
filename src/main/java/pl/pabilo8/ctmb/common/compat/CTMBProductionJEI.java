package pl.pabilo8.ctmb.common.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import pl.pabilo8.ctmb.common.CommonProxy;
import pl.pabilo8.ctmb.common.production.ProductionHandler;
import pl.pabilo8.ctmb.common.production.ProductionRecipe;
import pl.pabilo8.immersiveintelligence.client.gui.deco.util.DecoTextures;
import pl.pabilo8.immersiveintelligence.common.compat.jei.IIRecipeJEICategory;
import pl.pabilo8.immersiveintelligence.common.compat.jei.IIRecipeJEIWrapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Reuses II's renderers and wrappers; each CTMB handler has its own category and dimensions.
 */
@JEIPlugin
public final class CTMBProductionJEI implements IModPlugin
{
	private final List<Category> categories = new ArrayList<>();

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry)
	{
		categories.clear();
		registeredHandlers().forEach(h -> categories.add(new Category(h)));
		registry.addRecipeCategories(categories.toArray(new Category[0]));
	}

	static List<ProductionHandler> registeredHandlers()
	{
		return CommonProxy.MULTIBLOCKS.stream().flatMap(m -> m.productionHandlers.values().stream())
				.sorted(Comparator.comparing(ProductionHandler::uid)).collect(Collectors.toList());
	}

	@Override
	public void register(IModRegistry registry)
	{
		for(Category category : categories)
		{
			ItemStack machine = new ItemStack(category.handler.multiblock.getBlock());
			registry.addRecipeCatalyst(machine, category.getUid());
			// Prebuilt wrappers avoid one global class-to-category factory for all CTMB handlers.
			List<ProductionRecipe> recipes = category.handler.recipes();
			if(!recipes.isEmpty())
				registry.addRecipes(recipes.stream().map(r -> new IIRecipeJEIWrapper<>(r, machine)).collect(Collectors.toList()), category.getUid());
		}
	}

	private static final class Category extends IIRecipeJEICategory<ProductionRecipe>
	{
		private final ProductionHandler handler;
		private final IDrawable background;

		Category(ProductionHandler handler)
		{
			super(ProductionRecipe.class, handler.uid(), "desc.immersiveengineering.info.multiblock."+handler.multiblock.getUniqueName());
			this.handler = handler;
			background = new MostExcellentDrawableImplementation(handler.layoutWidth(), handler.layoutHeight(), DecoTextures.BG_STEEL, DecoTextures.TEMPLATE_ROUND);
		}

		@Override
		public String getTitle()
		{
			String title = I18n.format("desc.immersiveengineering.info.multiblock."+handler.multiblock.getUniqueName());
			return handler.multiblock.productionHandlers.size() > 1?title+" / "+handler.name: title;
		}

		@Override
		public String getUid()
		{
			return handler.uid();
		}

		@Override
		public String getRecipeCategoryUid()
		{
			return handler.uid();
		}

		@Override
		public String getModName()
		{
			return "ctmb";
		}

		@Override
		public IDrawable getBackground()
		{
			return background;
		}
	}
}
