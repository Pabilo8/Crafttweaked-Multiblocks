package pl.pabilo8.ctmb.common.amt;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import pl.pabilo8.immersiveintelligence.common.util.amt.AMTModelHeader;

import java.util.*;

/**
 * II header plus explicit part types. Geometry remains quads unless a type is declared.
 */
public final class CTMBAMTHeader extends AMTModelHeader
{
	private static final Set<String> TYPES = new HashSet<>(Arrays.asList(
			"quads", "locator", "item", "fluid", "text", "wire", "particle", "bullet", "banner", "blend_mode"));
	private final Map<String, String> types = new LinkedHashMap<>(), parents = new LinkedHashMap<>();
	private final Map<String, pl.pabilo8.immersiveintelligence.common.util.easynbt.EasyNBT> properties = new LinkedHashMap<>();
	private final Set<String> names = new LinkedHashSet<>();

	public CTMBAMTHeader(JsonObject json)
	{
		super(validate(json));
		if(json.has("origins")) json.getAsJsonObject("origins").entrySet().forEach(entry -> names.add(entry.getKey()));
		if(json.has("properties"))
			for(Map.Entry<String, JsonElement> entry : json.getAsJsonObject("properties").entrySet())
			{
				names.add(entry.getKey());
				properties.put(entry.getKey(), pl.pabilo8.immersiveintelligence.common.util.easynbt.EasyNBT.parseEasyNBT(entry.getValue().toString()));
			}
		if(json.has("types"))
			for(Map.Entry<String, JsonElement> entry : json.getAsJsonObject("types").entrySet())
			{
				types.put(entry.getKey(), entry.getValue().getAsString());
				names.add(entry.getKey());
			}
		if(json.has("hierarchy"))
			for(Map.Entry<String, JsonElement> entry : json.getAsJsonObject("hierarchy").entrySet())
			{
				String parent = entry.getValue().getAsString();
				names.add(entry.getKey());
				if(!parent.isEmpty())
				{
					parents.put(entry.getKey(), parent);
					names.add(parent);
				}
			}
	}

	private static JsonObject validate(JsonObject json)
	{
		for(String key : new String[]{"origins", "hierarchy", "properties", "types"})
			if(json.has(key)&&!json.get(key).isJsonObject())
				throw new IllegalArgumentException("AMT "+key+" must be an object");
		if(json.has("origins"))
			for(Map.Entry<String, JsonElement> entry : json.getAsJsonObject("origins").entrySet())
				CTMBAMTResources.vector(entry.getValue(), 1); // Validate finite pixel vectors before native parsing.
		if(json.has("types"))
			for(Map.Entry<String, JsonElement> entry : json.getAsJsonObject("types").entrySet())
				if(!entry.getValue().isJsonPrimitive()||!entry.getValue().getAsJsonPrimitive().isString()
						||!TYPES.contains(entry.getValue().getAsString()))
					throw new IllegalArgumentException("Unknown AMT type for "+entry.getKey()+": "+entry.getValue());
		if(json.has("properties"))
			for(Map.Entry<String, JsonElement> entry : json.getAsJsonObject("properties").entrySet())
			{
				if(!entry.getValue().isJsonObject())
					throw new IllegalArgumentException("AMT part properties must be an object");
				JsonObject properties = entry.getValue().getAsJsonObject();
				for(String field : new String[]{"position", "rotation", "scale"})
					if(properties.has(field)) CTMBAMTResources.vector(properties.get(field), 1);
			}
		Map<String, String> hierarchy = new LinkedHashMap<>();
		if(json.has("hierarchy"))
			for(Map.Entry<String, JsonElement> entry : json.getAsJsonObject("hierarchy").entrySet())
			{
				if(!entry.getValue().isJsonPrimitive()||!entry.getValue().getAsJsonPrimitive().isString())
					throw new IllegalArgumentException("AMT parents must be names");
				hierarchy.put(entry.getKey(), entry.getValue().getAsString());
			}
		for(String child : hierarchy.keySet())
		{
			Set<String> visited = new HashSet<>();
			for(String part = child; part!=null&&!part.isEmpty(); part = hierarchy.get(part))
				if(!visited.add(part)) throw new IllegalArgumentException("Cyclic AMT hierarchy at "+part);
		}
		return json;
	}

	public pl.pabilo8.immersiveintelligence.common.util.easynbt.EasyNBT properties(String name)
	{
		return properties.get(name);
	}

	public Map<String, String> types()
	{
		return Collections.unmodifiableMap(types);
	}

	public Map<String, String> parents()
	{
		return Collections.unmodifiableMap(parents);
	}

	public Set<String> names()
	{
		return Collections.unmodifiableSet(names);
	}
}
