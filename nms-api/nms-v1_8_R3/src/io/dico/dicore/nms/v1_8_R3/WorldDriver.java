package io.dico.dicore.nms.v1_8_R3;

import io.dico.dicore.Reflection;
import io.dico.dicore.nms.IWorldDriver;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;

import java.lang.reflect.Field;
import java.util.*;

final class WorldDriver implements IWorldDriver {
    private static final Field entitiesField = Reflection.restrictedSearchField(WorldServer.class, "entitiesByUUID");
    private final WorldServer world;
    private Map<UUID, Entity> entities;
    
    WorldDriver(World world) {
        this(((CraftWorld) world).getHandle());
    }
    
    WorldDriver(WorldServer world) {
        this.world = Objects.requireNonNull(world);
    }
    
    @Override
    public World getBukkitWorld() {
        return world.getWorld();
    }
    
    @Override
    public Entity getEntityByUUID(UUID uuid) {
        net.minecraft.server.v1_8_R3.Entity result = world.getEntity(uuid);
        return result == null ? null : result.getBukkitEntity();
    }
    
    @Override
    public Map<UUID, Entity> getEntities() {
        if (entities == null) {
            entities = new EntityMap(Reflection.getFieldValue(entitiesField, world));
        }
        return entities;
    }
    
    static final class EntityMap implements Map<UUID, Entity> {
        private final Map<UUID, net.minecraft.server.v1_8_R3.Entity> delegate;
        
        static net.minecraft.server.v1_8_R3.Entity getHandle(Entity entity) {
            return entity == null ? null : ((CraftEntity) entity).getHandle();
        }
        
        static Entity getBukkitEntity(net.minecraft.server.v1_8_R3.Entity entity) {
            return entity == null ? null : entity.getBukkitEntity();
        }
        
        public EntityMap(Map<UUID, net.minecraft.server.v1_8_R3.Entity> delegate) {
            this.delegate = delegate;
        }
        
        @Override
        public int size() {
            return delegate.size();
        }
        
        @Override
        public boolean isEmpty() {
            return size() == 0;
        }
        
        @Override
        public boolean containsKey(Object key) {
            return delegate.containsKey(key);
        }
        
        @Override
        public boolean containsValue(Object value) {
            if (value instanceof CraftEntity) {
                return delegate.containsValue(((CraftEntity) value).getHandle());
            }
            return false;
        }
        
        @Override
        public Entity get(Object key) {
            return getBukkitEntity(delegate.get(key));
        }
        
        @Override
        public Entity put(UUID key, Entity value) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public Entity remove(Object key) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void putAll(Map<? extends UUID, ? extends Entity> m) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public Set<UUID> keySet() {
            return Collections.unmodifiableSet(delegate.keySet());
        }
        
        @Override
        public Collection<Entity> values() {
            return new AbstractCollection<Entity>() {
                @Override
                public Iterator<Entity> iterator() {
                    return new Iterator<Entity>() {
                        Iterator<net.minecraft.server.v1_8_R3.Entity> iterator = delegate.values().iterator();
                        
                        @Override
                        public boolean hasNext() {
                            return iterator.hasNext();
                        }
                        
                        @Override
                        public Entity next() {
                            return getBukkitEntity(iterator.next());
                        }
                    };
                }
                
                @Override
                public int size() {
                    return EntityMap.this.size();
                }
                
                @Override
                public boolean contains(Object o) {
                    return EntityMap.this.containsValue(o);
                }
            };
        }
        
        @Override
        public Set<Entry<UUID, Entity>> entrySet() {
            return new AbstractSet<Entry<UUID, Entity>>() {
                @Override
                public Iterator<Entry<UUID, Entity>> iterator() {
                    return new Iterator<Entry<UUID, Entity>>() {
                        Iterator<Entry<UUID, net.minecraft.server.v1_8_R3.Entity>> iterator = EntityMap.this.delegate.entrySet().iterator();
                        
                        @Override
                        public boolean hasNext() {
                            return iterator.hasNext();
                        }
                        
                        @Override
                        public Entry<UUID, Entity> next() {
                            Entry<UUID, net.minecraft.server.v1_8_R3.Entity> entry = iterator.next();
                            return new AbstractMap.SimpleEntry<>(entry.getKey(), getBukkitEntity(entry.getValue()));
                        }
                    };
                }
                
                @Override
                public int size() {
                    return EntityMap.this.size();
                }
                
                @Override
                public void clear() {
                    throw new UnsupportedOperationException();
                }
            };
        }
        
    }
    
}
