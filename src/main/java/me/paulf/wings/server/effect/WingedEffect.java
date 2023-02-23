package me.paulf.wings.server.effect;

import me.paulf.wings.server.item.WingsItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.potion.EffectType;

import java.util.ArrayList;
import java.util.List;

public class WingedEffect extends MobEffect {
    protected WingedEffect(int color) {
        super(EffectType.BENEFICIAL, color);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        List<ItemStack> ret = new ArrayList<>();
        ret.add(new ItemStack(WingsItems.BAT_BLOOD_BOTTLE.get()));
        return ret;
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
