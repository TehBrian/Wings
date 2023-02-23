package me.paulf.wings.server.asm;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraftforge.eventbus.api.Event;

@Event.HasResult
public final class EmptyOffHandPresentEvent extends Event {
    private final AbstractClientPlayer player;

    public EmptyOffHandPresentEvent(AbstractClientPlayer player) {
        this.player = player;
    }

    public AbstractClientPlayer getPlayer() {
        return this.player;
    }
}
