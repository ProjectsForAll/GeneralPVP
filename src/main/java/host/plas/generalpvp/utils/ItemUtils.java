package host.plas.generalpvp.utils;

import host.plas.bou.scheduling.TaskManager;
import host.plas.generalpvp.GeneralPVP;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.atomic.AtomicBoolean;

public class ItemUtils {
    public static boolean checkItem(final HumanEntity player, final ItemStack stack) {
        AtomicBoolean needToCancel = new AtomicBoolean(false);

        GeneralPVP.getMainConfig().getItemConfigurations().forEach(r -> {
            if (needToCancel.get()) return;
            if (r.isCanAdd(player, stack)) return;

            needToCancel.set(true);
        });
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
