package io.dico.dicore.config;

import org.bukkit.configuration.ConfigurationSection;

public interface ConfigEntry {
    
    String getLocation();
    
    void load(ConfigurationSection section, ConfigLogging logger);
    
    void save(ConfigurationSection section, boolean firstCreated);
    
}
