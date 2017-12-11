package io.dico.dicore;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Attachable;
import org.bukkit.material.MaterialData;
import org.bukkit.projectiles.ProjectileSource;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SpigotUtil {
    
    private SpigotUtil() {
        throw new UnsupportedOperationException();
    }
    
    public static World matchWorld(String input) {
        try {
            UUID uid = UUID.fromString(input);
            World world = Bukkit.getWorld(uid);
            if (world != null) {
                return world;
            }
        } catch (IllegalArgumentException ignored) {
        }
        
        World result = Bukkit.getWorld(input);
        if (result == null) {
            input = input.toLowerCase().replace("_", "").replaceAll("-|_", "");
            for (World world : Bukkit.getWorlds()) {
                if (world.getName().toLowerCase().equals(input)) {
                    result = world;
                    break;
                }
            }
        }
        
        return result;
    }
    
    public static Block getSupportingBlock(Block block) {
        MaterialData data = block.getState().getData();
        if (data instanceof Attachable) {
            BlockFace attachedOn = ((Attachable) data).getAttachedFace();
            return block.getRelative(attachedOn);
        }
        return null;
    }
    
    public static boolean isItemPresent(ItemStack stack) {
        return stack != null && stack.getType() != Material.AIR && stack.getAmount() > 0;
    }
    
    public static boolean removeItems(Inventory from, ItemStack item, int amount) {
        for (Map.Entry<Integer, ? extends ItemStack> entry : from.all(item.getType()).entrySet()) {
            ItemStack stack = entry.getValue();
            if (item.isSimilar(stack)) {
                amount -= stack.getAmount();
                int stackAmount = -Math.min(0, amount);
                if (stackAmount == 0) {
                    from.setItem(entry.getKey(), null);
                } else {
                    stack.setAmount(stackAmount);
                }
            }
        }
        return amount <= 0;
    }
    
    public static BlockFace yawToFace(float yaw) {
        if ((yaw %= 360) < 0)
            yaw += 360;
        if (45 <= yaw && yaw < 135)
            return BlockFace.WEST;
        if (135 <= yaw && yaw < 225)
            return BlockFace.NORTH;
        if (225 <= yaw && yaw < 315)
            return BlockFace.EAST;
        return BlockFace.SOUTH;
    }
    
    public static void addItems(InventoryHolder entity, ItemStack... items) {
        Location dropLocation;
        if (entity instanceof Entity) {
            dropLocation = ((Entity) entity).getLocation();
        } else if (entity instanceof BlockState) {
            dropLocation = ((BlockState) entity).getLocation().add(0.5, 1, 0.5);
        } else {
            throw new IllegalArgumentException("Can't find location of this InventoryHolder: " + entity);
        }
        World world = dropLocation.getWorld();
        for (ItemStack toDrop : entity.getInventory().addItem(items).values()) {
            world.dropItemNaturally(dropLocation, toDrop);
        }
    }
    
    public static String asJsonString(Object object) {
        return asJsonString(null, object, 0);
    }
    
    public static String asJsonString(String key, Object object, int indentation) {
        String indent = new String(new char[indentation * 2]).replace('\0', ' ');
        StringBuilder builder = new StringBuilder(indent);
        if (key != null) {
            builder.append(key).append(": ");
        }
        if (object instanceof ConfigurationSerializable) {
            object = ((ConfigurationSerializable) object).serialize();
        }
        if (object instanceof Map) {
            builder.append("{\n");
            Map<?, ?> map = (Map) object;
            for (Map.Entry entry : map.entrySet()) {
                builder.append(asJsonString(String.valueOf(entry.getKey()), entry.getValue(), indentation + 1));
            }
            builder.append(indent).append("}");
        } else if (object instanceof List) {
            builder.append("[\n");
            List list = (List) object;
            for (Object entry : list) {
                builder.append(asJsonString(null, entry, indentation + 1));
            }
            builder.append(indent).append("]");
        } else {
            builder.append(String.valueOf(object));
        }
        return builder.append(",\n").toString();
    }
    
    public static BlockFace estimateDirectionTo(Location from, Location to) {
        double dx = from.getX() - to.getX();
        double dz = from.getZ() - to.getZ();
    
        boolean xGreater = Math.abs(dx) - Math.abs(dz) > 0;
        double f = xGreater ? 2 / Math.abs(dx) : 2 / Math.abs(dz);
        dx *= f;
        dz *= f;
    
        double other = Math.abs(xGreater ? dz : dx);
    
        if (other <= .5) {
            return xGreater ? (dx < 0 ? BlockFace.WEST : BlockFace.EAST) : (dz < 0 ? BlockFace.NORTH : BlockFace.SOUTH);
        }
    
        if (other < 1.5) {
            if (xGreater) {
                return dx < 0 ? (dz < 0 ? BlockFace.WEST_NORTH_WEST : BlockFace.WEST_SOUTH_WEST) : (dz < 0 ? BlockFace.EAST_NORTH_EAST : BlockFace.EAST_SOUTH_EAST);
            }
            return dx < 0 ? (dz < 0 ? BlockFace.NORTH_NORTH_WEST : BlockFace.SOUTH_SOUTH_WEST) : (dz < 0 ? BlockFace.NORTH_NORTH_EAST : BlockFace.SOUTH_SOUTH_EAST);
        }
    
        return dx < 0 ? (dz < 0 ? BlockFace.NORTH_WEST : BlockFace.SOUTH_WEST) : (dz < 0 ? BlockFace.NORTH_EAST : BlockFace.SOUTH_EAST);
    }
    
    public static Entity findEntityFromDamager(Entity damager, EntityType searched) {
        if (damager.getType() == searched) {
            return damager;
        }
        
        if (damager instanceof Projectile) {
            ProjectileSource shooter = ((Projectile) damager).getShooter();
            if (shooter instanceof Entity && ((Entity) shooter).getType() == searched) {
                return (Entity) shooter;
            }
            return null;
        }
        
        if (damager.getType() == EntityType.PRIMED_TNT) {
            Entity source = ((TNTPrimed) damager).getSource();
            if (source.getType() == searched) {
                return source;
            }
        }
    
        return null;
    }
    
}
