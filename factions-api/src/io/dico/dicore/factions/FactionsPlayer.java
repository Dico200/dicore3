package io.dico.dicore.factions;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public interface FactionsPlayer {
    
    Player getPlayer();
    
    default Relation getRelationTo(Player otherPlayer) {
        return FactionsFactory.getDriver().getFactionsPlayer(otherPlayer).getRelationTo(this);
    }
    
    Relation getRelationTo(FactionsPlayer otherPlayer);
    
    default Relation getRelationToCurrentTerritory() {
        return getRelationToTerritory(getPlayer().getLocation());
    }
    
    default Relation getRelationToTerritory(Chunk chunk) {
        return getRelationToTerritory(chunk.getWorld(), chunk.getX(), chunk.getZ());
    }
    
    default Relation getRelationToTerritory(Location territory) {
        return getRelationToTerritory(territory.getWorld(), territory.getBlockX() >> 4, territory.getBlockZ() >> 4);
    }
    
    Relation getRelationToTerritory(World world, int chunkX, int chunkZ);
    
}
