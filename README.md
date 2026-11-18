# Ducky's MC Utilities

A combined Fabric client mod for Minecraft Java Edition 26.2.

## Included features

### Offhand Utility
- Uses a suitable offhand pickaxe, axe, or shovel for mining when it is more effective or the correct tool.
- Uses an offhand sword for attacks when the main hand is not holding a sword.
- Items stay in their original inventory slots.

### Ducky's MC Optimised
- Entity culling toggle and render-distance slider.
- Block-entity culling toggle and render-distance slider.
- A settings button in Video Settings.
- Settings saved in `.minecraft/config/ducky-utilities.properties`.

YACL is bundled by the build, so it should not need to be installed separately.

## Requirements
- Minecraft Java 26.2
- Fabric Loader 0.20.1+
- Fabric API for 26.2
- Java 25

## Build
GitHub Actions builds the mod on pushes to `main`. Download the `ducky-s-utilities-fabric-26.2` artifact from a successful run, unzip it, and put the JAR in your `mods` folder.