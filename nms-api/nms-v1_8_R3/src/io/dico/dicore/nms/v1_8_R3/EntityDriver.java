package io.dico.dicore.nms.v1_8_R3;

import io.dico.dicore.Reflection;
import io.dico.dicore.nms.IEntityDriver;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftMonster;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;

final class EntityDriver implements IEntityDriver {
    private static final Field blockMaterialField = Reflection.restrictedSearchField(Block.class, "material");
    
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
    
}
