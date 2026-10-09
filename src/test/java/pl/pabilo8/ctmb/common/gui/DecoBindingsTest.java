package pl.pabilo8.ctmb.common.gui;

import net.minecraft.init.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pl.pabilo8.ctmb.common.block.TileEntityMultiblock;
import pl.pabilo8.ctmb.common.gui.component.GuiComponent;

import static org.junit.jupiter.api.Assertions.*;

class DecoBindingsTest
{
	private static int sequence;

	@BeforeAll
	static void bootstrap()
	{
		Bootstrap.register();
	}

	private MultiblockGuiLayout plan()
	{
		return new MultiblockGuiLayout(GuiDefinition.create("test:deco_"+(++sequence)), new TileEntityMultiblock());
	}

	@Test
	void groupedBarsRemainNamedFrozenAndIndependentOfScreenInstances()
	{
		GuiComponent first = GuiComponent.bar(0, 0).withID("first").withSize(12, 64);
		GuiComponent second = GuiComponent.bar(0, 0).withID("second").withSize(18, 40);
		GuiComponent group = GuiComponent.barGroup(20, 20).withID("group").withBars(first, second);
		MultiblockGuiLayout plan = plan().addComponents(group);
		assertEquals(36, group.width());
		assertEquals(64, group.height());
		assertEquals(3, plan.componentDefinitions().size());
		assertSame(first, plan.componentDefinitions().get("first"));
		assertThrows(IllegalStateException.class, () -> first.withValue(20));
		assertThrows(IllegalArgumentException.class, () -> plan.addComponents(GuiComponent.label(0, 0).withID("second")));
	}

	@Test
	void incompatibleChildrenAndImageDirectoriesCannotBecomeDisplays()
	{
		assertThrows(IllegalArgumentException.class, () -> GuiComponent.barGroup(0, 0).withBars(GuiComponent.gauge(0, 0)));
		assertThrows(IllegalArgumentException.class, () -> GuiComponent.image(0, 0).withImage(DecoTextures.RES_TEXTURES_DECO));
		assertThrows(IllegalArgumentException.class, () -> GuiComponent.gauge(0, 0).withRange(90, -90));
		assertThrows(IllegalArgumentException.class, () -> GuiComponent.gauge(0, 0).withAngle(Float.NaN));
	}

	@Test
	void customTexturesResolveThroughTheBracketAtScriptRuntime()
	{
		String name = "test:logo_"+(++sequence);
		DecoTexture registered = DecoTextures.register(name, "ctmb:gui/logo");
		assertSame(registered, DecoTextureBracketHandler.getTexture(name));
		GuiComponent image = GuiComponent.image(0, 0).withImage(registered);
		assertEquals("ctmb:gui/logo", image.getOptions().getString("image"));
		assertThrows(IllegalArgumentException.class, () -> DecoTextures.register(name, "ctmb:gui/other"));
	}
}
