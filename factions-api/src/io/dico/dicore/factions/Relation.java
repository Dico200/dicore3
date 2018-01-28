package io.dico.dicore.factions;

public enum Relation {
    MEMBER,
    ALLY,
    TRUCE,
    NEUTRAL,
    ENEMY;
    
    public boolean isAtLeast(Relation rel) {
        return ordinal() <= rel.ordinal();
    }
    
    public boolean isAtMost(Relation rel) {
        return ordinal() >= rel.ordinal();
    }
    
}
