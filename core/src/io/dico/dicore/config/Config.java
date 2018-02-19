package io.dico.dicore.config;

import io.dico.dicore.Logging;
import io.dico.dicore.Reflection;
import io.dico.dicore.serialization.FileAdapter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.ScalarStyle;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;

public final class Config {
    private final Logging logger;
    private final File file;
    private final Collection<ConfigEntry> configEntries = new ArrayList<>();
    
    public Config(Logging logger, File file) {
        this.logger = logger;
        this.file = file;
    }
    
    public void addConfigEntry(ConfigEntry entry) {
        configEntries.add(entry);
    }
    
    public void loadFromStream(InputStream stream) throws IOException, InvalidConfigurationException {
        try (Reader reader = new InputStreamReader(stream)) {
            loadFromReader(reader);
        }
    }
    
    public void loadFromReader(Reader reader) throws IOException, InvalidConfigurationException {
        YamlConfiguration config = new YamlConfiguration();
        config.load(reader);
        
        ConfigLogging logger = new ConfigLogging(this.logger, "[" + file.getName() + "]");
        for (ConfigEntry configEntry : this.configEntries) {
            try {
                configEntry.load(config, logger);
            } catch (Exception ex) {
                logger.error("Failed to load config at location " + configEntry.getLocation() + ":");
                ex.printStackTrace();
            }
        }
    }
    
    public void saveToStream(OutputStream stream, boolean created) throws IOException {
        try (Writer writer = new OutputStreamWriter(stream)) {
            saveToWriter(writer, created);
        }
    }
    
    public void saveToWriter(Writer writer, boolean created) throws IOException {
        YamlConfiguration config = new YamlConfiguration();
        Reflection.<DumperOptions>getFieldValue(YamlConfiguration.class, "yamlOptions", config).setDefaultScalarStyle(ScalarStyle.DOUBLE_QUOTED);
    
        ConfigLogging logger = new ConfigLogging(this.logger, "[" + file.getName() + "]");
        for (ConfigEntry configEntry : this.configEntries) {
            try {
                configEntry.save(config, created);
            } catch (Exception ex) {
                logger.error("Failed to save config at location " + configEntry.getLocation() + ":");
                ex.printStackTrace();
            }
        }
        
        String content = config.saveToString();
        writer.append(content);
    }
    
    public void load() {
        try {
            File file = this.file;
            if (file.exists()) {
    
                try (InputStream stream = new FileInputStream(file)) {
                    loadFromStream(stream);
                } catch (InvalidConfigurationException e) {
                    e.printStackTrace();
                }
                
            } else {
    
                file = FileAdapter.fileAt(file.getPath(), true);
                try (OutputStream stream = new FileOutputStream(file)) {
                    saveToStream(stream, true);
                }
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void save() {
        try {
            File file = FileAdapter.fileAt(this.file.getPath(), true);
            try (OutputStream stream = new FileOutputStream(file)) {
                saveToStream(stream, false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
