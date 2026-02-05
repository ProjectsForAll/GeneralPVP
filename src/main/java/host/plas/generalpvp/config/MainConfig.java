package host.plas.generalpvp.config;

import gg.drak.thebase.storage.resources.flat.simple.SimpleConfiguration;
import host.plas.generalpvp.GeneralPVP;
import host.plas.generalpvp.config.bits.ConfiguredEnchantment;
import host.plas.generalpvp.config.bits.ConfiguredItem;
import host.plas.generalpvp.config.bits.ConfiguredPotion;
import org.bukkit.Material;

import java.util.concurrent.ConcurrentSkipListSet;

public class MainConfig extends SimpleConfiguration {
    public MainConfig() {
        super("config.yml", GeneralPVP.getInstance(), true);
    }

    @Override
    public void init() {
        isAllowCrystalPVP();
        isAllowBedPVP();
        isAllowAnchorPVP();

        isDropExcess();
        isAllGoldenApplesSame();
        getItemConfigurations();

        getBypassItemCheckPermission();

        getPearlCooldown();

        getPotionConfigurations();
        getBypassPotionCheckPermission();

        getBypassEnchantmentCheckPermission();
        getEnchantmentConfigurations();
    }

    public boolean isAllowCrystalPVP() {
        reloadResource();

        return getOrSetDefault("allow.crystal-pvp", false);
    }

    public boolean isAllowBedPVP() {
        reloadResource();

        return getOrSetDefault("allow.bed-pvp", false);
    }

    public boolean isAllowAnchorPVP() {
        reloadResource();

        return getOrSetDefault("allow.anchor-pvp", false);
    }

    public void setAllowCrystalPVP(boolean allow) {
        reloadResource();

        write("allow.crystal-pvp", allow);
    }

    public void setAllowBedPVP(boolean allow) {
        reloadResource();

        write("allow.bed-pvp", allow);
    }

    public void setAllowAnchorPVP(boolean allow) {
        reloadResource();

        write("allow.anchor-pvp", allow);
    }

    public boolean isDropExcess() {
        reloadResource();

        return getOrSetDefault("items.drop-excess", true);
    }

    public boolean isAllGoldenApplesSame() {
        reloadResource();

        return getOrSetDefault("items.all-golden-apples-similar", true);
    }

    public ConcurrentSkipListSet<ConfiguredItem> getItemConfigurations() {
        reloadResource();

        ConcurrentSkipListSet<ConfiguredItem> r = new ConcurrentSkipListSet<>();

        singleLayerKeySet("items").forEach(key -> {
            if (key.equals("drop-excess")) return; // Skip the drop-excess key
            if (key.equals("all-golden-apples-similar")) return; // Skip the all-golden-apples-similar key

            String identifier = key;
            String materialName = getOrSetDefault("items." + key + ".material", "STONE");
            int maxAmount = getOrSetDefault("items." + key + ".max-amount", 64);

            try {
                Material material = Material.valueOf(materialName.toUpperCase());
                r.add(new ConfiguredItem(identifier, material, maxAmount));
            } catch (IllegalArgumentException e) {
                GeneralPVP.getInstance().getLogger().warning("Invalid material '" + materialName + "' for item '" + identifier + "'");
            }
        });

        return r;
    }

    public String getBypassItemCheckPermission() {
        reloadResource();

        return getOrSetDefault("bypass.item-check.permission", "generalpvp.bypass.item-check");
    }

    public String getBypassPotionCheckPermission() {
        reloadResource();

        return getOrSetDefault("bypass.potion-check.permission", "generalpvp.bypass.potion-check");
    }

    public String getBypassEnchantmentCheckPermission() {
        reloadResource();

        return getOrSetDefault("bypass.enchantment-check.permission", "generalpvp.bypass.enchantment-check");
    }

    public long getPearlCooldown() {
        reloadResource();

        return getOrSetDefault("cooldowns.pearl", 20 * 15L); // Default to 5 seconds
    }

    public ConcurrentSkipListSet<ConfiguredPotion> getPotionConfigurations() {
        reloadResource();

        ConcurrentSkipListSet<ConfiguredPotion> r = new ConcurrentSkipListSet<>();

        String mainKey = "potions";

        singleLayerKeySet(mainKey).forEach(key -> {
            try {
                String identifier = key;
                String typeStr = getOrSetDefault(mainKey + "." + key + ".type", "STRENGTH");
                int amplifier = getOrSetDefault(mainKey + "." + key + ".amplifier", 2);

                r.add(new ConfiguredPotion(identifier, typeStr, amplifier));
            } catch (Throwable e) {
                GeneralPVP.getInstance().getLogger().warning("Invalid potion type for item '" + key + "': " + e.getMessage());
            }
        });

        return r;
    }

    public ConcurrentSkipListSet<ConfiguredEnchantment> getEnchantmentConfigurations() {
        reloadResource();

        ConcurrentSkipListSet<ConfiguredEnchantment> r = new ConcurrentSkipListSet<>();

        String mainKey = "enchantments";

        singleLayerKeySet(mainKey).forEach(key -> {
            try {
                String identifier = key;
                String typeStr = getOrSetDefault(mainKey + "." + key + ".type", "PROTECTION");
                int amplifier = getOrSetDefault(mainKey + "." + key + ".amplifier", 4);

                r.add(new ConfiguredEnchantment(identifier, typeStr, amplifier));
            } catch (Throwable e) {
                GeneralPVP.getInstance().getLogger().warning("Invalid potion type for item '" + key + "': " + e.getMessage());
            }
        });

        return r;
    }

    public void setDropExcess(boolean drop) {
        reloadResource();

        write("items.drop-excess", drop);
    }

    public void setPearlCooldown(long ticks) {
        reloadResource();

        write("cooldowns.pearl", ticks);
    }

    public void addItemConfiguration(ConfiguredItem item) {
        reloadResource();

        write("items." + item.getIdentifier() + ".material", item.getMaterial().name());
        write("items." + item.getIdentifier() + ".max-amount", item.getMaxAmount());
    }

    public void removeItemConfiguration(String identifier) {
        reloadResource();

        write("items." + identifier, null);
    }

    public void addPotionConfiguration(ConfiguredPotion potion) {
        reloadResource();

        write("potions." + potion.getIdentifier() + ".type", potion.getType());
        write("potions." + potion.getIdentifier() + ".amplifier", potion.getAmplifier());
    }

    public void removePotionConfiguration(String identifier) {
        reloadResource();

        write("potions." + identifier, null);
    }

    public void addEnchantmentConfiguration(ConfiguredEnchantment enchantment) {
        reloadResource();

        write("enchantments." + enchantment.getIdentifier() + ".type", enchantment.getType());
        write("enchantments." + enchantment.getIdentifier() + ".amplifier", enchantment.getAmplifier());
    }

    public void removeEnchantmentConfiguration(String identifier) {
        reloadResource();

        write("enchantments." + identifier, null);
    }
}
