package host.plas.generalpvp.config.bits;

import gg.drak.thebase.objects.Identifiable;
import host.plas.generalpvp.GeneralPVP;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Getter @Setter
public class ConfiguredEnchantment implements Identifiable {
    private String identifier;

    private String type;
    private int amplifier;

    public ConfiguredEnchantment(String identifier, String type, int amplifier) {
        this.identifier = identifier;
        this.type = type;
        this.amplifier = amplifier;
    }

    public boolean checkAndReadd(HumanEntity player) {
        if (player == null) return false;
        if (player.hasPermission(GeneralPVP.getMainConfig().getBypassEnchantmentCheckPermission())) return true;

        PlayerInventory inventory = player.getInventory();

        ConcurrentSkipListMap<Integer, ItemStack> toSet = new ConcurrentSkipListMap<>();

        AtomicBoolean found = new AtomicBoolean(false);

        AtomicInteger s = new AtomicInteger(0);
        inventory.forEach(item -> {
            int currentSlot = s.getAndIncrement();
            if (! hasEnchantmentWithAmplifier(item)) return;

            ItemStack newItem = updateStack(item);

            found.set(true);
            toSet.put(currentSlot, newItem);
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

        return found.get();
    }

    public boolean isDefaultType() {
        return ! getType().contains(":");
    }

    public int getCurrentAmount(HumanEntity player) {
        PlayerInventory inventory = player.getInventory();
        AtomicInteger amount = new AtomicInteger(0);

        inventory.forEach(item -> {
            if (! hasEnchantmentWithAmplifier(item)) return;

            int amountInSlot = item.getAmount();
            amount.addAndGet(amountInSlot);
        });

        return amount.get();
    }

    public boolean isCanAdd(HumanEntity player, ItemStack stack) {
        if (player == null) return false;
        if (player.hasPermission(GeneralPVP.getMainConfig().getBypassEnchantmentCheckPermission())) return true;
        if (! hasEnchantmentWithAmplifier(stack)) return true;

        updateSingle(stack);

        return true;
    }

    public boolean isCanAdd(HumanEntity player, Item item) {
        return this.isCanAdd(player, item.getItemStack());
    }

    public boolean hasEnchantmentWithAmplifier(ItemStack item) {
        if (item == null) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        Enchantment ownEnch = asEnchantment();
        if (ownEnch == null) return false;

        return meta.getEnchants().keySet().stream()
                .anyMatch(enchantment -> enchantment.getKey().equals(ownEnch.getKey()) && meta.getEnchantLevel(enchantment) >= this.getAmplifier());
    }

    public Enchantment asEnchantment() {
        try {
            NamespacedKey key = null;
            if (isDefaultType()) {
                key = NamespacedKey.minecraft(getType().toLowerCase());
            } else {
                key = NamespacedKey.fromString(getType());
            }

            if (key == null) {
                GeneralPVP.getInstance().logInfo("ConfiguredEnchantment with type: " + getType() + " has null NamespacedKey.");
                return null;
            }

            return Enchantment.getByKey(key);
        } catch (Exception e) {
            GeneralPVP.getInstance().logInfo("Failed to convert enchantment type: " + getType() + " to Enchantment. " + e.getMessage());
            return null;
        }
    }

    public ItemStack updateStack(ItemStack item) {
        ItemStack newItem = item.clone();

        updateSingle(newItem);

        return newItem;
    }

    public void updateSingle(ItemStack stack) {
        int newAmplifier = this.getAmplifier() - 1;

        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            GeneralPVP.getInstance().logSevere("An item with type: " + stack.getType() + " has null ItemMeta.");
            return;
        }
        Enchantment ownEnch = asEnchantment();
        if (meta.getEnchants().keySet().stream()
                .anyMatch(enchantment -> enchantment.getKey().equals(ownEnch.getKey()) && meta.getEnchantLevel(enchantment) >= this.getAmplifier())) {
            meta.removeEnchant(ownEnch);
            if (newAmplifier > 0) {
                meta.addEnchant(ownEnch, newAmplifier, true);
            }

            stack.setItemMeta(meta);
        }
    }
}
