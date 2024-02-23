package xd.arkosammy.blopedit.files;

import net.coderbot.iris.Iris;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xd.arkosammy.blopedit.Blopedit;
import xd.arkosammy.blopedit.properties.FileLine;
import xd.arkosammy.blopedit.properties.PropertyEntry;
import xd.arkosammy.blopedit.properties.PropertyFileLine;
import xd.arkosammy.blopedit.util.Config;
import xd.arkosammy.blopedit.util.MatchingCondition;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class PropertiesFile {

    private static final String BLOCK_PROPERTIES_PATH = "/shaders/block.properties";
    private static final Path BLOCK_PROPERTIES_FOLDER = FabricLoader.getInstance().getGameDir().resolve("block_properties_file_copies");

    private final List<FileLine> fileLines = new ArrayList<>();
    private final String shaderPackName;
    private final Path shaderPackPath;

    public static Optional<PropertiesFile> getInstance(){
        Optional<String> currentShaderOptional = Iris.getIrisConfig().getShaderPackName();
        if(currentShaderOptional.isEmpty()){
            Blopedit.addMessageToHud(Text.literal("No shader is currently loaded!").formatted(Formatting.RED));
            return Optional.empty();
        } else {
            String currentShader = currentShaderOptional.get();
            PropertiesFile instance = new PropertiesFile(currentShader);
            return Optional.of(instance);
        }
    }

    PropertiesFile(String shaderPackName) {
        this.shaderPackName = shaderPackName;
        this.shaderPackPath = FabricLoader.getInstance().getGameDir().resolve("shaderpacks").resolve(this.shaderPackName);
        // Handle the case where the shader is either in its zipped or folder form
        if(this.shaderPackName.endsWith(".zip")) {
            try (FileSystem fs = FileSystems.newFileSystem(this.shaderPackPath);
                 BufferedReader br = Files.newBufferedReader(fs.getPath(BLOCK_PROPERTIES_PATH))) {
                br.lines().forEach(line -> this.fileLines.add(FileLine.newFileLine(line)));
            } catch (IOException e) {
                Blopedit.LOGGER.error("Error attempting to read block.properties file of shader " + shaderPackName + ": " + e);
            }
        } else {
            Path blockPropertiesPath = this.shaderPackPath.resolve("shaders").resolve("block.properties");
            try(BufferedReader br = Files.newBufferedReader(blockPropertiesPath)) {
                br.lines().forEach(line -> this.fileLines.add(FileLine.newFileLine(line)));
            } catch (IOException e){
                Blopedit.LOGGER.error("Error attempting to read block.properties file of shader " + shaderPackName + ": " + e);
            }
        }
    }

    public void processEditContext(FilePropertiesEditContext propertiesEditContext) {
        MatchingCondition matchingCondition = propertiesEditContext.getMatchingCondition();
        PropertyEntry source = propertiesEditContext.getSource();
        PropertyEntry destination = propertiesEditContext.getDestination();
        Set<PropertyEntry> matchingDestinationEntries = this.getFirstMatchingPropertyEntries(destination, matchingCondition.matchingPropertiesForDestination());
        if(matchingDestinationEntries.isEmpty()) {
            Blopedit.addMessageToHud(Text.empty().append(Text.literal("Found no entries matching destination property ").formatted(Formatting.YELLOW)).append(Text.literal(destination.toString()).formatted(Formatting.AQUA)).append(Text.literal(" in block.properties file of shader ").formatted(Formatting.YELLOW)).append(Text.literal(shaderPackName).formatted(Formatting.AQUA)));
            return;
        }
        if (matchingDestinationEntries.size() > 1) {
            Blopedit.addMessageToHud(Text.empty().append(Text.literal("Found multiple entries matching destination property ").formatted(Formatting.YELLOW)).append(Text.literal(destination.toString()).formatted(Formatting.AQUA)).append(Text.literal(" in block.properties file of shader ").formatted(Formatting.YELLOW)).append(Text.literal(shaderPackName).formatted(Formatting.AQUA)).append(Text.literal(": ").formatted(Formatting.YELLOW)).append(Text.literal(String.join(" ", matchingDestinationEntries.stream().map(PropertyEntry::toString).toList())).formatted(Formatting.AQUA)));
            return;
        }
        Set<PropertyEntry> deletedEntries = new HashSet<>();
        if(propertiesEditContext.moveSourceIfFound()){
            deletedEntries.addAll(this.deleteEntriesIfMatching(source, matchingCondition.matchingPropertiesForSource()));
            if(!deletedEntries.isEmpty()){
                Blopedit.addMessageToHud(Text.literal("Removing already present source entries: ").formatted(Formatting.YELLOW).append(Text.literal(String.join(" ", deletedEntries.stream().map(PropertyEntry::toString).toList())).formatted(Formatting.AQUA)));
            }
        }
        Set<PropertyEntry> matchingSourceEntries = this.getFirstMatchingPropertyEntries(source, matchingCondition.matchingPropertiesForSource());
        if(!matchingSourceEntries.isEmpty()){
            Blopedit.addMessageToHud(Text.empty().append(Text.literal("Source property ").formatted(Formatting.YELLOW)).append(Text.literal(source.toString()).formatted(Formatting.AQUA)).append(Text.literal(" already found in block.properties file of shader ").formatted(Formatting.YELLOW)).append(Text.literal(shaderPackName).formatted(Formatting.AQUA)).append(Text.literal(": ").formatted(Formatting.YELLOW)).append(Text.literal(String.join(" ", matchingSourceEntries.stream().map(PropertyEntry::toString).toList())).formatted(Formatting.AQUA)));
            return;
        }
        // Only include properties that match those of the previously present entries to not include unnecessary extra properties
        for(PropertyEntry deletedEntry : deletedEntries){
            source.removePropertiesNotIn(deletedEntry);
        }
        this.addSourcePropertyToDestination(source, destination, matchingCondition.matchingPropertiesForDestination());
        this.writeToFile();
        if(Config.getInstance().doAutoReloadShaders()) {
            try {
                Iris.reload();
            } catch (IOException e){
                Blopedit.addMessageToHud(Text.literal("Error attempting to reload shaders automatically! Check logs for more information.").formatted(Formatting.RED));
                Blopedit.LOGGER.error("Error attempting to reload shaders automatically: " + e);
            }
        }
        Blopedit.addMessageToHud(Text.empty().append(Text.literal("Source property ").formatted(Formatting.GREEN)).append(Text.literal(source.toString()).formatted(Formatting.AQUA)).append(Text.literal(" added to block.properties file at location of: ").formatted(Formatting.GREEN)).append(Text.literal(String.join(" ", matchingDestinationEntries.stream().map(PropertyEntry::toString).toList())).formatted(Formatting.AQUA)));
    }

    private void addSourcePropertyToDestination(PropertyEntry src, PropertyEntry dest, boolean matchingPropertiesForDestination){
        for(FileLine fileLine : this.fileLines){
            if (fileLine instanceof PropertyFileLine propertyFileLine && propertyFileLine.containsMatching(dest, matchingPropertiesForDestination)){
                propertyFileLine.appendProperty(src);
            }
        }
    }

    private Set<PropertyEntry> getFirstMatchingPropertyEntries(PropertyEntry propertyEntry, boolean matchProperties){
        Set<PropertyEntry> matchingEntries = new HashSet<>();
        for(FileLine fileLine : this.fileLines){
            if (fileLine instanceof PropertyFileLine propertyFileLine) {
                propertyFileLine.getFirstMatchingValue(propertyEntry, matchProperties).ifPresent(matchingEntries::add);
            }
        }
        return matchingEntries;
    }

    private Set<PropertyEntry> deleteEntriesIfMatching(PropertyEntry propertyEntry, boolean matchProperties){
        Set<PropertyEntry> deletedEntries = new HashSet<>();
        for(FileLine fileLine : this.fileLines){
            if(fileLine instanceof PropertyFileLine propertyFileLine){
                deletedEntries.addAll(propertyFileLine.removeIfMatching(propertyEntry, matchProperties));
            }
        }
        return deletedEntries;
    }

    void writeToFile(){
        // Handle the case where the shader is either in its zipped or folder form
        if(this.shaderPackName.endsWith(".zip")){
            try (FileSystem fs = FileSystems.newFileSystem(this.shaderPackPath);
                 BufferedWriter bw = Files.newBufferedWriter(fs.getPath(BLOCK_PROPERTIES_PATH))) {
                for(FileLine fileLine : this.fileLines) {
                    bw.write(fileLine.getString());
                    bw.newLine();
                }
            } catch (IOException e){
                Blopedit.LOGGER.error("Error attempting to write to block.properties file of shader " + shaderPackName + ": " + e);
            }
        } else {
            Path blockPropertiesPath = this.shaderPackPath.resolve("shaders").resolve("block.properties");
            try(BufferedWriter bw = Files.newBufferedWriter(blockPropertiesPath)) {
                for(FileLine fileLine : this.fileLines) {
                    bw.write(fileLine.getString());
                    bw.newLine();
                }
            } catch (IOException e){
                Blopedit.LOGGER.error("Error attempting to write to block.properties file of shader " + shaderPackName + ": " + e);
            }
        }

    }

    public void copyPropertiesFileToFolder() {
        try {
            // Check if block properties folder exists
            if (!Files.exists(BLOCK_PROPERTIES_FOLDER)) {
                Files.createDirectory(BLOCK_PROPERTIES_FOLDER);
            }

            // Check if specific folder for the shader's block properties file exists
            Path propertiesFolderForShaderPath = BLOCK_PROPERTIES_FOLDER.resolve(this.shaderPackName);
            if (!Files.exists(propertiesFolderForShaderPath)) {
                Files.createDirectory(propertiesFolderForShaderPath);
            }

            Path propertiesFilePath = propertiesFolderForShaderPath.resolve("block.properties");

            // Check if the block properties file already exists, and append a suffix if so
            int copyNumber = 1;
            while (Files.exists(propertiesFilePath)) {
                propertiesFilePath = propertiesFolderForShaderPath.resolve("block_(%d).properties".formatted(copyNumber));
                copyNumber++;

                // Add a safeguard to prevent potential infinite loop
                if (copyNumber > 100) {
                    throw new IOException("Too many attempts to find a unique file name for block.properties file in " + propertiesFolderForShaderPath);
                }
            }

            // Handle the case where the shader is either in its zipped or folder form
            if (this.shaderPackName.endsWith(".zip")) {
                try (FileSystem fs = FileSystems.newFileSystem(this.shaderPackPath)) {
                    Path blockPropertiesPath = fs.getPath(BLOCK_PROPERTIES_PATH);
                    Files.copy(blockPropertiesPath, propertiesFilePath);
                }
            } else {
                Path blockPropertiesPath = this.shaderPackPath.resolve("shaders").resolve("block.properties");
                Files.copy(blockPropertiesPath, propertiesFilePath);
            }
            Blopedit.addMessageToHud(Text.empty().append(Text.literal("Copied block.properties file of shader " + shaderPackName + " to: " ).formatted(Formatting.GREEN)).append(Text.literal(propertiesFilePath.toString()).setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, propertiesFilePath.toString())).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click here to open file")))).formatted(Formatting.DARK_PURPLE).formatted(Formatting.UNDERLINE)));

        } catch (IOException e) {

            Blopedit.addMessageToHud(Text.literal("Error attempting to copy block.properties file of shader " + shaderPackName + " to folder! Check logs for more information"));
            Blopedit.LOGGER.error("Error attempting to copy block.properties file of shader " + shaderPackName + ": " + e);
            e.printStackTrace();
        }
    }


}
