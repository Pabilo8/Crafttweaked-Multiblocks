package pl.pabilo8.ctmb.common.util;

import net.minecraft.client.resources.AbstractResourcePack;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Rightfully stolen from Contenttweaker/B.A.S.E.<br>
 * <a href="https://github.com/The-Acronym-Coders/BASE/blob/develop/1.12.0/src/main/java/com/teamacronymcoders/base/util/files/DirectoryResourcePack.java">https://github.com/The-Acronym-Coders/BASE/blob/develop/1.12.0/src/main/java/com/teamacronymcoders/base/util/files/DirectoryResourcePack.java</a>
 * @since 19.02.2022
 */
public class DirectoryResourcePack extends AbstractResourcePack
{
	private Set<String> domains;

	public DirectoryResourcePack(File resourceFolder)
	{
		super(resourceFolder);
	}

	@Override
	@Nonnull
	protected InputStream getInputStreamByName(@Nonnull String name) throws IOException
	{
		return Files.newInputStream(this.getFile(name).toPath());
	}

	@Override
	protected boolean hasResourceName(@Nonnull String name)
	{
		return this.getFile(name).exists();
	}

	private File getFile(String name)
	{
		return new File(this.resourcePackFile, name.replace("assets/", ""));
	}

	@Override
	@Nonnull
	public Set<String> getResourceDomains()
	{
		if(domains==null)
		{
			this.domains = new HashSet<>();
			String[] folderNames = this.resourcePackFile.list();
			if(folderNames!=null)
			{
				domains.addAll(Arrays.asList(folderNames));
			}
		}
		return domains;
	}
}
