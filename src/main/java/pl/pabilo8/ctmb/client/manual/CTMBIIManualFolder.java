package pl.pabilo8.ctmb.client.manual;

import blusunrize.immersiveengineering.api.ManualHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import pl.pabilo8.immersiveintelligence.client.manual.pages.IIManualPageFolder;

import java.util.List;

/**
 * Provides native II folders with valid parent navigation references.
 *
 * @author Pabilo8 (pabilo@iiteam.net)
 * @since 05.10.2026
 */
public class CTMBIIManualFolder extends IIManualPageFolder
{
	private final String referencePath;
	private final boolean root;

	public CTMBIIManualFolder(String id, String category)
	{
		super(ManualHelper.getManual(), id, category);
		referencePath = id;
		root = true;
	}

	@SuppressWarnings("deprecation")
	public CTMBIIManualFolder(String id, CTMBIIManualFolder parent)
	{
		super(ManualHelper.getManual(), id, parent);
		referencePath = parent.referencePath+"/"+id;
		root = false;
		//II exposes folder creation but always creates its own base class.
		List<IIManualPageFolder> siblings = ReflectionHelper.getPrivateValue(IIManualPageFolder.class, parent, "subFolders");
		siblings.add(this);
	}

	@Override
	public String getName()
	{
		return root?referencePath: "folder:"+referencePath;
	}
}
