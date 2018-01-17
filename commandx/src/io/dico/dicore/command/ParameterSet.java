package io.dico.dicore.command;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ParameterSet {
    private LinkedHashMap<String, Parameter<?>> parameters = new LinkedHashMap<>();
    private transient List<Parameter<?>> ordered;
    private transient List<Parameter<?>> flags;
    private int requiredCount = -1;
    private String mergeTokens;
    private char mergeTokenEscapeChar;
   
    public void addParameter(Parameter<?> parameter) {
        if (ordered != null || parameters.putIfAbsent(parameter.getName(), parameter) != null) {
            throw new IllegalStateException();
        }
    }
    
    public Parameter<?> getParameter(String name) {
        return parameters.get(name);
    }
    
    public int getRequiredCount() {
        if (requiredCount == -1) {
            requiredCount = parameters.values().stream().filter(p -> !p.isFlag()).mapToInt(e -> 1).sum();
        }
        return requiredCount;
    }
    
    public String getMergeTokens() {
        return mergeTokens;
    }
    
    public char getMergeTokenEscapeChar() {
        return mergeTokenEscapeChar;
    }
    
    public void setMergeOptions(String mergeTokens, char escapeChar) {
        if (mergeTokens.length() % 2 != 0) {
            throw new IllegalArgumentException();
        }
        this.mergeTokens = mergeTokens;
        this.mergeTokenEscapeChar = escapeChar;
    }
    
    private void ensureBaked() {
        if (ordered != null) return;
        List<Parameter<?>> ordered = new ArrayList<>();
        List<Parameter<?>> flags = new ArrayList<>();
        
        for (Parameter<?> parameter : parameters.values()) {
            (parameter.isFlag() ? flags : ordered).add(parameter);
        }
        
        this.ordered = ordered;
        this.flags = flags;
    }
    
    List<Parameter<?>> getOrdered() {
        ensureBaked();
        return ordered;
    }
    
    List<Parameter<?>> getFlags() {
        ensureBaked();
        return flags;
    }

}
