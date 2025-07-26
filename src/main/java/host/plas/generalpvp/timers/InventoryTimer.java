package host.plas.generalpvp.timers;

import host.plas.bou.scheduling.BaseRunnable;
import host.plas.generalpvp.utils.ItemUtils;
import org.bukkit.Bukkit;

public class InventoryTimer extends BaseRunnable {
    public InventoryTimer() {
        super(0, 5);
    }

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(ItemUtils::checkItemFromInventory);
    }
}
