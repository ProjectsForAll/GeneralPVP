package host.plas.generalpvp.items;

import host.plas.bou.scheduling.BaseDelayedRunnable;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class StickyTimer extends BaseDelayedRunnable {
    private final StickyItem item;

    public StickyTimer(long delay, StickyItem item) {
        super(delay);

        this.item = item;
    }

    @Override
    public void runDelayed() {
        ItemManager.unload(getItem());
    }
}
