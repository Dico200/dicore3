package io.dico.dicore.nms.inventory;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public interface IAnvilEnchantmentListener {

    void onEnchantmentsComputed(ComputeContext ctx);
    
    public static class ComputeContext {
        public final ItemStack inputItem1;
        public final ItemStack inputItem2;
        public ItemStack resultItem;
        public final Map<Enchantment, Integer> levels;
        public final Map<Enchantment, EnchantmentChange> changes;
        public int baseCost;
        public int itemWearCost;
        public int maxCost;
        public boolean updateEnchantments = true;
        public boolean increaseRepairCost = true;
    
        public ComputeContext(ItemStack inputItem1,
                              ItemStack inputItem2,
                              ItemStack resultItem,
                              Map<Enchantment, Integer> levels,
                              Map<Enchantment, EnchantmentChange> changes,
                              int baseCost,
                              int itemWearCost,
                              int maxCost) {
            this.inputItem1 = inputItem1;
            this.inputItem2 = inputItem2;
            this.resultItem = resultItem;
            this.levels = levels;
            this.changes = changes;
            this.baseCost = baseCost;
            this.itemWearCost = itemWearCost;
            this.maxCost = maxCost;
        }
    
        public static class EnchantmentChange {
            public final Enchantment enchantment;
            public final int oldLevel;
            public int newLevel;
            public int cost;
    
            public EnchantmentChange(Enchantment enchantment, int oldLevel, int newLevel, int cost) {
                this.enchantment = enchantment;
                this.oldLevel = oldLevel;
                this.newLevel = newLevel;
                this.cost = cost;
            }
        }
        
        
    }

}
