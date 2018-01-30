package io.dico.dicore.command;

import io.dico.dicore.command.chat.ChatControllers;
import io.dico.dicore.command.chat.IChatController;
import io.dico.dicore.command.predef.HelpCommand;
import io.dico.dicore.command.predef.PredefinedCommand;

import java.util.*;

public abstract class ModifiableCommandAddress implements ICommandAddress {
    Map<String, ChildCommandAddress> children;
    IChatController chatController;
    ModifiableCommandAddress helpChild;
    
    public ModifiableCommandAddress() {
        this.children = new LinkedHashMap<>(4);
    }
    
    @Override
    public boolean hasParent() {
        return getParent() != null;
    }
    
    @Override
    public boolean hasCommand() {
        return getCommand() != null;
    }
    
    @Override
    public boolean hasUserDeclaredCommand() {
        Command command = getCommand();
        return command != null && !(command instanceof PredefinedCommand);
    }
    
    @Override
    public Command getCommand() {
        return null;
    }
    
    @Override
    public boolean isRoot() {
        return false;
    }
    
    @Override
    public List<String> getNames() {
        return null;
    }
    
    @Override
    public List<String> getAliases() {
        List<String> names = getNames();
        if (names == null) {
            return null;
        }
        if (names.isEmpty()) {
            return Collections.emptyList();
        }
        return names.subList(1, names.size());
    }
    
    @Override
    public String getMainKey() {
        return null;
    }
    
    @Override
    public String getAddress() {
        return null;
    }
    
    public void setCommand(Command command) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public abstract ModifiableCommandAddress getParent();
    
    @Override
    public RootCommandAddress getRoot() {
        ModifiableCommandAddress out = this;
        while (out.hasParent()) {
            out = out.getParent();
        }
        return out.isRoot() ? (RootCommandAddress) out : null;
    }
    
    @Override
    public int getDepth() {
        int depth = 0;
        ICommandAddress address = this;
        while (address.hasParent()) {
            address = address.getParent();
            depth++;
        }
        return depth;
    }
    
    @Override
    public boolean isDepthLargerThan(int value) {
        int depth = 0;
        ICommandAddress address = this;
        do {
            if (depth > value) {
                return true;
            }
            
            address = address.getParent();
            depth++;
        } while (address != null);
        return false;
    }
    
    @Override
    public Map<String, ? extends ModifiableCommandAddress> getChildren() {
        return Collections.unmodifiableMap(children);
    }
    
    @Override
    public ChildCommandAddress getChild(String key) {
        return children.get(key);
    }
    
    public void addChild(ICommandAddress child) {
        if (!(child instanceof ChildCommandAddress)) {
            throw new IllegalArgumentException("Argument must be a ChildCommandAddress");
        }
        
        ChildCommandAddress mChild = (ChildCommandAddress) child;
        if (mChild.parent != null) {
            throw new IllegalArgumentException("Argument already has a parent");
        }
        
        if (mChild.names.isEmpty()) {
            throw new IllegalArgumentException("Argument must have names");
        }
        
        Iterator<String> names = mChild.modifiableNamesIterator();
        children.put(names.next(), mChild);
        
        while (names.hasNext()) {
            String name = names.next();
            if (children.putIfAbsent(name, mChild) != null) {
                names.remove();
            }
        }
        
        mChild.setParent(this);
        
        if (mChild.hasCommand() && mChild.getCommand() instanceof HelpCommand) {
            helpChild = mChild;
        }
    }
    
    public boolean hasHelpCommand() {
        return helpChild != null;
    }
    
    public ModifiableCommandAddress getHelpCommand() {
        return helpChild;
    }
    
    @Override
    public IChatController getChatController() {
        ModifiableCommandAddress cur = this;
        while (cur.chatController == null && cur.hasParent()) {
            cur = cur.getParent();
        }
        return cur.chatController == null ? ChatControllers.defaultChat() : cur.chatController;
    }
    
    public void setChatController(IChatController chatController) {
        this.chatController = chatController;
    }
    
    @Override
    public ICommandDispatcher getDispatcherForTree() {
        return getRoot();
    }
    
}
