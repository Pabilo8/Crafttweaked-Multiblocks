package pl.pabilo8.ctmb.common.storage;

import blusunrize.immersiveengineering.common.util.EnergyHelper;
import com.google.gson.JsonParser;
import net.minecraft.block.material.Material;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pl.pabilo8.ctmb.common.block.MultiblockDefinition;
import pl.pabilo8.ctmb.common.block.TileEntityMultiblock;
import pl.pabilo8.ctmb.common.block.crafttweaker.Multiblock;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Port regressions use the unchanged IIToolkit example, not rewritten rotations.
 */
class MelterPortTest
{
	private static int sequence;

	@BeforeAll
	static void bootstrap()
	{
		Bootstrap.register();
	}

	private static final class TestMultiblock extends Multiblock
	{
		TestMultiblock(MultiblockDefinition definition)
		{
			super("test:melter_ports_"+(++sequence), new ResourceLocation("test:unused"), Material.IRON, definition);
		}

		@Override
		public int[] getSize()
		{
			return new int[]{3, 4, 3}; // Tile dimensions are Y, Z, X.
		}
	}

	private static final class TestTile extends TileEntityMultiblock
	{
		private final TileEntityMultiblock owner;

		TestTile(Multiblock mb, TileEntityMultiblock owner)
		{
			super(mb);
			this.owner = owner;
			formed = true;
			offset = owner==null?new int[3]: new int[]{1, 0, 0};
			setWorld(mock(World.class));
		}

		@Override
		public TileEntityMultiblock master()
		{
			return owner==null?this: owner;
		}
	}

	private TestTile fixture(EnumFacing facing, boolean mirrored) throws IOException
	{
		MultiblockDefinition definition;
		InputStream resource = getClass().getResourceAsStream("/multiblocks/melter.json");
		assertNotNull(resource, "Missing authoritative IIToolkit test fixture");
		try(Reader input = new InputStreamReader(resource, StandardCharsets.UTF_8))
		{
			definition = MultiblockDefinition.fromJson(new ResourceLocation("ctmb:multiblocks/melter"), new JsonParser().parse(input).getAsJsonObject());
		}
		Multiblock mb = new TestMultiblock(definition);
		mb.setItemStorage("input_storage").withSize(1).withInputPort("input");
		mb.setEnergyStorage("power").withSize(16000).withInputPort("energy_in");
		mb.setFluidStorage("fluid").withSize(4000).withOutputPort("fluid_out");
		mb.freeze();
		TestTile master = new TestTile(mb, null);
		master.pos = 1;
		master.facing = facing;
		master.mirrored = mirrored;
		return master;
	}

	@Test
	void exposedEnergyFaceChargesMasterThroughIEAndForgeWrapper() throws IOException
	{
		for(EnumFacing facing : EnumFacing.HORIZONTALS)
			for(boolean mirrored : new boolean[]{false, true})
			{
				TestTile master = fixture(facing, mirrored);
				TestTile port = new TestTile(master.getMultiblock(), master);
				port.pos = 29; // Local [2,2,1], whose exposed face is +X.
				port.facing = facing;
				port.mirrored = mirrored;
				EnumFacing exposed = mirrored?facing.rotateYCCW(): facing.rotateY();
				for(EnumFacing side : EnumFacing.values())
				{
					assertEquals(side==exposed, port.canConnectEnergy(side));
					if(side!=exposed) assertEquals(0, EnergyHelper.insertFlux(port, side, 100, false));
				}
				assertEquals(0, port.receiveEnergy(null, 100, false));
				assertTrue(EnergyHelper.isFluxReceiver(port, exposed));
				assertEquals(100, EnergyHelper.insertFlux(port, exposed, 100, true));
				assertEquals(0, master.getStorageSystem().get("power").getEnergy());
				assertEquals(100, EnergyHelper.insertFlux(port, exposed, 100, false));
				EnergyHelper.IEForgeEnergyWrapper wrapper = port.getCapabilityWrapper(exposed);
				assertNotNull(wrapper);
				assertTrue(wrapper.canReceive());
				assertFalse(wrapper.canExtract());
				assertEquals(15900, wrapper.receiveEnergy(20000, true));
				assertEquals(100, master.getStorageSystem().get("power").getEnergy());
				assertEquals(15900, wrapper.receiveEnergy(20000, false));
				assertEquals(16000, master.getStorageSystem().get("power").getEnergy());
				assertEquals(0, wrapper.receiveEnergy(1, false));
				assertEquals(0, wrapper.extractEnergy(100, false));
				port.pos = 13;
				assertFalse(port.canConnectEnergy(exposed));
				port.pos = 29;
				port.formed = false;
				assertEquals(0, port.receiveEnergy(exposed, 100, false));
			}
	}

	@Test
	void originalItemFaceAndExportedFluidFaceExposeOnlyTheirProvider() throws IOException
	{
		for(EnumFacing facing : EnumFacing.HORIZONTALS)
			for(boolean mirrored : new boolean[]{false, true})
			{
				StorageSystem storage = fixture(facing, mirrored).getStorageSystem();
				EnumFacing fluidFace = mirrored?facing.rotateY(): facing.rotateYCCW();
				for(EnumFacing side : EnumFacing.values())
				{
					assertEquals(side==facing.getOpposite(), storage.view(13, side).has(StorageDefinition.Kind.ITEM));
					assertEquals(side==fluidFace, storage.view(21, side).has(StorageDefinition.Kind.FLUID));
					assertFalse(storage.view(13, side).has(StorageDefinition.Kind.ENERGY));
					assertFalse(storage.view(21, side).has(StorageDefinition.Kind.ENERGY));
				}
			}
	}
}
