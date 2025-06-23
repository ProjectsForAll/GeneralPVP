package host.plas.generalpvp.utils;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class MainUtils {
    public static boolean isBed(Block block) {
        if (block == null) return false;
        String type = block.getType().name().toLowerCase();

        return type.endsWith("bed");
    }

    public static boolean isAnchor(Block block) {
        if (block == null) return false;

        return block.getType() == Material.RESPAWN_ANCHOR;
    }
}
