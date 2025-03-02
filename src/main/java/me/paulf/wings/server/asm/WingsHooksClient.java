package me.paulf.wings.server.asm;

import com.mojang.blaze3d.vertex.PoseStack;
import me.paulf.wings.util.Access;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

import java.lang.invoke.MethodHandle;

public final class WingsHooksClient {
    private WingsHooksClient() {
    }

    private static int selectedItemSlot = 0;

    public static void onSetPlayerRotationAngles(LivingEntity living, PlayerModel<?> model, float ageTicks, float pitch) {
        if (living instanceof Player) {
            MinecraftForge.EVENT_BUS.post(new AnimatePlayerModelEvent((Player) living, model, ageTicks, pitch));
        }
    }

    public static void onApplyPlayerRotations(AbstractClientPlayer player, PoseStack matrixStack, float delta) {
        MinecraftForge.EVENT_BUS.post(new ApplyPlayerRotationsEvent(player, matrixStack, delta));
    }

    public static void onTurn(Entity entity, float deltaYaw) {
        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) entity;
            float theta = Mth.wrapDegrees(living.getYRot() - living.yBodyRot);
            GetLivingHeadLimitEvent ev = GetLivingHeadLimitEvent.create(living);
            MinecraftForge.EVENT_BUS.post(ev);
            float limit = ev.getHardLimit();
            if (theta < -limit || theta > limit) {
                living.yBodyRot += deltaYaw;
                living.yBodyRotO += deltaYaw;
            }
        }
    }

    public static boolean onCheckRenderEmptyHand(boolean isMainHand, ItemStack itemStackMainHand) {
        return isMainHand || !Holder.OPTIFUCK && !isMap(itemStackMainHand);
    }

    public static boolean onCheckDoReequipAnimation(ItemStack from, ItemStack to, int slot) {
        boolean fromEmpty = from.isEmpty();
        boolean toEmpty = to.isEmpty();
        boolean isOffHand = slot == -1;
        if (toEmpty && isOffHand) {
            Minecraft mc = Minecraft.getInstance();
            AbstractClientPlayer player = mc.player;
            if (player == null) {
                return true;
            }
            boolean fromMap = isMap(GetItemStackMainHand.invoke(mc.getItemInHandRenderer()));
            boolean toMap = isMap(player.getMainHandItem());
            if (fromMap || toMap) {
                return fromMap != toMap;
            }
            if (fromEmpty) {
                EmptyOffHandPresentEvent ev = new EmptyOffHandPresentEvent(player);
                MinecraftForge.EVENT_BUS.post(ev);
                return ev.getResult() != Event.Result.ALLOW;
            }
        }
        if (fromEmpty || toEmpty) {
            return fromEmpty != toEmpty;
        }
        boolean hasSlotChange = !isOffHand && selectedItemSlot != (selectedItemSlot = slot);
        return from.getItem().shouldCauseReequipAnimation(from, to, hasSlotChange);
    }

    private static boolean isMap(ItemStack stack) {
        return stack.getItem() instanceof MapItem;
    }

    private static final class GetItemStackMainHand {
        private GetItemStackMainHand() {
        }

        private static final MethodHandle MH = Access.getter(ItemInHandRenderer.class)
            .name("field_187467_d", "mainHandItem")
            .type(ItemStack.class);

        private static ItemStack invoke(ItemInHandRenderer instance) {
            try {
                return (ItemStack) MH.invokeExact(instance);
            } catch (Throwable t) {
                throw Access.rethrow(t);
            }
        }
    }

    private static final class Holder {
        private static final boolean OPTIFUCK;
        static {
            boolean present;
            try {
                Class.forName("optifine.ZipResourceProvider");
                present = true;
            } catch (ClassNotFoundException thankGod) {
                present = false;
            }
            OPTIFUCK = present;
        }
    }
}
