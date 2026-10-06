# Crafttweaked Multiblocks

**Crafttweaked Multiblocks** (**CTMB**) is an addon for Crafttweaker 1.12 that allows you to create Immersive Engineering system multiblocks using a combination of scripts and resource files.
It requires Immersive Engineering, Immersive Intelligence and Crafttweaker to run.

Multiblocks are defined using a JSON blueprint file and a structure .nbt file, and then registered and configured using Crafttweaker scripts.
The JSON schema is located in `./gradle/schema/multiblock.schema.json`.
Multiblock JSON files can be created using [IIToolkit plugin for Blockbench](https://assets.iiteam.net/iitoolkit/iitoolkit.js), which provides a Multiblock display mode, OBJ/AMT format model exporting and various utilities.

CTMB uses DecoGUI, AMT and the Multiblock System from Immersive Intelligence, making the development effort minimal and mostly based on visual tools.
