package xd.arkosammy.blopedit.files;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import xd.arkosammy.blopedit.Blopedit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class BlockPropertiesFile {

    private static BlockPropertiesFile cachedPropertiesFile;

    private final String shaderName;
    private final List<FileLine> fileLines = new ArrayList<>();
    private final Path shaderPath;
    private final List<PropertyLine> cachedMatches = new ArrayList<>();

    public static BlockPropertiesFile getInstance(String shaderPackName) {
        if(cachedPropertiesFile == null || !cachedPropertiesFile.shaderName.equals(shaderPackName)){
            cachedPropertiesFile = new BlockPropertiesFile(shaderPackName);
        }
        return cachedPropertiesFile;
    }

    private BlockPropertiesFile(String shaderPackName) {

        this.shaderName = shaderPackName;
        this.shaderPath = FabricLoader.getInstance().getGameDir().resolve("shaderpacks").resolve(shaderPackName);
        try(FileSystem fs = FileSystems.newFileSystem(this.shaderPath, (ClassLoader) null);
            BufferedReader br = Files.newBufferedReader(fs.getPath("/shaders/block.properties"))) {
            String currentLine = br.readLine();
            while(currentLine != null){
                if(currentLine.trim().isEmpty()){
                    this.fileLines.add(new EmptyLine());
                } else if (currentLine.trim().charAt(0) == '#') {
                    this.fileLines.add(new CommentLine(currentLine));
                } else {
                    this.fileLines.add(new PropertyLine(currentLine));
                }
                currentLine = br.readLine();
            }

        } catch (IOException e) {
            Blopedit.LOGGER.error("Error attempting to read block.properties file from shader " + shaderPackName + ": " + e);
        }

    }

    public void addIdentifierToDestination(Identifier src, Identifier dest){

        // Remove already present value before appending it again. TODO: Ask user for confirmation on this step!
        // TODO: DO NOT DELETE VALUE IF THE DESTINATION CANNOT BE FOUND
        for(FileLine fileLine : this.fileLines){
            if(fileLine instanceof PropertyLine propertyLine){

                if(propertyLine.containsIdentifier(src)){
                    Blopedit.LOGGER.info("Removed " + src + " from line: " + propertyLine.getString());
                    propertyLine.removeIdentifier(src);
                }
            }
        }

        for(FileLine fileLine : this.fileLines){
            if(fileLine instanceof PropertyLine propertyLine){
                if (propertyLine.containsIdentifier(dest)) {
                    propertyLine.appendIdentifier(src);
                    Blopedit.LOGGER.info("Added " + src + " to line " + propertyLine.getString());
                    break;
                }
            }
        }

        this.updateFile();

    }

    public void updateFile(){

        try(FileSystem fs = FileSystems.newFileSystem(this.shaderPath);
            BufferedWriter bf = Files.newBufferedWriter(fs.getPath("/shaders/block.properties"))) {

            for(FileLine fileLine : this.fileLines){

                bf.write(fileLine.getString());
                bf.newLine();
            }

        } catch (IOException e) {

            Blopedit.LOGGER.error("Error writing to block.properties file of shader " + this.shaderName + ": " + e);

        }

    }

}
