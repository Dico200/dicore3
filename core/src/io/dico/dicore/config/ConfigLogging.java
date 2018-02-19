package io.dico.dicore.config;

import gnu.trove.list.linked.TIntLinkedList;
import io.dico.dicore.Logging;

public final class ConfigLogging implements Logging {
    private Logging delegate;
    private StringBuilder currentPrefix;
    private TIntLinkedList prefixStack;
    
    public ConfigLogging(Logging delegate, String prefix) {
        this.delegate = delegate;
        this.currentPrefix = new StringBuilder(prefix);
        this.prefixStack = new TIntLinkedList();
    }
    
    public void enterPrefix(String prefix) {
        currentPrefix.append(prefix);
        prefixStack.add(prefix.length());
    }
    
    public void enterIndexPrefix(int index) {
        enterPrefix("[" + index + "]");
    }
    
    public void exitPrefix() {
        if (prefixStack.isEmpty()) {
            return;
        }
        
        int size = prefixStack.removeAt(prefixStack.size() - 1);
        currentPrefix.delete(currentPrefix.length() - size, currentPrefix.length());
    }

    @Override
    public void info(Object o) {
        delegate.info(prefix(o));
    }

    @Override
    public void warn(Object o) {
        delegate.warn(prefix(o));
    }

    @Override
    public void error(Object o) {
        delegate.error(prefix(o));
    }

    @Override
    public void debug(Object o) {
        delegate.debug(prefix(o));
    }

    @Override
    public boolean isDebugging() {
        return false;
    }
    
    @Override
    public void setDebugging(boolean debugging) {
    
    }
    
    private String prefix(Object o) {
        int start = currentPrefix.length();
        try {
            return currentPrefix.append(" ").append(o).toString();
        } finally {
            currentPrefix.delete(start, currentPrefix.length());
        }
    }
    
}
