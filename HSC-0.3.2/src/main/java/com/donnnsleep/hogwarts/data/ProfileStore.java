package com.donnnsleep.hogwarts.data;

import com.donnnsleep.hogwarts.HogwartsStoryCore;
import com.donnnsleep.hogwarts.model.House;
import com.donnnsleep.hogwarts.model.StudentProfile;
import com.donnnsleep.hogwarts.model.Wand;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lưu trữ hồ sơ học sinh theo file YAML: plugins/HogwartsStoryCore/players/&lt;uuid&gt;.yml
 * Đơn giản, dễ sửa tay khi test. Có thể thay bằng SQLite sau mà không đụng tới code khác.
 */
public class ProfileStore {

    private final HogwartsStoryCore plugin;
    private final File folder;
    private final Map<UUID, StudentProfile> cache = new ConcurrentHashMap<>();

    public ProfileStore(HogwartsStoryCore plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "players");
        if (!folder.exists() && !folder.mkdirs()) {
            plugin.getLogger().warning("Không tạo được thư mục players/");
        }
    }

    public StudentProfile get(UUID uuid) {
        return cache.get(uuid);
    }

    /**
     * Lấy hồ sơ của một người chơi, tự nạp từ đĩa nếu chưa có trong bộ nhớ.
     * Dùng hàm này ở mọi nơi có sẵn đối tượng Player, để lệnh không bao giờ
     * im lặng thoát ra chỉ vì hồ sơ chưa kịp nạp (ví dụ khi nạp plugin
     * giữa chừng mà không khởi động lại server).
     */
    public StudentProfile get(org.bukkit.entity.Player player) {
        if (player == null) return null;
        StudentProfile p = cache.get(player.getUniqueId());
        if (p == null) {
            p = load(player.getUniqueId(), player.getName());
            plugin.getLogger().info("Đã nạp muộn hồ sơ của " + player.getName()
                    + " (người chơi đã online sẵn khi plugin bật).");
        }
        return p;
    }

    public StudentProfile load(UUID uuid, String name) {
        StudentProfile cached = cache.get(uuid);
        if (cached != null) {
            cached.setName(name);
            return cached;
        }

        StudentProfile p = new StudentProfile(uuid, name);
        File f = new File(folder, uuid + ".yml");
        if (f.exists()) {
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
            p.setHouse(House.fromString(cfg.getString("house")));
            p.setYear(cfg.getInt("year", 1));
            p.setMagicLevel(cfg.getInt("magic.level", 1));
            p.setMagicXp(cfg.getInt("magic.xp", 0));
            p.setMaxMana(cfg.getDouble("magic.max-mana", 100));
            p.setMana(cfg.getDouble("magic.mana", p.getMaxMana()));
            p.setWand(Wand.deserialize(cfg.getString("wand")));
            p.setActiveSpell(cfg.getString("active-spell"));
            p.setContributedPoints(cfg.getInt("contributed-points", 0));
            p.getLearnedSpells().addAll(cfg.getStringList("spells"));
            p.getStoryFlags().addAll(cfg.getStringList("story-flags"));
        }
        cache.put(uuid, p);
        return p;
    }

    public void save(StudentProfile p) {
        File f = new File(folder, p.getUuid() + ".yml");
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("name", p.getName());
        cfg.set("house", p.getHouse() == null ? null : p.getHouse().name());
        cfg.set("year", p.getYear());
        cfg.set("magic.level", p.getMagicLevel());
        cfg.set("magic.xp", p.getMagicXp());
        cfg.set("magic.mana", p.getMana());
        cfg.set("magic.max-mana", p.getMaxMana());
        cfg.set("wand", p.getWand() == null ? null : p.getWand().serialize());
        cfg.set("active-spell", p.getActiveSpell());
        cfg.set("contributed-points", p.getContributedPoints());
        cfg.set("spells", new ArrayList<>(p.getLearnedSpells()));
        cfg.set("story-flags", new ArrayList<>(p.getStoryFlags()));
        try {
            cfg.save(f);
        } catch (IOException e) {
            plugin.getLogger().severe("Không lưu được hồ sơ " + p.getName() + ": " + e.getMessage());
        }
    }

    public void unload(UUID uuid) {
        StudentProfile p = cache.remove(uuid);
        if (p != null) save(p);
    }

    public void saveAll() {
        cache.values().forEach(this::save);
    }

    public Collection<StudentProfile> online() {
        return cache.values();
    }
}
