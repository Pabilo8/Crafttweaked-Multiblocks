package pl.pabilo8.ctmb.client.render;

import net.minecraftforge.client.model.obj.OBJModel;
import pl.pabilo8.ctmb.common.amt.CTMBAMTHeader;
import pl.pabilo8.immersiveintelligence.client.util.amt.models.AMTModel;
import pl.pabilo8.immersiveintelligence.client.util.amt.parts.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builds typed placeholders once and replaces named OBJ groups without duplicate AMT names.
 */
public final class CTMBAMTParts
{
	private CTMBAMTParts()
	{
	}

	public static AMTModel create(OBJModel obj, CTMBAMTHeader header)
	{
		AMTModel geometry = new AMTModel((net.minecraft.block.state.IBlockState)null, obj, null, null);
		Map<String, AMT> parts = new LinkedHashMap<>();
		for(AMT part : geometry) parts.put(part.getName(), part);
		for(String name : header.names())
		{
			String type = header.types().get(name);
			if(type==null||type.equals("quads"))
			{
				if(type!=null&&!parts.containsKey(name))
					throw new IllegalArgumentException("AMT quads part has no OBJ geometry: "+name);
				if(!parts.containsKey(name)) parts.put(name, new AMTLocator(name, header));
				continue;
			}
			AMT replacement;
			switch(type)
			{
				case "locator":
					replacement = new AMTLocator(name, header);
					break;
				case "item":
					replacement = new AMTItem(name, header);
					break;
				case "fluid":
					replacement = new AMTFluid(name, header);
					break;
				case "text":
					replacement = new AMTText(name, header);
					break;
				case "wire":
					replacement = new AMTWire(name, header);
					break;
				case "particle":
					replacement = new AMTParticle(name, header);
					break;
				case "bullet":
					replacement = new AMTBullet(name, header, null);
					break;
				case "banner":
					replacement = new AMTBanner(name, header);
					break;
				case "blend_mode":
					replacement = new AMTBlendModeGroup(name, header);
					break;
				default:
					throw new IllegalArgumentException("Unsupported AMT type "+type);
			}
			AMT previous = parts.put(name, replacement);
			if(previous!=null) previous.disposeOf();
		}
		// Rebuild quads with header pivots before attaching typed parts and hierarchy.
		for(Map.Entry<String, AMT> entry : parts.entrySet())
			if(entry.getValue() instanceof AMTQuads)
			{
				AMTQuads old = (AMTQuads)entry.getValue();
				entry.setValue(new AMTQuads(entry.getKey(), header.getOffset(entry.getKey()), old.getQuads()));
				old.disposeOf();
			}
		header.applyHierarchy(parts.values());
		return new AMTModel(parts.values().stream().filter(part -> !part.isChild()).toArray(AMT[]::new));
	}
}
