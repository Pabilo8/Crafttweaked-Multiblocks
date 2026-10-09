package pl.pabilo8.ctmb.client.compat;

import net.minecraft.block.material.Material;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pl.pabilo8.ctmb.common.CommonProxy;
import pl.pabilo8.ctmb.common.block.crafttweaker.Multiblock;
import pl.pabilo8.ctmb.common.compat.CTMBProductionJEI;
import pl.pabilo8.ctmb.common.production.ProductionHandler;
import pl.pabilo8.ctmb.common.production.RecipeLayout;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CTMBProductionJEITest
{
	private static int sequence;

	@BeforeAll
	static void bootstrap()
	{
		Bootstrap.register();
	}

	private static final class TestMultiblock extends Multiblock
	{
		TestMultiblock()
		{
			super("test:jei_"+(++sequence), new ResourceLocation("test:unused"), Material.IRON, null);
			setItemStorage("items");
			setFluidStorage("fluid").withSize(1000);
		}
	}

	@Test
	void allHandlersHaveIndependentUidsAndDimensionsEvenWithoutRecipes()
	{
		List<Multiblock> previous = new ArrayList<>(CommonProxy.MULTIBLOCKS);
		try
		{
			CommonProxy.MULTIBLOCKS.clear();
			Multiblock first = new TestMultiblock(), second = new TestMultiblock();
			ProductionHandler narrow = first.setProductionHandler("main").withInput(0, "items").withOutput(0, "fluid").withEnergy(0);
			ProductionHandler wide = first.setProductionHandler("secondary").withInput(0, "items").withOutput(0, "fluid").withEnergy(0)
					.withRecipeLayout(RecipeLayout.slot(0, 10, 10), RecipeLayout.outputFluidTank(0, 260, 20));
			ProductionHandler other = second.setProductionHandler("main").withInput(0, "items").withOutput(0, "fluid").withEnergy(0);
			first.freeze();
			second.freeze();
			CommonProxy.MULTIBLOCKS.add(first);
			CommonProxy.MULTIBLOCKS.add(second);
			List<ProductionHandler> handlers = CTMBProductionJEI.registeredHandlers();
			assertEquals(3, handlers.size());
			assertTrue(handlers.contains(narrow));
			assertTrue(handlers.contains(wide));
			assertTrue(handlers.contains(other));
			assertTrue(handlers.stream().allMatch(h -> h.recipes().isEmpty()));
			assertEquals(3, handlers.stream().map(ProductionHandler::uid).distinct().count());
			assertEquals(156, narrow.layoutWidth());
			assertEquals(286, wide.layoutWidth());
			assertEquals(87, wide.layoutHeight());
		} finally
		{
			CommonProxy.MULTIBLOCKS.clear();
			CommonProxy.MULTIBLOCKS.addAll(previous);
		}
	}
}
