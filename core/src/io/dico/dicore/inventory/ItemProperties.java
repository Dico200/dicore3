package io.dico.dicore.inventory;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.dico.dicore.Formatting;
import io.dico.dicore.serialization.JsonLoadable;
import io.dico.dicore.serialization.JsonUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.util.*;

/**
 * Save and load {@link ItemStack} in json format
 * Any additional NBT data not included by {@link ItemMeta} is discarded.
 */
@SuppressWarnings({"UnusedReturnValue", "StringEquality"})
public class ItemProperties implements JsonLoadable {
    private int id;
    private short data;
    private int amount;
    private Map<Enchantment, Integer> enchantments;
    private List<String> lore;
    private String displayName;
    private boolean unbreakable;
    private transient boolean translateColorsEnabled;
    
    public ItemProperties() {
    }
    
    public ItemProperties(boolean translateColorsEnabled) {
        this.translateColorsEnabled = translateColorsEnabled;
    }
    
    public ItemProperties(ItemStack item) {
        loadFrom(item);
    }
    
    public ItemProperties(ItemStack item, boolean translateColorsEnabled) {
        this.translateColorsEnabled = translateColorsEnabled;
        loadFrom(item);
    }
    
    public ItemStack toItemStack(ItemStack ref) {
        if (ref == null) {
            return toItemStack();
        }
        
        ref.setTypeId(id);
        ref.setAmount(amount);
        ref.setDurability(data);
        ref.setItemMeta(makeMeta());
        return ref;
    }
    
    public ItemStack toItemStack() {
        ItemStack result = new ItemStack(id, amount, data);
        result.setItemMeta(makeMeta());
        return result;
    }
    
    private ItemMeta makeMeta() {
        Material type = getType();
        if (type == null || type == Material.AIR) {
            return null;
        }
        
        ItemMeta originalMeta = Bukkit.getItemFactory().getItemMeta(type);
        ItemMeta meta = StorageForwardingMeta.ensureNotStored(originalMeta);
        
        if (enchantments != null) {
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                meta.addEnchant(entry.getKey(), entry.getValue(), true);
            }
        }
        
        if (lore != null) {
            meta.setLore(lore);
        }
        
        if (displayName != null) {
            meta.setDisplayName(displayName);
        }
        
        meta.spigot().setUnbreakable(unbreakable);
        
        return originalMeta;
    }
    
    @Override
    public void writeTo(JsonWriter writer) throws IOException {
        writer.beginObject();
        
        writer.name("id").value(id);
        writer.name("data").value(data);
        writer.name("amount").value(amount);
        
        if (displayName != null) {
            writer.name("displayName");
            writer.value(getDisplayNameInConfig());
        }
        
        if (enchantments != null) {
            writer.name("enchantments");
            Map<Enchantment, Integer> enchantments = this.enchantments;
            Map<String, Integer> stringKeys = new HashMap<>(enchantments.size());
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                stringKeys.put(entry.getKey().getName(), entry.getValue());
            }
            
            JsonUtil.insert(writer, stringKeys);
        }
        
        if (lore != null) {
            writer.name("lore");
            JsonUtil.insert(writer, getLoreInConfig());
        }
        
        if (unbreakable) {
            writer.name("unbreakable").value(true);
        }
        
        writer.endObject();
    }
    
    public void writeTo(Map<String, Object> map) {
        map.put("id", id);
        map.put("data", data);
        map.put("amount", amount);
        if (displayName != null) {
            map.put("displayName", getDisplayNameInConfig());
        }
        if (enchantments != null) {
            Map<Enchantment, Integer> enchantments = this.enchantments;
            Map<String, Integer> stringKeys = new HashMap<>();
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                stringKeys.put(entry.getKey().getName(), entry.getValue());
            }
            map.put("enchantments", stringKeys);
        }
        if (lore != null) {
            map.put("lore", getLoreInConfig());
        }
        if (unbreakable) {
            map.put("unbreakable", true);
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void loadFrom(JsonReader reader) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            final String key = reader.nextName();
            switch (key) {
                case "id":
                    id = reader.nextInt();
                    break;
                case "data":
                    data = (byte) reader.nextInt();
                    break;
                case "amount":
                    amount = reader.nextInt();
                    break;
                case "unbreakable":
                    unbreakable = reader.nextBoolean();
                    break;
                case "enchantments": {
                    if (enchantments == null) {
                        enchantments = new HashMap<>();
                    } else if (!enchantments.isEmpty()) {
                        enchantments.clear();
                    }
                    Map<String, Object> read = (Map<String, Object>) JsonUtil.read(reader);
                    if (read != null) {
                        for (Map.Entry<String, Object> entry : read.entrySet()) {
                            Enchantment ench = Enchantment.getByName(entry.getKey());
                            if (ench != null) {
                                int level = toInt(entry.getValue(), 1);
                                enchantments.put(ench, level);
                            }
                        }
                    }
                    break;
                }
                case "lore":
                    setLoreFromConfig((List<String>) JsonUtil.read(reader));
                    break;
                case "displayName":
                    setDisplayNameFromConfig(reader.nextString());
                    break;
                default:
                    reader.skipValue();
            }
        }
        reader.endObject();
    }
    
    private static Map<String, Object> toMap(Object object) {
        //noinspection unchecked
        return object instanceof ConfigurationSection ? ((ConfigurationSection) object).getValues(false) : (Map<String, Object>) object;
    }
    
    private static int toInt(Object object, int def) {
        if (object instanceof Number) {
            return ((Number) object).intValue();
        }
        try {
            return Integer.parseInt(Objects.toString(object));
        } catch (IllegalArgumentException ex) {
            return def;
        }
    }
    
    public ItemProperties loadFrom(Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            final String key = entry.getKey();
            switch (key) {
                case "id":
                    id = toInt(entry.getValue(), 0);
                    break;
                case "data":
                    data = (byte) toInt(entry.getValue(), 0);
                    break;
                case "amount":
                    amount = toInt(entry.getValue(), 1);
                    break;
                case "unbreakable":
                    unbreakable = (boolean) entry.getValue();
                    break;
                case "enchantments": {
                    if (enchantments == null) {
                        enchantments = new HashMap<>();
                    } else if (!enchantments.isEmpty()) {
                        enchantments.clear();
                    }
                    
                    //noinspection unchecked
                    Map<String, Object> enchantmentKeys = toMap(entry.getValue());
                    if (enchantmentKeys != null) {
                        for (Map.Entry<String, Object> entry2 : enchantmentKeys.entrySet()) {
                            Enchantment ench = Enchantment.getByName(entry2.getKey());
                            if (ench != null) {
                                enchantments.put(ench, toInt(entry2.getValue(), 1));
                            }
                        }
                    }
                    break;
                }
                case "lore":
                    //noinspection unchecked
                    setLoreFromConfig((List<String>) entry.getValue());
                    break;
                case "displayName":
                    setDisplayNameFromConfig((String) entry.getValue());
                    break;
                default:
            }
        }
        return this;
    }
    
    public ItemProperties loadFrom(ItemStack item) {
        id = item.getTypeId();
        data = item.getDurability();
        amount = item.getAmount();
        enchantments = new HashMap<>();
        
        ItemMeta meta = StorageForwardingMeta.ensureNotStored(item.getItemMeta());
        if (meta != null) {
            if (meta.hasEnchants()) {
                enchantments.putAll(meta.getEnchants());
            }
            
            if (meta.hasLore()) {
                lore = meta.getLore();
            }
            
            if (meta.hasDisplayName()) {
                displayName = meta.getDisplayName();
            }
            
            unbreakable = meta.spigot().isUnbreakable();
        }
        return this;
    }
    
    public int getId() {
        return id;
    }
    
    public ItemProperties setId(int id) {
        this.id = id;
        return this;
    }
    
    public Material getType() {
        Material rv = Material.getMaterial(id);
        return rv == null ? Material.AIR : rv;
    }
    
    public ItemProperties setType(Material type) {
        this.id = type == null ? 0 : type.getId();
        return this;
    }
    
    public short getData() {
        return data;
    }
    
    public ItemProperties setData(byte data) {
        this.data = data;
        return this;
    }
    
    public int getAmount() {
        return amount;
    }
    
    public ItemProperties setAmount(int amount) {
        this.amount = amount;
        return this;
    }
    
    public Map<Enchantment, Integer> getEnchantments() {
        return enchantments;
    }
    
    public ItemProperties setEnchantments(Map<Enchantment, Integer> enchantments) {
        this.enchantments = enchantments;
        return this;
    }
    
    public List<String> getLore() {
        return lore;
    }
    
    public List<String> getLoreInConfig() {
        List<String> rv = new ArrayList<>(lore);
        if (translateColorsEnabled) {
            setColorCharAbsent(rv);
        }
        return rv;
    }
    
    public ItemProperties setLore(List<String> lore) {
        this.lore = lore;
        return this;
    }
    
    public ItemProperties setLoreFromConfig(List<String> lore) {
        this.lore = new ArrayList<>(lore);
        setColorCharPresent(this.lore);
        return this;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDisplayNameInConfig() {
        return translateColorsEnabled ? Formatting.revert(displayName) : displayName;
    }
    
    public ItemProperties setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }
    
    public ItemProperties setDisplayNameFromConfig(String displayName) {
        this.displayName = translateColorsEnabled ? Formatting.translate(displayName) : displayName;
        return this;
    }
    
    public boolean isUnbreakable() {
        return unbreakable;
    }
    
    public ItemProperties setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }
    
    public boolean isTranslateColorsEnabled() {
        return translateColorsEnabled;
    }
    
    public ItemProperties setTranslateColorsEnabled(boolean translateColors) {
        this.translateColorsEnabled = translateColors;
        return this;
    }
    
    private static void setColorCharPresent(List<String> list) {
        ListIterator<String> iterator = list.listIterator();
        while (iterator.hasNext()) {
            String element = iterator.next();
            if (element != null && element != (element = Formatting.translate(element))) {
                iterator.set(element);
            }
        }
    }
    
    private static void setColorCharAbsent(List<String> list) {
        ListIterator<String> iterator = list.listIterator();
        while (iterator.hasNext()) {
            String element = iterator.next();
            if (element != null && element != (element = Formatting.revert(element))) {
                iterator.set(element);
            }
        }
    }
    
}
