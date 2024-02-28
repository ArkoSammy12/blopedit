# Blopedit

### Before using Blopedit, it is strongly recommended to create a backup of the `block.properties` file of your shaders. While Blopedit is designed to streamline the process of integrating modded blocks with shaders, unforeseen issues or user mistakes may occur during the editing process.

Blopedit is a small Fabric client-side mod that facilitates the editing of the `block.properties` file in shaders, offering commands and utilizing your own camera.
This mod is particularly useful for efficiently integrating modded blocks with shaders that leverage Physically Based Rendering (PBR) for distinct visual properties, such as animated leaves or glowing ores.

## The `block.properties` File

The `block.properties` file is utilized by shaders to assign various visual properties to different blocks. It can be found in `/.minecraft/shaderpacks/myShader.zip/shaders/block.properties`. Each block in the file is known as a "property entry," appearing next to block ID keys that define the block type.
A property entry is unique and can only appear once in the file unless it corresponds to different Minecraft versions.

A property entry comprises a block identifier, consisting of:
- An identifier **namespace**, representing the block's origin (e.g., `minecraft` for vanilla blocks or the mod's modID for modded blocks, such as `farmersdelight`).
- An identifier **path**, indicating the actual block name (e.g., `farmland`).
- An optional list of **block state properties**, where each property consists of a name followed by an equals sign and the property value.

These elements are separated by a colon `:` in the mentioned order. An example property entry is `minecraft:farmland:moisture=3`.

## How to Use 

Blopedit edits the `block.properties` file of the currently loaded shader on Iris. It allows you to add new entries, referred to as "source entries," by using a "destination entry" that specifies how the shader should treat the source entry.
The source entry is added where the destination entry is.

- `/blopedit addToPropertiesFile <blockStateArgument>`: Run this command while looking at a block (treated as the source entry), with the block state argument as the destination entry.
- `/blopedit addToPropertiesFile <blockStateArgument> matchingCondition <stringArgument>`: An optional argument determining how Blopedit should match source and destination entries with those in the `block.properties` file, affecting where the source entry is added.
- `blopedit addToPropertiesFile <blockStateArgument> matchingCondition <stringArgument> moveSourceIfFound <booleanArgument>`: Another optional argument determining whether matching entries in the `block.properties` file should be deleted and moved to the new destination entry. If not, the addition of the source entry fails, showing a warning.

## Matching Conditions

Blopedit offers four options to determine matching behavior:

- **Match Identifiers**: Matches source and destination entries using only their identifiers, ignoring block state properties.
- **Match with Properties**: Matches entries using both identifiers and properties. The input entry must contain matching properties and values.
- **Match with Properties Source**: Matches source entry properties, but only matches destination entry by identifiers.
- **Match with Properties Destination**: Matches destination entry properties, but only matches source entry by identifiers.

## Adding Properties

The addition of a source entry to a destination entry occurs only if:
- There is only one matching destination entry.
- There are no file entries matching the source entry, or the option to move the source if found is enabled.

If successful, the source entry is added to all lines where a matching destination entry was found. If moving the source if found is enabled, it also deletes matched entries and reinserts them where matching destination entries were found. The resulting source entry includes only properties present in the matched already present entries.

## Extra Features

Additional utility commands include:

- `/blopedit settings doAutoReloadShaders <booleanArgument>`: Configures whether the shader should automatically reload after a successful `block.properties` edit operation.
- `/blopedit copyPropertiesFileToFolder`: Copies the `block.properties` file of the current shader to `/.minecraft/blopedit/shaderPackName/block.properties`, simplifying sharing and debugging.

## Support

To report bugs or make suggestions, use the mod's [issue tracker](https://github.com/ArkoSammy12/blopedit/issues) or join the [Discord server](https://discord.gg/wScNgcvJ3y).

## Credit

- Thanks to [@spaceagle17](https://github.com/SpacEagle17) for suggesting the idea for this mod. Special thanks to the Complementary Shaders community for their outstanding support!
