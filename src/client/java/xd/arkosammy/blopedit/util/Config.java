package xd.arkosammy.blopedit.util;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import xd.arkosammy.blopedit.Blopedit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {

    private static Config instance = new Config();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("blopedit-config.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    private boolean autoReloadShaders = true;
    public static Config getInstance() {
        if(instance == null){
            instance = new Config();
        }
        return instance;
    }

    private Config(){}

    public boolean doAutoReloadShaders() {
        return this.autoReloadShaders;
    }

    public void setDoAutoReloadShaders(boolean autoReloadShaders){
        this.autoReloadShaders = autoReloadShaders;
        this.writeConfig();
    }

    public void writeConfig() {
        String json = GSON.toJson(getInstance());
        try(BufferedWriter bf = Files.newBufferedWriter(CONFIG_PATH)) {
            if(!Files.exists(CONFIG_PATH)) {
                Files.createFile(CONFIG_PATH);
            }
            bf.write(json);
        } catch (IOException e){
            Blopedit.LOGGER.error("Error writing to config file " + CONFIG_PATH + ": " + e);
        }
    }

    public void readConfig() {
        if(Files.exists(CONFIG_PATH)) {
            try(BufferedReader br = Files.newBufferedReader(CONFIG_PATH)) {
                instance = GSON.fromJson(br, Config.class);
            } catch (IOException e){
                Blopedit.LOGGER.error("Error reading from config file " + CONFIG_PATH + ": " + e);
            }
        } else {
            Blopedit.LOGGER.warn("Creating new config file at " + CONFIG_PATH + "...");
            getInstance().writeConfig();
        }
    }

}
