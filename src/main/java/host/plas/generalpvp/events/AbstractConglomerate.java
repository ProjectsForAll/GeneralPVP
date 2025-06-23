package host.plas.generalpvp.events;

import gg.drak.thebase.events.BaseEventHandler;
import host.plas.bou.events.ListenerConglomerate;
import host.plas.generalpvp.GeneralPVP;
import org.bukkit.Bukkit;

public class AbstractConglomerate implements ListenerConglomerate {
    public AbstractConglomerate() {
        register();
    }

    public void register() {
        Bukkit.getPluginManager().registerEvents(this, GeneralPVP.getInstance());
        BaseEventHandler.bake(this, GeneralPVP.getInstance());
        GeneralPVP.getInstance().logInfo("Registered listeners for: &c" + this.getClass().getSimpleName());
    }
}
