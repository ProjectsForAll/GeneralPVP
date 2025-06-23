package host.plas.generalpvp.items;

import gg.drak.thebase.objects.Identifiable;
import host.plas.bou.items.ItemUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

@Getter @Setter
public class StickyItem implements Identifiable {
    private String identifier;
    private ItemStack stack;

    private StickyTimer timer;

    public StickyItem(ItemStack stack) {
        this.identifier = ItemUtils.getItemNBTStrict(stack);
        this.stack = stack;

        load();
        makeTimer();
    }

    public void makeTimer() {
        this.timer = new StickyTimer(20L * 2, this);
    }

    public void load() {
        ItemManager.load(this);
    }

    public void unload() {
        ItemManager.unload(this);
    }
}
