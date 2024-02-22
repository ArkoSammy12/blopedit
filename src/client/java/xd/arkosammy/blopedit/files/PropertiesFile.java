package xd.arkosammy.blopedit.files;

import net.coderbot.iris.Iris;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xd.arkosammy.blopedit.Blopedit;
import xd.arkosammy.blopedit.properties.FileLine;
import xd.arkosammy.blopedit.properties.PropertyEntry;
import xd.arkosammy.blopedit.properties.PropertyFileLine;
import xd.arkosammy.blopedit.util.MatchingCondition;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PropertiesFile {

    public static final String BLOCK_PROPERTIES_PATH = "/shaders/block.properties";
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

    public void processEditContext(FilePropertiesEditContext propertiesEditContext) throws IOException {
        PropertyEntry source = propertiesEditContext.getSource();
        PropertyEntry destination = propertiesEditContext.getDestination();
        Set<PropertyEntry> matchingDestinationEntries = propertiesEditContext.getMatchingCondition() == MatchingCondition.MATCH_WITH_PROPERTIES ? this.getFirstMatchingEntriesForProperties(destination) : this.getFirstMatchingEntriesForIdentifier(destination);
        if(matchingDestinationEntries.isEmpty()) {
            Blopedit.addMessageToHud(Text.empty().append(Text.literal("Found no entries matching destination property ").formatted(Formatting.YELLOW)).append(Text.literal(destination.toString()).formatted(Formatting.AQUA)).append(Text.literal(" in block.properties file of shader ").formatted(Formatting.YELLOW)).append(Text.literal(shaderPackName).formatted(Formatting.AQUA)));
        } else if (matchingDestinationEntries.size() > 1) {
            Blopedit.addMessageToHud(Text.empty().append(Text.literal("Found multiple entries matching destination property ").formatted(Formatting.YELLOW)).append(Text.literal(destination.toString()).formatted(Formatting.AQUA)).append(Text.literal(" in block.properties file of shader ").formatted(Formatting.YELLOW)).append(Text.literal(shaderPackName).formatted(Formatting.AQUA)).append(Text.literal(": ").formatted(Formatting.YELLOW)).append(Text.literal(String.join(" ", matchingDestinationEntries.stream().map(PropertyEntry::toString).toList())).formatted(Formatting.AQUA)));
        } else {
            Set<PropertyEntry> matchingSourceEntries = propertiesEditContext.getMatchingCondition() == MatchingCondition.MATCH_WITH_PROPERTIES ? this.getFirstMatchingEntriesForProperties(source) : this.getFirstMatchingEntriesForIdentifier(source);
            if(!matchingSourceEntries.isEmpty()){
                Blopedit.addMessageToHud(Text.empty().append(Text.literal("Source property ").formatted(Formatting.YELLOW)).append(Text.literal(source.toString()).formatted(Formatting.AQUA)).append(Text.literal(" already found in block.properties file of shader ").formatted(Formatting.YELLOW)).append(Text.literal(shaderPackName).formatted(Formatting.AQUA)));
            } else {
                if (propertiesEditContext.getMatchingCondition() == MatchingCondition.MATCH_WITH_PROPERTIES) {
                    this.addSourceToDestinationForProperties(source, destination);
                } else {
                    this.addSourceToDestinationForIdentifier(source, destination);
                }
                this.writeToFile();
                Iris.reload();
                Blopedit.addMessageToHud(Text.empty().append(Text.literal("Source property ").formatted(Formatting.GREEN)).append(Text.literal(source.toString()).formatted(Formatting.AQUA)).append(Text.literal(" added to block.properties file at location of ").formatted(Formatting.GREEN)).append(Text.literal(destination.toString()).formatted(Formatting.AQUA)));

            }
        }
    }

    private void addSourceToDestinationForIdentifier(PropertyEntry src, PropertyEntry dest){
        for(FileLine fileLine : this.fileLines){
            if (fileLine instanceof PropertyFileLine propertyFileLine && propertyFileLine.containsIdentifier(dest.getBlockIdentifier())){
                propertyFileLine.appendProperty(src);
            }
        }
    }

    private void addSourceToDestinationForProperties(PropertyEntry src, PropertyEntry dest){
        for(FileLine fileLine : this.fileLines){
            if(fileLine instanceof PropertyFileLine propertyFileLine && propertyFileLine.containsIdentifierWithProperties(dest)) {
                propertyFileLine.appendProperty(src);
            }
        }
    }

    private Set<PropertyEntry> getFirstMatchingEntriesForIdentifier(PropertyEntry propertyEntry) {
        Set<PropertyEntry> matchingEntries = new HashSet<>();
        for(FileLine fileLine : this.fileLines){
            if (fileLine instanceof PropertyFileLine propertyFileLine) {
                propertyFileLine.getFirstMatchingValueForIdentifier(propertyEntry).ifPresent(matchingEntries::add);
            }
        }
        return matchingEntries;
    }

    private Set<PropertyEntry> getFirstMatchingEntriesForProperties(PropertyEntry propertyEntry){
        Set<PropertyEntry> matchingEntries = new HashSet<>();
        for(FileLine fileLine : this.fileLines){
            if(fileLine instanceof PropertyFileLine propertyFileLine) {
                propertyFileLine.getFirstMatchingValueForProperties(propertyEntry).ifPresent(matchingEntries::add);
            }
        }
        return matchingEntries;
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

}
