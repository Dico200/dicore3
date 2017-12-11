package io.dico.dicore.nms.v1_8_R3;

import io.dico.dicore.Reflection;
import io.dico.dicore.nbt.INbtMap;
import io.dico.dicore.nms.IItemDriver;
import net.minecraft.server.v1_8_R3.Blocks;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;

final class ItemDriver implements IItemDriver {
    private static final Field itemHandleField = Reflection.restrictedSearchField(CraftItemStack.class, "handle");
    
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
    
}
