package host.plas.generalpvp.timers;

import host.plas.bou.scheduling.BaseRunnable;
import host.plas.bou.scheduling.TaskManager;
import host.plas.generalpvp.GeneralPVP;
import org.bukkit.Bukkit;

public class InventoryTimer extends BaseRunnable {
    public InventoryTimer() {
        super(0, 5);
    }

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            TaskManager.runTask(player, () -> {
                GeneralPVP.getMainConfig().getItemConfigurations().forEach(configuredItem -> {
                    configuredItem.checkAndDrop(player);
                });
            });
        });
    }
}
