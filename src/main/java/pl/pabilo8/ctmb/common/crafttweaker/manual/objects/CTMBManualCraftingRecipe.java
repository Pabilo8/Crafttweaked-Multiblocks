package pl.pabilo8.ctmb.common.crafttweaker.manual.objects;

import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.ManualPages.PositionedItemStack;
import blusunrize.lib.manual.ManualUtils;
import blusunrize.lib.manual.gui.GuiButtonManualNavigation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import pl.pabilo8.ctmb.common.crafttweaker.manual.CTMBManualObject;
import pl.pabilo8.ctmb.common.crafttweaker.manual.CTMBManualPage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * @author Pabilo8
 * @since 22.05.2022
 */
public class CTMBManualCraftingRecipe extends CTMBManualObject
{
	GuiButtonManualNavigation buttonNext, buttonPrev;
	ItemStack[] stacks;
	ItemStack highlighted = ItemStack.EMPTY;
	ArrayList<PositionedItemStack[]> recipes = new ArrayList<>();
	int recipePage;
	int yOff;

	public CTMBManualCraftingRecipe(ManualObjectInfo info, NBTTagCompound compound)
	{
		super(info, compound);
		if(compound.hasKey("item"))
		{
			stacks = new ItemStack[]{new ItemStack(compound.getCompoundTag("item"))};
		}
		else if(compound.hasKey("items"))
		{
			NBTTagList items = compound.getTagList("items", NBT.TAG_COMPOUND);
			stacks = items.tagList.stream()
					.filter(nbt -> nbt instanceof NBTTagCompound)
					.map(nbt -> ((NBTTagCompound)nbt))
					.map(ItemStack::new)
					.toArray(ItemStack[]::new);
		}
		else
			stacks = new ItemStack[0];
	}

	@Override
	public void postInit(CTMBManualPage page)
	{
		super.postInit(page);

		recalculateCraftingRecipes(page);
		if(this.recipes.size() > 1)
		{
			buttonPrev = new GuiButtonManualNavigation(gui, 100, x-2, y+yOff/2-3, 8, 10, 0);
			buttonNext = new GuiButtonManualNavigation(gui, 100+1, x+122-16, y+yOff/2-3, 8, 10, 1);
		}

		height = yOff;
	}

	//--- Rendering, Reaction ---//

	@Override
	public void drawButton(Minecraft mc, int mx, int my, float partialTicks)
	{
		super.drawButton(mc, mx, my, partialTicks);


		ManualInstance manual = gui.getManual();

		highlighted = ItemStack.EMPTY;

		GlStateManager.pushMatrix();
		if(recipes.size() > 1)
		{
			buttonNext.drawButton(mc, mx, my, partialTicks);
			buttonPrev.drawButton(mc, mx, my, partialTicks);
		}

		if(!recipes.isEmpty()&&recipePage >= 0&&recipePage < this.recipes.size())
		{
			GlStateManager.enableRescaleNormal();
			RenderHelper.enableGUIStandardItemLighting();

			int maxX = 0;
			for(PositionedItemStack pstack : recipes.get(recipePage))
				if(pstack!=null)
				{
					if(pstack.x > maxX)
						maxX = pstack.x;
					gui.drawGradientRect(x+pstack.x, y+pstack.y, x+pstack.x+16, y+pstack.y+16, 0x33666666, 0x33666666);
				}
			ManualUtils.bindTexture(manual.texture);
			ManualUtils.drawTexturedRect(x+maxX-17, y+yOff/2-5, 16, 10, 0/256f, 16/256f, 226/256f, 236/256f);

		}

		GlStateManager.translate(0, 0, 300);

		manual.fontRenderer.setUnicodeFlag(false);
		//RenderItem.getInstance().renderWithColor=true;
		if(!recipes.isEmpty()&&recipePage >= 0&&recipePage < this.recipes.size())
		{
			for(PositionedItemStack pstack : recipes.get(recipePage))
				if(pstack!=null)
					if(!pstack.getStack().isEmpty())
					{
						ManualUtils.renderItem().renderItemAndEffectIntoGUI(pstack.getStack(), x+pstack.x, y+pstack.y);
						ManualUtils.renderItem().renderItemOverlayIntoGUI(manual.fontRenderer, pstack.getStack(), x+pstack.x, y+pstack.y, null);

						if(mx >= x+pstack.x&&mx < x+pstack.x+16&&my >= y+pstack.y&&my < y+pstack.y+16)
							highlighted = pstack.getStack();
					}
		}

		GlStateManager.translate(0, 0, -300);
		GlStateManager.disableRescaleNormal();
		GlStateManager.enableBlend();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.popMatrix();

	}

	@Override
	protected int getDefaultHeight()
	{
		return 20;
	}

	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
	{
		if(recipes.size() > 1&&super.mousePressed(mc, mouseX, mouseY))
		{
			if(buttonPrev.mousePressed(mc, mouseX, mouseY))
			{
				if(recipePage > 0)
					recipePage--;
			}
			else if(buttonNext.mousePressed(mc, mouseX, mouseY))
			{
				if(recipePage < recipes.size()-1)
					recipePage++;
			}
			else
				return false;
			return true;
		}

		return false;
	}

	@Override
	public void mouseDragged(int x, int y, int clickX, int clickY, int mx, int my, int lastX, int lastY, int button)
	{

	}

	@Override
	public void drawTooltip(Minecraft mc, int mx, int my)
	{
		gui.getManual().fontRenderer.setUnicodeFlag(false);
		if(!highlighted.isEmpty())
			gui.renderToolTip(highlighted, mx, my);

		RenderHelper.disableStandardItemLighting();
	}

	//--- Private Methods (for parsing) ---//

	private void recalculateCraftingRecipes(CTMBManualPage page)
	{
		this.recipes.clear();
		Set<Integer> searchCrafting = new HashSet<>();

		IntStream.range(0, stacks.length).forEachOrdered(iStack -> {
			searchCrafting.add(iStack);
			if(stacks[iStack]!=null)
				page.addProvidedItem(stacks[iStack]);
		});

		if(!searchCrafting.isEmpty())
			for(IRecipe recipe : CraftingManager.REGISTRY)
				for(int iStack : searchCrafting)
					if(!recipe.getRecipeOutput().isEmpty()&&ManualUtils.stackMatchesObject(recipe.getRecipeOutput(), stacks[iStack]))
						handleRecipe(recipe, iStack);
	}

	private void handleRecipe(IRecipe recipe, int iStack)
	{
		int w, h;
		NonNullList<Ingredient> ingredientsPre = recipe.getIngredients();

		if(recipe instanceof ShapelessRecipes||recipe instanceof ShapelessOreRecipe)
		{
			w = ingredientsPre.size() > 6?3: ingredientsPre.size() > 1?2: 1;
			h = ingredientsPre.size() > 4?3: ingredientsPre.size() > 2?2: 1;
		}
		else if(recipe instanceof IShapedRecipe)
		{
			w = ((IShapedRecipe)recipe).getRecipeWidth();
			h = ((IShapedRecipe)recipe).getRecipeWidth();
		}
		else
			return;

		PositionedItemStack[] pIngredients = new PositionedItemStack[ingredientsPre.size()+1];
		int xBase = (120-(w+2)*18)/2;
		for(int hh = 0; hh < h; hh++)
			for(int ww = 0; ww < w; ww++)
				if(hh*w+ww < ingredientsPre.size())
					pIngredients[hh*w+ww] = new PositionedItemStack(ingredientsPre.get(hh*w+ww), xBase+ww*18, hh*18);
		pIngredients[pIngredients.length-1] = new PositionedItemStack(recipe.getRecipeOutput(), xBase+w*18+18, (int)(h/2f*18)-8);
		if(iStack < this.recipes.size())
			this.recipes.add(iStack, pIngredients);
		else
			this.recipes.add(pIngredients);
		if(h*18 > yOff)
			yOff = h*18;
	}
}
