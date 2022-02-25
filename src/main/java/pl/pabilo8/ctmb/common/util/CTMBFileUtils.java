package pl.pabilo8.ctmb.common.util;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Deque;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Rightfully stolen From B.A.S.E./ContentTweaker, which borrowed from EnderCore<br>
 * <a href="https://github.com/The-Acronym-Coders/BASE/blob/develop/1.12.0/src/main/java/com/teamacronymcoders/base/util/files/BaseFileUtils.java">https://github.com/The-Acronym-Coders/BASE/blob/develop/1.12.0/src/main/java/com/teamacronymcoders/base/util/files/BaseFileUtils.java</a><br>
 * <a href="https://github.com/SleepyTrousers/EnderCore/blob/1.10/src/main/java/com/enderio/core/common/util/EnderFileUtils.java">https://github.com/SleepyTrousers/EnderCore/blob/1.10/src/main/java/com/enderio/core/common/util/EnderFileUtils.java</a><br>
 */
@SuppressWarnings("unused")
public class CTMBFileUtils
{
	/**
	 * @param jarClass A class from the jar in question
	 * @param filename Name of the file to copy, automatically prepended with "/assets/"
	 * @param to       File to copy to
	 */
	public static void copyFromJar(Class<?> jarClass, String filename, File to)
	{
		URL url = jarClass.getResource("/assets/"+filename);

		try
		{
			assert url!=null;
			FileUtils.copyURLToFile(url, to);
		}
		catch(IOException|NullPointerException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param directory The directory to zip the contents of. Content structure will be
	 *                  preserved.
	 * @param zipfile   The zip file to output to.
	 * @throws IOException if any file activity fails
	 * @author McDowell - <a href="http://stackoverflow.com/questions/1399126/java-util-zip-recreating-directory-structure">http://stackoverflow.com/questions/1399126/java-util-zip-recreating-directory-structure</a>
	 */
	@SuppressWarnings("resource")
	public static void zipFolderContents(File directory, File zipfile) throws IOException
	{
		URI base = directory.toURI();
		Deque<File> queue = new LinkedList<>();
		queue.push(directory);
		OutputStream out = new FileOutputStream(zipfile);
		Closeable res = out;
		try
		{
			ZipOutputStream zout = new ZipOutputStream(out);
			res = zout;
			while(!queue.isEmpty())
			{
				directory = queue.pop();
				File[] files = directory.listFiles();
				if(files!=null)
					for(File child : files)
					{
						String name = base.relativize(child.toURI()).getPath();
						if(child.isDirectory())
						{
							queue.push(child);
							name = name.endsWith("/")?name: name+"/";
							zout.putNextEntry(new ZipEntry(name));
						}
						else
						{
							zout.putNextEntry(new ZipEntry(name));
							copy(child, zout);
							zout.closeEntry();
						}
					}
			}
		} finally
		{
			res.close();
		}
	}

	/**
	 * @see #zipFolderContents(File, File)
	 */
	private static void copy(InputStream in, OutputStream out) throws IOException
	{
		byte[] buffer = new byte[1024];
		while(true)
		{
			int readCount = in.read(buffer);
			if(readCount < 0)
				break;
			out.write(buffer, 0, readCount);
		}
	}

	/**
	 * @see #zipFolderContents(File, File)
	 */
	private static void copy(File file, OutputStream out) throws IOException
	{
		try(InputStream in = new FileInputStream(file))
		{
			copy(in, out);
		}
	}

	public static boolean safeDelete(File file)
	{
		try
		{
			return file.delete();
		}
		catch(Exception e)
		{
			CTMBLogger.error("Deleting file "+file.getAbsolutePath()+" failed.");
		}
		return false;
	}

	public static void safeDeleteDirectory(File file)
	{
		try
		{
			FileUtils.deleteDirectory(file);
		}
		catch(Exception e)
		{
			CTMBLogger.error("Deleting directory "+file.getAbsolutePath()+" failed.");
		}
	}

	public static void createFolder(File file)
	{
		if(!file.exists()&&!file.mkdirs())
			CTMBLogger.error("Couldn't create folder called: "+file.getName());
	}

	public static String readFileToString(File file)
	{
		String string = "";
		try
		{
			string = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
		}
		catch(IOException e)
		{
			CTMBLogger.warn(e);
		}
		return string;
	}

	public static void writeStringToFile(String string, File file)
	{
		boolean exists = file.exists();
		if(!exists)
			try
			{
				file.getParentFile().mkdirs();
				exists = file.createNewFile();
			}
			catch(IOException e)
			{
				CTMBLogger.warn(e);
			}
		if(exists)
			try
			{
				FileUtils.writeStringToFile(file, string, java.nio.charset.StandardCharsets.UTF_8);
			}
			catch(IOException e)
			{
				CTMBLogger.warn(e);
			}
		else
			CTMBLogger.error("Couldn't create File: "+file.getName());
	}

	public static boolean createFile(File file)
	{
		createFolder(file.getParentFile());
		try
		{
			return file.createNewFile();
		}
		catch(IOException e)
		{
			CTMBLogger.error("Couldn't create File "+file.getName());
		}
		return false;
	}
}
