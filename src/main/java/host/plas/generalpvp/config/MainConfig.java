package host.plas.generalpvp.config;

import gg.drak.thebase.storage.resources.flat.simple.SimpleConfiguration;
import host.plas.generalpvp.GeneralPVP;
import host.plas.generalpvp.config.bits.ConfiguredItem;
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

        ConcurrentSkipListSet<ConfiguredItem> items = new ConcurrentSkipListSet<>();

        singleLayerKeySet("items").forEach(key -> {
            if (key.equals("drop-excess")) return; // Skip the drop-excess key
            if (key.equals("all-golden-apples-similar")) return; // Skip the all-golden-apples-similar key

            String identifier = key;
            String materialName = getOrSetDefault("items." + key + ".material", "STONE");
            int maxAmount = getOrSetDefault("items." + key + ".max-amount", 64);

            try {
                Material material = Material.valueOf(materialName.toUpperCase());
                items.add(new ConfiguredItem(identifier, material, maxAmount));
            } catch (IllegalArgumentException e) {
                GeneralPVP.getInstance().getLogger().warning("Invalid material '" + materialName + "' for item '" + identifier + "'");
            }
        });

        return items;
    }

    public String getBypassItemCheckPermission() {
        reloadResource();

        return getOrSetDefault("bypass.item-check.permission", "generalpvp.bypass.item-check");
    }

    public long getPearlCooldown() {
        reloadResource();

        return getOrSetDefault("cooldowns.pearl", 20 * 15L); // Default to 5 seconds
    }
}
