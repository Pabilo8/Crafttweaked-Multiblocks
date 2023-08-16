package pl.pabilo8.ctmb.common;

import crafttweaker.api.data.*;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.registry.GameRegistry;
import pl.pabilo8.ctmb.CTMB;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Pabilo8
 * @since 29.01.2022
 */
@SuppressWarnings("unused")
public class CommonUtils
{
	/**
	 * @return a {@link TargetPoint} for a network message
	 */
	public static TargetPoint targetPointFromTile(TileEntity tile, int range)
	{
		return targetPointFromPos(new Vec3d(tile.getPos()), tile.getWorld(), range);
	}

	/**
	 * @return a {@link TargetPoint} for a network message
	 */
	public static TargetPoint targetPointFromPos(Vec3d pos, World world, int range)
	{
		return new TargetPoint(world.provider.getDimension(), pos.x, pos.y, pos.z, range);
	}

	public static float[] rgbIntToRGB(int rgb)
	{
		float r = ((rgb>>16)&255)/255f;
		float g = ((rgb>>8)&255)/255f;
		float b = (rgb&255)/255f;
		return new float[]{r, g, b};
	}

	/**
	 * Makes an integer color from the given red, green, and blue float (0-1) values
	 * Stolen from MathHelper because of Side=Client annotation
	 */
	public static int rgb(float rIn, float gIn, float bIn)
	{
		return rgb(MathHelper.floor(rIn*255.0F), MathHelper.floor(gIn*255.0F), MathHelper.floor(bIn*255.0F));
	}

	/**
	 * Makes a single int color with the given red, green, and blue (0-255) values.
	 * Stolen from MathHelper because of Side=Client annotation
	 */
	public static int rgb(int rIn, int gIn, int bIn)
	{
		int lvt_3_1_ = (rIn<<8)+gIn;
		lvt_3_1_ = (lvt_3_1_<<8)+bIn;
		return lvt_3_1_;
	}

	//Converts snake_case to camelCase or CamelCase
	//Copy as you wish
	public static String toCamelCase(String string, boolean startSmall)
	{
		StringBuilder result = new StringBuilder();
		String[] all = string.split("_");
		for(String s : all)
		{
			result.append(Character.toUpperCase(s.charAt(0)));
			result.append(s.substring(1));
		}
		if(startSmall)
			result.setCharAt(0, Character.toLowerCase(result.charAt(0)));
		return result.toString();
	}

	public static void registerTile(Class<? extends TileEntity> tile)
	{
		String s = tile.getSimpleName();
		s = s.substring(s.indexOf("TileEntity")+"TileEntity".length());
		GameRegistry.registerTileEntity(tile, new ResourceLocation(CTMB.MODID+":"+s));
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public static <T> T getMapElement(LinkedHashMap<String, T> map, int id)
	{
		return (T)map.values().toArray()[id];
	}

	public static int getColorFromData(IData data)
	{
		if(data instanceof DataInt)
			return data.asInt();
		if(data instanceof DataIntArray)
		{
			int[] rgb = data.asIntArray();
			return rgb(rgb[0], rgb[1], rgb[2]);
		}
		if(data instanceof DataString)
		{
			return Integer.parseInt(data.asString().replaceFirst("#", ""), 16);
		}
		return 0;
	}

	public static int[] get4ParIntArrayFromData(IData data)
	{
		if(data instanceof DataInt)
		{
			int i = data.asInt();
			return new int[]{i, i, i, i};
		}
		else if(data instanceof DataIntArray)
		{
			int[] i = data.asIntArray();
			if(i.length==1)
				return new int[]{i[0], i[0], i[0], i[0]};
			else if(i.length==2)
				return new int[]{i[0], i[0], i[1], i[1]};
			else if(i.length==4)
				return i;
		}
		return new int[]{0, 0, 0, 0};
	}

	public static boolean[] get4ParBoolArrayFromData(IData data)
	{
		if(data instanceof DataBool)
		{
			boolean i = data.asBool();
			return new boolean[]{i, i, i, i};
		}
		else if(data instanceof DataList)
		{
			List<IData> i = data.asList();
			if(i.size()==1)
				return new boolean[]{i.get(0).asBool(), i.get(0).asBool(), i.get(0).asBool(), i.get(0).asBool()};
			else if(i.size()==2)
				return new boolean[]{i.get(0).asBool(), i.get(0).asBool(), i.get(1).asBool(), i.get(1).asBool()};
			else if(i.size()==4)
				return new boolean[]{i.get(0).asBool(), i.get(1).asBool(), i.get(2).asBool(), i.get(3).asBool()};
		}
		return new boolean[]{false, false, false, false};
	}

	public static boolean dataCheck(IData data)
	{
		return data instanceof DataMap&&data.length() > 0;
	}

	public static IData getDataFromString(String s)
	{
		try
		{
			return CraftTweakerMC.getIData(JsonToNBT.getTagFromJson(s));
		} catch(NBTException e)
		{
			return CraftTweakerMC.getIData(new NBTTagCompound());
		}
	}
}
