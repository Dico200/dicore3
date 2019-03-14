package io.dico.dicore.command;

import com.google.common.collect.ImmutableList;
import io.dico.dicore.command.predef.HelpCommand;

import java.util.*;

public class ChildCommandAddress extends ModifiableCommandAddress {
    ModifiableCommandAddress parent;
    List<String> names = new ArrayList<>(4);
    Command command;
    
    public ChildCommandAddress() {
    }
    
    public ChildCommandAddress(Command command) {
        this.command = command;
    }
    
    public ChildCommandAddress(Command command, String name, String... aliases) {
        this(command);
        addNameAndAliases(name, aliases);
    }
    
    public static ChildCommandAddress newPlaceHolderCommand(String name, String... aliases) {
        ChildCommandAddress rv = new ChildCommandAddress(null, name, aliases);
        HelpCommand.registerAsChild(rv);
        return rv;
    }
    
    @Override
    public boolean isRoot() {
        return false;
    }
    
    @Override
    public ModifiableCommandAddress getParent() {
        return parent;
    }
    
    @Override
    public Command getCommand() {
        return command;
    }
    
    @Override
    public void setCommand(Command command) {
        if (hasUserDeclaredCommand()) {
            throw new IllegalStateException("Command is already set at address \"" + getAddress() + "\"");
        }
        this.command = command;
    }
    
    @Override
    public List<String> getNames() {
        return names;
    }
    
    public void addNameAndAliases(String name, String... aliases) {
        names.add(name);
        names.addAll(Arrays.asList(aliases));
    }
    
    @Override
    public String getMainKey() {
        return names.isEmpty() ? null : names.get(0);
    }
    
    @Override
    public String getAddress() {
        ICommandAddress address = this;
        int depth = getDepth();
        String[] keys = new String[depth];
        for (int i = depth - 1; i >= 0; i--) {
            keys[i] = address.getMainKey();
            address = address.getParent();
        }
        return String.join(" ", keys);
    }
    
    public void finalizeNames() {
        if (names instanceof ArrayList) {
            names = ImmutableList.copyOf(names);
        }
    }
    
    Iterator<String> modifiableNamesIterator() {
        if (names instanceof ArrayList) {
            return names.iterator();
        }
        return new Iterator<String>() {
            List<String> names = ChildCommandAddress.this.names;
            int idx = 0;
            int size = names.size();
            boolean removed = true;
            
            @Override
            public boolean hasNext() {
                return idx < size;
            }
    
            @Override
            public String next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                removed = false;
                return names.get(idx++);
            }
    
            @Override
            public void remove() {
                if (removed) {
                    throw new IllegalStateException();
                }
                removed = true;
                idx--;
                size--;
                
                if (names instanceof ArrayList) {
                    names.remove(idx);
                } else {
                    names = new ArrayList<>(names);
                    names.remove(idx);
                    names = ImmutableList.copyOf(names);
                }
            }
        };
    }
    
    void setParent(ModifiableCommandAddress parent) {
        finalizeNames();
        this.parent = parent;
    }
    
}
