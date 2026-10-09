package pl.pabilo8.ctmb.common.gui;

import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenProperty;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@ZenRegister
@ZenClass("mods.ctmb.gui.DecoTextures")
public final class DecoTextures
{
	private static final Map<String, DecoTexture> REGISTRY = new LinkedHashMap<>();
	@ZenProperty
	public static final DecoTexture TEXTURE_WHITE = builtin("texture_white", "immersiveengineering:items/white");
	@ZenProperty
	public static final DecoTexture RES_TEXTURES_DECO = builtin("res_textures_deco", "immersiveintelligence:gui/deco/");
	@ZenProperty
	public static final DecoTexture RES_TEXTURES_DECO_BACKGROUND = builtin("res_textures_deco_background", "immersiveintelligence:gui/deco/background/");
	@ZenProperty
	public static final DecoTexture BG_WOODEN = builtin("bg_wooden", "immersiveintelligence:gui/deco/background/wooden");
	@ZenProperty
	public static final DecoTexture BG_STEEL = builtin("bg_steel", "immersiveintelligence:gui/deco/background/steel");
	@ZenProperty
	public static final DecoTexture BG_STEEL_ROUGH = builtin("bg_steel_rough", "immersiveintelligence:gui/deco/background/steel_rough");
	@ZenProperty
	public static final DecoTexture BG_ALUMINIUM = builtin("bg_aluminium", "immersiveintelligence:gui/deco/background/aluminium");
	@ZenProperty
	public static final DecoTexture BG_PAPER = builtin("bg_paper", "immersiveintelligence:gui/deco/background/paper");
	@ZenProperty
	public static final DecoTexture BG_BLUEPRINT = builtin("bg_blueprint", "immersiveintelligence:gui/deco/background/blueprint");
	@ZenProperty
	public static final DecoTexture BG_BRICKS = builtin("bg_bricks", "immersiveintelligence:gui/deco/background/bricks");
	@ZenProperty
	public static final DecoTexture BG_CONCRETE = builtin("bg_concrete", "immersiveintelligence:gui/deco/background/concrete");
	@ZenProperty
	public static final DecoTexture BG_SANDBAGS = builtin("bg_sandbags", "immersiveintelligence:gui/deco/background/sandbags");
	@ZenProperty
	public static final DecoTexture BG_DARK = builtin("bg_dark", "immersiveintelligence:gui/deco/background/dark");
	@ZenProperty
	public static final DecoTexture BG_DARK_TANK = builtin("bg_dark_tank", "immersiveintelligence:gui/deco/background/dark_tank");
	@ZenProperty
	public static final DecoTexture BG_VANILLA = builtin("bg_vanilla", "immersiveintelligence:gui/deco/background/vanilla");
	@ZenProperty
	public static final DecoTexture RES_TEXTURES_DECO_FRAME = builtin("res_textures_deco_frame", "immersiveintelligence:gui/deco/frame/");
	@ZenProperty
	public static final DecoTexture FRAME_CORNERS_SILVER = builtin("frame_corners_silver", "immersiveintelligence:gui/deco/frame/corners_silver");
	@ZenProperty
	public static final DecoTexture FRAME_CORNERS_BRASS = builtin("frame_corners_brass", "immersiveintelligence:gui/deco/frame/corners_brass");
	@ZenProperty
	public static final DecoTexture FRAME_STEEL_THIN = builtin("frame_steel_thin", "immersiveintelligence:gui/deco/frame/steel_thin");
	@ZenProperty
	public static final DecoTexture FRAME_STEEL = builtin("frame_steel", "immersiveintelligence:gui/deco/frame/steel");
	@ZenProperty
	public static final DecoTexture FRAME_WOODEN_THIN = builtin("frame_wooden_thin", "immersiveintelligence:gui/deco/frame/wooden_thin");
	@ZenProperty
	public static final DecoTexture FRAME_PAPER = builtin("frame_paper", "immersiveintelligence:gui/deco/frame/corners_manual");
	@ZenProperty
	public static final DecoTexture TEMPLATE_ROUND = builtin("template_round", "immersiveintelligence:gui/deco/template/round");
	@ZenProperty
	public static final DecoTexture TEMPLATE_SQUARE = builtin("template_square", "immersiveintelligence:gui/deco/template/square");
	@ZenProperty
	public static final DecoTexture TEMPLATE_ROUND_WOODEN = builtin("template_round_wooden", "immersiveintelligence:gui/deco/template/round_wooden");
	@ZenProperty
	public static final DecoTexture TEMPLATE_TICKET = builtin("template_ticket", "immersiveintelligence:gui/deco/template/ticket");
	@ZenProperty
	public static final DecoTexture TEMPLATE_PAPER = builtin("template_paper", "immersiveintelligence:gui/deco/template/paper");
	@ZenProperty
	public static final DecoTexture SLOT_VANILLA = builtin("slot_vanilla", "immersiveintelligence:gui/deco/slot/vanilla");
	@ZenProperty
	public static final DecoTexture SLOT_IE = builtin("slot_ie", "immersiveintelligence:gui/deco/slot/steel");
	@ZenProperty
	public static final DecoTexture SLOT_IE_MARKER = builtin("slot_ie_marker", "immersiveintelligence:gui/deco/slot/steel_marker");
	@ZenProperty
	public static final DecoTexture SLOT_IE_BRASS = builtin("slot_ie_brass", "immersiveintelligence:gui/deco/slot/brass");
	@ZenProperty
	public static final DecoTexture SLOT_IE_BRASS_MARKER = builtin("slot_ie_brass_marker", "immersiveintelligence:gui/deco/slot/brass_marker");
	@ZenProperty
	public static final DecoTexture SLOT_IE_MANUAL = builtin("slot_ie_manual", "immersiveintelligence:gui/deco/slot/manual");
	@ZenProperty
	public static final DecoTexture SLOT_IE_MANUAL_MARKER = builtin("slot_ie_manual_marker", "immersiveintelligence:gui/deco/slot/manual_marker");
	@ZenProperty
	public static final DecoTexture SLOT_VANILLA_STEEL = builtin("slot_vanilla_steel", "immersiveintelligence:gui/deco/slot/vanilla_steel");
	@ZenProperty
	public static final DecoTexture LABEL_WOODEN = builtin("label_wooden", "immersiveintelligence:gui/deco/label/label_wooden");
	@ZenProperty
	public static final DecoTexture LABEL_STEEL = builtin("label_steel", "immersiveintelligence:gui/deco/label/label_steel");
	@ZenProperty
	public static final DecoTexture LABEL_ALUMINIUM = builtin("label_aluminium", "immersiveintelligence:gui/deco/label/label_aluminium");
	@ZenProperty
	public static final DecoTexture LABEL_STEEL_ROUGH = builtin("label_steel_rough", "immersiveintelligence:gui/deco/label/label_steel_rough");
	@ZenProperty
	public static final DecoTexture LABEL_HAZARD = builtin("label_hazard", "immersiveintelligence:gui/deco/label/label_hazard");
	@ZenProperty
	public static final DecoTexture LABEL_PAPER = builtin("label_paper", "immersiveintelligence:gui/deco/label/label_paper");
	@ZenProperty
	public static final DecoTexture LABEL_BLUEPRINT = builtin("label_blueprint", "immersiveintelligence:gui/deco/label/label_blueprint");
	@ZenProperty
	public static final DecoTexture LABEL_VANILLA = builtin("label_vanilla", "immersiveintelligence:gui/deco/label/label_vanilla");
	@ZenProperty
	public static final DecoTexture COMPONENT_BUTTON = builtin("component_button", "immersiveintelligence:gui/deco/component/button");
	@ZenProperty
	public static final DecoTexture COMPONENT_TEXT_FIELD = builtin("component_text_field", "immersiveintelligence:gui/deco/component/text_field");
	@ZenProperty
	public static final DecoTexture COMPONENT_TAB = builtin("component_tab", "immersiveintelligence:gui/deco/component/tab");
	@ZenProperty
	public static final DecoTexture COMPONENT_TAB_VERTICAL = builtin("component_tab_vertical", "immersiveintelligence:gui/deco/component/tab_vertical");
	@ZenProperty
	public static final DecoTexture COMPONENT_TAB_WIDGET = builtin("component_tab_widget", "immersiveintelligence:gui/deco/component/tab_widget");
	@ZenProperty
	public static final DecoTexture COMPONENT_CHECKBOX = builtin("component_checkbox", "immersiveintelligence:gui/deco/component/checkbox");
	@ZenProperty
	public static final DecoTexture COMPONENT_SWITCH = builtin("component_switch", "immersiveintelligence:gui/deco/component/switch");
	@ZenProperty
	public static final DecoTexture COMPONENT_SWITCH_MOVING = builtin("component_switch_moving", "immersiveintelligence:gui/deco/component/switch_moving");
	@ZenProperty
	public static final DecoTexture COMPONENT_DROPDOWN_SYMBOL = builtin("component_dropdown_symbol", "immersiveintelligence:gui/deco/component/dropdown");
	@ZenProperty
	public static final DecoTexture COMPONENT_DROPDOWN_DATA_LETTER = builtin("component_dropdown_data_letter", "immersiveintelligence:gui/deco/component/data_letter_dropdown");
	@ZenProperty
	public static final DecoTexture COMPONENT_SLIDER = builtin("component_slider", "immersiveintelligence:gui/deco/component/slider");
	@ZenProperty
	public static final DecoTexture COMPONENT_SLIDER_BAR = builtin("component_slider_bar", "immersiveintelligence:gui/deco/component/slider_bar");
	@ZenProperty
	public static final DecoTexture COMPONENT_ARROWS = builtin("component_arrows", "immersiveintelligence:gui/deco/component/arrows");
	@ZenProperty
	public static final DecoTexture COMPONENT_FRAME = builtin("component_frame", "immersiveintelligence:gui/deco/component/frame");
	@ZenProperty
	public static final DecoTexture BAR_ICON_BACKGROUND = builtin("bar_icon_background", "immersiveintelligence:gui/deco/component/bar_icon_background");
	@ZenProperty
	public static final DecoTexture COMPONENT_TANK = builtin("component_tank", "immersiveintelligence:gui/deco/component/tank");
	@ZenProperty
	public static final DecoTexture COMPONENT_TANK_MARKER = builtin("component_tank_marker", "immersiveintelligence:gui/deco/component/tank_marker");
	@ZenProperty
	public static final DecoTexture COMPONENT_TANK_DUST = builtin("component_tank_dust", "immersiveintelligence:gui/deco/component/dust");
	@ZenProperty
	public static final DecoTexture COMPONENT_COLOR = builtin("component_color", "immersiveintelligence:gui/deco/component/color");
	@ZenProperty
	public static final DecoTexture TABS = builtin("tabs", "immersiveintelligence:gui/tab_icons/");
	@ZenProperty
	public static final DecoTexture ICON_STORAGE = builtin("icon_storage", "immersiveintelligence:gui/tab_icons/storage");
	@ZenProperty
	public static final DecoTexture ICON_MEMORY = builtin("icon_memory", "immersiveintelligence:gui/tab_icons/memory");
	@ZenProperty
	public static final DecoTexture ICON_VARIABLES = builtin("icon_variables", "immersiveintelligence:gui/tab_icons/variables");
	@ZenProperty
	public static final DecoTexture ICON_TASKS = builtin("icon_tasks", "immersiveintelligence:gui/tab_icons/tasks");
	@ZenProperty
	public static final DecoTexture ICON_STATUS = builtin("icon_status", "immersiveintelligence:gui/tab_icons/status");
	@ZenProperty
	public static final DecoTexture ICON_CONFIG = builtin("icon_config", "immersiveintelligence:gui/tab_icons/config");
	@ZenProperty
	public static final DecoTexture ICON_TARGETS = builtin("icon_targets", "immersiveintelligence:gui/tab_icons/targets");
	@ZenProperty
	public static final DecoTexture ICON_FIRE_MISSIONS = builtin("icon_fire_missions", "immersiveintelligence:gui/tab_icons/fire_missions");
	@ZenProperty
	public static final DecoTexture ICON_STYLE = builtin("icon_style", "immersiveintelligence:gui/tab_icons/style");
	@ZenProperty
	public static final DecoTexture ICON_OWNERSHIP = builtin("icon_ownership", "immersiveintelligence:gui/tab_icons/ownership");
	@ZenProperty
	public static final DecoTexture ICON_MAP = builtin("icon_map", "immersiveintelligence:gui/tab_icons/map");
	@ZenProperty
	public static final DecoTexture ICON_FACTION_CONFIG = builtin("icon_faction_config", "immersiveintelligence:gui/tab_icons/faction_management");
	@ZenProperty
	public static final DecoTexture RES_TEXTURES_DECO_ICON = builtin("res_textures_deco_icon", "immersiveintelligence:gui/deco/icons/");
	@ZenProperty
	public static final DecoTexture ICON_FUEL = builtin("icon_fuel", "immersiveintelligence:gui/deco/icons/icon_fuel");
	@ZenProperty
	public static final DecoTexture ICON_TIME = builtin("icon_time", "immersiveintelligence:gui/deco/icons/icon_time");
	@ZenProperty
	public static final DecoTexture ICON_SPEED = builtin("icon_speed", "immersiveintelligence:gui/deco/icons/icon_speed");
	@ZenProperty
	public static final DecoTexture ICON_PROGRESS = builtin("icon_progress", "immersiveintelligence:gui/deco/icons/icon_progress");
	@ZenProperty
	public static final DecoTexture ICON_SOIL_FERTILITY = builtin("icon_soil_fertility", "immersiveintelligence:gui/deco/icons/icon_soil_fertility");
	@ZenProperty
	public static final DecoTexture ICON_EXREAC_ARMOR_INTEGRITY = builtin("icon_exreac_armor_integrity", "immersiveintelligence:gui/deco/icons/icon_exreac_armor_integrity");
	@ZenProperty
	public static final DecoTexture ICON_ARMOR_INTEGRITY = builtin("icon_armor_integrity", "immersiveintelligence:gui/deco/icons/icon_armor_integrity");
	@ZenProperty
	public static final DecoTexture ICON_STRUCTURAL_INTEGRITY = builtin("icon_structural_integrity", "immersiveintelligence:gui/deco/icons/icon_structural_integrity");
	@ZenProperty
	public static final DecoTexture ICON_MECH_TORQUE_OUTPUT = builtin("icon_mech_torque_output", "immersiveintelligence:gui/deco/icons/icon_mech_torque_output");
	@ZenProperty
	public static final DecoTexture ICON_MECH_TORQUE_INPUT = builtin("icon_mech_torque_input", "immersiveintelligence:gui/deco/icons/icon_mech_torque_input");
	@ZenProperty
	public static final DecoTexture ICON_MECH_TORQUE = builtin("icon_mech_torque", "immersiveintelligence:gui/deco/icons/icon_mech_torque");
	@ZenProperty
	public static final DecoTexture ICON_MECH_SPEED_OUTPUT = builtin("icon_mech_speed_output", "immersiveintelligence:gui/deco/icons/icon_mech_speed_output");
	@ZenProperty
	public static final DecoTexture ICON_MECH_SPEED_INPUT = builtin("icon_mech_speed_input", "immersiveintelligence:gui/deco/icons/icon_mech_speed_input");
	@ZenProperty
	public static final DecoTexture ICON_MECH_SPEED = builtin("icon_mech_speed", "immersiveintelligence:gui/deco/icons/icon_mech_speed");
	@ZenProperty
	public static final DecoTexture ICON_HEAT_OUTPUT = builtin("icon_heat_output", "immersiveintelligence:gui/deco/icons/icon_heat_output");
	@ZenProperty
	public static final DecoTexture ICON_HEAT_INPUT = builtin("icon_heat_input", "immersiveintelligence:gui/deco/icons/icon_heat_input");
	@ZenProperty
	public static final DecoTexture ICON_HEAT = builtin("icon_heat", "immersiveintelligence:gui/deco/icons/icon_heat");
	@ZenProperty
	public static final DecoTexture ICON_AIR_PRESSURE_OUTPUT = builtin("icon_air_pressure_output", "immersiveintelligence:gui/deco/icons/icon_air_pressure_output");
	@ZenProperty
	public static final DecoTexture ICON_AIR_PRESSURE_INPUT = builtin("icon_air_pressure_input", "immersiveintelligence:gui/deco/icons/icon_air_pressure_input");
	@ZenProperty
	public static final DecoTexture ICON_AIR_PRESSURE = builtin("icon_air_pressure", "immersiveintelligence:gui/deco/icons/icon_air_pressure");
	@ZenProperty
	public static final DecoTexture ICON_ENERGY_OUTPUT = builtin("icon_energy_output", "immersiveintelligence:gui/deco/icons/icon_energy_output");
	@ZenProperty
	public static final DecoTexture ICON_ENERGY_INPUT = builtin("icon_energy_input", "immersiveintelligence:gui/deco/icons/icon_energy_input");
	@ZenProperty
	public static final DecoTexture ICON_ENERGY = builtin("icon_energy", "immersiveintelligence:gui/deco/icons/icon_energy");
	@ZenProperty
	public static final DecoTexture ICON_CASING_OUTPUT = builtin("icon_casing_output", "immersiveintelligence:gui/deco/icons/icon_casing_output");
	@ZenProperty
	public static final DecoTexture ICON_CASING_INPUT = builtin("icon_casing_input", "immersiveintelligence:gui/deco/icons/icon_casing_input");
	@ZenProperty
	public static final DecoTexture ICON_CASING = builtin("icon_casing", "immersiveintelligence:gui/deco/icons/icon_casing");
	@ZenProperty
	public static final DecoTexture ICON_AMMO_OUTPUT = builtin("icon_ammo_output", "immersiveintelligence:gui/deco/icons/icon_ammo_output");
	@ZenProperty
	public static final DecoTexture ICON_AMMO_INPUT = builtin("icon_ammo_input", "immersiveintelligence:gui/deco/icons/icon_ammo_input");
	@ZenProperty
	public static final DecoTexture ICON_AMMO = builtin("icon_ammo", "immersiveintelligence:gui/deco/icons/icon_ammo");
	@ZenProperty
	public static final DecoTexture ICON_RANGE_VISION = builtin("icon_range_vision", "immersiveintelligence:gui/deco/icons/icon_range_vision");
	@ZenProperty
	public static final DecoTexture ICON_RANGE_ATTACK = builtin("icon_range_attack", "immersiveintelligence:gui/deco/icons/icon_range_attack");
	@ZenProperty
	public static final DecoTexture ICON_CONTACT = builtin("icon_contact", "immersiveintelligence:gui/deco/icons/icon_fuse_contact");
	@ZenProperty
	public static final DecoTexture ICON_TIMED = builtin("icon_timed", "immersiveintelligence:gui/deco/icons/icon_fuse_timed");
	@ZenProperty
	public static final DecoTexture ICON_PROXIMITY = builtin("icon_proximity", "immersiveintelligence:gui/deco/icons/icon_fuse_proximity");
	@ZenProperty
	public static final DecoTexture ICON_ACTION_DUPLICATE = builtin("icon_action_duplicate", "immersiveintelligence:gui/deco/icons/action_duplicate");
	@ZenProperty
	public static final DecoTexture ICON_ACTION_EDIT = builtin("icon_action_edit", "immersiveintelligence:gui/deco/icons/action_edit");
	@ZenProperty
	public static final DecoTexture ICON_ACTION_REMOVE = builtin("icon_action_remove", "immersiveintelligence:gui/deco/icons/action_remove");
	@ZenProperty
	public static final DecoTexture ICON_ACTION_ADD = builtin("icon_action_add", "immersiveintelligence:gui/deco/icons/action_add");
	@ZenProperty
	public static final DecoTexture ICON_ACTION_CLEAR = builtin("icon_action_clear", "immersiveintelligence:gui/deco/icons/action_clear");
	@ZenProperty
	public static final DecoTexture ICON_ACTION_ACCEPT = builtin("icon_action_accept", "immersiveintelligence:gui/deco/icons/action_accept");
	@ZenProperty
	public static final DecoTexture ICON_ACTION_REJECT = builtin("icon_action_reject", "immersiveintelligence:gui/deco/icons/action_reject");
	@ZenProperty
	public static final DecoTexture ICON_ACTION_HELP = builtin("icon_action_help", "immersiveintelligence:gui/deco/icons/action_help");
	@ZenProperty
	public static final DecoTexture COMPONENT_BUTTON_PAPER = builtin("component_button_paper", "immersiveintelligence:gui/deco/component/button_paper");
	@ZenProperty
	public static final DecoTexture COMPONENT_BUTTON_PAPER_HIGHLIGHT = builtin("component_button_paper_highlight", "immersiveintelligence:gui/deco/component/button_paper_highlight");
	@ZenProperty
	public static final DecoTexture COMPONENT_BUTTON_HANGING = builtin("component_button_hanging", "immersiveintelligence:gui/deco/component/button_hanging");
	@ZenProperty
	public static final DecoTexture COMPONENT_ARROWS_PAPER = builtin("component_arrows_paper", "immersiveintelligence:gui/deco/component/arrows_paper");
	@ZenProperty
	public static final DecoTexture COMPONENT_BUTTON_ROUND = builtin("component_button_round", "immersiveintelligence:gui/deco/component/button_round");
	@ZenProperty
	public static final DecoTexture COMPONENT_SLIDER_PAPER = builtin("component_slider_paper", "immersiveintelligence:gui/deco/component/slider_paper");
	@ZenProperty
	public static final DecoTexture COMPONENT_SLIDER_VANILLA = builtin("component_slider_vanilla", "immersiveintelligence:gui/deco/component/slider_vanilla");
	@ZenProperty
	public static final DecoTexture COMPONENT_DROPDOWN_DATA_LETTER_PAPER = builtin("component_dropdown_data_letter_paper", "immersiveintelligence:gui/deco/component/data_letter_dropdown_paper");
	@ZenProperty
	public static final DecoTexture COMPONENT_DROPDOWN_SYMBOL_PAPER = builtin("component_dropdown_symbol_paper", "immersiveintelligence:gui/deco/component/dropdown_paper");
	@ZenProperty
	public static final DecoTexture COMPONENT_TANK_PAPER = builtin("component_tank_paper", "immersiveintelligence:gui/deco/component/tank_manual");
	@ZenProperty
	public static final DecoTexture MAP_MARKER_ENTITY = builtin("map_marker_entity", "immersiveintelligence:gui/deco/map/marker/entity");
	@ZenProperty
	public static final DecoTexture MAP_MARKER_VEHICLE = builtin("map_marker_vehicle", "immersiveintelligence:gui/deco/map/marker/vehicle");
	@ZenProperty
	public static final DecoTexture MAP_MARKER_TRAINCAR = builtin("map_marker_traincar", "immersiveintelligence:gui/deco/map/marker/traincar");
	@ZenProperty
	public static final DecoTexture MAP_MARKER_AIRCRAFT = builtin("map_marker_aircraft", "immersiveintelligence:gui/deco/map/marker/aircraft");
	@ZenProperty
	public static final DecoTexture MAP_MARKER_DRONE = builtin("map_marker_drone", "immersiveintelligence:gui/deco/map/marker/drone");
	@ZenProperty
	public static final DecoTexture MAP_MARKER_BULLET = builtin("map_marker_bullet", "immersiveintelligence:gui/deco/map/marker/bullet");
	@ZenProperty
	public static final DecoTexture MAP_MARKER_MISSILE = builtin("map_marker_missile", "immersiveintelligence:gui/deco/map/marker/missile");
	@ZenProperty
	public static final DecoTexture MAP_MARKER_DRAGON = builtin("map_marker_dragon", "immersiveintelligence:gui/deco/map/marker/dragon");
	@ZenProperty
	public static final DecoTexture MAP_MARKER_WYRM = builtin("map_marker_wyrm", "immersiveintelligence:gui/deco/map/marker/wyrm");
	@ZenProperty
	public static final DecoTexture MAP_MARKER_FLAGPOLE = builtin("map_marker_flagpole", "immersiveintelligence:gui/deco/map/marker/flagpole");
	@ZenProperty
	public static final DecoTexture MAP_MARKER_EMPLACEMENT = builtin("map_marker_emplacement", "immersiveintelligence:gui/deco/map/marker/emplacement");
	@ZenProperty
	public static final DecoTexture MAP_MARKER_ARTILLERY_HOWITZER = builtin("map_marker_artillery_howitzer", "immersiveintelligence:gui/deco/map/marker/artillery_howitzer");
	@ZenProperty
	public static final DecoTexture MAP_MARKER_MISSILE_SILO = builtin("map_marker_missile_silo", "immersiveintelligence:gui/deco/map/marker/missile_silo");
	@ZenProperty
	public static final DecoTexture MAP_MARKER_RADAR = builtin("map_marker_radar", "immersiveintelligence:gui/deco/map/marker/radar");
	@ZenProperty
	public static final DecoTexture MAP_MARKER_RADIO_STATION = builtin("map_marker_radio_station", "immersiveintelligence:gui/deco/map/marker/radio_station");
	@ZenProperty
	public static final DecoTexture ICON_INVENTORY_FACTION_INVITES = builtin("icon_inventory_faction_invites", "immersiveintelligence:gui/deco/icons/icon_faction_invites");
	@ZenProperty
	public static final DecoTexture ICON_INVENTORY_FACTION_INVITES_ACTIVE = builtin("icon_inventory_faction_invites_active", "immersiveintelligence:gui/deco/icons/icon_faction_invites_active");

	private DecoTextures()
	{
	}

	private static DecoTexture builtin(String name, String location)
	{
		DecoTexture texture = new DecoTexture(location);
		REGISTRY.put(name, texture);
		return texture;
	}

	@ZenMethod
	public static DecoTexture register(String name, String location)
	{
		if(name==null||!name.matches("[a-z0-9_.:/-]+"))
			throw new IllegalArgumentException("Invalid Deco texture name "+name);
		if(REGISTRY.containsKey(name)) throw new IllegalArgumentException("Duplicate Deco texture "+name);
		if(location==null||!location.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")||location.endsWith("/")||location.endsWith(".png")||location.contains(".."))
			throw new IllegalArgumentException("Custom Deco textures use namespace:atlas/path without textures/ or .png");
		DecoTexture texture = new DecoTexture(location);
		REGISTRY.put(name, texture);
		return texture;
	}

	public static DecoTexture find(String name)
	{
		DecoTexture result = REGISTRY.get(name);
		if(result==null) throw new IllegalArgumentException("Unregistered Deco texture "+name);
		return result;
	}

	public static Collection<DecoTexture> all()
	{
		return Collections.unmodifiableCollection(REGISTRY.values());
	}
}
