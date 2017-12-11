package io.dico.dicore.nms;

import io.dico.dicore.nbt.INbtMap;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiPredicate;

/**
 * This interface contains methods that query and modify {@link org.bukkit.inventory.ItemStack items}
 *
 * An instance of this interface can be retrieved from {@link NmsFactory#getItemDriver()}
 */
public interface IItemDriver {

    /**
     * Set the {@link INbtMap} tag of {@code item}
     *
     * @param item The item
     * @param tag  The tag
     * @return {@code item}, or a copy if it can't hold NBT data.
     * @throws NullPointerException if item is null
     */
    ItemStack setTag(ItemStack item, INbtMap tag);

    /**
     * Gets the {@link INbtMap} tag of {@code item}
     * <p>
     * <i>
     * It is not guaranteed that changes made to the returned map have an effect on the item.
     * This is because not all implementations of {@link ItemStack} store NBT data.
     * In order to make sure that changes take effect, you can use code such as
     * <p>
     * {@code
     * IItemDriver driver = IItemDriver.INSTANCE;
     * ItemStack item = <your item>;
     * INbtMap tag = driver.getTag(item);
     * if (tag == INbtMap.EMPTY) {
     * item = driver.setTag(item, tag = INmsDriver.INSTANCE.newWrappedNbtMap());
     * }
     * <p>
     * // make your changes to the tag here
     * }
     * <p>
     * or, alternatively, a better solution
     * <p>
     * {@code
     * IItemDriver driver = IItemDriver.INSTANCE;
     * ItemStack item = {your item};
     * INbtMap tag = driver.getTag(item = driver.nmsMirror(item));
     * <p>
     * // make your changes to the tag here
     * }
     * <p>
     * </i>
     *
     * @param item The item
     * @return The described tag, or {@code INbtMap.EMPTY} if there is none or the item can't hold NBT data.
     * @throws NullPointerException if item is null
     */
    INbtMap getTag(ItemStack item);

    /**
     * Explores the {@link INbtMap} tag of {@code item.
     *
     * @param item     The item
     * @param explorer A function whose {@link INbtMap } parameter is the described tag.
     *                 The tag is not null and mutable. If the tag is edited, a return value of @{code true} is expected.
     *                 The driver will then update the tag of the item.
     * @return {@code item}, or a copy of it if {@code explorer} returned {@code true} and {@code item} can't hold NBT data.
     * @throws NullPointerException if item is null or explorer is null
     */
    default ItemStack exploreNBT(ItemStack item, BiPredicate<ItemStack, INbtMap> explorer) {
        INbtMap map = getTag(item);
        if (map == INbtMap.EMPTY) {
            map = NmsFactory.getDriver().newWrappedNBTMap();
            if (explorer.test(item, map)) {
                return setTag(item, map);
            }
            return item;
        }
        explorer.test(item, map);
        return item;
    }

    /**
     * Sets a single element of {@code item}'s nbt data.
     * <p>
     * <i> These methods exist because they can perform better than wrapping
     * an NMS object and calling a method on it</i>
     *
     * @param item  The item
     * @param key   The NBT key
     * @param value The NBT value
     * @return {@code item}, or a copy if it can't hold NBT data.
     * @see #getNBTElement
     */
    ItemStack setNBTElement(ItemStack item, String key, Object value);

    /**
     * Gets a single element of {@code item}'s nbt data.
     *
     * @param item The item
     * @param key  The NBT key
     * @return The NBT data assigned to {@code key}
     * @see #setNBTElement
     */
    Object getNBTElement(ItemStack item, String key);

    /**
     * Returns an item that is guaranteed to be a nmsMirror of an nms item stack.
     * <p>
     * Using this before modifying items counters issues found with, for example,
     * {@link #getTag(ItemStack)}
     * </p>
     *
     * @param item The item to nmsMirror
     * @return An {@link ItemStack item} which mirrors an nms item stack.
     * @see #getTag(ItemStack)
     */
    ItemStack nmsMirror(ItemStack item);
}
