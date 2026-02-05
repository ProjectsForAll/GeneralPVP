package host.plas.generalpvp.utils;

import host.plas.bou.scheduling.TaskManager;
import host.plas.generalpvp.GeneralPVP;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ItemUtils {
    public static boolean checkItem(final HumanEntity player, final ItemStack stack) {
        return checkItem(player, stack, false);
    }

    public static boolean checkItem(final HumanEntity player, final ItemStack stack, boolean isPickUp) {
        AtomicBoolean needToCancel = new AtomicBoolean(false);

        if (! isPickUp) {
            GeneralPVP.getMainConfig().getItemConfigurations().forEach(r -> {
                if (needToCancel.get()) return;
                if (r.isCanAdd(player, stack)) return;

                needToCancel.set(true);
            });
        }
        GeneralPVP.getMainConfig().getPotionConfigurations().forEach(r -> {
            if (needToCancel.get()) return;
            if (r.isCanAdd(player, stack)) return;

            needToCancel.set(true);
        });
        GeneralPVP.getMainConfig().getEnchantmentConfigurations().forEach(r -> {
            if (needToCancel.get()) return;
            if (r.isCanAdd(player, stack)) return;

            needToCancel.set(true);
        });

        return needToCancel.get();
    }

    public static int getPickUpStack(final HumanEntity player, final ItemStack stack) {
        AtomicInteger countToTake = new AtomicInteger(-1);

        GeneralPVP.getMainConfig().getItemConfigurations().forEach(r -> {
            if (countToTake.get() != -1) return; // Already found a valid item

            if (r.getMaterial() != stack.getType()) return; // Only check matching items

            int current = r.getCurrentAmount(player);
            int max = r.getMaxAmount();
            if (current >= max) {
                countToTake.set(0);
                return; // Already at max, no need to check further
            }

            int itemAmount = stack.getAmount();

            int pickUpAmount = Math.min(itemAmount, max - current);

            if (pickUpAmount <= 0) {
                countToTake.set(0);
                return; // Already at max, no need to check further
            }

            countToTake.set(pickUpAmount);
        });

        return countToTake.get();
    }

    public static void checkItemFromInventory(final Player player) {
        TaskManager.runTask(player, () -> {
            GeneralPVP.getMainConfig().getItemConfigurations().forEach(r -> {
                r.checkAndDrop(player);
            });
            GeneralPVP.getMainConfig().getPotionConfigurations().forEach(r -> {
                r.checkAndDropAndClear(player);
            });
            GeneralPVP.getMainConfig().getEnchantmentConfigurations().forEach(r -> {
                r.checkAndReadd(player);
            });
        });
    }
}
