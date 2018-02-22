package io.dico.dicore.nms.v1_8_R3;

import io.dico.dicore.SpigotUtil;
import io.dico.dicore.nbt.INbtList;
import io.dico.dicore.nbt.INbtMap;
import io.dico.dicore.nms.*;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftSound;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftItem;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

// instantiated reflectively by the NmsFactory
final class NmsDriver implements INmsDriver {
    /* normalize whatever it starts at so getServerTick() returns 0 in the first tick.
     * Like this it takes approximately 3.25 years for it to get to Integer.MAX_VALUE,
     * and users of the tick shouldn't have to take into account any overflow issues when calculating time differences and such.
     */
    private static final int tickOffset = -MinecraftServer.currentTick;
    private static final IEntityDriver entityDriver = new EntityDriver();
    private static final IItemDriver itemDriver = new ItemDriver();
    private static final IInventoryDriver inventoryDriver = new InventoryDriver();
    
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
    
    @Override
    public IInventoryDriver getInventoryDriver() {
        return inventoryDriver;
    }
    
    @Override
    public boolean isChunkLoaded(World world, int cx, int cz) {
        WorldServer handle = ((CraftWorld) world).getHandle();
        return _isChunkLoaded(handle, cx, cz);
    }
    
    static boolean _isChunkLoaded(WorldServer world, int cx, int cz) {
        IChunkProvider cp = world.N();
        return cp.isChunkLoaded(cx, cz) && !cp.getOrCreateChunk(cx, cz).isEmpty();
    }
    
    @Override
    public boolean isChunkLoaded(Block block) {
        return isChunkLoaded(block.getWorld(), MathHelper.floor(block.getX() / 16), MathHelper.floor(block.getZ() / 16));
    }
    
    @Override
    public org.bukkit.entity.Item dropItemNaturally(Block block, ItemStack itemStack) {
        net.minecraft.server.v1_8_R3.World world = ((CraftWorld) block.getWorld()).getHandle();
        double d0 = (double) (world.random.nextFloat() * 0.5F) + 0.25;
        double d1 = (double) (world.random.nextFloat() * 0.5F) + 0.25;
        double d2 = (double) (world.random.nextFloat() * 0.5F) + 0.25;
        EntityItem item = new EntityItem(world, block.getX() + d0, block.getY() + d1, block.getZ() + d2, CraftItemStack.asNMSCopy(itemStack));
        item.p();
        world.addEntity(item);
        CraftItem rv = new CraftItem(world.getServer(), item);
        EntityDriver._setBukkitEntity(item, rv);
        return rv;
    }
    
    @Override
    public String asJsonString(Object obj) {
        return SpigotUtil.asJsonString(null, obj, 0, (list, builder) -> {
            if (list instanceof INbtList) {
                builder.append(" etype: ").append(((INbtList) list).getElementType());
            }
        });
    }
    
}
