package com.donnnsleep.hogwarts.house;

import com.donnnsleep.hogwarts.HogwartsStoryCore;
import com.donnnsleep.hogwarts.model.CommonRoom;
import com.donnnsleep.hogwarts.model.House;
import com.donnnsleep.hogwarts.model.StudentProfile;
import com.donnnsleep.hogwarts.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Quản lý bốn Phòng Sinh hoạt chung: cấu hình, mật khẩu, câu đố, và việc cho vào.
 */
public class CommonRoomManager {

    private final HogwartsStoryCore plugin;
    private final File file;
    private final EnumMap<House, CommonRoom> rooms = new EnumMap<>(House.class);

    /** Người chơi đang chờ nhập mật khẩu / trả lời câu đố qua khung chat. */
    private final Map<UUID, PendingEntry> pending = new ConcurrentHashMap<>();

    /** Đếm số lần gõ thùng của học sinh Hufflepuff. */
    private final Map<UUID, KnockState> knocks = new ConcurrentHashMap<>();

    private List<String> passwordWords = new ArrayList<>();
    private int rotateDays = 7;

    public record PendingEntry(House house, CommonRoom.Riddle riddle, long expiresAt, int attempts) {}
    public record KnockState(int count, long lastKnock) {}

    public CommonRoomManager(HogwartsStoryCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "commonrooms.yml");
        load();
    }

    // ------------------------------------------------------------
    //  NẠP / LƯU
    // ------------------------------------------------------------
    public void load() {
        if (!file.exists()) plugin.saveResource("commonrooms.yml", false);
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        rotateDays = cfg.getInt("password-rotate-days", 7);
        passwordWords = cfg.getStringList("password-words");
        if (passwordWords.isEmpty()) {
            passwordWords = List.of("Caput Draconis", "Fortuna Major", "Balderdash",
                    "Pig Snout", "Wattlebird", "Scurvy Cur", "Oddsbodikins");
        }

        rooms.clear();
        for (House h : House.values()) {
            ConfigurationSection s = cfg.getConfigurationSection("rooms." + h.name());
            CommonRoom.EntryType type = defaultType(h);
            if (s != null) {
                try {
                    type = CommonRoom.EntryType.valueOf(
                            s.getString("entry-type", type.name()).toUpperCase());
                } catch (IllegalArgumentException ignored) {}
            }

            CommonRoom room = new CommonRoom(h, type);
            if (s != null) {
                room.setEntrance(readLoc(s, "entrance"));
                room.setSpawn(readLoc(s, "spawn"));
                room.setRadius(s.getDouble("radius", 3.0));
                room.setPassword(s.getString("password"));
                room.setPasswordSetAt(s.getLong("password-set-at", 0));
                room.setKnockCount(s.getInt("knock-count", 7));
                room.setRegion(readLoc(s, "region.a"), readLoc(s, "region.b"));

                for (Map<?, ?> raw : s.getMapList("riddles")) {
                    Object q = raw.get("question");
                    Object a = raw.get("answers");
                    if (q instanceof String question && a instanceof List<?> list) {
                        List<String> answers = list.stream().map(String::valueOf)
                                .map(String::toLowerCase).toList();
                        room.getRiddles().add(new CommonRoom.Riddle(question, answers));
                    }
                }
            }
            if (room.getPassword() == null) rotatePassword(room, false);
            rooms.put(h, room);
        }
        save();
    }

    private CommonRoom.EntryType defaultType(House h) {
        return switch (h) {
            case GRYFFINDOR -> CommonRoom.EntryType.PASSWORD;
            case SLYTHERIN  -> CommonRoom.EntryType.PASSWORD_WALL;
            case RAVENCLAW  -> CommonRoom.EntryType.RIDDLE;
            case HUFFLEPUFF -> CommonRoom.EntryType.KNOCK;
        };
    }

    public void save() {
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        cfg.set("password-rotate-days", rotateDays);
        cfg.set("password-words", passwordWords);

        for (CommonRoom room : rooms.values()) {
            String base = "rooms." + room.getHouse().name();
            cfg.set(base + ".entry-type", room.getEntryType().name());
            writeLoc(cfg, base + ".entrance", room.getEntrance());
            writeLoc(cfg, base + ".spawn", room.getSpawn());
            cfg.set(base + ".radius", room.getRadius());
            cfg.set(base + ".password", room.getPassword());
            cfg.set(base + ".password-set-at", room.getPasswordSetAt());
            cfg.set(base + ".knock-count", room.getKnockCount());
            writeLoc(cfg, base + ".region.a", room.getRegionMin());
            writeLoc(cfg, base + ".region.b", room.getRegionMax());

            List<Map<String, Object>> riddles = new ArrayList<>();
            for (CommonRoom.Riddle r : room.getRiddles()) {
                riddles.add(Map.of("question", r.question(), "answers", r.answers()));
            }
            if (!riddles.isEmpty()) cfg.set(base + ".riddles", riddles);
        }
        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Không lưu được commonrooms.yml: " + e.getMessage());
        }
    }

    private Location readLoc(ConfigurationSection s, String path) {
        if (!s.isConfigurationSection(path)) return null;
        var w = Bukkit.getWorld(s.getString(path + ".world", "world"));
        if (w == null) return null;
        return new Location(w, s.getDouble(path + ".x"), s.getDouble(path + ".y"),
                s.getDouble(path + ".z"), (float) s.getDouble(path + ".yaw"),
                (float) s.getDouble(path + ".pitch"));
    }

    private void writeLoc(YamlConfiguration cfg, String path, Location loc) {
        if (loc == null || loc.getWorld() == null) { cfg.set(path, null); return; }
        cfg.set(path + ".world", loc.getWorld().getName());
        cfg.set(path + ".x", loc.getX());
        cfg.set(path + ".y", loc.getY());
        cfg.set(path + ".z", loc.getZ());
        cfg.set(path + ".yaw", loc.getYaw());
        cfg.set(path + ".pitch", loc.getPitch());
    }

    // ------------------------------------------------------------
    //  MẬT KHẨU
    // ------------------------------------------------------------
    public void rotatePassword(CommonRoom room, boolean announce) {
        String old = room.getPassword();
        String next = old;
        Random rng = new Random();
        int guard = 0;
        while (Objects.equals(next, old) && guard++ < 20) {
            next = passwordWords.get(rng.nextInt(passwordWords.size()));
        }
        room.setPassword(next);
        save();

        if (announce) {
            for (Player pl : Bukkit.getOnlinePlayers()) {
                StudentProfile p = plugin.getProfiles().get(pl);
                if (p != null && p.getHouse() == room.getHouse()) {
                    pl.sendMessage(Msg.msg("&7Mật khẩu Phòng Sinh hoạt chung tuần này: &f" + next));
                }
            }
        }
    }

    /** Chạy mỗi giờ, đổi mật khẩu khi tới hạn. */
    public void checkRotation() {
        long ttl = TimeUnit.DAYS.toMillis(rotateDays);
        for (CommonRoom room : rooms.values()) {
            if (room.getEntryType() == CommonRoom.EntryType.RIDDLE) continue;
            if (System.currentTimeMillis() - room.getPasswordSetAt() >= ttl) {
                rotatePassword(room, true);
            }
        }
    }

    // ------------------------------------------------------------
    //  VÀO PHÒNG
    // ------------------------------------------------------------
    public CommonRoom get(House h) { return rooms.get(h); }
    public Collection<CommonRoom> all() { return rooms.values(); }

    /** Tìm cửa vào gần vị trí này, nếu có. */
    public CommonRoom entranceAt(Location loc) {
        for (CommonRoom room : rooms.values()) {
            Location e = room.getEntrance();
            if (e == null || e.getWorld() == null || loc.getWorld() == null) continue;
            if (!e.getWorld().equals(loc.getWorld())) continue;
            if (e.distanceSquared(loc) <= room.getRadius() * room.getRadius()) return room;
        }
        return null;
    }

    /** Người chơi nhấp chuột phải vào cửa vào. */
    public void attemptEntry(Player player, CommonRoom room) {
        StudentProfile p = plugin.getProfiles().get(player);
        if (p == null) return;

        boolean sameHouse = p.getHouse() == room.getHouse();
        boolean bypass = player.hasPermission("hogwarts.commonroom.bypass");

        // Ravenclaw là ngoại lệ trong nguyên tác: ai trả lời đúng câu đố cũng vào được
        boolean ravenclawGuest = room.getEntryType() == CommonRoom.EntryType.RIDDLE;

        if (!sameHouse && !bypass && !ravenclawGuest) {
            player.sendMessage(Msg.color(room.getGuardianName()
                    + ": &7&oĐây không phải Nhà của con. Đi đi."));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.6f);
            return;
        }

        if (!room.isReady()) {
            player.sendMessage(Msg.msg("&cPhòng này chưa được thiết lập xong."));
            return;
        }

        if (bypass && !sameHouse) {
            enter(player, room);
            return;
        }

        switch (room.getEntryType()) {
            case PASSWORD, PASSWORD_WALL -> {
                pending.put(player.getUniqueId(),
                        new PendingEntry(room.getHouse(), null, System.currentTimeMillis() + 30000, 0));
                player.sendMessage(Msg.color(""));
                player.sendMessage(Msg.color(room.getGuardianName() + ": &7&o"
                        + (room.getEntryType() == CommonRoom.EntryType.PASSWORD
                        ? "Mật khẩu?"
                        : "...")));
                player.sendMessage(Msg.color("&8Gõ mật khẩu vào khung chat &7(30 giây)"));
                player.sendMessage(Msg.color(""));
            }
            case RIDDLE -> {
                if (room.getRiddles().isEmpty()) { enter(player, room); return; }
                CommonRoom.Riddle r = room.getRiddles()
                        .get(new Random().nextInt(room.getRiddles().size()));
                pending.put(player.getUniqueId(),
                        new PendingEntry(room.getHouse(), r, System.currentTimeMillis() + 60000, 0));
                player.sendMessage(Msg.color(""));
                player.sendMessage(Msg.color(room.getGuardianName() + " &7hỏi:"));
                player.sendMessage(Msg.color("  &f&o" + r.question()));
                player.sendMessage(Msg.color("&8Trả lời vào khung chat &7(60 giây)"));
                player.sendMessage(Msg.color(""));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 1.2f);
            }
            case KNOCK -> handleKnock(player, room);
        }
    }

    /** Hufflepuff: gõ thùng theo nhịp "Hel-ga Huf-fle-puff" — 7 nhịp. */
    private void handleKnock(Player player, CommonRoom room) {
        long now = System.currentTimeMillis();
        KnockState st = knocks.get(player.getUniqueId());

        // quá 2 giây không gõ tiếp thì tính là bắt đầu lại
        int count = (st != null && now - st.lastKnock() <= 2000) ? st.count() + 1 : 1;
        knocks.put(player.getUniqueId(), new KnockState(count, now));

        player.playSound(player.getLocation(), Sound.BLOCK_BARREL_OPEN, 1f, 1.3f);
        player.sendActionBar(Msg.color("&eGõ &f" + count + "&e/&f" + room.getKnockCount()));

        if (count == room.getKnockCount()) {
            knocks.remove(player.getUniqueId());
            player.sendMessage(Msg.color("&eDãy thùng lăn sang bên, để lộ một lối đi thấp."));
            enter(player, room);
        } else if (count > room.getKnockCount()) {
            knocks.remove(player.getUniqueId());
            failKnock(player);
        }
    }

    private void failKnock(Player player) {
        player.sendMessage(Msg.color("&2Một vòi giấm phun thẳng vào mặt bạn."));
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_SPLASH, 1f, 1.4f);
        player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.BLINDNESS, 60, 0));
        player.getWorld().spawnParticle(org.bukkit.Particle.SPLASH,
                player.getEyeLocation(), 40, 0.3, 0.3, 0.3, 0.1);
    }

    /** Xử lý câu trả lời gõ trong chat. Trả về true nếu đã tiêu thụ tin nhắn. */
    public boolean handleChatAnswer(Player player, String message) {
        PendingEntry pe = pending.get(player.getUniqueId());
        if (pe == null) return false;

        if (System.currentTimeMillis() > pe.expiresAt()) {
            pending.remove(player.getUniqueId());
            player.sendMessage(Msg.msg("&7Hết giờ rồi. Thử lại đi."));
            return true;
        }

        CommonRoom room = rooms.get(pe.house());
        String answer = message.trim();
        boolean correct;

        if (pe.riddle() != null) {
            String norm = answer.toLowerCase();
            correct = pe.riddle().answers().stream().anyMatch(norm::contains);
        } else {
            correct = room.getPassword() != null && room.getPassword().equalsIgnoreCase(answer);
        }

        if (correct) {
            pending.remove(player.getUniqueId());
            if (pe.riddle() != null) {
                player.sendMessage(Msg.color("&9&oCâu trả lời chu đáo. Mời vào."));
            } else {
                player.sendMessage(Msg.color(room.getGuardianName() + ": &7&oĐúng rồi. Mời vào."));
            }
            // handleChatAnswer chạy trên luồng async, nên mọi thao tác thế giới
            // phải đẩy về luồng chính.
            Bukkit.getScheduler().runTask(plugin, () -> enter(player, room));
        } else {
            int attempts = pe.attempts() + 1;
            if (attempts >= 3) {
                pending.remove(player.getUniqueId());
                player.sendMessage(Msg.color(room.getGuardianName()
                        + ": &7&oSai ba lần rồi. Ta không mở cửa nữa đâu."));
                Bukkit.getScheduler().runTask(plugin, () -> player.playSound(
                        player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f));
            } else {
                pending.put(player.getUniqueId(),
                        new PendingEntry(pe.house(), pe.riddle(), pe.expiresAt(), attempts));
                player.sendMessage(Msg.color(room.getGuardianName()
                        + ": &7&oKhông đúng. &8(còn " + (3 - attempts) + " lần thử)"));
            }
        }
        return true;
    }

    public void enter(Player player, CommonRoom room) {
        player.teleport(room.getSpawn());
        player.playSound(room.getSpawn(), Sound.BLOCK_WOODEN_DOOR_OPEN, 1f, 1f);
        player.sendMessage(Msg.color("&7Bạn bước vào Phòng Sinh hoạt chung của Nhà "
                + room.getHouse().getLegacyColor() + room.getHouse().getDisplayName()));
    }

    public void clear(UUID uuid) {
        pending.remove(uuid);
        knocks.remove(uuid);
    }

    /** Chạy định kỳ: đuổi người Nhà khác đang ở trong phòng ra ngoài. */
    public void enforceRegions() {
        for (Player pl : Bukkit.getOnlinePlayers()) {
            if (pl.hasPermission("hogwarts.commonroom.bypass")) continue;
            StudentProfile p = plugin.getProfiles().get(pl);
            if (p == null) continue;

            for (CommonRoom room : rooms.values()) {
                if (!room.hasRegion() || room.getHouse() == p.getHouse()) continue;
                if (room.contains(pl.getLocation())) {
                    Location out = room.getEntrance() != null
                            ? room.getEntrance().clone().add(0, 1, 0)
                            : pl.getWorld().getSpawnLocation();
                    pl.teleport(out);
                    pl.sendMessage(Msg.msg("&7Một sức mạnh vô hình đẩy bạn ra khỏi phòng của Nhà khác."));
                    pl.playSound(pl.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.8f);
                    break;
                }
            }
        }
    }
}
