package host.plas.generalpvp.events;

import host.plas.bou.utils.SenderUtils;
import host.plas.generalpvp.GeneralPVP;
import host.plas.generalpvp.items.ItemManager;
import host.plas.generalpvp.items.StickyItem;
import host.plas.generalpvp.utils.MainUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.atomic.AtomicBoolean;

public class MainListener extends AbstractConglomerate {
    @EventHandler
    public void onRightClickBed(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block == null) return;

        Location location = block.getLocation();

        if (MainUtils.isBed(block) && ! GeneralPVP.getMainConfig().isAllowBedPVP()) {
            if (location.getWorld().getEnvironment() == World.Environment.NORMAL) return;

            AtomicBoolean stop = new AtomicBoolean(false);

            location.getNearbyEntitiesByType(Player.class, 15).forEach(p -> {
                if (p.equals(player)) return;

                stop.set(true);
            });

            if (stop.get()) {
                SenderUtils.getSender(player).sendMessage("&cYou cannot explode beds close to other players in this dimension!");
            }
        }

        if (MainUtils.isAnchor(block) && ! GeneralPVP.getMainConfig().isAllowAnchorPVP()) {
            RespawnAnchor anchor = (RespawnAnchor) block.getBlockData();
            if (anchor.getCharges() <= anchor.getMaximumCharges()) return;

            event.setCancelled(true);

            SenderUtils.getSender(player).sendMessage("&cYou cannot explode respawn anchors!");
        }
    }

    @EventHandler
    public void onCrystalExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        Location location = entity.getLocation();

        if (entity instanceof EnderCrystal && ! GeneralPVP.getMainConfig().isAllowCrystalPVP()) {
            if (location.getNearbyEntitiesByType(Player.class, 15).isEmpty()) return;

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onAnchorExplode(BlockExplodeEvent event) {
        Block block = event.getBlock();
        Location location = block.getLocation();

        if (MainUtils.isAnchor(block) && ! GeneralPVP.getMainConfig().isAllowAnchorPVP()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItemDrop();
        ItemStack stack = item.getItemStack();

        new StickyItem(stack);
    }
}
