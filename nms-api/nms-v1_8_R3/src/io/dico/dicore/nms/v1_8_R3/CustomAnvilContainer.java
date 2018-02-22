package io.dico.dicore.nms.v1_8_R3;

import io.dico.dicore.Reflection;
import io.dico.dicore.event.SimpleListener;
import io.dico.dicore.nms.inventory.IAnvilEnchantmentListener;
import io.dico.dicore.nms.inventory.IAnvilEnchantmentListener.ComputeContext.EnchantmentChange;
import io.dico.dicore.nms.inventory.IAnvilInventoryHandle;
import net.minecraft.server.v1_8_R3.*;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class CustomAnvilContainer extends ContainerAnvil implements IAnvilInventoryHandle {
    private static final Field anvilOutputItemsField = Reflection.restrictedSearchField(ContainerAnvil.class,"g");
    private static final Field anvilItemNameField = Reflection.restrictedSearchField(ContainerAnvil.class, "l");
    private static final Field anvilMaterialCostField = Reflection.restrictedSearchField(ContainerAnvil.class, "k");
    private static final Field anvilPlayerInventoryField = Reflection.restrictedSearchField(ContainerAnvil.class, "player");
    private static final Field anvilWorldField = Reflection.restrictedSearchField(ContainerAnvil.class, "i");
    private static final Field anvilBlockField = Reflection.restrictedSearchField(ContainerAnvil.class, "j");
    private static final Field anvilPlayerField = Reflection.restrictedSearchField(ContainerAnvil.class, "m");
    
    private EntityHuman thePlayer;
    private InventoryCraftResult outputInventory;
    private List<SimpleListener<IAnvilInventoryHandle>> updateListeners;
    private IAnvilEnchantmentListener anvilEnchantmentListener;
    
    private boolean checkLastInputItems = false;
    private ItemStack lastInputItem1 = null;
    private ItemStack lastInputItem2 = null;
    private int lastTickUpdated;
    private int sendUpdatesInDelay = -1;
    
    CustomAnvilContainer(PlayerInventory inv, World world, BlockPosition blockPosIn, EntityHuman player) {
        super(inv, world, blockPosIn, player);
        this.thePlayer = player;
        this.outputInventory = Reflection.getFieldValue(anvilOutputItemsField, this);
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
    public HumanEntity getViewer() {
        return thePlayer.getBukkitEntity();
    }
    
    @Override
    public InventoryView getView() {
        return getBukkitView();
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
        ItemStack inputItem2 = getItem(1);
        // added to attempt to make it less buggy
        if (checkLastInputItems && ItemStack.equals(lastInputItem1, inputItem1) && ItemStack.equals(inputItem2, lastInputItem2)) {
            return;
        }
        // end
        
        setCost(1);
        int baseRepairCost = 0;
        int repairCostFromItemWear = 0;
        int tempRepairCost = 0;
        
        // added for api
        int maxRepairCost = 39;
        boolean updateEnchantments = true;
        boolean increaseRepairCost = true;
        // end
        
        if (inputItem1 == null) {
            outputInventory.setItem(0, null);
            setCost(0);
        } else {
            ItemStack resultItem = inputItem1.cloneItemStack();
            
            Map<Integer, Integer> enchantments = EnchantmentManager.a(resultItem);
            boolean isEnchantedBook;
            repairCostFromItemWear = repairCostFromItemWear + inputItem1.getRepairCost() + (inputItem2 == null ? 0 : inputItem2.getRepairCost());
            setMaterialCost(0);
            
            if (inputItem2 != null) {
                // Enchanted book with at least 1 stored enchantment
                isEnchantedBook = inputItem2.getItem() == Items.ENCHANTED_BOOK && Items.ENCHANTED_BOOK.h(inputItem2).size() > 0;
                
                // e() --> isItemStackDamageable()
                // a(ItemStack, ItemStack) --> getIsRepairable()
                if (resultItem.e() && resultItem.getItem().a(inputItem1, inputItem2)) {
                    
                    // h() --> getItemDamage()
                    // j() --> getMaxDamage()
                    int itemDamageQuarter = Math.min(resultItem.h(), resultItem.j() / 4);
                    
                    if (itemDamageQuarter <= 0) {
                        outputInventory.setItem(0, null);
                        setCost(0);
                        sendUpdates();
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
                    if (!isEnchantedBook && (resultItem.getItem() != inputItem2.getItem() || !resultItem.e())) {
                        outputInventory.setItem(0, null);
                        setCost(0);
                        return;
                    }
                    
                    if (resultItem.e() && !isEnchantedBook) {
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
                    
                    // added for api
                    Map<org.bukkit.enchantments.Enchantment, EnchantmentChange> changes = new HashMap<>();
                    // end
                    
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
                                if (presentEnchantmentId != addedEnchantmentId && !enchantment.a(Enchantment.getById(presentEnchantmentId))) {
                                    canEnchant = false;
                                    ++baseRepairCost;
                                }
                            }
                            
                            if (canEnchant) {
                                if (addedLevel > enchantment.getMaxLevel()) {
                                    addedLevel = enchantment.getMaxLevel();
                                }
                                
                                // enchantments.put(addedEnchantmentId, addedLevel);
                                int levelCostMultiplier = 0;
                                
                                switch (enchantment.getRandomWeight()) {
                                    case 1:
                                        levelCostMultiplier = 8;
                                        break;
                                    
                                    case 2:
                                        levelCostMultiplier = 4;
                                    
                                    case 3:
                                    case 4:
                                    case 6:
                                    case 7:
                                    case 8:
                                    case 9:
                                    default:
                                        break;
                                    
                                    case 5:
                                        levelCostMultiplier = 2;
                                        break;
                                    
                                    case 10:
                                        levelCostMultiplier = 1;
                                }
                                
                                if (isEnchantedBook) {
                                    levelCostMultiplier = Math.max(1, levelCostMultiplier / 2);
                                }
                                
                                int cost = levelCostMultiplier * addedLevel;
                                // baseRepairCost += cost
                                
                                org.bukkit.enchantments.Enchantment bukkitEnchantment = org.bukkit.enchantments.Enchantment.getById(addedEnchantmentId);
                                if (bukkitEnchantment != null) {
                                    EnchantmentChange change = new EnchantmentChange(bukkitEnchantment, presentLevel, addedLevel, cost);
                                    changes.put(bukkitEnchantment, change);
                                }
                            }
                        }
                    }
                    
                    // added for api
                    if (anvilEnchantmentListener != null) {
                        
                        Map<org.bukkit.enchantments.Enchantment, Integer> levels = new HashMap<>();
                        for (Map.Entry<Integer, Integer> entry : enchantments.entrySet()) {
                            org.bukkit.enchantments.Enchantment enchantment = org.bukkit.enchantments.Enchantment.getById(entry.getKey());
                            if (enchantment != null) {
                                levels.put(enchantment, entry.getValue());
                            }
                        }
                        
                        CraftItemStack bukkitResult = CraftItemStack.asCraftMirror(resultItem);
                        IAnvilEnchantmentListener.ComputeContext ctx = new IAnvilEnchantmentListener.ComputeContext(
                                CraftItemStack.asCraftMirror(inputItem1),
                                CraftItemStack.asCraftMirror(inputItem2),
                                bukkitResult,
                                levels,
                                changes,
                                baseRepairCost,
                                repairCostFromItemWear,
                                maxRepairCost
                        );
                        
                        anvilEnchantmentListener.onEnchantmentsComputed(ctx);
                        
                        if (bukkitResult != ctx.resultItem) {
                            resultItem = ItemDriver.getHandle(ctx.resultItem);
                            if (resultItem == null) {
                                resultItem = new ItemStack((Item) null, 1, 0);
                            }
                        }
                        
                        baseRepairCost = ctx.baseCost;
                        repairCostFromItemWear = ctx.itemWearCost;
                        maxRepairCost = ctx.maxCost;
                        updateEnchantments = ctx.updateEnchantments;
                        increaseRepairCost = ctx.increaseRepairCost;
                    }
    
                    for (EnchantmentChange change : changes.values()) {
                        int id = change.enchantment.getId();
                        enchantments.put(id, change.newLevel);
                        baseRepairCost += change.cost;
                    }
                    
                    // end
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
            
            if (baseRepairCost <= 0 || resultItem.getItem() == null) {
                resultItem = null;
            }
            
            if (tempRepairCost == baseRepairCost && tempRepairCost > 0 && getCost() > maxRepairCost) {
                setCost(maxRepairCost);
            }
            
            if (getCost() > maxRepairCost && !this.thePlayer.abilities.canInstantlyBuild) {
                resultItem = null;
            }
            
            if (resultItem != null) {
                if (increaseRepairCost) {
                    int k4 = resultItem.getRepairCost();
    
                    if (inputItem2 != null && k4 < inputItem2.getRepairCost()) {
                        k4 = inputItem2.getRepairCost();
                    }
    
                    k4 = k4 * 2 + 1;
                    resultItem.setRepairCost(k4);
                }
                
                if (updateEnchantments) {
                    // a(Map<Integer, Integer>, ItemStack) --> setEnchantments
                    EnchantmentManager.a(enchantments, resultItem);
                }
            }
            
            outputInventory.setItem(0, resultItem);
            
            // added for api
            for (SimpleListener<IAnvilInventoryHandle> updateListener : updateListeners) {
                updateListener.accept(this);
            }
            
            // b() --> detectAndSendChanges()
            this.b();
            sendUpdates();
        }
    }
    
    @Override
    public void a(String s) {
        checkLastInputItems = false;
        super.a(s);
    }
    
    private void sendUpdates() {
        lastInputItem1 = getItem(0);
        lastInputItem2 = getItem(1);
        checkLastInputItems = true;
        
        // added this because the client is extrapolating the result, and the server never sends it normally
        sendData();
        sendUpdatesInDelay = 0;
    }
    
    private void sendData() {
        if (thePlayer instanceof EntityPlayer) {
            EntityPlayer p = (EntityPlayer) thePlayer;
            p.playerConnection.sendPacket(new PacketPlayOutSetSlot(windowId, 2, outputInventory.getItem(2)));
            p.playerConnection.sendPacket(new PacketPlayOutWindowData(windowId, 0, getCost()));
        }
    }
    
    // Called every tick the container is open. Checks if the player can interact with the container (by checking the positions)
    @Override
    public boolean a(EntityHuman entityhuman) {
        int tick = MinecraftServer.currentTick;
        if (tick != lastTickUpdated) {
            lastTickUpdated = tick;
            tick();
        }
        
        return super.a(entityhuman);
    }
    
    private void tick() {
        if (sendUpdatesInDelay >= 0) {
            if (sendUpdatesInDelay == 0) {
                sendData();
            }
            sendUpdatesInDelay--;
        }
    }
    
    /*
    
    @Override
    public void e() {
        ItemStack inputItem1 = getItem(0);
        setCost(1);
        int baseRepairCost = 0;
        int repairCostFromItemWear = 0;
        int tempRepairCost = 0;
        
        if (inputItem1 == null) {
            outputInventory.setItem(0, null);
            setCost(0);
        } else {
            ItemStack resultItem = inputItem1.cloneItemStack();
            ItemStack inputItem2 = getItem(1);
            Map<Integer, Integer> enchantments = EnchantmentManager.a(resultItem);
            boolean isEnchantedBook;
            repairCostFromItemWear = repairCostFromItemWear + inputItem1.getRepairCost() + (inputItem2 == null ? 0 : inputItem2.getRepairCost());
            setMaterialCost(0);
            
            if (inputItem2 != null) {
                // Enchanted book with at least 1 stored enchantment
                isEnchantedBook = inputItem2.getItem() == Items.ENCHANTED_BOOK && Items.ENCHANTED_BOOK.h(inputItem2).size() > 0;
                
                // e() --> isItemStackDamageable()
                // a(ItemStack, ItemStack) --> getIsRepairable()
                if (resultItem.e() && resultItem.getItem().a(inputItem1, inputItem2)) {
                    
                    // h() --> getItemDamage()
                    // j() --> getMaxDamage()
                    int itemDamageQuarter = Math.min(resultItem.h(), resultItem.j() / 4);
                    
                    if (itemDamageQuarter <= 0) {
                        outputInventory.setItem(0, null);
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
                    if (!isEnchantedBook && (resultItem.getItem() != inputItem2.getItem() || !resultItem.e())) {
                        outputInventory.setItem(0, (ItemStack) null);
                        setCost(0);
                        return;
                    }
                    
                    if (resultItem.e() && !isEnchantedBook) {
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
                    
                    // added for api
                    Map<Enchantment, EnchantmentChange> changes = new HashMap<>();
                    // end
                    
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
                                if (presentEnchantmentId != addedEnchantmentId && !enchantment.a(Enchantment.getById(presentEnchantmentId))) {
                                    canEnchant = false;
                                    ++baseRepairCost;
                                }
                            }
                            
                            if (canEnchant) {
                                if (addedLevel > enchantment.getMaxLevel()) {
                                    addedLevel = enchantment.getMaxLevel();
                                }
                                
                                enchantments.put(addedEnchantmentId, addedLevel);
                                int levelCostMultiplier = 0;
                                
                                switch (enchantment.getRandomWeight()) {
                                    case 1:
                                        levelCostMultiplier = 8;
                                        break;
                                    
                                    case 2:
                                        levelCostMultiplier = 4;
                                    
                                    case 3:
                                    case 4:
                                    case 6:
                                    case 7:
                                    case 8:
                                    case 9:
                                    default:
                                        break;
                                    
                                    case 5:
                                        levelCostMultiplier = 2;
                                        break;
                                    
                                    case 10:
                                        levelCostMultiplier = 1;
                                }
                                
                                if (isEnchantedBook) {
                                    levelCostMultiplier = Math.max(1, levelCostMultiplier / 2);
                                }
                                
                                baseRepairCost += levelCostMultiplier * addedLevel;
                                
                                org.bukkit.enchantments.Enchantment bukkitEnchantment = org.bukkit.enchantments.Enchantment.getById()
                            }
                        }
                    }
                    
                    // added for api
                    if (anvilEnchantmentListener != null) {
                        baseRepairCost = anvilEnchantmentListener.onEnchantmentsComputed(
                                CraftItemStack.asCraftMirror(inputItem1),
                                CraftItemStack.asCraftMirror(inputItem2),
                                CraftItemStack.asCraftMirror(resultItem),
                                baseRepairCost
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
            
            if (baseRepairCost <= 0 || resultItem.getItem() == null) {
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
            
            outputInventory.setItem(0, resultItem);
            
            // added for api
            for (SimpleListener<IAnvilInventoryHandle> updateListener : updateListeners) {
                updateListener.accept(this);
            }
    
            // b() --> detectAndSendChanges()
            this.b();
            
            // added this because the client is extrapolating the result, and the server never sends it normally
            if (thePlayer instanceof EntityPlayer) {
                EntityPlayer p = (EntityPlayer) thePlayer;
                p.updateInventory(p.activeContainer);
                p.setContainerData(this, 0, getCost());
            }
        }
    }
     */
}
