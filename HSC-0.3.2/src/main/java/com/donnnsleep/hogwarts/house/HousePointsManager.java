package com.donnnsleep.hogwarts.house;

import com.donnnsleep.hogwarts.HogwartsStoryCore;
import com.donnnsleep.hogwarts.model.House;
import com.donnnsleep.hogwarts.model.StudentProfile;
import com.donnnsleep.hogwarts.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Điểm Nhà và House Cup. Điểm được lưu ở houses.yml, tồn tại qua restart.
 */
public class HousePointsManager {

    private final HogwartsStoryCore plugin;
    private final File file;
    private final EnumMap<House, Integer> points = new EnumMap<>(House.class);

    public HousePointsManager(HogwartsStoryCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "houses.yml");
        load();
    }

    public void load() {
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        for (House h : House.values()) {
            points.put(h, cfg.getInt("points." + h.name(), 0));
        }
    }

    public void save() {
        YamlConfiguration cfg = new YamlConfiguration();
        for (House h : House.values()) {
            cfg.set("points." + h.name(), points.get(h));
        }
        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Không lưu được houses.yml: " + e.getMessage());
        }
    }

    public int get(House h) {
        return points.getOrDefault(h, 0);
    }

    public void set(House h, int value) {
        points.put(h, Math.max(0, value));
        save();
    }

    /** Cộng/trừ điểm và thông báo toàn server. */
    public void award(House h, int amount, String reason) {
        if (h == null || amount == 0) return;
        points.put(h, Math.max(0, get(h) + amount));
        save();

        String sign = amount > 0 ? "&a+" : "&c";
        String verb = amount > 0 ? "được cộng" : "bị trừ";
        Bukkit.broadcast(Msg.color(h.getLegacyColor() + "&l" + h.getDisplayName().toUpperCase()
                + " &r" + sign + Math.abs(amount) + " &7điểm " + verb + " — &f" + reason));
        Bukkit.getOnlinePlayers().forEach(pl ->
                pl.playSound(pl.getLocation(),
                        amount > 0 ? Sound.BLOCK_NOTE_BLOCK_CHIME : Sound.BLOCK_NOTE_BLOCK_BASS,
                        0.7f, amount > 0 ? 1.6f : 0.7f));
    }

    /** Cộng điểm cho Nhà của một người chơi, đồng thời ghi nhận đóng góp cá nhân. */
    public void awardPlayer(Player player, int amount, String reason) {
        StudentProfile p = plugin.getProfiles().get(player);
        if (p == null || p.getHouse() == null) return;
        p.addContributedPoints(amount);
        award(p.getHouse(), amount, reason + " (" + player.getName() + ")");
    }

    /** Bảng xếp hạng từ cao xuống thấp. */
    public List<Map.Entry<House, Integer>> ranking() {
        List<Map.Entry<House, Integer>> list = new ArrayList<>(points.entrySet());
        list.sort((a, b) -> b.getValue() - a.getValue());
        return list;
    }

    public House leader() {
        return ranking().get(0).getKey();
    }

    /** Trao House Cup cuối năm và reset điểm. */
    public void awardHouseCup() {
        House winner = leader();
        Bukkit.broadcast(Msg.color("&8&m                                                  "));
        Bukkit.broadcast(Msg.color("&6&l              ✦ CÚP NHÀ ✦"));
        Bukkit.broadcast(Msg.color(""));
        for (var e : ranking()) {
            Bukkit.broadcast(Msg.color("   " + e.getKey().getLegacyColor() + e.getKey().getDisplayName()
                    + " &8· &f" + e.getValue() + " &7điểm"));
        }
        Bukkit.broadcast(Msg.color(""));
        Bukkit.broadcast(Msg.color("   &fNhà " + winner.getLegacyColor() + "&l" + winner.getDisplayName()
                + " &fgiành Cúp Nhà năm nay!"));
        Bukkit.broadcast(Msg.color("&8&m                                                  "));

        Bukkit.getOnlinePlayers().forEach(pl -> {
            pl.playSound(pl.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            pl.showTitle(net.kyori.adventure.title.Title.title(
                    Msg.color(winner.getLegacyColor() + "&l" + winner.getDisplayName()),
                    Msg.color("&7đoạt Cúp Nhà")));
        });

        for (House h : House.values()) points.put(h, 0);
        plugin.getProfiles().online().forEach(p -> p.setContributedPoints(0));
        save();
    }
}
