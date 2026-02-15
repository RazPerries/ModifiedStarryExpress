package org.aussiebox.starexpress;

import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.aussiebox.starexpress.item.StarryExpressItems;

import java.util.ArrayList;
import java.util.List;

public interface StarryExpressConstants {
    List<ShopEntry> MUZZLER_SHOP = Util.make(new ArrayList<>(), (entries) -> {
        entries.addAll(GameConstants.SHOP_ENTRIES);
        entries.addFirst(new ShopEntry(StarryExpressItems.TAPE.getDefaultInstance(), 75, ShopEntry.Type.WEAPON));
    });

    ResourceLocation SILENCED_OUTSIDE_DEATH_REASON = StarryExpress.id("silenced_and_outside");
    ResourceLocation SILENCED_TAPE_REMOVED_DEATH_REASON = StarryExpress.id("tape_removed_low_mood");
}
