#loader ctmb
#priority 20

import mods.ctmb.gui.Gui;
import mods.ctmb.gui.Component;

val melterGUI = Gui.create("example:melter").withTitle("desc.immersiveengineering.info.multiblock.example:melter");
melterGUI.onInit(function(gui, mb, player) {
    gui.addBackground()
        .withBox(<deco:bg_steel>, 0, 0, 176, 88)
        .withSlot(0, <slotstyle:vanilla>, 8, 24)
        .withTitleBar()
        .withBox(<deco:bg_wooden>, <deco:template_round_wooden>, 0, 88, 176, 104)
        .withPlayerInventory(<slotstyle:vanilla>, 7, 24)
        .withInventoryTitleBar()
        .build();
    gui.addComponents(
        Component.fluidTank(48, 18).withID("output").withDataSource(mb.getStorage("fluid")),
        Component.energyBar(156, 18).withID("power").withDataSource(mb.getStorage("power")),
        Component.bar(138, 18).withID("progress").withDataSource(mb.getProduction("main"))
    );
});
<multiblock:example:melter>.setMainGui(melterGUI);
