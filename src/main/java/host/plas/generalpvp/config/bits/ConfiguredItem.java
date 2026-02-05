package host.plas.generalpvp.config.bits;

import gg.drak.thebase.objects.Identifiable;
import host.plas.generalpvp.GeneralPVP;
import host.plas.generalpvp.utils.InvUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Getter @Setter
public class ConfiguredItem implements Identifiable {
    private String identifier;

    private Material material;
    private int maxAmount;

    public ConfiguredItem(String identifier, Material material, int maxAmount) {
        this.identifier = identifier;
        this.material = material;
        this.maxAmount = maxAmount;
    }

    public boolean checkAndDrop(HumanEntity player) {
        if (player == null) return false;
        if (player.hasPermission(GeneralPVP.getMainConfig().getBypassItemCheckPermission())) return true;

        PlayerInventory inventory = player.getInventory();

        ConcurrentSkipListMap<Integer, ItemStack> toSet = new ConcurrentSkipListMap<>();

        AtomicBoolean dropped = new AtomicBoolean(false);
        AtomicInteger amount = new AtomicInteger(0);

        if (GeneralPVP.getMainConfig().isAllGoldenApplesSame() &&
                (this.getMaterial() == Material.ENCHANTED_GOLDEN_APPLE || this.getMaterial() == Material.GOLDEN_APPLE)) {
            InvUtils.handleInventory(inventory, toSet, dropped, amount, player, Material.GOLDEN_APPLE, this.getMaxAmount());
            InvUtils.handleInventory(inventory, toSet, dropped, amount, player, Material.ENCHANTED_GOLDEN_APPLE, this.getMaxAmount());
        } else {
            InvUtils.handleInventory(inventory, toSet, dropped, amount, player, this.getMaterial(), this.getMaxAmount());
        }

        if (! toSet.isEmpty()) {
            InvUtils.handleToSet(toSet, inventory);
        }

        return dropped.get();
    }

    public int getCurrentAmount(HumanEntity player) {
        PlayerInventory inventory = player.getInventory();
        return InvUtils.getCount(inventory, player, this.getMaterial());
    }

    public boolean isCanAdd(HumanEntity player, ItemStack stack) {
        if (player == null) return false;
        if (player.hasPermission(GeneralPVP.getMainConfig().getBypassItemCheckPermission())) return true;

        if (stack == null || stack.getType() == Material.AIR) return true;
        if (stack.getType() != this.getMaterial()) return true;

        int currentAmount = this.getCurrentAmount(player);
        int newAmount = currentAmount + stack.getAmount();

        return newAmount <= this.getMaxAmount();
    }

    public boolean isCanAdd(HumanEntity player, Item item) {
        return this.isCanAdd(player, item.getItemStack());
    }
}
