package pl.pabilo8.ctmb.common.block;

import blusunrize.immersiveengineering.client.ClientProxy;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 * @author Pabilo8
 * @since 23.05.2022
 */
public class ItemBlockCTMBMultiblock extends ItemBlock
{
	public ItemBlockCTMBMultiblock(BlockCTMBMultiblock block)
	{
		super(block);
	}

	@Nonnull
	@Override
	public BlockCTMBMultiblock getBlock()
	{
		return ((BlockCTMBMultiblock)super.getBlock());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public FontRenderer getFontRenderer(@Nonnull ItemStack stack)
	{
		return ClientProxy.itemFont;
	}

	@Override
	public boolean placeBlockAt(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing side, float hitX, float hitY, float hitZ, @Nonnull IBlockState newState)
	{
		return false;
	}

	/**
	 * Omit the .name part for no repetition in the lang file
	 */
	@Nonnull
	@Override
	public String getItemStackDisplayName(@Nonnull ItemStack stack)
	{
		return this.getUnlocalizedNameInefficiently(stack);
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUse(@Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumHand hand, @Nonnull EnumFacing side, float hitX, float hitY, float hitZ)
	{
		return EnumActionResult.FAIL;
	}

}
