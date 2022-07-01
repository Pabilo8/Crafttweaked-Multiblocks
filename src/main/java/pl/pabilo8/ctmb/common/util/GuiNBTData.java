package pl.pabilo8.ctmb.common.util;

import crafttweaker.api.data.DataList;
import crafttweaker.api.data.IData;
import pl.pabilo8.ctmb.common.CommonUtils;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Pabilo8
 * @since 01.07.2022
 */
@SuppressWarnings("unused")
public class GuiNBTData
{
	final Map<String, IData> map;
	final boolean isValid;

	public GuiNBTData(IData data)
	{
		this.isValid = CommonUtils.dataCheck(data);
		this.map = isValid?data.asMap(): new HashMap<>();
	}

	//--- Utilities ---//

	public boolean isValid()
	{
		return isValid;
	}

	/**
	 * <b>USE ONLY WHEN NECESSARY</b>
	 *
	 * @return whether a key exists in the nbt map
	 */
	public boolean has(String key)
	{
		return map.containsKey(key);
	}

	/**
	 * <b>USE ONLY WHEN NECESSARY</b>
	 *
	 * @return key from the nbt map
	 */
	@Nullable
	public IData get(String key)
	{
		return map.get(key);
	}

	//--- Integers ---//

	public int getInt(String key, int def)
	{
		return map.containsKey(key)?map.get(key).asInt(): def;
	}

	public int getX(int def)
	{
		return getInt("x", def);
	}

	public int getY(int def)
	{
		return getInt("y", def);
	}

	public int getWidth(int def)
	{
		return getInt("w", def);
	}

	public int getHeight(int def)
	{
		return getInt("h", def);
	}

	/**
	 * For storage components
	 */
	public int getID()
	{
		return getInt("id", 0);
	}

	public int getColor(int def)
	{
		return getColor("color", def);
	}

	public int getColor(String name, int def)
	{
		return map.containsKey(name)?CommonUtils.getColorFromData(map.get(name)): def;
	}

	//--- Strings ---//

	public String getText(String def)
	{
		return getText("text", def);
	}

	public String getText(String key, String def)
	{
		return map.containsKey(key)?map.get(key).asString(): def;
	}

	public String[] getStringArray(String key)
	{
		IData data = map.get(key);
		if(data instanceof DataList)
			return data.asList().stream().map(IData::asString).toArray(String[]::new);
		return new String[0];
	}

	//--- Booleans ---//

	public boolean getProperty(String property, boolean def)
	{
		return map.containsKey(property)?map.get(property).asBool():def;
	}

	public boolean getProperty(String property)
	{
		return map.containsKey(property)&&map.get(property).asBool();
	}

	public boolean getTranslated()
	{
		return getProperty("translated");
	}

	//--- Floats ---//

	public float getFloat(String key, float def)
	{
		return map.containsKey(key)?map.get(key).asFloat():def;
	}

	//--- GUIs ---//

	public int getStyle()
	{
		return map.containsKey("style_id")?CommonUtils.getColorFromData(map.get("style_id")): 0;
	}
}
