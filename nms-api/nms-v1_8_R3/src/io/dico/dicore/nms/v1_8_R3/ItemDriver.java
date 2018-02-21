package io.dico.dicore.nms.v1_8_R3;

import io.dico.dicore.Reflection;
import io.dico.dicore.nbt.INbtMap;
import io.dico.dicore.nms.IItemDriver;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

final class ItemDriver implements IItemDriver {
    private static final Field itemHandleField = Reflection.restrictedSearchField(CraftItemStack.class, "handle");
    private static final Method applyToItemMethod = Reflection.restrictedSearchMethod(
            Bukkit.getItemFactory().getItemMeta(Material.STONE).getClass(),
            "applyToItem",
            NBTTagCompound.class);
    
    static net.minecraft.server.v1_8_R3.ItemStack getHandle(ItemStack item) {
        if (item instanceof CraftItemStack) {
            return getPresentHandle((CraftItemStack) item);
        }
        return CraftItemStack.asNMSCopy(item);
    }
    
    static net.minecraft.server.v1_8_R3.ItemStack getPresentHandle(CraftItemStack item) {
        net.minecraft.server.v1_8_R3.ItemStack result = Reflection.getFieldValue(itemHandleField, item);
        if (result == null) {
            result = new net.minecraft.server.v1_8_R3.ItemStack(Blocks.AIR);
            Reflection.setFieldValue(itemHandleField, item, result);
        }
        return result;
    }
    
    static CraftItemStack ensureHandlePresent(ItemStack item) {
        return item instanceof CraftItemStack ? (CraftItemStack) item : CraftItemStack.asCraftCopy(item);
    }
    
    @Override
    public ItemStack setTag(ItemStack item, INbtMap tag) {
        net.minecraft.server.v1_8_R3.ItemStack handle = getPresentHandle((CraftItemStack) (item = ensureHandlePresent(item)));
        handle.setTag(NBT.getDelegate(tag));
        return item;
    }
    
    @Override
    public INbtMap getTag(ItemStack item) {
        if (item instanceof CraftItemStack) {
            net.minecraft.server.v1_8_R3.ItemStack handle = getPresentHandle((CraftItemStack) item);
            NBTTagCompound tag = handle.getTag();
            if (tag == null) {
                handle.setTag(tag = new NBTTagCompound());
            }
            return new WrappedNbtMap(tag);
        }
        return INbtMap.EMPTY;
    }
    
    @Override
    public ItemStack setNBTElement(ItemStack item, String key, Object value) {
        net.minecraft.server.v1_8_R3.ItemStack handle = getPresentHandle((CraftItemStack) (item = ensureHandlePresent(item)));
        NBTTagCompound tag = handle.getTag();
        if (tag == null) {
            handle.setTag(tag = new NBTTagCompound());
        }
        
        tag.set(key, NBT.toNativeNbt(value));
        return item;
    }
    
    @Override
    public Object getNBTElement(ItemStack item, String key) {
        if (item instanceof CraftItemStack) {
            net.minecraft.server.v1_8_R3.ItemStack handle = Reflection.getFieldValue(itemHandleField, item);
            NBTTagCompound tag;
            if (handle != null && (tag = handle.getTag()) != null) {
                return NBT.fromNativeNbt(tag.get(key));
            }
        }
        return null;
    }
    
    @Override
    public ItemStack nmsMirror(ItemStack item) {
        return ensureHandlePresent(item);
    }
    
    @Override
    public ItemStack addItemMeta(ItemStack item, ItemMeta meta) {
        Objects.requireNonNull(meta);
        net.minecraft.server.v1_8_R3.ItemStack handle = getPresentHandle((CraftItemStack) (item = ensureHandlePresent(item)));
        NBTTagCompound tag = handle.getTag();
        if (tag == null) {
            handle.setTag(tag = new NBTTagCompound());
        }
    
        try {
            // virtual invoke
            applyToItemMethod.invoke(meta, tag);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    
        return item;
    }
    
    @Override
    public boolean updateTagRef(INbtMap map, ItemStack item) {
        Objects.requireNonNull(map);
        if (!(item instanceof CraftItemStack)) {
            return false;
        }
        
        if (!map.isWrapper()) {
            return false;
        }
        
        net.minecraft.server.v1_8_R3.ItemStack handle = getPresentHandle((CraftItemStack) item);
        NBTTagCompound tag = handle.getTag();
        if (tag == null) {
            handle.setTag(tag = new NBTTagCompound());
        }
    
        ((WrappedNbtMap) map).setDelegate(tag);
        return true;
    }
    
    @Override
    public boolean updateItemRef(ItemStack item, INbtMap map) {
        Objects.requireNonNull(map);
        if (!(item instanceof CraftItemStack)) {
            return false;
        }
    
        if (!map.isWrapper()) {
            return false;
        }
    
        net.minecraft.server.v1_8_R3.ItemStack handle = getPresentHandle((CraftItemStack) item);
        handle.setTag(((WrappedNbtMap) map).delegate);
        return true;
    }
    
    @Override
    public void setType(ItemStack item, Material type) {
        if (!(item instanceof CraftItemStack)) {
            item.setType(type);
            return;
        }
    
        net.minecraft.server.v1_8_R3.ItemStack handle = getPresentHandle((CraftItemStack) item);
        handle.setItem(CraftMagicNumbers.getItem(type));
    }
    
    @Override
    public ItemStack onBlockDestroyed(ItemStack item, Player player, Block block, Material type) {
        if (item == null) {
            return null;
        }
        
        net.minecraft.server.v1_8_R3.ItemStack itemHandle = getHandle(item);
        
        net.minecraft.server.v1_8_R3.Block typeHandle = CraftMagicNumbers.getBlock(type);
        WorldServer worldHandle = ((CraftWorld) block.getWorld()).getHandle();
        BlockPosition blockPos = new BlockPosition(block.getX(), block.getY(), block.getZ());
        EntityPlayer playerHandle = ((CraftPlayer) player).getHandle();
    
        itemHandle.a(worldHandle, typeHandle, blockPos, playerHandle);
    
        return itemHandle.count == 0 ? null : CraftItemStack.asCraftMirror(itemHandle);
    }
    
}
