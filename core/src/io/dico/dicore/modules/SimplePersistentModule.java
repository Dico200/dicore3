package io.dico.dicore.modules;

import io.dico.dicore.serialization.FileAdapter;
import io.dico.dicore.serialization.JsonFileAdapter;
import io.dico.dicore.serialization.JsonLoadable;

import java.util.function.Consumer;

public abstract class SimplePersistentModule<Manager extends ModuleManager, Data extends JsonLoadable> extends PersistentModuleBase<Manager, Data> {
    
    protected SimplePersistentModule(String name, Manager manager, boolean usesConfig, boolean debugging) {
        super(name, manager, usesConfig, debugging);
    }
    
    @Override
    FileAdapter<Data> newAdapter(Consumer<Throwable> onErrorLoad, Consumer<Throwable> onErrorSave) {
        return JsonFileAdapter.create(this::generateDefaultData, onErrorLoad, onErrorSave);
    }
    
}
