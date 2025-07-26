package host.plas.generalpvp.commands;

import host.plas.bou.commands.CommandContext;
import host.plas.bou.commands.SimplifiedCommand;
import host.plas.generalpvp.GeneralPVP;

public class ReloadCMD extends SimplifiedCommand {
    public ReloadCMD() {
        super("gpvpreload", GeneralPVP.getInstance());
    }

    @Override
    public boolean command(CommandContext ctx) {
        ctx.sendMessage("&eReloading &bGeneralPVP&7...");

        ctx.sendMessage("&eReloading &dConfigurations&7...");
        GeneralPVP.getMainConfig().init();

        ctx.sendMessage("&eReloaded &bGeneralPVP&7...");
        return true;
    }
}
