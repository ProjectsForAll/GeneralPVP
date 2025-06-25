package host.plas.generalpvp.config.bits;

import gg.drak.thebase.objects.Identifiable;
import host.plas.generalpvp.GeneralPVP;
import host.plas.generalpvp.items.ItemManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
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

    public boolean checkAndDrop(Player player) {
        if (player == null) return false;
        if (player.hasPermission(GeneralPVP.getMainConfig().getBypassItemCheckPermission())) return true;

        PlayerInventory inventory = player.getInventory();

        ConcurrentSkipListMap<Integer, ItemStack> toSet = new ConcurrentSkipListMap<>();

        AtomicBoolean dropped = new AtomicBoolean(false);
        AtomicInteger amount = new AtomicInteger(0);
        inventory.all(this.getMaterial()).forEach((slot, item) -> {
            if (item == null || item.getType() != material) return;

            int beforeAmount = amount.get();
            int currentAmount = item.getAmount();
            int newAmount = beforeAmount + currentAmount;

            if (newAmount > this.getMaxAmount()) {
                int excessAmount = newAmount - this.getMaxAmount();
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

        inventory.all(this.getMaterial()).forEach((slot, item) -> {
            if (item == null || item.getType() != material) return;

            int currentAmount = item.getAmount();
            amount.addAndGet(currentAmount);
        });

        return amount.get();
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
