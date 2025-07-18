package host.plas.generalpvp.data;

import host.plas.bou.scheduling.BaseDelayedRunnable;
import host.plas.generalpvp.GeneralPVP;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.concurrent.ConcurrentSkipListSet;

@Getter @Setter
public class PearlCooldown extends BaseDelayedRunnable {
    private String ownUuid;

    public PearlCooldown(long ticks, String ownUuid) {
        super(ticks);
        this.ownUuid = ownUuid;
    }

    public PearlCooldown(String ownUuid) {
        this(getCooldownTicks(), ownUuid);
    }

    public PearlCooldown(Player player) {
        this(player.getUniqueId().toString());
    }

    public PearlCooldown(long ticks, Player player) {
        this(ticks, player.getUniqueId().toString());
    }

    public void register() {
        hardAddCooldown(getOwnUuid());
    }

    @Override
    public void runDelayed() {
        hardRemoveCooldown(getOwnUuid());
    }

    @Getter @Setter
    private static ConcurrentSkipListSet<String> uuidsOnCooldown = new ConcurrentSkipListSet<>();

    public static boolean isOnCooldown(String uuid) {
        return getUuidsOnCooldown().stream().anyMatch(string -> string.equalsIgnoreCase(uuid));
    }

    public static boolean isOnCooldown(Player player) {
        return isOnCooldown(player.getUniqueId().toString());
    }

    public static void hardAddCooldown(String uuid) {
        getUuidsOnCooldown().add(uuid);
    }

    public static void hardRemoveCooldown(String uuid) {
        getUuidsOnCooldown().removeIf(string -> string.equalsIgnoreCase(uuid));
    }

    public static void addCooldown(String uuid) {
        new PearlCooldown(uuid);
    }

    public static void addCooldown(Player player) {
        new PearlCooldown(player);
    }

    public static long getCooldownTicks() {
        return GeneralPVP.getMainConfig().getPearlCooldown();
    }
}
