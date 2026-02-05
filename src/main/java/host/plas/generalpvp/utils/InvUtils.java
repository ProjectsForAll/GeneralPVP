package host.plas.generalpvp.utils;

import host.plas.generalpvp.GeneralPVP;
import host.plas.generalpvp.items.ItemManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class InvUtils {
    public static int getInventorySlots() {
        return 4 * 9; // 4 rows of 9 slots each
    }

    public static void forItem(ItemStack item, ConcurrentSkipListMap<Integer, ItemStack> toSet, AtomicBoolean dropped,
                               AtomicInteger amount, EquipmentSlot slot, HumanEntity player, Material check, int maxAmount) {
        forItem(item, toSet, dropped, amount, getSlot(slot), player, check, maxAmount);
    }

    public static void forItem(ItemStack item, ConcurrentSkipListMap<Integer, ItemStack> toSet, AtomicBoolean dropped,
                               AtomicInteger amount, int slot, HumanEntity player, Material check, int maxAmount) {
        if (item == null || item.getType() != check) return;

        int beforeAmount = amount.get();
        int currentAmount = item.getAmount();
        int newAmount = beforeAmount + currentAmount;

        if (newAmount > maxAmount) {
            int excessAmount = newAmount - maxAmount;
            int keepAmount = currentAmount - excessAmount;

            handleDropExcess(player, item, newAmount - maxAmount);
            if (keepAmount > 0) {
                item.setAmount(keepAmount);
                toSet.put(slot, item);
            } else {
                toSet.put(slot, new ItemStack(Material.AIR));
            }

            dropped.set(true);
        } else {
            amount.set(newAmount);
        }
    }

    public static boolean isDropExcess() {
        return GeneralPVP.getMainConfig().isDropExcess();
    }

    public static void handleDropExcess(HumanEntity player, ItemStack stack, int excessAmount) {
        if (! isDropExcess()) return;
        if (stack == null || stack.getType() == Material.AIR) return;
        if (excessAmount <= 0) return;

        if (ItemManager.has(stack)) return; // Delete the item if player is duping it.

        Location location = player.getLocation();
        ItemStack excessItem = stack.clone();
        excessItem.setAmount(excessAmount);
        Item item = location.getWorld().dropItemNaturally(location, excessItem);
//        item.setCanMobPickup(false);
//        item.setCanPlayerPickup(false);

//        TaskManager.runTaskLater(item, () -> {
//            item.setCanMobPickup(true);
//            item.setCanPlayerPickup(true);
//        }, 20L * 2); // 2-second delay
    }

    public static int getSlot(EquipmentSlot slot) {
        switch (slot) {
            case HAND:
                return -100;
            case OFF_HAND:
                return -101;
            case HEAD:
                return -102;
            case CHEST:
                return -103;
            case LEGS:
                return -104;
            case FEET:
                return -105;
            case BODY:
                return -106;
            default:
                return -1; // Invalid slot
        }
    }

    public static EquipmentSlot toSlot(int slot) {
        switch (slot) {
            case -100:
                return EquipmentSlot.HAND;
            case -101:
                return EquipmentSlot.OFF_HAND;
            case -102:
                return EquipmentSlot.HEAD;
            case -103:
                return EquipmentSlot.CHEST;
            case -104:
                return EquipmentSlot.LEGS;
            case -105:
                return EquipmentSlot.FEET;
            case -106:
                return EquipmentSlot.BODY;
            default:
                return null; // Invalid slot
        }
    }

    public static void handleToSet(ConcurrentSkipListMap<Integer, ItemStack> toSet, PlayerInventory inventory) {
        if (! toSet.isEmpty()) {
            toSet.forEach((slot, item) -> {
                EquipmentSlot s = toSlot(slot);
                if (s != null) {
                    if (item == null || item.getType() == Material.AIR) {
                        inventory.setItem(s, new ItemStack(Material.AIR));
                    } else {
                        inventory.setItem(s, item);
                    }
                } else {
                    if (item == null || item.getType() == Material.AIR) {
                        inventory.setItem(slot, new ItemStack(Material.AIR));
                    } else {
                        inventory.setItem(slot, item);
                    }
                }
            });
        }
    }

    public static void handleInventory(PlayerInventory inventory, ConcurrentSkipListMap<Integer, ItemStack> toSet,
                                       AtomicBoolean dropped, AtomicInteger amount, HumanEntity player, Material material, int maxAmount) {
        ItemStack offHand = inventory.getItemInOffHand();
        forItem(offHand, toSet, dropped, amount, EquipmentSlot.OFF_HAND, player, material, maxAmount);

        ItemStack helmet = inventory.getHelmet();
        ItemStack chestplate = inventory.getChestplate();
        ItemStack leggings = inventory.getLeggings();
        ItemStack boots = inventory.getBoots();

        forItem(helmet, toSet, dropped, amount, EquipmentSlot.HEAD, player, material, maxAmount);
        forItem(chestplate, toSet, dropped, amount, EquipmentSlot.CHEST, player, material, maxAmount);
        forItem(leggings, toSet, dropped, amount, EquipmentSlot.LEGS, player, material, maxAmount);
        forItem(boots, toSet, dropped, amount, EquipmentSlot.FEET, player, material, maxAmount);

        inventory.all(material).forEach((slot, item) -> {
            forItem(item, toSet, dropped, amount, slot, player, material, maxAmount);
        });
    }

    public static AtomicInteger countItem(ItemStack item, EquipmentSlot slot, HumanEntity player, AtomicInteger amount, Material check) {
        return countItem(item, getSlot(slot), player, amount, check);
    }

    public static AtomicInteger countItem(ItemStack item, int slot, HumanEntity player, AtomicInteger amount, Material check) {
        if (item == null || item.getType() != check) return amount;

        int currentAmount = item.getAmount();
        amount.getAndAdd(currentAmount);

        return amount;
    }

    public static int getCount(PlayerInventory inventory, HumanEntity player, Material material) {
        AtomicInteger amount = new AtomicInteger(0);

        ItemStack offHand = inventory.getItemInOffHand();
        countItem(offHand, EquipmentSlot.OFF_HAND, player, amount, material);

        ItemStack helmet = inventory.getHelmet();
        ItemStack chestplate = inventory.getChestplate();
        ItemStack leggings = inventory.getLeggings();
        ItemStack boots = inventory.getBoots();

        countItem(helmet, EquipmentSlot.HEAD, player, amount, material);
        countItem(chestplate, EquipmentSlot.CHEST, player, amount, material);
        countItem(leggings, EquipmentSlot.LEGS, player, amount, material);
        countItem(boots, EquipmentSlot.FEET, player, amount, material);

        inventory.all(material).forEach((slot, item) -> {
            countItem(item, slot, player, amount, material);
        });

        return amount.get();
    }
}
