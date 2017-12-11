package io.dico.dicore.nms.v1_8_R3;

import io.dico.dicore.nbt.INbtList;
import io.dico.dicore.nbt.INbtMap;
import io.dico.dicore.nms.IEntityDriver;
import io.dico.dicore.nms.IItemDriver;
import io.dico.dicore.nms.INmsDriver;
import io.dico.dicore.nms.IWorldDriver;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftSound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

// instantiated reflectively by the NmsFactory
final class NmsDriver implements INmsDriver {
    /* normalize whatever it starts at so getServerTick() returns 0 in the first tick.
     * Like this it takes approximately 3.25 years for it to get to Integer.MAX_VALUE,
     * and users of the tick shouldn't have to take into account any overflow issues when calculating time differences and such.
     */
    private static final int tickOffset = -MinecraftServer.currentTick;
    private static final IEntityDriver entityDriver = new EntityDriver();
    private static final IItemDriver itemDriver = new ItemDriver();
    
    public NmsDriver() {
    }
    
    @Override
    public int getServerTick() {
        return MinecraftServer.currentTick + tickOffset;
    }
    
    @Override
    public IWorldDriver getWorldDriver(World world) {
        return new WorldDriver(world);
    }
    
    @Override
    public INbtMap newWrappedNBTMap() {
        return new WrappedNbtMap(new NBTTagCompound());
    }
    
    @Override
    public INbtList newWrappedNBTList() {
        return new WrappedNbtList(new NBTTagList());
    }
    
    @Override
    public int getNutritionalValue(Material material) {
        Item nmsItem = Item.getById(material.getId());
        if (nmsItem instanceof ItemFood) {
            return ((ItemFood) nmsItem).getNutrition(null);
        }
        return 0;
    }
    
    @Override
    public void sendSoundPacket(Player player, Sound sound, float volume, float pitch) {
        EntityPlayer p = ((CraftPlayer) player).getHandle();
        p.playerConnection.sendPacket(new PacketPlayOutNamedSoundEffect(CraftSound.getSound(sound), p.locX, p.locY, p.locZ, volume, pitch));
    }
    
    @Override
    public void sendSoundPacket(Player player, Sound sound, Location loc, float volume, float pitch) {
        EntityPlayer p = ((CraftPlayer) player).getHandle();
        p.playerConnection.sendPacket(new PacketPlayOutNamedSoundEffect(CraftSound.getSound(sound), loc.getX(), loc.getY(), loc.getZ(), volume, pitch));
        
    }
    
    @Override
    public IEntityDriver getEntityDriver() {
        return entityDriver;
    }
    
    @Override
    public IItemDriver getItemDriver() {
        return itemDriver;
    }
    
}
