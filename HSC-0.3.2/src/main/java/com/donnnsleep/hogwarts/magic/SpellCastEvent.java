package com.donnnsleep.hogwarts.magic;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Bắn ra mỗi khi một người chơi niệm phép thành công.
 * Plugin khác (hoặc addon của bạn) có thể lắng nghe sự kiện này.
 */
public class SpellCastEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Spell spell;

    public SpellCastEvent(Player player, Spell spell) {
        this.player = player;
        this.spell = spell;
    }

    public Player getPlayer() { return player; }
    public Spell getSpell() { return spell; }

    @Override public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
