package host.plas.generalpvp;

import host.plas.bou.BetterPlugin;
import host.plas.generalpvp.commands.ManageCMD;
import host.plas.generalpvp.commands.ReloadCMD;
import host.plas.generalpvp.config.MainConfig;
import host.plas.generalpvp.events.MainListener;
import host.plas.generalpvp.timers.InventoryTimer;
import lombok.Getter;
import lombok.Setter;

public final class GeneralPVP extends BetterPlugin {
    @Getter @Setter
    private static GeneralPVP instance;
    @Getter @Setter
    private static MainConfig mainConfig;
    @Getter @Setter
    private static MainListener mainListener;
    @Getter @Setter
    private static InventoryTimer inventoryTimer;
    @Getter @Setter
    private static ReloadCMD reloadCMD;
    @Getter @Setter
    private static ManageCMD manageCMD;

    public GeneralPVP() {
        super();
    }

    @Override
    public void onBaseEnabled() {
        // Plugin startup logic
        setInstance(this);

        setMainConfig(new MainConfig());

        setMainListener(new MainListener());

        setInventoryTimer(new InventoryTimer());

        setReloadCMD(new ReloadCMD());
        setManageCMD(new ManageCMD());
    }

    @Override
    public void onBaseDisable() {
        // Plugin shutdown logic

        if (getInventoryTimer() != null) {
            getInventoryTimer().cancel();
            setInventoryTimer(null);
        }
    }
}
