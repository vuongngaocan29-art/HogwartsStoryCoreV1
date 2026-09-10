package com.donnnsleep.hogwarts.house;

import com.donnnsleep.hogwarts.HogwartsStoryCore;
import com.donnnsleep.hogwarts.model.CommonRoom;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class CommonRoomListener implements Listener {

    private final HogwartsStoryCore plugin;

    public CommonRoomListener(HogwartsStoryCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return;
        if (e.getClickedBlock() == null) return;

        CommonRoom room = plugin.getCommonRooms().entranceAt(e.getClickedBlock().getLocation());
        if (room == null) return;

        e.setCancelled(true);
        plugin.getCommonRooms().attemptEntry(e.getPlayer(), room);
    }

    /**
     * Bắt tin nhắn của người đang chờ nhập mật khẩu / trả lời câu đố.
     * Huỷ tin nhắn để mật khẩu không bị lộ ra chat công cộng.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent e) {
        Player player = e.getPlayer();
        String msg = PlainTextComponentSerializer.plainText().serialize(e.message());

        boolean consumed = plugin.getCommonRooms().handleChatAnswer(player, msg);
        if (consumed) e.setCancelled(true);
    }
}
