#priority 100
import crafttweaker.item.IIngredient;

// The default handler consumes a single obsidian per 100-tick, 1600-IF cycle.
<multiblock:example:melter>.addProductionRecipe([<ore:obsidian>, <liquid:lava>*1000] as IIngredient[])
    .withName("obsidian");
