package com.donnnsleep.hogwarts;

import com.donnnsleep.hogwarts.model.StudentProfile;
import com.donnnsleep.hogwarts.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final HogwartsStoryCore plugin;

    public PlayerListener(HogwartsStoryCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        StudentProfile p = plugin.getProfiles().load(player.getUniqueId(), player.getName());

        // Học sinh mới: gửi thư nhập học sau vài giây
        if (!p.hasFlag("year1.letter")) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) return;
                plugin.getStory().sendAcceptanceLetter(player);
                player.sendMessage(Msg.color("&8Lần đầu chơi? Gõ &f/trogiup &8để xem sổ tay hướng dẫn."));
            }, 100L);
            return;
        }

        // Đã có thư nhưng chưa Phân Loại
        if (!p.isSorted()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) return;
                if (!p.hasWand()) {
                    player.sendMessage(Msg.msg("&7Con chưa có đũa phép. Hãy tới &fTiệm Ollivander&7."));
                } else {
                    player.sendMessage(Msg.msg("&7Lễ Phân Loại đang chờ con ở &fĐại Sảnh Đường&7."));
                }
            }, 60L);
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            player.sendMessage(Msg.color(""));
            player.sendMessage(Msg.color("&7Chào mừng trở lại, học sinh "
                    + p.getHouse().getLegacyColor() + p.getHouse().getDisplayName()
                    + " &7— &fNăm " + com.donnnsleep.hogwarts.story.StoryManager.roman(p.getYear())));
            player.sendMessage(Msg.color("&8Gõ &f/trogiup &8nếu cần xem lại hướng dẫn."));
            player.sendMessage(Msg.color(""));
        }, 40L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.getProfiles().unload(e.getPlayer().getUniqueId());
        plugin.getSpellManager().clear(e.getPlayer().getUniqueId());
        plugin.getSilenced().remove(e.getPlayer().getUniqueId());
        plugin.getCommonRooms().clear(e.getPlayer().getUniqueId());
        plugin.getSorting().clear(e.getPlayer().getUniqueId());
    }
}
