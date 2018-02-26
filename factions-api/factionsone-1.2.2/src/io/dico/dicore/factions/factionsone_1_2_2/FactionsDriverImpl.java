package io.dico.dicore.factions.factionsone_1_2_2;

import com.massivecraft.factions.*;
import com.massivecraft.factions.iface.RelationParticipator;
import com.massivecraft.factions.struct.Rel;
import io.dico.dicore.factions.IFactionsDriver;
import io.dico.dicore.factions.IFactionsPlayer;
import io.dico.dicore.factions.Relation;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

final class FactionsDriverImpl implements IFactionsDriver {
    
    public static void checkPluginMatch() {
        PluginDescriptionFile pdf = Bukkit.getPluginManager().getPlugin("Factions").getDescription();
        if (!"1.8.2".equals(pdf.getVersion()) || !"FactionsOne v1.2.2".equals(Patch.getFullName())) {
            throw new RuntimeException();
        }
    }
    
    @Override
    public IFactionsPlayer getFactionsPlayer(Player player) {
        return new FPlayerWrapper(FPlayers.i.get(player));
    }
    
    @Override
    public Relation getRelationBetween(Player player1, Player player2) {
        return getRelation(FPlayers.i.get(player1), FPlayers.i.get(player2));
    }
    
    private static Relation getRelation(RelationParticipator first, FPlayer second) {
        Rel rv = first.getRelationTo(second);
        if (rv.isAtLeast(Rel.MEMBER)) {
            Faction faction = second.getFaction();
            if (!faction.isNormal()) {
                rv = Rel.NEUTRAL;
            }
        }
        
        return relFromExRel(rv);
    }
    
    private static class FPlayerWrapper implements IFactionsPlayer {
        private final FPlayer fPlayer;
        
        public FPlayerWrapper(FPlayer fPlayer) {
            this.fPlayer = fPlayer;
        }
        
        public FPlayer getDelegate() {
            return fPlayer;
        }
        
        @Override
        public Player getPlayer() {
            return fPlayer.getPlayer();
        }
        
        @Override
        public Relation getRelationTo(IFactionsPlayer otherPlayer) {
            return getRelation(fPlayer, ((FPlayerWrapper) otherPlayer).getDelegate());
        }
        
        @Override
        public Relation getRelationTo(Player otherPlayer) {
            return getRelation(fPlayer, FPlayers.i.get(otherPlayer));
        }
        
        @Override
        public Relation getRelationToTerritory(World world, int chunkX, int chunkZ) {
            return getRelation(Board.getFactionAt(new FLocation(world.getName(), chunkX, chunkZ)), fPlayer);
        }
    }
    
    private static Relation relFromExRel(Rel rel) {
        switch (rel) {
            case ALLY:
                return Relation.ALLY;
            case TRUCE:
                return Relation.TRUCE;
            case LEADER:
            case OFFICER:
            case RECRUIT:
            case MEMBER:
                return Relation.MEMBER;
            case NEUTRAL:
                return Relation.NEUTRAL;
            case ENEMY:
                return Relation.ENEMY;
            default: {
                if (rel.isAtLeast(Rel.MEMBER)) {
                    return Relation.MEMBER;
                }
                if (rel.isAtLeast(Rel.ALLY)) {
                    return Relation.ALLY;
                }
                if (rel.isAtLeast(Rel.TRUCE)) {
                    return Relation.TRUCE;
                }
                if (rel.isAtLeast(Rel.NEUTRAL)) {
                    return Relation.NEUTRAL;
                }
                if (rel.isAtLeast(Rel.ENEMY)) {
                    return Relation.ENEMY;
                }
                
                return Relation.NEUTRAL;
            }
        }
    }
    
}
