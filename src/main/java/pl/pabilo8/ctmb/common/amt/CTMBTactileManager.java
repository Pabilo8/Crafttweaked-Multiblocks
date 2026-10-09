package pl.pabilo8.ctmb.common.amt;

import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import pl.pabilo8.ctmb.common.block.TileEntityMultiblock;
import pl.pabilo8.ctmb.common.util.CTMBLogger;
import pl.pabilo8.immersiveintelligence.common.IIConfigHandler.IIConfig.Graphics;
import pl.pabilo8.immersiveintelligence.common.entity.tactile.EntityAMTTactile;
import pl.pabilo8.immersiveintelligence.common.entity.tactile.TactileManager;
import pl.pabilo8.immersiveintelligence.common.util.ResLoc;
import pl.pabilo8.immersiveintelligence.common.util.amt.IIAnimation;
import pl.pabilo8.immersiveintelligence.common.util.amt.IIAnimationCollisionMap;

import java.util.*;

/**
 * External-resource adapter for the released II manager. Uses native entities and collision maps.
 * Own pose calculation follows AMT's Y/Z/-X transform order and supports rotated/scaled AABBs.
 */
public final class CTMBTactileManager extends TactileManager
{
	private final TileEntityMultiblock tile;
	private final ResourceLocation source;
	private final Map<String, EntityAMTTactile> nodes = new LinkedHashMap<>();
	private final Map<String, String> parents = new LinkedHashMap<>();
	private final Map<EntityAMTTactile, String> partNames = new IdentityHashMap<>();
	private final List<Box> boxes = new ArrayList<>();
	private final Map<ResLoc, IIAnimationCollisionMap> animations = new HashMap<>();
	private boolean initialized, failed;
	private CTMBAMTHeader header;
	private EnumFacing loadedFacing;
	private boolean loadedMirror;

	public CTMBTactileManager(TileEntityMultiblock tile, ResourceLocation source)
	{
		super(ResLoc.of(source), tile, tile::getWorld, tile::getPos, () -> tile.facing, () -> tile.mirrored);
		this.tile = tile;
		this.source = source;
	}

	public String partName(EntityAMTTactile entity)
	{
		return partNames.getOrDefault(entity, entity.name);
	}

	private boolean initialize()
	{
		if(tile.getWorld().isRemote||!Graphics.tactileAMT||failed) return false;
		if(initialized&&(loadedFacing!=tile.facing||loadedMirror!=tile.mirrored)) forceReload();
		if(initialized) return true;
		try
		{
			JsonObject json = CTMBAMTResources.read(source);
			JsonObject tactile = json.getAsJsonObject("tactile");
			if(tactile==null||!tactile.has("_schema"))
				throw new IllegalArgumentException("Tactile source requires tactile._schema");
			header = new CTMBAMTHeader(CTMBAMTResources.read(new ResourceLocation(tactile.get("_schema").getAsString())));
			parents.putAll(header.parents());
			Set<String> names = new LinkedHashSet<>(header.names());
			tactile.entrySet().stream().filter(entry -> !entry.getKey().startsWith("_")).forEach(entry -> names.add(entry.getKey()));
			for(String name : names)
			{
				EntityAMTTactile entity = new EntityAMTTactile(this, name, header.getOffset(name), new AxisAlignedBB(0, 0, 0, 0, 0, 0));
				nodes.put(name, entity);
				getEntities().add(entity);
				partNames.put(entity, name);
			}
			for(Map.Entry<String, JsonElement> entry : tactile.entrySet())
			{
				if(entry.getKey().startsWith("_")) continue;
				if(!entry.getValue().isJsonArray())
					throw new IllegalArgumentException("Tactile part must contain an array of boxes: "+entry.getKey());
				int index = 0;
				for(JsonElement element : entry.getValue().getAsJsonArray())
				{
					JsonObject box = element.getAsJsonObject();
					JsonElement bounds = box.get("bounds");
					if(box.has("type"))
					{
						if(!json.has("bounds")) throw new IllegalArgumentException("Missing tactile bounds dictionary");
						bounds = json.getAsJsonObject("bounds").get(box.get("type").getAsString());
					}
					if(bounds==null)
						throw new IllegalArgumentException("Unknown or missing tactile bounds for "+entry.getKey());
					Vec3d offset = box.has("offset")?CTMBAMTResources.vector(box.get("offset"), 1/16d): Vec3d.ZERO;
					EntityAMTTactile entity = new EntityAMTTactile(this, entry.getKey()+"_child"+(index++), header.getOffset(entry.getKey()), bounds(bounds));
					entity.setParent(nodes.get(entry.getKey()));
					getEntities().add(entity);
					partNames.put(entity, entry.getKey());
					boxes.add(new Box(entry.getKey(), entity, entity.aabb, offset));
				}
			}
			// II 0.3.1 makes ownership private and has no registration API. Register our native
			// entities in its existing set, so its tracker/collision/interaction code owns them.
			managed().addAll(getEntities());
			initialized = true;
			loadedFacing = tile.facing;
			loadedMirror = tile.mirrored;
			resetPose();
			applyPose(true);
			for(EntityAMTTactile entity : getEntities()) tile.getWorld().spawnEntity(entity);
			synchronizeEntities(); // II spawn data omits visibility and dimensions.
			return true;
		} catch(RuntimeException exception)
		{
			forceReload();
			failed = true;
			CTMBLogger.error("Cannot initialize CTMB tactiles "+source+": "+exception.getMessage());
			return false;
		}
	}

	@SuppressWarnings("deprecation")
	private Set<EntityAMTTactile> managed()
	{
		return ReflectionHelper.getPrivateValue(TactileManager.class, this, "managedEntities");
	}

	static AxisAlignedBB bounds(JsonElement value)
	{
		if(!value.isJsonArray()||value.getAsJsonArray().size()!=6)
			throw new IllegalArgumentException("Tactile bounds require six pixel coordinates");
		JsonArray array = value.getAsJsonArray();
		for(JsonElement number : array)
			if(!number.isJsonPrimitive()||!number.getAsJsonPrimitive().isNumber()||!Double.isFinite(number.getAsDouble()))
				throw new IllegalArgumentException("Tactile bounds must be finite");
		for(int axis = 0; axis < 3; axis++)
			if(array.get(axis).getAsDouble() > array.get(axis+3).getAsDouble())
				throw new IllegalArgumentException("Inverted tactile bounds");
		return new AxisAlignedBB(array.get(0).getAsDouble()/16, array.get(1).getAsDouble()/16, array.get(2).getAsDouble()/16,
				array.get(3).getAsDouble()/16, array.get(4).getAsDouble()/16, array.get(5).getAsDouble()/16);
	}

	@Override
	public EntityAMTTactile getPart(String name)
	{
		return initialize()?nodes.get(name): null;
	}

	@Override
	public void defaultize()
	{
		if(initialize())
		{
			resetPose();
			applyPose(false);
		}
	}

	@Override
	public void update(ResLoc animation, float time)
	{
		update(animation==null?new ResLoc[0]: new ResLoc[]{animation}, animation==null?new float[0]: new float[]{time});
	}

	@Override
	public void update(ResLoc[] locations, float[] times)
	{
		if(locations.length!=times.length) throw new IllegalArgumentException("Animation/time counts differ");
		if(!initialize()) return;
		resetPose();
		for(int index = 0; index < locations.length; index++)
		{
			if(!Float.isFinite(times[index]))
				throw new IllegalArgumentException("Tactile animation time must be finite");
			if(locations[index]==null) continue;
			final ResLoc location = locations[index];
			if(!animations.containsKey(location))
				try
				{
					IIAnimation animation = new IIAnimation(location, CTMBAMTResources.read(CTMBAMTResources.animation(location)));
					// Canonical conversion supplies AMT translation (-X,Y,Z) and Euler (-X,-Y,Z).
					// Machine facing/mirroring are applied once by the shared outer transform.
					animations.put(location, IIAnimationCollisionMap.create(new ArrayList<>(nodes.values()), animation, EnumFacing.SOUTH, false));
				} catch(RuntimeException exception)
				{
					animations.put(location, null);
					CTMBLogger.error("Cannot load CTMB tactile animation "+location+": "+exception.getMessage());
				}
			IIAnimationCollisionMap animation = animations.get(location);
			if(animation!=null) animation.apply(times[index]);
		}
		applyPose(false);
	}

	private void resetPose()
	{
		nodes.values().forEach(entity -> {
			entity.defaultizeAnimation();
			entity.scale = new Vec3d(1, 1, 1);
			pl.pabilo8.immersiveintelligence.common.util.easynbt.EasyNBT properties = header.properties(entity.name);
			if(properties!=null)
			{
				properties.checkSetBoolean("visible", value -> entity.visibility = value);
				properties.checkSetVec3D("position", value -> entity.translation = new Vec3d(-value.x, value.y, value.z));
				properties.checkSetVec3D("rotation", value -> entity.rotation = new Vec3d(-value.x, -value.y, value.z));
				properties.checkSetVec3D("scale", value -> entity.scale = value);
			}
		});
	}

	private Matrix4 pose(String name, Map<String, Matrix4> poses, Map<String, Boolean> visibility)
	{
		if(poses.containsKey(name)) return poses.get(name);
		String parent = parents.get(name);
		Matrix4 matrix = parent==null?CTMBModelTransform.matrix(tile.facing, tile.mirrored): pose(parent, poses, visibility).copy();
		EntityAMTTactile entity = nodes.get(name);
		Vec3d origin = entity.offset;
		matrix.translate(entity.translation.x, entity.translation.y, entity.translation.z)
				.translate(origin.x, origin.y, origin.z)
				.rotate(Math.toRadians(-entity.rotation.y), 0, 1, 0)
				.rotate(Math.toRadians(entity.rotation.z), 0, 0, 1)
				.rotate(Math.toRadians(entity.rotation.x), 1, 0, 0)
				.scale(entity.scale.x, entity.scale.y, entity.scale.z)
				.translate(-origin.x, -origin.y, -origin.z);
		poses.put(name, matrix);
		visibility.put(name, entity.visibility&&(parent==null||visibility.get(parent)));
		return matrix;
	}

	private void applyPose(boolean initial)
	{
		Map<String, Matrix4> poses = new HashMap<>();
		Map<String, Boolean> visibility = new HashMap<>();
		for(Map.Entry<String, EntityAMTTactile> entry : nodes.entrySet())
		{
			Matrix4 matrix = pose(entry.getKey(), poses, visibility);
			move(entry.getValue(), matrix.apply(entry.getValue().offset), entry.getValue().aabb, visibility.get(entry.getKey()), initial);
		}
		for(Box box : boxes)
		{
			Matrix4 matrix = poses.get(box.part);
			Vec3d pivot = nodes.get(box.part).offset.add(box.offset);
			Vec3d position = matrix.apply(pivot);
			AxisAlignedBB transformed = transformBounds(box.bounds.offset(pivot), matrix).offset(-position.x, -position.y, -position.z);
			move(box.entity, position, transformed, visibility.get(box.part), initial);
		}
	}

	static AxisAlignedBB transformBounds(AxisAlignedBB box, Matrix4 matrix)
	{
		AxisAlignedBB result = null;
		for(int x = 0; x < 2; x++)
			for(int y = 0; y < 2; y++)
				for(int z = 0; z < 2; z++)
				{
					Vec3d point = matrix.apply(new Vec3d(x==0?box.minX: box.maxX, y==0?box.minY: box.maxY, z==0?box.minZ: box.maxZ));
					AxisAlignedBB corner = new AxisAlignedBB(point, point);
					result = result==null?corner: result.union(corner);
				}
		return result;
	}

	private void move(EntityAMTTactile entity, Vec3d local, AxisAlignedBB bounds, boolean visible, boolean initial)
	{
		boolean changed = entity.visibility!=visible||!entity.aabb.equals(bounds);
		entity.prevPosX = entity.posX;
		entity.prevPosY = entity.posY;
		entity.prevPosZ = entity.posZ;
		entity.aabb = bounds;
		entity.visibility = visible;
		entity.width = (float)Math.max(bounds.maxX-bounds.minX, bounds.maxZ-bounds.minZ);
		entity.height = (float)(bounds.maxY-bounds.minY);
		entity.setPosition(tile.getPos().getX()+local.x, tile.getPos().getY()+local.y, tile.getPos().getZ()+local.z);
		entity.motionX = initial?0: entity.posX-entity.prevPosX;
		entity.motionY = initial?0: entity.posY-entity.prevPosY;
		entity.motionZ = initial?0: entity.posZ-entity.prevPosZ;
		if(initial)
		{
			entity.prevPosX = entity.lastTickPosX = entity.posX;
			entity.prevPosY = entity.lastTickPosY = entity.posY;
			entity.prevPosZ = entity.lastTickPosZ = entity.posZ;
		}
		else if(changed) entity.synchronize();
	}

	@Override
	public void forceReload()
	{
		getEntities().forEach(EntityAMTTactile::setDead);
		getEntities().clear();
		managed().clear();
		nodes.clear();
		parents.clear();
		boxes.clear();
		partNames.clear();
		animations.clear();
		initialized = failed = false;
	}

	private static final class Box
	{
		final String part;
		final EntityAMTTactile entity;
		final AxisAlignedBB bounds;
		final Vec3d offset;

		Box(String part, EntityAMTTactile entity, AxisAlignedBB bounds, Vec3d offset)
		{
			this.part = part;
			this.entity = entity;
			this.bounds = bounds;
			this.offset = offset;
		}
	}
}
