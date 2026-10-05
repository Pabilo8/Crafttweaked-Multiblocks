package pl.pabilo8.ctmb.client.manual;

import pl.pabilo8.ctmb.common.manual.CTMBManualEntry;
import pl.pabilo8.ctmb.common.manual.CTMBManualPage;
import pl.pabilo8.immersiveintelligence.client.manual.IIManualEntry;

/**
 * Keeps II manual links consistent with scripted section order.
 *
 * @author Pabilo8 (pabilo@iiteam.net)
 * @since 05.10.2026
 */
public class CTMBIIManualEntry extends IIManualEntry
{
	private final CTMBManualPage[] sections;

	public CTMBIIManualEntry(CTMBManualEntry definition)
	{
		super(definition.getName(), definition.getCategory());
		sections = definition.getPages();
	}

	@Override
	public int getSubPageID(String name)
	{
		if(sections.length==0) return super.getSubPageID(name);
		for(int i = 0; i < sections.length; i++)
			if(sections[i].getName().equals(name)) return i;
		return -1;
	}
}
