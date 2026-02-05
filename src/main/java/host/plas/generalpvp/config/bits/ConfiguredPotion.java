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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
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

    public boolean checkAndDropAndClear(HumanEntity player) {
        return checkEffects(player) || checkInventory(player);
    }

    public boolean checkEffects(HumanEntity player) {
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

    public boolean checkInventory(HumanEntity player) {
        if (player == null) return false;
        if (player.hasPermission(GeneralPVP.getMainConfig().getBypassPotionCheckPermission())) return true;

        PlayerInventory inventory = player.getInventory();

        ConcurrentSkipListMap<Integer, ItemStack> toSet = new ConcurrentSkipListMap<>();

        AtomicBoolean found = new AtomicBoolean(false);
        AtomicInteger amount = new AtomicInteger(0);

        inventory.all(Material.POTION).forEach((slot, item) -> {
            ItemStack newStack = updateStack(item);

            toSet.put(slot, newStack);

            found.set(true);
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

    public PotionEffectType asEffectType() {
        try {
            if (isDefaultType()) {
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

    public boolean isDefaultType() {
        return ! getType().contains(":");
    }

    public boolean isCanAdd(HumanEntity player, ItemStack stack) {
        if (player == null) return false;
        if (player.hasPermission(GeneralPVP.getMainConfig().getBypassPotionCheckPermission())) return true;

        if (stack == null || stack.getType() == Material.AIR) return true;
        if (stack.getType() != Material.POTION) return true;

        if (! isOfOwnType(stack)) return true;

        updateSingle(stack);
        return true;
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
        if (isDefaultType()) {
            PotionType ownType = this.asPotionType();
            return ownType != null && ownType.equals(pt);
        } else {
            List<PotionEffect> effects = new ArrayList<>(potionMeta.getCustomEffects());
            PotionEffectType ownType = this.asEffectType();
            if (ownType == null) return false;
            return effects.stream().anyMatch(effect -> effect.getType().equals(ownType));
        }
    }

    public ItemStack updateStack(ItemStack item) {
        ItemStack newItem = item.clone();

        updateSingle(newItem);

        return newItem;
    }

    public void updateSingle(ItemStack item) {
        if (item == null || item.getType() != Material.POTION) return;

        int newAmplifier = this.getAmplifier() - 1 - 1; // -1 because amplifier is 0-based

        ItemMeta meta = item.getItemMeta();
        if (! (meta instanceof PotionMeta)) return;
        PotionMeta potionMeta = (PotionMeta) meta;
        if (potionMeta.hasCustomEffects()) {
            List<PotionEffect> effects = new ArrayList<>(potionMeta.getCustomEffects());
            List<PotionEffect> newEffects = new ArrayList<>();
            effects.forEach(effect -> {
                if (!asEffectType().equals(effect.getType())) newEffects.add(effect);

                if (effect.getAmplifier() + 1 >= this.getAmplifier()) { // +1 because amplifier is 0-based
                    if (newAmplifier < 0) return; // Remove the effect completely // is 0-based

                    PotionEffect newEffect = new PotionEffect(effect.getType(), effect.getDuration(), newAmplifier); // double -1 because amplifier is 0-based
                    newEffects.add(newEffect);
                }
            });

            if (newEffects.isEmpty()) {
                // Remove the item completely
                item.setType(Material.AIR);
                item.setAmount(0);
            } else {
                potionMeta.clearCustomEffects();
                newEffects.forEach(effect -> {
                    potionMeta.addCustomEffect(effect, true);
                });
                item.setItemMeta(potionMeta);
            }
        } else {
            PotionType ownType = this.asPotionType();
            if (ownType == null) return;

            if (ownType.equals(potionMeta.getBasePotionType())) {
                // Update the amplifier
                potionMeta.setBasePotionType(ownType);
                potionMeta.setMainEffect(asEffectType());

                PotionData potionData = potionMeta.getBasePotionData();
                boolean isUpgraded = newAmplifier > 0;

                PotionData newPotionData = new PotionData(ownType, potionData.isExtended(), isUpgraded);
                potionMeta.setBasePotionData(newPotionData);

                item.setItemMeta(potionMeta);
            }
        }
    }
}
