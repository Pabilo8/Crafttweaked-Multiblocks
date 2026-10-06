#loader ctmb
#priority 100

import mods.ctmb.multiblock.Multiblock;
import mods.ctmb.production.RecipeLayout;

val melter = Multiblock.create("ctmb:multiblocks/melter");
melter.setItemStorage("input_storage").withSize(1).withInputPort("input");
melter.setEnergyStorage("power").withSize(16000).withInputPort("energy_in");
melter.setFluidStorage("fluid").withSize(4000).withOutputPort("fluid_out");

melter.setProductionHandler("main")
    .withInput(0, "input_storage", [0])
    .withOutput(0, "fluid")
    .withEnergyStorage("power")
    .withTime(100).withEnergy(1600)
    .withRecipeLayout(
        RecipeLayout.slot(0, 24, 10),
        RecipeLayout.outputFluidTank(0, 106, 10)
    );
