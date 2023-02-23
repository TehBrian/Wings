package me.paulf.wings.server.item;

import me.paulf.wings.server.apparatus.FlightApparatus;
import me.paulf.wings.server.effect.WingsEffects;
import me.paulf.wings.server.flight.Flights;
import me.paulf.wings.server.sound.WingsSounds;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.util.DrinkHelper;
import net.minecraft.world.level.Level;

public class BatBloodBottleItem extends Item {
    public BatBloodBottleItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity living) {
        if (living instanceof ServerPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer) living, stack);
            if (removeWings((ServerPlayer) living)) {
                world.playSound(null, living.getX(), living.getY(), living.getZ(), WingsSounds.ITEM_ARMOR_EQUIP_WINGS.get(), SoundSource.PLAYERS, 1.0F, 0.8F);
            }
        }
        if (living instanceof Player) {
            Player player = (Player) living;
            player.awardStat(Stats.ITEM_USED.get(this));
            if (!player.abilities.instabuild) {
                stack.shrink(1);
            }
        }
        if (stack.isEmpty()) {
            return new ItemStack(Items.GLASS_BOTTLE);
        }
        if (living instanceof Player && !((Player) living).abilities.instabuild) {
            ItemStack emptyBottle = new ItemStack(Items.GLASS_BOTTLE);
            Player player = (Player) living;
            if (!player.inventory.add(emptyBottle)) {
                player.drop(emptyBottle, false);
            }
        }
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 40;
    }

    @Override
    public UseAction getUseAnimation(ItemStack stack) {
        return UseAction.DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        return DrinkHelper.useDrink(world, player, hand);
    }

    public static boolean removeWings(ServerPlayer player) {
        return player.removeEffect(WingsEffects.WINGS.get());
    }

    public static boolean removeWings(ServerPlayer player, FlightApparatus wings) {
        boolean changed = Flights.get(player).filter(flight -> flight.getWing() == wings).isPresent();
        return changed && player.removeEffect(WingsEffects.WINGS.get());
    }
}
