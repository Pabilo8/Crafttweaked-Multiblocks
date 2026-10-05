package pl.pabilo8.ctmb.common.block;

import com.google.gson.*;
import net.minecraft.block.material.Material;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3i;
import pl.pabilo8.ctmb.common.CommonProxy;
import pl.pabilo8.immersiveintelligence.common.util.IIStringUtil;
import pl.pabilo8.immersiveintelligence.common.util.raytracer.AxisAlignedFacingBB;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * Common-side JSON definition, using II's pixel bounds and position-list syntax.
 */
public final class MultiblockDefinition
{
	public final String name;
	public final ResourceLocation resource, structure;
	public final Vec3i master;
	public final Material material;
	private final Map<Integer, List<AxisAlignedFacingBB>> bounds = new HashMap<>();
	private final Map<String, int[]> points = new LinkedHashMap<>();
	private final Map<String, Direction> directions = new LinkedHashMap<>();

	private final JsonObject extensionData;

	private MultiblockDefinition(ResourceLocation resource, JsonObject json)
	{
		this.resource = resource;
		for(String field : new String[]{"name", "master", "bounds", "positions"})
			if(!json.has(field)) fail("Missing required field "+field);
		if(!json.get("name").isJsonPrimitive()||!json.get("name").getAsJsonPrimitive().isString())
			fail("name must be a string");
		if(!json.get("master").isJsonArray()||!json.get("bounds").isJsonObject()||!json.get("positions").isJsonObject())
			fail("Invalid master/bounds/positions types");
		extensionData = new JsonParser().parse(json.toString()).getAsJsonObject();
		name = json.get("name").getAsString();
		if(!name.matches("[A-Za-z0-9_.-]+:[A-Za-z0-9_./-]+")) fail("Invalid multiblock name: "+name);
		structure = json.has("structure")?new ResourceLocation(json.get("structure").getAsString()): resource;
		JsonArray offset = json.getAsJsonArray("master");
		if(offset==null||offset.size()!=3) fail("master must have three integer coordinates");
		master = new Vec3i(integer(offset.get(0)), integer(offset.get(1)), integer(offset.get(2)));
		String mat = json.has("material")?json.get("material").getAsString(): "iron";
		switch(mat)
		{
			case "iron":
				material = Material.IRON;
				break;
			case "wood":
				material = Material.WOOD;
				break;
			case "rock":
				material = Material.ROCK;
				break;
			case "glass":
				material = Material.GLASS;
				break;
			default:
				throw new IllegalArgumentException(resource+": Unknown material "+mat);
		}
		Map<String, AxisAlignedFacingBB> dictionary = new HashMap<>();
		for(Map.Entry<String, JsonElement> entry : json.getAsJsonObject("bounds").entrySet())
		{
			JsonArray box = entry.getValue().getAsJsonArray();
			if(box.size()!=6) fail("Bound "+entry.getKey()+" must contain six numbers");
			for(JsonElement value : box)
				if(!value.isJsonPrimitive()||!value.getAsJsonPrimitive().isNumber()||!Double.isFinite(value.getAsDouble()))
					fail("Bound coordinates must be finite numbers");
			for(int i = 0; i < 3; i++)
				if(box.get(i).getAsDouble() > box.get(i+3).getAsDouble()) fail("Inverted bound "+entry.getKey());
			dictionary.put(entry.getKey(), new AxisAlignedFacingBB(box));
		}
		for(Map.Entry<String, JsonElement> entry : json.getAsJsonObject("positions").entrySet())
		{
			List<AxisAlignedFacingBB> group = new ArrayList<>();
			for(JsonElement value : list(entry.getValue()))
			{
				AxisAlignedFacingBB box = dictionary.get(value.getAsString());
				if(box==null) fail("Unknown bound "+value.getAsString());
				AxisAlignedBB north = box.getFacing(EnumFacing.NORTH, false);
				// II's zero-volume "none" sentinel represents no selectable/collidable volume.
				if(north.maxX > north.minX&&north.maxY > north.minY&&north.maxZ > north.minZ) group.add(box);
			}
			for(int position : positions(new JsonPrimitive(entry.getKey())))
			{
				if(bounds.putIfAbsent(position, Collections.unmodifiableList(group))!=null)
					fail("Duplicate bounds for position "+position);
			}
		}
		if(json.has("poi")) for(Map.Entry<String, JsonElement> entry : json.getAsJsonObject("poi").entrySet())
			points.put(entry.getKey(), positions(entry.getValue()));
		for(String field : new String[]{"rotations", "directions"})
			if(json.has(field))
				for(Map.Entry<String, JsonElement> entry : json.getAsJsonObject(field).entrySet())
				{
					String direction = entry.getValue().getAsString().toLowerCase(Locale.ROOT);
					if(!Arrays.asList("none", "clockwise_90", "clockwise_180", "counterclockwise_90", "up", "down").contains(direction))
						fail("Unknown direction "+direction);
					if(directions.putIfAbsent(entry.getKey(), Direction.parse(direction))!=null)
						fail("Duplicate direction "+entry.getKey());
				}
	}

	public static MultiblockDefinition load(String name)
	{
		ResourceLocation resource = new ResourceLocation(name.replaceFirst("\\.json$", ""));
		File file = new File(CommonProxy.RESOURCE_LOADER.getResourceFolder(), resource.getResourceDomain()+"/"+resource.getResourcePath()+".json");
		try(InputStream input = file.isFile()?Files.newInputStream(file.toPath()): MultiblockDefinition.class.getResourceAsStream("/assets/"+resource.getResourceDomain()+"/"+resource.getResourcePath()+".json"))
		{
			if(input==null) throw new IOException("Missing definition "+file);
			JsonObject json = new JsonParser().parse(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
			return fromJson(resource, json);
		} catch(IOException|RuntimeException e)
		{
			throw new IllegalArgumentException("Cannot load CTMB multiblock "+resource+": "+e.getMessage(), e);
		}
	}

	/**
	 * Native entry point for future geometry extensions and definition validation tools.
	 */
	public static MultiblockDefinition fromJson(ResourceLocation resource, JsonObject json)
	{
		return new MultiblockDefinition(resource, json);
	}

	private void fail(String message)
	{
		throw new IllegalArgumentException(resource+": "+message);
	}

	private static int integer(JsonElement value)
	{
		if(!value.isJsonPrimitive()||!value.getAsJsonPrimitive().isNumber())
			throw new IllegalArgumentException("Expected an integer: "+value);
		double n = value.getAsDouble();
		if(!Double.isFinite(n)||n!=Math.rint(n)||n < 0||n > Integer.MAX_VALUE)
			throw new IllegalArgumentException("Expected a non-negative integer: "+value);
		return (int)n;
	}

	private static List<JsonElement> list(JsonElement value)
	{
		List<JsonElement> result = new ArrayList<>();
		if(value.isJsonArray()) value.getAsJsonArray().forEach(result::add);
		else result.add(value);
		return result;
	}

	private static int[] positions(JsonElement value)
	{
		return list(value).stream().flatMap(v -> {
					String text = v.getAsString().replace(" ", "");
					if(!text.matches("[0-9]+(:[0-9]+)?(,[0-9]+(:[0-9]+)?)*"))
						throw new IllegalArgumentException("Invalid position list: "+v);
					// II's utility silently returns zero on malformed/overflowing numbers: validate first.
					for(String number : text.split("[:,]")) Integer.parseInt(number);
					return Arrays.stream(IIStringUtil.parseNumberListString(text));
				})
				.mapToInt(Integer::intValue).peek(n -> {
					if(n < 0) throw new IllegalArgumentException("Negative position");
				}).distinct().sorted().toArray();
	}

	public void validate(int[] size)
	{
		long count = (long)size[0]*size[1]*size[2];
		if(master.getY() >= size[0]||master.getZ() >= size[1]||master.getX() >= size[2])
			fail("master is outside the NBT structure");
		for(int p : bounds.keySet()) if(p >= count) fail("Bounds position outside the NBT structure: "+p);
		points.forEach((name, values) -> {
			for(int p : values) if(p >= count) fail("POI "+name+" is outside the NBT structure: "+p);
		});
	}

	public boolean hasPOI(String name)
	{
		return points.containsKey(name)&&points.get(name).length > 0;
	}

	public boolean hasDirection(String name)
	{
		return directions.containsKey(name);
	}

	public int[] getPOI(String name)
	{
		if(!points.containsKey(name)) fail("Unknown POI "+name);
		return points.get(name).clone();
	}

	public boolean isPOI(String name, int position)
	{
		return Arrays.binarySearch(getPOI(name), position) >= 0;
	}

	public Direction getDirection(String name)
	{
		Direction direction = directions.get(name);
		if(direction==null)
		{
			fail("Missing direction "+name);
			return null;
		}
		return direction;
	}

	public EnumFacing direction(String name, EnumFacing facing, boolean mirrored)
	{
		return getDirection(name).resolve(facing, mirrored);
	}

	/**
	 * Future modules receive their own copy; Tactile sections are retained without interpretation.
	 */
	public JsonObject extensionData()
	{
		return new JsonParser().parse(extensionData.toString()).getAsJsonObject();
	}

	public List<AxisAlignedBB> bounds(int position, EnumFacing facing, boolean mirrored)
	{
		List<AxisAlignedFacingBB> boxes = bounds.get(position);
		if(boxes==null) return Collections.singletonList(new AxisAlignedBB(0, 0, 0, 1, 1, 1));
		List<AxisAlignedBB> result = new ArrayList<>();
		for(AxisAlignedFacingBB box : boxes) result.add(box.getFacing(facing, mirrored));
		return result;
	}
}
