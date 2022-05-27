package pl.pabilo8.ctmb.common.block;

import blusunrize.immersiveengineering.client.ClientProxy;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import net.minecraft.block.Block;
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

	@Override
	public BlockCTMBMultiblock getBlock()
	{
		return ((BlockCTMBMultiblock)super.getBlock());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public FontRenderer getFontRenderer(ItemStack stack)
	{
		return ClientProxy.itemFont;
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState)
	{
		return false;
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		return EnumActionResult.FAIL;
	}

}
