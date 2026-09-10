package com.donnnsleep.hogwarts.magic;

import com.donnnsleep.hogwarts.HogwartsStoryCore;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

/**
 * Kho phép thuật. Đọc từ spells.yml để bạn thêm/sửa phép mà không cần build lại plugin.
 */
public class SpellRegistry {

    private final HogwartsStoryCore plugin;
    private final Map<String, Spell> spells = new LinkedHashMap<>();

    public SpellRegistry(HogwartsStoryCore plugin) {
        this.plugin = plugin;
    }

    public void load() {
        spells.clear();
        File f = new File(plugin.getDataFolder(), "spells.yml");
        if (!f.exists()) plugin.saveResource("spells.yml", false);

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
        ConfigurationSection root = cfg.getConfigurationSection("spells");
        if (root == null) {
            plugin.getLogger().warning("spells.yml không có mục 'spells'.");
            return;
        }

        for (String id : root.getKeys(false)) {
            ConfigurationSection s = root.getConfigurationSection(id);
            if (s == null) continue;
            Spell spell = new Spell(
                    id.toLowerCase(),
                    s.getString("incantation", id),
                    s.getString("vi-name", id),
                    s.getString("description", ""),
                    s.getInt("year", 1),
                    s.getString("subject", "Charms"),
                    s.getDouble("mana-cost", 10),
                    s.getLong("cooldown-ms", 1000),
                    s.getBoolean("unforgivable", false)
            );
            spells.put(spell.id(), spell);
        }
        plugin.getLogger().info("Đã nạp " + spells.size() + " câu thần chú.");
    }

    public Spell get(String id) {
        return id == null ? null : spells.get(id.toLowerCase());
    }

    /** Tìm theo câu chú người chơi gõ, ví dụ "Wingardium Leviosa". */
    public Spell byIncantation(String text) {
        if (text == null) return null;
        String norm = text.trim().replaceAll("\\s+", " ");
        for (Spell s : spells.values()) {
            if (s.incantation().equalsIgnoreCase(norm)) return s;
        }
        return null;
    }

    public Collection<Spell> all() {
        return spells.values();
    }

    public List<Spell> byYear(int year) {
        return spells.values().stream().filter(s -> s.year() == year).toList();
    }

    public List<Spell> bySubject(String subject) {
        return spells.values().stream().filter(s -> s.subject().equalsIgnoreCase(subject)).toList();
    }

    public Set<String> ids() {
        return spells.keySet();
    }
}
