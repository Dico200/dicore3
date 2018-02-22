package io.dico.dicore.nms.v1_8_R3;

import io.dico.dicore.Reflection;
import io.dico.dicore.event.SimpleListener;
import io.dico.dicore.nms.inventory.IAnvilEnchantmentListener;
import io.dico.dicore.nms.inventory.IAnvilInventoryHandle;
import net.minecraft.server.v1_8_R3.*;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class CustomAnvilContainer extends ContainerAnvil implements IAnvilInventoryHandle {
    private static final Field anvilItemNameField = Reflection.restrictedSearchField(ContainerAnvil.class, "l");
    private static final Field anvilMaterialCostField = Reflection.restrictedSearchField(ContainerAnvil.class, "k");
    private static final Field anvilPlayerInventoryField = Reflection.restrictedSearchField(ContainerAnvil.class, "player");
    private static final Field anvilWorldField = Reflection.restrictedSearchField(ContainerAnvil.class, "i");
    private static final Field anvilBlockField = Reflection.restrictedSearchField(ContainerAnvil.class, "j");
    private static final Field anvilPlayerField = Reflection.restrictedSearchField(ContainerAnvil.class, "m");
    
    private EntityHuman thePlayer;
    private List<SimpleListener<IAnvilInventoryHandle>> updateListeners;
    private IAnvilEnchantmentListener anvilEnchantmentListener;
    
    CustomAnvilContainer(PlayerInventory inv, World world, BlockPosition blockPosIn, EntityHuman player) {
        super(inv, world, blockPosIn, player);
        this.thePlayer = player;
        this.updateListeners = new ArrayList<>();
    }
    
    static CustomAnvilContainer forAnvilContainer(ContainerAnvil anvil) {
        PlayerInventory playerInventory = Reflection.getFieldValue(anvilPlayerInventoryField, anvil);
        World world = Reflection.getFieldValue(anvilWorldField, anvil);
        BlockPosition block = Reflection.getFieldValue(anvilBlockField, anvil);
        EntityHuman player = Reflection.getFieldValue(anvilPlayerField, anvil);
        return new CustomAnvilContainer(playerInventory, world, block, player);
    }
    
    static CustomAnvilContainer replaceExisting(EntityHuman player) {
        Container openedContainer = player.activeContainer;
        if (openedContainer instanceof CustomAnvilContainer) {
            return (CustomAnvilContainer) openedContainer;
        }
        if (openedContainer instanceof ContainerAnvil) {
            CustomAnvilContainer newContainer = forAnvilContainer((ContainerAnvil) openedContainer);
            newContainer.windowId = openedContainer.windowId;
            player.activeContainer = newContainer;
            return newContainer;
        }
        return null;
    }

    private ItemStack getItem(int index) {
        return this.getSlot(index).getItem();
    }
    
    private org.bukkit.inventory.ItemStack getBukkitItem(int index) {
        ItemStack item = getItem(index);
        return item == null ? null : CraftItemStack.asCraftMirror(item);
    }
    
    private void setBukkitItem(int index, org.bukkit.inventory.ItemStack bukkitItem) {
        setItem(index, ItemDriver.getHandle(bukkitItem));
    }
    
    @Override
    public void setEnchantmentListener(IAnvilEnchantmentListener listener) {
        this.anvilEnchantmentListener = listener;
    }
    
    @Override
    public org.bukkit.inventory.ItemStack getLeftInputItem() {
        return getBukkitItem(0);
    }
    
    @Override
    public org.bukkit.inventory.ItemStack getRightInputItem() {
        return getBukkitItem(1);
    }
    
    @Override
    public org.bukkit.inventory.ItemStack getResultItem() {
        return getBukkitItem(2);
    }
    
    @Override
    public void setLeftInputItem(org.bukkit.inventory.ItemStack item) {
        setBukkitItem(0, item);
    }
    
    @Override
    public void setRightInputItem(org.bukkit.inventory.ItemStack item) {
        setBukkitItem(1, item);
    }
    
    @Override
    public void setResultItem(org.bukkit.inventory.ItemStack item) {
        setBukkitItem(2, item);
    }
    
    @Override
    public void addCraftMatrixChangeListener(SimpleListener<IAnvilInventoryHandle> listener) {
        if (!updateListeners.contains(listener)) {
            updateListeners.add(listener);
        }
    }
    
    @Override
    public String getRepairedItemName() {
        return Reflection.getFieldValue(anvilItemNameField, this);
    }
    
    @Override
    public void setRepairedItemName(String repairedItemName) {
        this.a(repairedItemName);
    }
    
    @Override
    public int getCost() {
        return this.a;
    }
    
    @Override
    public void setCost(int cost) {
        this.a = cost;
    }
    
    private int getMaterialCost() {
        return Reflection.getFieldValue(anvilMaterialCostField, this);
    }
    
    private void setMaterialCost(int cost) {
        Reflection.setFieldValue(anvilMaterialCostField, this, cost);
    }
    
    @Override
    public void e() {
        ItemStack inputItem1 = getItem(0);
        setCost(1);
        int baseRepairCost = 0;
        int repairCostFromItemWear = 0;
        int tempRepairCost = 0;
        
        if (inputItem1 == null) {
            setItem(0, null);
            setCost(0);
        } else {
            ItemStack resultItem = inputItem1.cloneItemStack();
            ItemStack inputItem2 = getItem(1);
            Map<Integer, Integer> enchantments = EnchantmentManager.a(resultItem);
            boolean flag = false;
            repairCostFromItemWear = repairCostFromItemWear + inputItem1.getRepairCost() + (inputItem2 == null ? 0 : inputItem2.getRepairCost());
            setMaterialCost(0);
            
            if (inputItem2 != null) {
                // Enchanted book with at least 1 stored enchantment
                flag = inputItem2.getItem() == Items.ENCHANTED_BOOK && Items.ENCHANTED_BOOK.h(inputItem2).size() > 0;
                
                // e() --> isItemStackDamageable()
                // a(ItemStack, ItemStack) --> getIsRepairable()
                if (resultItem.e() && resultItem.getItem().a(inputItem1, inputItem2)) {
                    
                    // h() --> getItemDamage()
                    // j() --> getMaxDamage()
                    int itemDamageQuarter = Math.min(resultItem.h(), resultItem.j() / 4);
                    
                    if (itemDamageQuarter <= 0) {
                        setItem(0, null);
                        setCost(0);
                        return;
                    }
                    
                    int materialCost;
                    
                    for (materialCost = 0; itemDamageQuarter > 0 && materialCost < inputItem2.count; ++materialCost) {
                        int newDamage = resultItem.h() - itemDamageQuarter;
                        resultItem.setData(newDamage);
                        ++baseRepairCost;
                        itemDamageQuarter = Math.min(resultItem.h(), resultItem.j() / 4);
                    }
                    
                    setMaterialCost(materialCost);
                } else {
                    if (!flag && (resultItem.getItem() != inputItem2.getItem() || !resultItem.e())) {
                        setItem(0, (ItemStack) null);
                        setCost(0);
                        return;
                    }
                    
                    if (resultItem.e() && !flag) {
                        int k2 = inputItem1.j() - inputItem1.h();
                        int l2 = inputItem2.j() - inputItem2.h();
                        int i3 = l2 + resultItem.j() * 12 / 100;
                        int j3 = k2 + i3;
                        int k3 = resultItem.j() - j3;
                        
                        if (k3 < 0) {
                            k3 = 0;
                        }
                        
                        // getData() --> getMetadata()
                        if (k3 < resultItem.getData()) {
                            resultItem.setData(k3);
                            baseRepairCost += 2;
                        }
                    }
                    
                    Map<Integer, Integer> addedEnchantments = EnchantmentManager.a(inputItem2);
                    
                    for (int addedEnchantmentId : addedEnchantments.keySet()) {
                        Enchantment enchantment = Enchantment.getById(addedEnchantmentId);
                        
                        if (enchantment != null) {
                            int presentLevel = enchantments.getOrDefault(addedEnchantmentId, 0);
                            int addedLevel = addedEnchantments.get(addedEnchantmentId);
                            int newLevel;
                            
                            if (presentLevel == addedLevel) {
                                ++addedLevel;
                                newLevel = addedLevel;
                            } else {
                                newLevel = Math.max(addedLevel, presentLevel);
                            }
                            
                            addedLevel = newLevel;
                            boolean canEnchant = enchantment.canEnchant(inputItem1);
                            
                            // isCreativeMode
                            if (this.thePlayer.abilities.canInstantlyBuild || inputItem1.getItem() == Items.ENCHANTED_BOOK) {
                                canEnchant = true;
                            }
                            
                            for (int presentEnchantmentId : enchantments.keySet()) {
                                // a(Enchantment) --> canApplyTogether
                                if ((presentEnchantmentId) != addedEnchantmentId && !enchantment.a(Enchantment.getById((presentEnchantmentId)))) {
                                    canEnchant = false;
                                    ++baseRepairCost;
                                }
                            }
                            
                            if (canEnchant) {
                                if (addedLevel > enchantment.getMaxLevel()) {
                                    addedLevel = enchantment.getMaxLevel();
                                }
                                
                                enchantments.put(addedEnchantmentId, addedLevel);
                                int l5 = 0;
                                
                                switch (enchantment.getRandomWeight()) {
                                    case 1:
                                        l5 = 8;
                                        break;
                                    
                                    case 2:
                                        l5 = 4;
                                    
                                    case 3:
                                    case 4:
                                    case 6:
                                    case 7:
                                    case 8:
                                    case 9:
                                    default:
                                        break;
                                    
                                    case 5:
                                        l5 = 2;
                                        break;
                                    
                                    case 10:
                                        l5 = 1;
                                }
                                
                                if (flag) {
                                    l5 = Math.max(1, l5 / 2);
                                }
                                
                                baseRepairCost += l5 * addedLevel;
                            }
                        }
                    }
                    
                    // added for api
                    if (anvilEnchantmentListener != null) {
                        anvilEnchantmentListener.onEnchantmentsComputed(
                                CraftItemStack.asCraftMirror(inputItem1),
                                CraftItemStack.asCraftMirror(inputItem2),
                                CraftItemStack.asCraftMirror(resultItem)
                        );
                    }
                }
            }
            
            String repairedItemName = getRepairedItemName();
            if (StringUtils.isBlank(repairedItemName)) {
                // hasName() --> hasDisplayName
                if (inputItem1.hasName()) {
                    tempRepairCost = 1;
                    baseRepairCost += tempRepairCost;
                    // r() --> clearCustomName
                    resultItem.r();
                }
                // getName() --> getDisplayName
            } else if (!repairedItemName.equals(inputItem1.getName())) {
                tempRepairCost = 1;
                baseRepairCost += tempRepairCost;
                // c() --> setStackDisplayName
                resultItem.c(repairedItemName);
            }
            
            setCost(repairCostFromItemWear + baseRepairCost);
            
            if (baseRepairCost <= 0) {
                resultItem = null;
            }
            
            if (tempRepairCost == baseRepairCost && tempRepairCost > 0 && getCost() >= 40) {
                setCost(39);
            }
            
            if (getCost() >= 40 && !this.thePlayer.abilities.canInstantlyBuild) {
                resultItem = null;
            }
            
            if (resultItem != null) {
                int k4 = resultItem.getRepairCost();
                
                if (inputItem2 != null && k4 < inputItem2.getRepairCost()) {
                    k4 = inputItem2.getRepairCost();
                }
                
                k4 = k4 * 2 + 1;
                resultItem.setRepairCost(k4);
                // a(Map<Integer, Integer>, ItemStack) --> setEnchantments
                EnchantmentManager.a(enchantments, resultItem);
            }
            
            setItem(0, resultItem);
            // b() --> detectAndSendChanges()
            this.b();
    
            // added for api
            for (SimpleListener<IAnvilInventoryHandle> updateListener : updateListeners) {
                updateListener.accept(this);
            }
        }
    }
}
