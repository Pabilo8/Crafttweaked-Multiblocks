package pl.pabilo8.ctmb.client.manual;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.AbstractResourcePack;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import pl.pabilo8.ctmb.common.manual.CTMBManualEntry;
import pl.pabilo8.ctmb.common.manual.ManualTweaker;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;

/**
 * Routes II manual file requests to registered CTMB resources.
 *
 * @author Pabilo8 (pabilo@iiteam.net)
 * @since 05.10.2026
 */
public class CTMBManualResourcePack extends AbstractResourcePack
{
	private static final String PREFIX = "assets/immersiveintelligence/ie_manual/";

	public CTMBManualResourcePack(File folder) { super(folder); }

	private ResourceLocation resolve(String name)
	{
		if(!name.startsWith(PREFIX)) return null;
		String path = name.substring(PREFIX.length());
		int separator = path.indexOf('/');
		if(separator < 0||!path.endsWith(".md")) return null;
		String key = path.substring(separator+1, path.length()-3);
		CTMBManualEntry entry = ManualTweaker.ENTRIES.get(key);
		return entry==null?null:new ResourceLocation(entry.getResource().replace("{lang}", path.substring(0, separator)));
	}

	@Override
	@Nonnull
	protected InputStream getInputStreamByName(@Nonnull String name) throws IOException
	{
		if(name.equals("pack.mcmeta"))
			return new java.io.ByteArrayInputStream("{\"pack\":{\"pack_format\":3,\"description\":\"CTMB manual resource adapter\"}}".getBytes(java.nio.charset.StandardCharsets.UTF_8));
		ResourceLocation source = resolve(name);
		if(source==null) throw new FileNotFoundException(name);
		return Minecraft.getMinecraft().getResourceManager().getResource(source).getInputStream();
	}

	@Override
	protected boolean hasResourceName(@Nonnull String name)
	{
		if(name.equals("pack.mcmeta")) return true;
		ResourceLocation source = resolve(name);
		if(source==null) return false;
		try(IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(source))
		{
			return true;
		}
		catch(IOException ignored) { return false; }
	}

	@Override
	@Nonnull
	public Set<String> getResourceDomains() { return Collections.singleton("immersiveintelligence"); }
}
