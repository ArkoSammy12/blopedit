# Blopedit

Blopedit is a small Fabric client-side mode to more easily edit the `block.properties` file of a shader, via some commands and your own camera!
This mod is useful for quickly integrating modded blocks with shaders that make use of PBR to give blocks certain unique visual properties, such as making leaves wave, or making ores glow.

## The `block.properties` file

The `block.properties` file is what your shader uses to assign different visual properties to different blocks. It can be found in `/.minecraft/shaderpacks/myShader.zip/shaders/block.properties`. Each block in the file is known as a "property entry". These entries appear next to block ID keys that determine what kind of block they are.
Each property entry is unique and can only appear once in the file, except when they appear in lines corresponding to different Minecraft versions.

A property entry consists of a block identifier. An identifier consists of:
- A namespace, which corresponds to where this block comes from. For vanilla blocks, this value is `minecraft`. For modded blocks, it corresponds to the mod's modID, such as `farmersdelight`.
- A path, which corresponds to the actual name of the block, such as `farmland`.
- An optional list of block state properties. Each property is made up of a property name, followed by an equals sign, followed by the value of the property.

These elements are separated by a colon `:` in that order. An example property entry is `minecraft:farmland:moisture=3`.

## How to use 

Blopedit will edit the `block.properties` file of the currently loaded shader on Iris. Blopedit allows you to add new entries, now called "source entries" by using another "destination entry" which determines how the source entry should be treated as by the shader.
The source entry is added to where the destination entry is. 

- `/blopedit addToPropertiesFile <blockStateArgument>` This command should be run while looking at a block, which will be treated as the source entry, while the block state argument will be treated as the destination entry. 
- `/blopedit addToPropertiesFile <blockStateArgument> matchingCondition <stringArgument>` This is an optional argument that determines how Blopedit should attempt to match the source and destination entries with entries already present in the `block.properties` file. This determines where the source entry is added to, and whether the source entry is added (since there cannot be multiple entries).
- `blopedit addToPropertiesFile <blockStateArgument> matchingCondition <stringArgument> moveSourceIfFound <booleanArgument>` This is another optional argument that determines whether entries already present in the `block.properties` file that match with the source entry should be deleted and then moved to the new destination entry. If not, the addition of the source entry fails and a warning is shown.

## Matching conditions

Blopedit has four options to determine the matching behaviour of input entries to compare them against entries in the file. This controls whether you see that your destination entry matches multiple, or only a single entry in the file, or whether a source entry is found in the file.

- Match Identifiers: This option makes the mod match both the source entry and the destination entry using only their identifiers. This means that, for an input entry, any entry in the file whose identifiers are the same will be considered a match. When matching only with identifiers, the block state's properties are not taken into account.
- Match with Properties: This option makes the mod match both the source entry and the destination entry using both the identifiers and the properties of the entries. Specifically, for any entry present in the file, if an input entry contains the properties and property values, as the ones in the entry in the file, and the identifiers are the same. It will be considered a match. For example, `minecraft:oak_stair:waterlogged=true:facing=north` is considered to match with `minecraft:oak_stair:waterlogged=true`, as the former contains properties present in the latter, and the matching properties' values are the same.
- Match with Properties Source: This option enables matching with properties for the source entry, while only matching by identifiers for the destination block.
- Match with Properties Destination: This option matching with properties for the destination entry, while only matching by identifiers for the source entry.

## Adding properties

Once attempted to add a source entry to where a destination entry is, this operation will only be performed if:

- There is only one matching destination entry.
- There are no entries in the file that match the source entry or the option to move the source if found is enabled.

If the operation is successful, the source entry will be added to all lines where a matching destination entry was found. If the option to move the source if found is enabled, it will also delete all the entries matched with the source entry, and then place them again in the places where a matching destination entry was found.
Additionally, if a source entry was matched with already present entries, the  resulting source entry will only include properties that were present in the matched already present entries.

## Extra features

There are a couple of extra utility commands, such as:

- `/blopedit settings doAutoReloadShaders <booleanArgument>` This setting configures whether the shader should be automatically reloaded whenever a successful `block.properties` edit operation is done.
- `/blopedit copyPropertiesFileToFolder` This command, when run, will copy the `block.properties` file of the current shader and paste it in `/.minecraft/blopedit/shaderPackName/block.properties`. This allows for easy of sharing and debugging.

## Support

If you would like to report a bug, or make a suggestion, you can do so via the mod's [issue tracker](https://github.com/ArkoSammy12/blopedit/issues) or join my [Discord server](https://discord.gg/wScNgcvJ3y). 

## Credit

- Thanks to [@spaceagle17](https://github.com/SpacEagle17) for suggesting the idea for this mod, and thanks to the rest of the Complementary shaders community for the awesome support it has given me!