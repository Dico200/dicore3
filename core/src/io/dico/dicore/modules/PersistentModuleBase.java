package io.dico.dicore.modules;

import io.dico.dicore.serialization.FileAdapter;
import io.dico.dicore.serialization.Saveable;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Objects;
import java.util.function.Consumer;

abstract class PersistentModuleBase<Manager extends ModuleManager, Data> extends Module<Manager> implements Saveable {
    private final FileAdapter<Data> fileAdapter;
    private final String file;
    private Data data;
    private boolean saveScheduled = false;
    
    protected PersistentModuleBase(String name, Manager manager, boolean usesConfig, boolean debugging) {
        super(name, manager, usesConfig, debugging);
        file = new File(getDataFolder(), "data.json").getAbsolutePath();
        
        final Consumer<Throwable> onErrorLoad = t -> {
            if (t instanceof FileNotFoundException) {
                return;
            }
            error("Error occurred whilst loading data for module " + getName());
            t.printStackTrace();
        };
        
        final Consumer<Throwable> onErrorSave = t -> {
            error("Failed to save data for module " + getName());
            t.printStackTrace();
        };
        
        fileAdapter = Objects.requireNonNull(newAdapter(onErrorLoad, onErrorSave));
    }
    
    abstract FileAdapter<Data> newAdapter(Consumer<Throwable> onErrorLoad, Consumer<Throwable> onErrorSave);
    
    protected abstract Data generateDefaultData();
    
    protected void dataLoaded() {
    }
    
    protected Data getData() {
        return data;
    }
    
    public boolean isSaveScheduled() {
        if (saveScheduled) {
            saveScheduled = false;
            return true;
        }
        return false;
    }
    
    public final void scheduleSave() {
        saveScheduled = true;
    }
    
    protected void loadData() {
        Data result = fileAdapter.load(file);
        if (result == null) {
            if (data == null) {
                data = generateDefaultData();
            } else {
                return;
            }
        } else {
            data = result;
        }
        dataLoaded();
    }
    
    protected void saveData() {
        if (data != null) {
            fileAdapter.save(data, file);
        }
    }
    
    @Override
    protected void update() {
        super.update();
        if (isSaveScheduled()) {
            saveData();
        }
    }
    
    @Override
    void setEnabled(boolean enabled) {
        if (enabled != isEnabled()) {
            super.setEnabled(enabled);
            
            if (!enabled) {
                saveData();
            }
            
        }
    }
}
