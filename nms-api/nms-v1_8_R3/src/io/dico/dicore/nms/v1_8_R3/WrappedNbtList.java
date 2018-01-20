package io.dico.dicore.nms.v1_8_R3;

import io.dico.dicore.Reflection;
import io.dico.dicore.nbt.ENbtType;
import io.dico.dicore.nbt.INbtList;
import io.dico.dicore.nbt.INbtMap;
import net.minecraft.server.v1_8_R3.*;

import java.lang.reflect.Field;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.RandomAccess;
import java.util.function.Supplier;

final class WrappedNbtList extends AbstractList<Object> implements INbtList, RandomAccess {
    private static final Field nbtListField = Reflection.restrictedSearchField(NBTTagList.class, "list");
    final NBTTagList delegate;
    private final List<NBTBase> list;
    
    WrappedNbtList(NBTTagList delegate) {
        this.delegate = delegate;
        
        List<NBTBase> list = Reflection.getFieldValue(nbtListField, delegate);
        if (list == null) {
            Reflection.setFieldValue(nbtListField, delegate, list = new ArrayList<>());
        }
        this.list = list;
    }
    
    @Override
    public ENbtType getElementType() {
        return NBT.getElementType(delegate.getTypeId());
    }
    
    @Override
    public boolean isWrapper() {
        return true;
    }
    
    @Override
    public INbtList asWrapper() {
        return this;
    }
    
    @Override
    public Object get(int index) {
        return NBT.fromNativeNbt(list.get(index));
    }
    
    @Override
    public int size() {
        return list.size();
    }
    
    @Override
    public boolean add(Object o) {
        return list.add(NBT.toNativeNbt(o));
    }
    
    @Override
    public Object set(int index, Object element) {
        return NBT.fromNativeNbt(list.set(index, NBT.toNativeNbt(element)));
    }
    
    @Override
    public void add(int index, Object element) {
        list.add(index, NBT.toNativeNbt(element));
    }
    
    @Override
    public Object remove(int index) {
        return NBT.fromNativeNbt(list.remove(index));
    }
    
    @Override
    public int indexOf(Object o) {
        return list.indexOf(NBT.toNativeNbt(o));
    }
    
    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(NBT.toNativeNbt(o));
    }
    
    @Override
    public void clear() {
        list.clear();
    }
    
    
    @Override
    public INbtMap getMap(int index, INbtMap absent) {
        NBTBase result = list.get(index);
        if (result instanceof NBTTagCompound) {
            return new WrappedNbtMap((NBTTagCompound) result);
        }
        return absent;
    }
    
    @Override
    public INbtMap getMap(int index, Supplier<INbtMap> absent) {
        INbtMap result = getMap(index, (INbtMap) null);
        if (result == null) {
            result = absent.get();
        }
        return result;
    }
    
    @Override
    public INbtMap getPresentMap(int index, INbtMap absent) {
        INbtMap result = getMap(index, (INbtMap) null);
        if (result == null) {
            set(index, result = absent);
        }
        return result;
    }
    
    @Override
    public INbtMap getPresentMap(int index, Supplier<INbtMap> absent) {
        INbtMap result = getPresentMap(index, (INbtMap) null);
        if (result == null) {
            set(index, result = absent.get());
        }
        return result;
    }
    
    @Override
    public INbtList getList(int index, INbtList absent) {
        NBTBase result = list.get(index);
        if (result instanceof NBTTagList) {
            return new WrappedNbtList((NBTTagList) result);
        }
        return absent;
    }
    
    @Override
    public INbtList getList(int index, Supplier<INbtList> absent) {
        INbtList result = getList(index, (INbtList) null);
        if (result == null) {
            result = absent.get();
        }
        return result;
    }
    
    @Override
    public INbtList getPresentList(int index, INbtList absent) {
        INbtList result = getList(index, (INbtList) null);
        if (result == null) {
            set(index, result = absent);
        }
        return result;
    }
    
    @Override
    public INbtList getPresentList(int index, Supplier<INbtList> absent) {
        INbtList result = getList(index, (INbtList) null);
        if (result == null) {
            set(index, result = absent.get());
        }
        return result;
    }
    
    @Override
    public int[] getIntArray(int index, int[] absent) {
        NBTBase result = list.get(index);
        if (result instanceof NBTTagIntArray) {
            return ((NBTTagIntArray) result).c();
        }
        return absent;
    }
    
    @Override
    public byte[] getByteArray(int index, byte[] absent) {
        NBTBase result = list.get(index);
        if (result instanceof NBTTagByteArray) {
            return ((NBTTagByteArray) result).c();
        }
        return absent;
    }
    
    @Override
    public double getDouble(int index, double absent) {
        NBTBase result = list.get(index);
        if (result instanceof NBTTagDouble) {
            return ((NBTTagDouble) result).g();
        }
        return absent;
    }
    
    @Override
    public float getFloat(int index, float absent) {
        NBTBase result = list.get(index);
        if (result instanceof NBTTagFloat) {
            return ((NBTTagFloat) result).h();
        }
        return absent;
    }
    
    @Override
    public String getString(int index, String absent) {
        NBTBase result = list.get(index);
        if (result instanceof NBTTagString) {
            return ((NBTTagString) result).a_();
        }
        return absent;
    }
    
    @Override
    public long getLong(int index, long absent) {
        NBTBase result = list.get(index);
        if (result instanceof NBTTagLong) {
            return ((NBTTagLong) result).c();
        }
        return absent;
    }
    
    @Override
    public int getInt(int index, int absent) {
        NBTBase result = list.get(index);
        if (result instanceof NBTTagInt) {
            return ((NBTTagInt) result).d();
        }
        return absent;
    }
    
    @Override
    public short getShort(int index, int absent) {
        NBTBase result = list.get(index);
        if (result instanceof NBTTagShort) {
            return ((NBTTagShort) result).e();
        }
        return (short) absent;
    }
    
    @Override
    public byte getByte(int index, int absent) {
        NBTBase result = list.get(index);
        if (result instanceof NBTTagByte) {
            return ((NBTTagByte) result).f();
        }
        return (byte) absent;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WrappedNbtList)) return false;
        
        WrappedNbtList objects = (WrappedNbtList) o;
        
        return list.equals(objects.list);
    }
    
    @Override
    public int hashCode() {
        return list.hashCode();
    }
    
    @Override
    public String toString() {
        return "INbtList[" + delegate.toString() + "]";
    }
    
}
