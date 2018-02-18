package io.dico.dicore.nms.v1_8_R3;

import io.dico.dicore.nbt.ENbtType;
import io.dico.dicore.nbt.INbtList;
import io.dico.dicore.nbt.INbtMap;
import net.minecraft.server.v1_8_R3.*;

class NBT {
    
    private NBT() {
        
    }
    
    static NBTTagCompound getDelegate(INbtMap map) {
        return ((WrappedNbtMap) map.asWrapper()).delegate;
    }
    
    static NBTTagList getDelegate(INbtList list) {
        return ((WrappedNbtList) list.asWrapper()).delegate;
    }
    
    static Object fromNativeNbt(NBTBase nbt) {
        if (nbt == null) {
            return null;
        }
        switch (nbt.getTypeId()) {
            case 1:
                return ((NBTTagByte) nbt).f();
            case 2:
                return ((NBTTagShort) nbt).e();
            case 3:
                return ((NBTTagInt) nbt).d();
            case 4:
                return ((NBTTagLong) nbt).c();
            case 5:
                return ((NBTTagFloat) nbt).h();
            case 6:
                return ((NBTTagDouble) nbt).g();
            case 7:
                return ((NBTTagByteArray) nbt).c();
            case 8:
                return ((NBTTagString) nbt).a_();
            case 9:
                return new WrappedNbtList((NBTTagList) nbt);
            case 10:
                return new WrappedNbtMap((NBTTagCompound) nbt);
            case 11:
                return ((NBTTagIntArray) nbt).c();
            default:
                return null;
        }
    }
    
    static NBTBase toNativeNbt(Object nbt) {
        switch (ENbtType.of(nbt)) {
            case BYTE:
                return new NBTTagByte(((Number) nbt).byteValue());
            case SHORT:
                return new NBTTagShort(((Number) nbt).shortValue());
            case INT:
                return new NBTTagInt(((Number) nbt).intValue());
            case LONG:
                return new NBTTagLong(((Number) nbt).longValue());
            case FLOAT:
                return new NBTTagFloat(((Number) nbt).floatValue());
            case DOUBLE:
                return new NBTTagDouble(((Number) nbt).doubleValue());
            case STRING:
                return new NBTTagString((String) nbt);
            case BYTE_ARRAY:
                return new NBTTagByteArray((byte[]) nbt);
            case LIST:
                return ((WrappedNbtList) ((INbtList) nbt).asWrapper()).delegate;
            case MAP:
                return ((WrappedNbtMap) ((INbtMap) nbt).asWrapper()).delegate;
            case INT_ARRAY:
                return new NBTTagIntArray((int[]) nbt);
            default:
                throw new IllegalArgumentException();
        }
    }
    
    static ENbtType getElementType(byte elementTypeId) {
        switch (elementTypeId) {
            case 1:
                return ENbtType.BYTE;
            case 2:
                return ENbtType.SHORT;
            case 3:
                return ENbtType.INT;
            case 4:
                return ENbtType.LONG;
            case 5:
                return ENbtType.FLOAT;
            case 6:
                return ENbtType.DOUBLE;
            case 7:
                return ENbtType.BYTE_ARRAY;
            case 8:
                return ENbtType.STRING;
            case 9:
                return ENbtType.LIST;
            case 10:
                return ENbtType.MAP;
            case 11:
                return ENbtType.INT_ARRAY;
            default:
                return null;
        }
    }
    
    static byte getElementTypeId(ENbtType elementType) {
        switch (elementType) {
            case BYTE:
                return 1;
            case SHORT:
                return 2;
            case INT:
                return 3;
            case LONG:
                return 4;
            case FLOAT:
                return 5;
            case DOUBLE:
                return 6;
            case BYTE_ARRAY:
                return 7;
            case STRING:
                return 8;
            case LIST:
                return 9;
            case MAP:
                return 10;
            case INT_ARRAY:
                return 11;
        }
        throw new IllegalArgumentException();
    }
    
}
