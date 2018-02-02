package io.dico.dicore.nms.v1_8_R3;

import com.mojang.authlib.GameProfile;
import io.dico.dicore.Reflection;
import io.dico.dicore.nms.IEntityDriver;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.*;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

final class EntityDriver implements IEntityDriver {
    private static final Field blockMaterialField = Reflection.restrictedSearchField(Block.class, "material");
    private static final Field worldAccessesListField = Reflection.restrictedSearchField(World.class, "u");
    private static final Field bukkitEntityField = Reflection.restrictedSearchField(net.minecraft.server.v1_8_R3.Entity.class, "bukkitEntity");
    
    @Override
    public boolean isInWater(Entity entity) {
        return ((CraftEntity) entity).getHandle().inWater;
    }
    
    @Override
    public boolean isInOtherBlock(Entity entity, org.bukkit.Material type) {
        Block block = Block.getById(type.getId());
        if (block == null) {
            throw new IllegalArgumentException(type + " not a block");
        }
        
        Material material = Reflection.getFieldValue(blockMaterialField, block);
        net.minecraft.server.v1_8_R3.Entity handle = ((CraftEntity) entity).getHandle();
        return handle.world.a(handle.getBoundingBox().grow(0.0D, -0.4000000059604645D, 0.0D)
                .shrink(0.001D, 0.001D, 0.001D), material, handle);
    }
    
    @Override
    public long getTimeOfLastAction(Player player) {
        return ((CraftPlayer) player).getHandle().D();
    }
    
    @Override
    public ItemStack getEquipmentItem(LivingEntity entity, EquipmentSlot equipmentSlot) {
        int idx;
        switch (equipmentSlot) {
            case CHEST:
                idx = 3;
                break;
            case HAND:
                idx = 0;
                break;
            case LEGS:
                idx = 2;
                break;
            case HEAD:
                idx = 4;
                break;
            case FEET:
                idx = 1;
                break;
            default:
                throw new NullPointerException("equipmentSlot is null");
        }
        
        EntityLiving ent = ((CraftLivingEntity) entity).getHandle();
        return CraftItemStack.asCraftMirror(ent.getEquipment(idx));
    }
    
    @Override
    public boolean commenceMobAttack(Monster bukkitMonster, Entity bukkitTarget) {
        EntityMonster monster = ((CraftMonster) bukkitMonster).getHandle();
        return monster.r(((CraftEntity) bukkitTarget).getHandle());
    }
    
    @Override
    public void commencePlayerAttack(Player bukkitPlayer, Entity bukkitTarget) {
        EntityPlayer player = ((CraftPlayer) bukkitPlayer).getHandle();
        player.attack(((CraftEntity) bukkitTarget).getHandle());
    }
    
    @Override
    public void igniteCreeper(Creeper bukkitCreeper) {
        EntityCreeper creeper = ((CraftCreeper) bukkitCreeper).getHandle();
        creeper.co();
    }
    
    @Override
    public boolean isAiDisabled(LivingEntity entity) {
        EntityLiving el = ((CraftLivingEntity) entity).getHandle();
        return el instanceof EntityInsentient && ((EntityInsentient) el).ce();
    }
    
    @Override
    public void setAiDisabled(LivingEntity entity, boolean disabled) {
        EntityLiving el = ((CraftLivingEntity) entity).getHandle();
        if (el instanceof EntityInsentient) {
            ((EntityInsentient) el).k(disabled);
        }
    }
    
    @Override
    public Entity spawnEntity(Location location, EntityType type) {
        return spawnEntity(location, type, SpawnReason.CUSTOM);
    }
    
    @Override
    public Entity spawnEntity(Location location, EntityType type, SpawnReason spawnReason) {
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        
        net.minecraft.server.v1_8_R3.Entity entity = getNewEntity(world, x, y, z, type);
        entity.setPosition(x, y, z);
        
        world.addEntity(entity, spawnReason);
        return entity.getBukkitEntity();
    }
    
    private net.minecraft.server.v1_8_R3.Entity getNewEntity(WorldServer world, double x, double y, double z, EntityType type) {
        switch (type) {
            //@formatter:off
            case ARMOR_STAND: return new EntityArmorStand(world);
            case ARROW: return new EntityArrow(world);
            case BAT: return new EntityBat(world);
            case BLAZE: return new EntityBlaze(world);
            case BOAT: return new EntityBoat(world);
            case CAVE_SPIDER: return new EntityCaveSpider(world);
            case CHICKEN: return new EntityChicken(world);
            case COMPLEX_PART: throw new IllegalArgumentException("Complex part cannot be spawned standalone");
            case COW: return new EntityCow(world);
            case CREEPER: return new EntityCreeper(world);
            case DROPPED_ITEM: return new EntityItem(world);
            case EGG: return new EntityEgg(world);
            case ENDERMAN: return new EntityEnderman(world);
            case ENDERMITE: return new EntityEndermite(world);
            case ENDER_CRYSTAL: return new EntityEnderCrystal(world);
            case ENDER_DRAGON: return new EntityEnderDragon(world);
            case ENDER_PEARL: return new EntityEnderPearl(world);
            case ENDER_SIGNAL: return new EntityEnderSignal(world);
            case EXPERIENCE_ORB: return new EntityExperienceOrb(world);
            case FALLING_BLOCK: return new EntityFallingBlock(world);
            case FIREBALL: return new EntitySmallFireball(world);
            case FIREWORK: return new EntityFireworks(world);
            case FISHING_HOOK: return new EntityFishingHook(world);
            case GHAST: return new EntityGhast(world);
            case GIANT: return new EntityGiantZombie(world);
            case GUARDIAN: return new EntityGuardian(world);
            case HORSE: return new EntityHorse(world);
            case IRON_GOLEM: return new EntityIronGolem(world);
            case ITEM_FRAME: return new EntityItemFrame(world);
            case LEASH_HITCH: return new EntityLeash(world);
            case WEATHER:
            case LIGHTNING: return new EntityLightning(world, x, y, z);
            case MAGMA_CUBE: return new EntityMagmaCube(world);
            case MINECART: return new EntityMinecartRideable(world);
            case MINECART_CHEST: return new EntityMinecartChest(world);
            case MINECART_COMMAND: return new EntityMinecartCommandBlock(world);
            case MINECART_FURNACE: return new EntityMinecartFurnace(world);
            case MINECART_HOPPER: return new EntityMinecartHopper(world);
            case MINECART_MOB_SPAWNER: return new EntityMinecartMobSpawner(world);
            case MINECART_TNT: return new EntityMinecartTNT(world);
            case MUSHROOM_COW: return new EntityMushroomCow(world);
            case OCELOT: return new EntityOcelot(world);
            case PAINTING: return new EntityPainting(world);
            case PIG: return new EntityPig(world);
            case PIG_ZOMBIE: return new EntityPigZombie(world);
            case PLAYER: return new EntityPlayer(MinecraftServer.getServer(), world, new GameProfile(UUID.randomUUID(), "Steve"), new PlayerInteractManager(world));
            case PRIMED_TNT: return new EntityTNTPrimed(world);
            case RABBIT: return new EntityRabbit(world);
            case SHEEP: return new EntitySheep(world);
            case SILVERFISH: return new EntitySilverfish(world);
            case SKELETON: return new EntitySkeleton(world);
            case SLIME: return new EntitySlime(world);
            case SMALL_FIREBALL: return new EntitySmallFireball(world);
            case SNOWBALL: return new EntitySnowball(world);
            case SPIDER: return new EntitySpider(world);
            case SPLASH_POTION: return new EntityPotion(world);
            case SQUID: return new EntitySquid(world);
            case THROWN_EXP_BOTTLE: return new EntityThrownExpBottle(world);
            case VILLAGER: return new EntityVillager(world);
            case WITCH: return new EntityWitch(world);
            case WITHER: return new EntityWither(world);
            case WITHER_SKULL: return new EntityWitherSkull(world);
            case WOLF: return new EntityWolf(world);
            case ZOMBIE: return new EntityZombie(world);
            case UNKNOWN:
            default: throw new IllegalArgumentException("Cannot spawn this entity");
            //@formatter:on
        }
    }
    
    static void _setBukkitEntity(net.minecraft.server.v1_8_R3.Entity entity, CraftEntity to) {
        Reflection.setFieldValue(bukkitEntityField, entity, to);
    }
    
    @Override
    public ExperienceOrb spawnExperienceOrb(Location location) {
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        int chunkX = MathHelper.floor(location.getX() / 16);
        int chunkZ = MathHelper.floor(location.getZ() / 16);
        if (!NmsDriver._isChunkLoaded(world, chunkX, chunkZ)) {
            return null;
        }
        
        EntityExperienceOrb orb = new EntityExperienceOrb(world);
        orb.setPosition(location.getX(), location.getY(), location.getZ());
        
        world.getChunkAt(chunkX, chunkZ).a(orb);
        world.entityList.add(orb);
        
        List<IWorldAccess> worldAccesses = Reflection.getFieldValue(worldAccessesListField, world);
        for (int n = worldAccesses.size(), i = 0; i < n; i++) {
            worldAccesses.get(i).a(orb);
        }
        
        CraftExperienceOrb rv = new CraftExperienceOrb(world.getServer(), orb);
        _setBukkitEntity(orb, rv);
        return rv;
    }
}
