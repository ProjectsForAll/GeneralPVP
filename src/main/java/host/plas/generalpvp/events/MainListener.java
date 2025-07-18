package host.plas.generalpvp.events;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import host.plas.bou.utils.SenderUtils;
import host.plas.generalpvp.GeneralPVP;
import host.plas.generalpvp.data.PearlCooldown;
import host.plas.generalpvp.items.StickyItem;
import host.plas.generalpvp.utils.MainUtils;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.concurrent.atomic.AtomicBoolean;

public class MainListener extends AbstractConglomerate {
    @EventHandler
    public void onRightClickBed(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block == null) {
            block = player.getTargetBlockExact(15, FluidCollisionMode.NEVER);
            if (block == null) return;
        }

        Location location = block.getLocation();

        if (MainUtils.isBed(block) && ! GeneralPVP.getMainConfig().isAllowBedPVP()) {
            if (location.getWorld().getEnvironment() == World.Environment.NORMAL) return;
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

            AtomicBoolean stop = new AtomicBoolean(false);

            location.getNearbyEntitiesByType(Player.class, 15).forEach(p -> {
                if (p.equals(player)) return;

                stop.set(true);
            });

            if (stop.get()) {
                SenderUtils.getSender(player).sendMessage("&cYou cannot explode beds close to other players in this dimension!");
                event.setCancelled(true);

                return;
            }
        }

        if (MainUtils.isAnchor(block) && ! GeneralPVP.getMainConfig().isAllowAnchorPVP()) {
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

            RespawnAnchor anchor = (RespawnAnchor) block.getBlockData();
            if (anchor.getCharges() < anchor.getMaximumCharges()) return;

            SenderUtils.getSender(player).sendMessage("&cYou cannot explode respawn anchors!");
            event.setCancelled(true);

            return;
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

    @EventHandler
    public void onPickUpItem(PlayerAttemptPickupItemEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItem();

        AtomicBoolean needToCancel = new AtomicBoolean(false);
        GeneralPVP.getMainConfig().getItemConfigurations().forEach(configuredItem -> {
            if (needToCancel.get()) return;
            if (configuredItem.isCanAdd(player, item)) return;

            needToCancel.set(true);
        });

        if (! needToCancel.get()) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onMoveItem(InventoryMoveItemEvent event) {
        Inventory inventory = event.getDestination();
        if (! (inventory instanceof PlayerInventory)) return;
        PlayerInventory pi = (PlayerInventory) inventory;
        HumanEntity player = pi.getHolder();

        ItemStack item = event.getItem();

        AtomicBoolean needToCancel = new AtomicBoolean(false);
        GeneralPVP.getMainConfig().getItemConfigurations().forEach(configuredItem -> {
            if (needToCancel.get()) return;
            if (configuredItem.isCanAdd(player, item)) return;

            needToCancel.set(true);
        });

        if (! needToCancel.get()) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onMoveItem(InventoryClickEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        PlayerInventory pi = humanEntity.getInventory();
        ClickType clickType = event.getClick();
        Inventory iiq = event.getInventory();
        boolean onCursor = false;

        if (event.getSlotType() == InventoryType.SlotType.QUICKBAR) return; // If hotbar slot is clicked, return.

        switch (clickType) {
            case LEFT:
            case RIGHT:
            case CREATIVE:
                if (event.getClickedInventory() != pi) return;
                iiq = pi;
                onCursor = true;
                break;
            case NUMBER_KEY:
                if (event.getClickedInventory() == pi) return; // For in player inventory -> different slot in player inventory.
                iiq = pi;
                onCursor = false;
                break;
            case SHIFT_LEFT:
            case SHIFT_RIGHT:
                if (event.getClickedInventory() == pi) return;
                iiq = pi;
                onCursor = false;
                break;
            case MIDDLE:
            case UNKNOWN:
            case CONTROL_DROP:
            case DROP:
            case WINDOW_BORDER_LEFT:
            case WINDOW_BORDER_RIGHT:
            case DOUBLE_CLICK:
            case SWAP_OFFHAND:
                return;
        }

        if (! (iiq instanceof PlayerInventory)) return;
        PlayerInventory piiq = (PlayerInventory) iiq;
        HumanEntity player = piiq.getHolder();

        ItemStack item = null;
        if (onCursor) {
            item = event.getCursor();
        } else {
            item = event.getCurrentItem();
        }

        AtomicBoolean needToCancel = new AtomicBoolean(false);
        ItemStack finalItem = item;
        GeneralPVP.getMainConfig().getItemConfigurations().forEach(configuredItem -> {
            if (needToCancel.get()) return;
            if (configuredItem.isCanAdd(player, finalItem)) return;

            needToCancel.set(true);
        });

        if (! needToCancel.get()) return;

        event.setCancelled(true);
    }

    public void onPearl(PlayerLaunchProjectileEvent event) {
        Projectile projectile = event.getProjectile();
        if (! (projectile instanceof EnderPearl)) return;
        Player player = event.getPlayer();

        if (PearlCooldown.isOnCooldown(player)) {
            event.setCancelled(true);
            return;
        } else {
            PearlCooldown.addCooldown(player);
        }
    }
}
