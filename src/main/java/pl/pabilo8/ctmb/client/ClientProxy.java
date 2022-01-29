package pl.pabilo8.ctmb.client;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import pl.pabilo8.ctmb.common.CommonProxy;

import javax.annotation.Nullable;

/**
 * @author Pabilo8
 * @since 29.01.2022
 */
public class ClientProxy extends CommonProxy
{
	// TODO: 29.01.2022 multiblock GUIs
	@Nullable
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		return null;
	}
}
