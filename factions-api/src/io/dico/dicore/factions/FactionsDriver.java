package io.dico.dicore.factions;

import org.bukkit.entity.Player;

public interface FactionsDriver {
    
    FactionsPlayer getFactionsPlayer(Player player);
    
    Relation getRelationBetween(Player player1, Player player2);
    
}
