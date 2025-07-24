package host.plas.generalpvp.config.bits;

import gg.drak.thebase.objects.Identifiable;
import host.plas.generalpvp.GeneralPVP;
import host.plas.generalpvp.items.ItemManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Getter @Setter
public class ConfiguredPotion implements Identifiable {
    private String identifier;

    private String type;
    private int amplifier;

    public ConfiguredPotion(String identifier, String type, int amplifier) {
        this.identifier = identifier;
        this.type = type;
        this.amplifier = amplifier;
    }

    public boolean checkAndDropAndClear(Player player) {
        return checkEffects(player) || checkInventory(player);
    }

    public boolean checkEffects(Player player) {
        if (player == null) return false;
        if (player.hasPermission(GeneralPVP.getMainConfig().getBypassPotionCheckPermission())) return true;

        List<PotionEffect> effects = new ArrayList<>(player.getActivePotionEffects());

        AtomicBoolean hasEffect = new AtomicBoolean(false);
        AtomicBoolean needsRemoval = new AtomicBoolean(false);
        effects.forEach(effect -> {
            if (effect == null) return;
            PotionEffectType type = effect.getType();

            PotionEffectType ownType = this.asEffectType();
            if (ownType == null) return;
            if (! type.equals(ownType)) return;

            if (effect.getAmplifier() + 1 >= this.getAmplifier()) { // +1 because amplifier is 0-based
                needsRemoval.set(true);

                player.removePotionEffect(type);
                if (this.getAmplifier() > 0) {
                    PotionEffect newEffect = new PotionEffect(type, effect.getDuration(), this.getAmplifier() - 1 - 1); // double -1 because amplifier is 0-based

                    player.addPotionEffect(newEffect);
                }
            }
        });

        return needsRemoval.get();
    }

    public boolean checkInventory(Player player) {
        if (player == null) return false;
        if (player.hasPermission(GeneralPVP.getMainConfig().getBypassPotionCheckPermission())) return true;

        PlayerInventory inventory = player.getInventory();

        ConcurrentSkipListMap<Integer, ItemStack> toSet = new ConcurrentSkipListMap<>();

        AtomicBoolean dropped = new AtomicBoolean(false);
        AtomicInteger amount = new AtomicInteger(0);

        inventory.all(Material.POTION).forEach((slot, item) -> {
            if (! isOfOwnType(item)) return;

            int amountInSlot = item.getAmount();

            handleDropExcess(player, item, amountInSlot);

            dropped.set(true);
            toSet.put(slot, new ItemStack(Material.AIR));
        });

        if (! toSet.isEmpty()) {
            toSet.forEach((slot, item) -> {
                if (item == null || item.getType() == Material.AIR) {
                    inventory.setItem(slot, new ItemStack(Material.AIR));
                } else {
                    inventory.setItem(slot, item);
                }
            });
        }

        return dropped.get();
    }

    public PotionEffectType asEffectType() {
        try {
            if (isProbablyPotionType()) {
                PotionType ownType = this.asPotionType();
                if (ownType == null) return null;

                return ownType.getEffectType();
            }

            NamespacedKey key = NamespacedKey.fromString(getType());
            PotionEffectType effectType = PotionEffectType.getByKey(key);

            return effectType;
        } catch (Exception e) {
            GeneralPVP.getInstance().logInfo("Failed to convert potion type: " + getType() + " to PotionEffectType. " + e.getMessage());
            return null;
        }
    }

    public PotionType asPotionType() {
        try {
            return PotionType.valueOf(getType().toUpperCase());
        } catch (Exception e) {
            GeneralPVP.getInstance().logInfo("Failed to convert potion type: " + getType() + " to PotionType. " + e.getMessage());
            return null;
        }
    }

    public boolean isProbablyPotionType() {
        return ! getType().contains(":");
    }

    public static boolean isDropExcess() {
        return GeneralPVP.getMainConfig().isDropExcess();
    }

    public static void handleDropExcess(Player player, ItemStack stack, int excessAmount) {
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

    public int getCurrentAmount(HumanEntity player) {
        PlayerInventory inventory = player.getInventory();
        AtomicInteger amount = new AtomicInteger(0);

        inventory.all(Material.POTION).forEach((slot, item) -> {
            if (! isOfOwnType(item)) return;

            int amountInSlot = item.getAmount();
            amount.addAndGet(amountInSlot);
        });

        return amount.get();
    }

    public boolean isCanAdd(HumanEntity player, ItemStack stack) {
        if (player == null) return false;
        if (player.hasPermission(GeneralPVP.getMainConfig().getBypassItemCheckPermission())) return true;

        if (stack == null || stack.getType() == Material.AIR) return true;
        if (stack.getType() != Material.POTION) return true;

        return ! isOfOwnType(stack);
    }

    public boolean isCanAdd(HumanEntity player, Item item) {
        return this.isCanAdd(player, item.getItemStack());
    }

    public boolean isOfOwnType(ItemStack item) {
        if (item == null || item.getType() != Material.POTION) return false;

        ItemMeta meta = item.getItemMeta();
        if (! (meta instanceof PotionMeta)) return false;
        PotionMeta potionMeta = (PotionMeta) meta;
        PotionType pt = potionMeta.getBasePotionType();
        if (isProbablyPotionType()) {
            PotionType ownType = this.asPotionType();
            return ownType != null && ownType.equals(pt);
        } else {
            List<PotionEffect> effects = new ArrayList<>(potionMeta.getCustomEffects());
            PotionEffectType ownType = this.asEffectType();
            if (ownType == null) return false;
            return effects.stream().anyMatch(effect -> effect.getType().equals(ownType));
        }
    }
}
