package pl.pabilo8.ctmb.common.amt;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import pl.pabilo8.ctmb.common.CommonProxy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Common resource access: external CTMB assets first, packaged assets second.
 */
public final class CTMBAMTResources
{
	private CTMBAMTResources()
	{
	}

	public static JsonObject read(ResourceLocation resource)
	{
		String path = resource.getResourcePath();
		if(path.startsWith("/")||path.contains("..")||path.contains("\\"))
			throw new IllegalArgumentException("Invalid AMT resource path "+resource);
		File root = CommonProxy.RESOURCE_LOADER.getResourceFolder();
		File file = root==null?null: new File(root, resource.getResourceDomain()+"/"+path);
		try(InputStream stream = file!=null&&file.isFile()?Files.newInputStream(file.toPath()):
				CTMBAMTResources.class.getResourceAsStream("/assets/"+resource.getResourceDomain()+"/"+path))
		{
			if(stream==null) throw new IOException("Missing AMT resource "+resource);
			return new JsonParser().parse(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
		} catch(IOException|RuntimeException exception)
		{
			throw new IllegalArgumentException("Cannot load "+resource+": "+exception.getMessage(), exception);
		}
	}

	public static ResourceLocation animation(ResourceLocation id)
	{
		return new ResourceLocation(id.getResourceDomain(), "animations/"+id.getResourcePath()+".json");
	}

	public static Vec3d vector(JsonElement value, double scale)
	{
		if(!value.isJsonArray()||value.getAsJsonArray().size()!=3)
			throw new IllegalArgumentException("Expected three coordinates");
		JsonArray array = value.getAsJsonArray();
		for(JsonElement number : array)
			if(!number.isJsonPrimitive()||!number.getAsJsonPrimitive().isNumber()||!Double.isFinite(number.getAsDouble()))
				throw new IllegalArgumentException("AMT coordinates must be finite numbers");
		return new Vec3d(array.get(0).getAsDouble(), array.get(1).getAsDouble(), array.get(2).getAsDouble()).scale(scale);
	}
}
