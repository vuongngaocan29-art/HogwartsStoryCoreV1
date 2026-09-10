package com.donnnsleep.hogwarts.command;

import com.donnnsleep.hogwarts.HogwartsStoryCore;
import com.donnnsleep.hogwarts.model.House;
import com.donnnsleep.hogwarts.model.StudentProfile;
import com.donnnsleep.hogwarts.story.StoryManager;
import com.donnnsleep.hogwarts.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class HogwartsCommand implements CommandExecutor, TabCompleter {

    private final HogwartsStoryCore plugin;

    public HogwartsCommand(HogwartsStoryCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
                             @NotNull String label, @NotNull String[] args) {

        if (args.length == 0) {
            if (sender instanceof Player pl) showProfile(pl);
            else sender.sendMessage("Dùng: /hogwarts points|sorting|year|teach|cup|reload");
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "profile", "hoso" -> {
                if (sender instanceof Player pl) showProfile(pl);
            }

            case "points", "diem" -> showPoints(sender);

            case "sorting", "phanloai" -> {
                if (!(sender instanceof Player pl)) return true;
                if (args.length > 1 && sender.hasPermission("hogwarts.admin")) {
                    Player t = Bukkit.getPlayerExact(args[1]);
                    if (t == null) { sender.sendMessage(Msg.msg("&cKhông tìm thấy người chơi.")); return true; }
                    plugin.getSorting().begin(t);
                } else {
                    plugin.getSorting().begin(pl);
                }
            }

            case "wand", "dua" -> {
                if (!(sender instanceof Player pl)) return true;
                plugin.getStory().giveWand(pl);
            }

            case "letter", "thu" -> {
                if (!(sender instanceof Player pl)) return true;
                plugin.getStory().sendAcceptanceLetter(pl);
            }

            case "spells", "phep" -> {
                if (!(sender instanceof Player pl)) return true;
                showSpells(pl);
            }

            // ---------------- ADMIN ----------------
            case "award" -> {
                if (!sender.hasPermission("hogwarts.admin")) { noPerm(sender); return true; }
                if (args.length < 3) {
                    sender.sendMessage(Msg.msg("&7Dùng: /hogwarts award <nhà> <điểm> [lý do]"));
                    return true;
                }
                House h = House.fromString(args[1]);
                if (h == null) { sender.sendMessage(Msg.msg("&cNhà không hợp lệ.")); return true; }
                int amount;
                try { amount = Integer.parseInt(args[2]); }
                catch (NumberFormatException ex) { sender.sendMessage(Msg.msg("&cSố điểm không hợp lệ.")); return true; }
                String reason = args.length > 3
                        ? String.join(" ", Arrays.copyOfRange(args, 3, args.length))
                        : "quyết định của giáo sư";
                plugin.getPoints().award(h, amount, reason);
            }

            case "year", "nam" -> {
                if (!sender.hasPermission("hogwarts.admin")) { noPerm(sender); return true; }
                if (args.length < 3) {
                    sender.sendMessage(Msg.msg("&7Dùng: /hogwarts year <người chơi> <1-7|next>"));
                    return true;
                }
                Player t = Bukkit.getPlayerExact(args[1]);
                if (t == null) { sender.sendMessage(Msg.msg("&cKhông tìm thấy người chơi.")); return true; }
                if (args[2].equalsIgnoreCase("next")) {
                    plugin.getStory().advanceYear(t);
                } else {
                    try {
                        StudentProfile p = plugin.getProfiles().get(t);
                        p.setYear(Integer.parseInt(args[2]));
                        plugin.getProfiles().save(p);
                        sender.sendMessage(Msg.msg("&aĐã đặt " + t.getName() + " vào Năm "
                                + StoryManager.roman(p.getYear())));
                    } catch (NumberFormatException ex) {
                        sender.sendMessage(Msg.msg("&cNăm không hợp lệ."));
                    }
                }
            }

            case "teach", "day" -> {
                if (!sender.hasPermission("hogwarts.admin")) { noPerm(sender); return true; }
                if (args.length < 3) {
                    sender.sendMessage(Msg.msg("&7Dùng: /hogwarts teach <người chơi> <phép|all-year>"));
                    return true;
                }
                Player t = Bukkit.getPlayerExact(args[1]);
                if (t == null) { sender.sendMessage(Msg.msg("&cKhông tìm thấy người chơi.")); return true; }
                if (args[2].equalsIgnoreCase("all-year")) {
                    StudentProfile p = plugin.getProfiles().get(t);
                    int n = 0;
                    for (var s : plugin.getSpells().all()) {
                        if (s.year() <= p.getYear() && !s.unforgivable()
                                && plugin.getStory().teachSpell(t, s.id(), true)) n++;
                    }
                    sender.sendMessage(Msg.msg("&aĐã dạy " + n + " phép cho " + t.getName()));
                } else {
                    plugin.getStory().teachSpell(t, args[2], false);
                }
            }

            case "cup" -> {
                if (!sender.hasPermission("hogwarts.admin")) { noPerm(sender); return true; }
                plugin.getPoints().awardHouseCup();
            }

            case "sethouse" -> {
                if (!sender.hasPermission("hogwarts.admin")) { noPerm(sender); return true; }
                if (args.length < 3) {
                    sender.sendMessage(Msg.msg("&7Dùng: /hogwarts sethouse <người chơi> <nhà>"));
                    return true;
                }
                Player t = Bukkit.getPlayerExact(args[1]);
                House h = House.fromString(args[2]);
                if (t == null || h == null) { sender.sendMessage(Msg.msg("&cTham số không hợp lệ.")); return true; }
                StudentProfile p = plugin.getProfiles().get(t);
                p.setHouse(h);
                plugin.getProfiles().save(p);
                sender.sendMessage(Msg.msg("&aĐã chuyển " + t.getName() + " sang " + h.getDisplayName()));
            }

            case "resort", "phanloailai" -> {
                if (!sender.hasPermission("hogwarts.admin")) { noPerm(sender); return true; }
                Player muc = args.length > 1 ? Bukkit.getPlayerExact(args[1])
                        : (sender instanceof Player pl2 ? pl2 : null);
                if (muc == null) {
                    sender.sendMessage(Msg.msg("&cKhông tìm thấy người chơi."));
                    return true;
                }
                plugin.getSorting().resetSorting(muc);
                plugin.getSorting().begin(muc);
                sender.sendMessage(Msg.msg("&aĐã cho " + muc.getName() + " phân loại lại."));
            }

            case "debug" -> {
                if (!sender.hasPermission("hogwarts.admin")) { noPerm(sender); return true; }
                sender.sendMessage(Msg.line());
                sender.sendMessage(Msg.color("  &f&lKIỂM TRA HỆ THỐNG"));
                sender.sendMessage(Msg.color(""));
                sender.sendMessage(Msg.color("  &7Phiên bản plugin  &f"
                        + plugin.getPluginMeta().getVersion()));
                sender.sendMessage(Msg.color("  &7Phiên bản server  &f"
                        + Bukkit.getVersion()));
                sender.sendMessage(Msg.color("  &7Thần chú đã nạp   &f"
                        + plugin.getSpells().all().size()));
                sender.sendMessage(Msg.color("  &7Hồ sơ trong RAM   &f"
                        + plugin.getProfiles().online().size()));
                sender.sendMessage(Msg.color("  &7PlaceholderAPI    "
                        + (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null
                        ? "&a✔ có" : "&c✘ không")));
                sender.sendMessage(Msg.color("  &7BetonQuest        "
                        + (Bukkit.getPluginManager().getPlugin("BetonQuest") != null
                        ? "&a✔ có" : "&c✘ không")));
                sender.sendMessage(Msg.color("  &7Citizens          "
                        + (Bukkit.getPluginManager().getPlugin("Citizens") != null
                        ? "&a✔ có" : "&c✘ không")));
                sender.sendMessage(Msg.color("  &7Vault             "
                        + (Bukkit.getPluginManager().getPlugin("Vault") != null
                        ? "&a✔ có" : "&c✘ không")));
                if (sender instanceof Player pl) {
                    StudentProfile p = plugin.getProfiles().get(pl);
                    sender.sendMessage(Msg.color(""));
                    sender.sendMessage(Msg.color("  &7Hồ sơ của bạn     "
                            + (p == null ? "&c✘ KHÔNG CÓ" : "&a✔ đã nạp")));
                    if (p != null) {
                        sender.sendMessage(Msg.color("  &7Nhà               &f"
                                + (p.getHouse() == null ? "chưa phân loại" : p.getHouse().getDisplayName())));
                        sender.sendMessage(Msg.color("  &7Đũa phép          &f"
                                + (p.hasWand() ? "có" : "chưa có")));
                        sender.sendMessage(Msg.color("  &7Phép đã học       &f"
                                + p.getLearnedSpells().size()));
                    }
                }
                sender.sendMessage(Msg.line());
            }

            case "reload" -> {
                if (!sender.hasPermission("hogwarts.admin")) { noPerm(sender); return true; }
                plugin.reloadConfig();
                plugin.getSpells().load();
                plugin.getPoints().load();
                sender.sendMessage(Msg.msg("&aĐã nạp lại cấu hình và danh sách phép."));
            }

            default -> sender.sendMessage(Msg.msg("&7Lệnh không tồn tại. Gõ &f/hogwarts"));
        }
        return true;
    }

    private void noPerm(CommandSender s) {
        s.sendMessage(Msg.msg("&cCon không có quyền dùng lệnh này."));
    }

    private void showProfile(Player pl) {
        StudentProfile p = plugin.getProfiles().get(pl);
        if (p == null) {
            pl.sendMessage(Msg.msg("&cKhông đọc được hồ sơ của bạn. Hãy thoát ra vào lại server."));
            plugin.getLogger().warning("Không tạo được hồ sơ cho " + pl.getName());
            return;
        }
        String house = p.getHouse() == null ? "&8Chưa phân loại"
                : p.getHouse().getLegacyColor() + p.getHouse().getDisplayName();

        pl.sendMessage(Msg.line());
        pl.sendMessage(Msg.color("&5&l          ✦ HỒ SƠ HỌC SINH ✦"));
        pl.sendMessage(Msg.color(""));
        pl.sendMessage(Msg.color("  &7Tên          &f" + p.getName()));
        pl.sendMessage(Msg.color("  &7Nhà          " + house));
        pl.sendMessage(Msg.color("  &7Năm học      &f" + StoryManager.roman(p.getYear())));
        pl.sendMessage(Msg.color("  &7Cấp phép     &f" + p.getMagicLevel()
                + " &8(" + p.getMagicXp() + "/" + p.xpToNextLevel() + " XP)"));
        pl.sendMessage(Msg.color("  &7Ma lực       &b" + (int) p.getMana()
                + "&8/&b" + (int) p.effectiveMaxMana()));
        pl.sendMessage(Msg.color("  &7Đũa phép     &f"
                + (p.hasWand() ? p.getWand().describe() : "&8chưa có")));
        pl.sendMessage(Msg.color("  &7Phép đã học  &f" + p.getLearnedSpells().size()));
        if (p.getHouse() != null) {
            pl.sendMessage(Msg.color("  &7Đóng góp     &e" + p.getContributedPoints() + " &7điểm Nhà"));
        }
        pl.sendMessage(Msg.line());
    }

    private void showPoints(CommandSender s) {
        s.sendMessage(Msg.line());
        s.sendMessage(Msg.color("&6&l          ✦ ĐIỂM NHÀ ✦"));
        s.sendMessage(Msg.color(""));
        int rank = 1;
        for (var e : plugin.getPoints().ranking()) {
            s.sendMessage(Msg.color("  &8" + rank++ + ". " + e.getKey().getLegacyColor()
                    + e.getKey().getDisplayName() + " &8· &f" + e.getValue() + " &7điểm"));
        }
        s.sendMessage(Msg.line());
    }

    private void showSpells(Player pl) {
        StudentProfile p = plugin.getProfiles().get(pl);
        pl.sendMessage(Msg.line());
        pl.sendMessage(Msg.color("&d&l          ✦ PHÉP ĐÃ HỌC ✦"));
        pl.sendMessage(Msg.color(""));
        if (p.getLearnedSpells().isEmpty()) {
            pl.sendMessage(Msg.color("  &8Chưa học được phép nào."));
        } else {
            for (String id : p.getLearnedSpells()) {
                var s = plugin.getSpells().get(id);
                if (s == null) continue;
                String mark = id.equals(p.getActiveSpell()) ? "&a➤ " : "&8• ";
                pl.sendMessage(Msg.color("  " + mark + s.displayColor() + s.incantation()
                        + " &8— &7" + s.viName()));
            }
        }
        pl.sendMessage(Msg.color(""));
        pl.sendMessage(Msg.color("  &8Shift + chuột phải bằng đũa phép để đổi phép"));
        pl.sendMessage(Msg.line());
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> base = new ArrayList<>(List.of("profile", "points", "spells", "sorting", "wand", "letter"));
            if (sender.hasPermission("hogwarts.admin")) {
                base.addAll(List.of("award", "year", "teach", "cup", "sethouse", "resort", "debug", "reload"));
            }
            return base.stream().filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("award")) {
                return Arrays.stream(House.values()).map(Enum::name).map(String::toLowerCase).toList();
            }
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("teach")) {
            List<String> ids = new ArrayList<>(plugin.getSpells().ids());
            ids.add("all-year");
            return ids.stream().filter(s -> s.startsWith(args[2].toLowerCase())).toList();
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("sethouse")) {
            return Arrays.stream(House.values()).map(Enum::name).map(String::toLowerCase).toList();
        }
        return List.of();
    }
}
