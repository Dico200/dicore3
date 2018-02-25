package io.dico.dicore.config;

import gnu.trove.list.linked.TIntLinkedList;
import io.dico.dicore.Logging;

public final class ConfigLogging implements Logging {
    private Logging delegate;
    private StringBuilder currentPrefix;
    private TIntLinkedList prefixStack;
    private TIntLinkedList indexStack;
    
    public ConfigLogging(Logging delegate, String prefix) {
        this.delegate = delegate;
        this.currentPrefix = new StringBuilder(prefix);
        this.prefixStack = new TIntLinkedList();
        this.indexStack = new TIntLinkedList();
    }
    
    public void enterPrefix(String prefix) {
        currentPrefix.append(prefix);
        prefixStack.add(prefix.length());
    }
    
    public void enterIndexPrefix(int index) {
        enterPrefix("[" + index + "]");
    }
    
    public void enterSubPrefix(String key) {
        enterPrefix("." + key);
    }
    
    public void enterList(String key) {
        enterPrefix("." + key);
        indexStack.add(0);
        enterNextElementPrefix();
    }
    
    public void enterNextElementPrefix() {
        if (indexStack.isEmpty()) {
            return;
        }
        int index = indexStack.get(indexStack.size() - 1);
        if (index > 0) {
            exitPrefix();
        }
        enterIndexPrefix(index);
        indexStack.set(indexStack.size() - 1, index + 1);
    }
    
    public void exitList() {
        if (indexStack.isEmpty()) {
            return;
        }
        int index = indexStack.removeAt(indexStack.size() - 1);
        if (index > 0) {
            exitPrefix();
        }
        exitPrefix();
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
