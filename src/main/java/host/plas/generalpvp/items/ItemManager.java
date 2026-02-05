package host.plas.generalpvp.items;

import host.plas.bou.items.ItemUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;

public class ItemManager {
    @Getter @Setter
    private static ConcurrentSkipListSet<StickyItem> loadedItems = new ConcurrentSkipListSet<>();

    public static void load(StickyItem item) {
        getLoadedItems().add(item);
    }

    public static void unload(StickyItem item) {
        unload(item.getStack());
    }

    public static void unload(String identifier) {
        getLoadedItems().removeIf(item -> item.getIdentifier().equals(identifier));
    }

    public static void unload(ItemStack item) {
        getLoadedItems().removeIf(stickyItem -> stickyItem.getStack().equals(item) && stickyItem.getIdentifier().equals(ItemUtils.getItemNBTStrict(item)));
    }

    public static Optional<StickyItem> get(String identifier) {
        return getLoadedItems().stream()
                .filter(item -> item.getIdentifier().equals(identifier))
                .findFirst();
    }

    public static Optional<StickyItem> get(ItemStack stack) {
        return  getLoadedItems().stream()
                .filter(item -> item.getStack().equals(stack) && item.getIdentifier().equals(ItemUtils.getItemNBTStrict(stack)))
                .findFirst();
    }

    public static boolean has(String identifier) {
        return get(identifier).isPresent();
    }

    public static boolean has(ItemStack stack) {
        return get(stack).isPresent();
    }
}
