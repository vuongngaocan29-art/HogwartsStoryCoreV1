package com.donnnsleep.hogwarts;

import com.donnnsleep.hogwarts.command.CommonRoomCommand;
import com.donnnsleep.hogwarts.command.HelpCommand;
import com.donnnsleep.hogwarts.command.HogwartsCommand;
import com.donnnsleep.hogwarts.command.SpellCommand;
import com.donnnsleep.hogwarts.data.ProfileStore;
import com.donnnsleep.hogwarts.hook.HogwartsPlaceholders;
import com.donnnsleep.hogwarts.house.CommonRoomListener;
import com.donnnsleep.hogwarts.house.CommonRoomManager;
import com.donnnsleep.hogwarts.house.HousePointsManager;
import com.donnnsleep.hogwarts.magic.SpellManager;
import com.donnnsleep.hogwarts.magic.SpellRegistry;
import com.donnnsleep.hogwarts.magic.WandListener;
import com.donnnsleep.hogwarts.model.StudentProfile;
import com.donnnsleep.hogwarts.sorting.SortingCeremony;
import com.donnnsleep.hogwarts.story.StoryManager;
import com.donnnsleep.hogwarts.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HogwartsStoryCore extends JavaPlugin {

    private ProfileStore profiles;
    private SpellRegistry spells;
    private SpellManager spellManager;
    private HousePointsManager points;
    private CommonRoomManager commonRooms;
    private SortingCeremony sorting;
    private StoryManager story;

    private NamespacedKey wandKey;
    private final Map<UUID, Long> silenced = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("spells.yml", false);
        saveResource("commonrooms.yml", false);

        this.wandKey = new NamespacedKey(this, "wand");

        this.profiles = new ProfileStore(this);
        this.spells = new SpellRegistry(this);
        this.spells.load();
        this.spellManager = new SpellManager(this);
        this.points = new HousePointsManager(this);
        this.commonRooms = new CommonRoomManager(this);
        this.sorting = new SortingCeremony(this);
        this.story = new StoryManager(this);

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new WandListener(this), this);
        getServer().getPluginManager().registerEvents(sorting, this);
        getServer().getPluginManager().registerEvents(new CommonRoomListener(this), this);

        HogwartsCommand hwCmd = new HogwartsCommand(this);
        getCommand("hogwarts").setExecutor(hwCmd);
        getCommand("hogwarts").setTabCompleter(hwCmd);
        SpellCommand spellCmd = new SpellCommand(this);
        getCommand("spell").setExecutor(spellCmd);
        getCommand("spell").setTabCompleter(spellCmd);
        HelpCommand helpCmd = new HelpCommand(this);
        getCommand("trogiup").setExecutor(helpCmd);
        getCommand("trogiup").setTabCompleter(helpCmd);
        CommonRoomCommand crCmd = new CommonRoomCommand(this);
        getCommand("commonroom").setExecutor(crCmd);
        getCommand("commonroom").setTabCompleter(crCmd);

        // nạp hồ sơ cho người đang online (khi /reload)
        Bukkit.getOnlinePlayers().forEach(pl -> profiles.load(pl.getUniqueId(), pl.getName()));

        startManaTask();
        startAutoSave();
        startCommonRoomTasks();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new HogwartsPlaceholders(this).register();
            getLogger().info("Đã móc nối PlaceholderAPI.");
        }

        getLogger().info("HogwartsStoryCore đã bật. Chào mừng tới Hogwarts.");
    }

    @Override
    public void onDisable() {
        if (profiles != null) profiles.saveAll();
        if (points != null) points.save();
        if (commonRooms != null) commonRooms.save();
    }

    /** Đổi mật khẩu định kỳ và đuổi người Nhà khác ra khỏi phòng. */
    private void startCommonRoomTasks() {
        // kiểm tra hạn mật khẩu mỗi 30 phút
        Bukkit.getScheduler().runTaskTimer(this, () -> commonRooms.checkRotation(),
                600L, 20L * 60 * 30);
        // quét vùng phòng mỗi 2 giây
        Bukkit.getScheduler().runTaskTimer(this, () -> commonRooms.enforceRegions(), 100L, 40L);
    }

    /** Hồi ma lực định kỳ, hiển thị trên action bar. */
    private void startManaTask() {
        long period = getConfig().getLong("magic.mana-regen-ticks", 40L);
        double amount = getConfig().getDouble("magic.mana-regen-amount", 3.0);
        boolean actionBar = getConfig().getBoolean("magic.show-mana-actionbar", true);

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player pl : Bukkit.getOnlinePlayers()) {
                StudentProfile p = profiles.get(pl.getUniqueId());
                if (p == null) continue;

                double max = p.effectiveMaxMana();
                if (p.getMana() < max) {
                    p.setMaxMana(p.getMaxMana());
                    p.setMana(Math.min(max, p.getMana() + amount));
                }

                if (actionBar && p.hasWand() && pl.getInventory().getItemInMainHand().getType()
                        == org.bukkit.Material.BLAZE_ROD) {
                    var s = spells.get(p.getActiveSpell());
                    String bar = manaBar(p.getMana(), max);
                    pl.sendActionBar(Msg.color("&9Ma lực " + bar + " &b" + (int) p.getMana()
                            + "&8/&b" + (int) max
                            + (s == null ? "" : "   &8| " + s.displayColor() + s.incantation())));
                }
            }
        }, 40L, period);
    }

    private String manaBar(double cur, double max) {
        int filled = (int) Math.round(10.0 * cur / Math.max(1, max));
        StringBuilder sb = new StringBuilder("&8[");
        for (int i = 0; i < 10; i++) sb.append(i < filled ? "&b|" : "&8|");
        sb.append("&8]");
        return sb.toString();
    }

    private void startAutoSave() {
        long minutes = getConfig().getLong("storage.autosave-minutes", 5);
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            profiles.saveAll();
            points.save();
        }, minutes * 1200L, minutes * 1200L);
    }

    // ---- getters ----
    public ProfileStore getProfiles() { return profiles; }
    public SpellRegistry getSpells() { return spells; }
    public SpellManager getSpellManager() { return spellManager; }
    public HousePointsManager getPoints() { return points; }
    public CommonRoomManager getCommonRooms() { return commonRooms; }
    public SortingCeremony getSorting() { return sorting; }
    public StoryManager getStory() { return story; }
    public NamespacedKey getWandKey() { return wandKey; }
    public Map<UUID, Long> getSilenced() { return silenced; }
}
