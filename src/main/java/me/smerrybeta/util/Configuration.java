package me.smerrybeta.util;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class Configuration extends YamlConfiguration {
    Plugin plugin;
    private final String path;
    private final String name;
    private File customConfigFile = null;
    private final File file;
    public Configuration(Plugin plugin, String path){
        this.plugin = plugin;
        this.path = path;
        Path filePath = Paths.get(path);
        this.name = filePath.getFileName().toString();
        this.file = new File(plugin.getDataFolder() + "/" + filePath);
    }
    public void reloadCustomConfig() {
        if (!file.exists()) {
            if (plugin.getResource(path) == null) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                plugin.saveResource(path, true);
            }
        }

        try {
            super.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
    public void save() {
        try {
            super.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveDefaultConfig() {
        if (customConfigFile == null) {
            customConfigFile = new File(plugin.getDataFolder(), "customConfig.yml");
        }
        if (!customConfigFile.exists()) {
            plugin.saveResource("customConfig.yml", false);
        }
    }

    public void copyDefaults() {

        Reader defaultConfigSearchResult = null;

        if (plugin.getResource(path) != null) {
            defaultConfigSearchResult = new InputStreamReader(Objects.requireNonNull(plugin.getResource(path)), StandardCharsets.UTF_8);
        }

        if (defaultConfigSearchResult != null) {
            YamlConfiguration.loadConfiguration(defaultConfigSearchResult);
            saveDefaultConfig();
        }

        save();
    }

    public @NotNull String getName() { return name; }
}