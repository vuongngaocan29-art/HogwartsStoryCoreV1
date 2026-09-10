package com.donnnsleep.hogwarts.command;

import com.donnnsleep.hogwarts.HogwartsStoryCore;
import com.donnnsleep.hogwarts.model.CommonRoom;
import com.donnnsleep.hogwarts.model.House;
import com.donnnsleep.hogwarts.model.StudentProfile;
import com.donnnsleep.hogwarts.util.Msg;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CommonRoomCommand implements CommandExecutor, TabCompleter {

    private final HogwartsStoryCore plugin;
    /** Vị trí góc thứ nhất khi admin đang chọn vùng. */
    private final Map<UUID, Location> corner = new HashMap<>();

    public CommonRoomCommand(HogwartsStoryCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
                             @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Lệnh này chỉ dùng được trong game.");
            return true;
        }
        StudentProfile p = plugin.getProfiles().get(player);
        if (p == null) {
            player.sendMessage(Msg.msg("&cKhông đọc được hồ sơ của bạn. Hãy thoát ra vào lại server."));
            return true;
        }

        if (args.length == 0) {
            showInfo(player, p);
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "password", "matkhau" -> {
                if (p.getHouse() == null) {
                    player.sendMessage(Msg.msg("&7Con chưa được Phân Loại."));
                    return true;
                }
                CommonRoom room = plugin.getCommonRooms().get(p.getHouse());
                if (room.getEntryType() == CommonRoom.EntryType.RIDDLE) {
                    player.sendMessage(Msg.msg("&7Nhà Ravenclaw không dùng mật khẩu — con đại bàng hỏi câu đố."));
                } else if (room.getEntryType() == CommonRoom.EntryType.KNOCK) {
                    player.sendMessage(Msg.msg("&7Gõ vào thùng gỗ &f" + room.getKnockCount()
                            + " &7nhịp, theo vần &fHel-ga Huf-fle-puff&7."));
                } else {
                    player.sendMessage(Msg.msg("&7Mật khẩu tuần này: &f" + room.getPassword()));
                    player.sendMessage(Msg.color("&8Đừng viết nó ra giấy rồi làm rơi ngoài hành lang."));
                }
            }

            // ---------------- ADMIN ----------------
            case "setentrance" -> {
                if (!admin(player)) return true;
                House h = house(args, 1, player);
                if (h == null) return true;
                var block = player.getTargetBlockExact(6);
                if (block == null) {
                    player.sendMessage(Msg.msg("&cHãy nhìn vào khối làm cửa vào (cách tối đa 6 khối)."));
                    return true;
                }
                plugin.getCommonRooms().get(h).setEntrance(block.getLocation());
                plugin.getCommonRooms().save();
                player.sendMessage(Msg.msg("&aĐã đặt cửa vào cho " + h.getDisplayName()));
            }

            case "setspawn" -> {
                if (!admin(player)) return true;
                House h = house(args, 1, player);
                if (h == null) return true;
                plugin.getCommonRooms().get(h).setSpawn(player.getLocation());
                plugin.getCommonRooms().save();
                player.sendMessage(Msg.msg("&aĐã đặt điểm vào phòng cho " + h.getDisplayName()));
            }

            case "pos1" -> {
                if (!admin(player)) return true;
                corner.put(player.getUniqueId(), player.getLocation());
                player.sendMessage(Msg.msg("&aĐã ghi góc thứ nhất. Đi tới góc đối diện rồi dùng &f/cr pos2 <nhà>"));
            }

            case "pos2" -> {
                if (!admin(player)) return true;
                House h = house(args, 1, player);
                if (h == null) return true;
                Location a = corner.get(player.getUniqueId());
                if (a == null) {
                    player.sendMessage(Msg.msg("&cChưa có góc thứ nhất. Dùng &f/cr pos1 &ctrước."));
                    return true;
                }
                plugin.getCommonRooms().get(h).setRegion(a, player.getLocation());
                plugin.getCommonRooms().save();
                corner.remove(player.getUniqueId());
                player.sendMessage(Msg.msg("&aĐã đặt vùng phòng cho " + h.getDisplayName()
                        + ". Người Nhà khác vào sẽ bị đẩy ra."));
            }

            case "newpassword" -> {
                if (!admin(player)) return true;
                House h = house(args, 1, player);
                if (h == null) return true;
                plugin.getCommonRooms().rotatePassword(plugin.getCommonRooms().get(h), true);
                player.sendMessage(Msg.msg("&aĐã đổi mật khẩu cho " + h.getDisplayName()));
            }

            case "tp" -> {
                if (!admin(player)) return true;
                House h = house(args, 1, player);
                if (h == null) return true;
                CommonRoom room = plugin.getCommonRooms().get(h);
                if (room.getSpawn() == null) {
                    player.sendMessage(Msg.msg("&cPhòng này chưa đặt điểm vào."));
                    return true;
                }
                player.teleport(room.getSpawn());
            }

            case "status" -> {
                if (!admin(player)) return true;
                player.sendMessage(Msg.line());
                for (CommonRoom r : plugin.getCommonRooms().all()) {
                    player.sendMessage(Msg.color("  " + r.getHouse().getLegacyColor()
                            + r.getHouse().getDisplayName() + " &8· &7"
                            + r.getEntryType().name().toLowerCase()
                            + " &8| cửa " + (r.getEntrance() != null ? "&a✔" : "&c✘")
                            + " &8| điểm vào " + (r.getSpawn() != null ? "&a✔" : "&c✘")
                            + " &8| vùng " + (r.hasRegion() ? "&a✔" : "&c✘")));
                }
                player.sendMessage(Msg.line());
            }

            case "reload" -> {
                if (!admin(player)) return true;
                plugin.getCommonRooms().load();
                player.sendMessage(Msg.msg("&aĐã nạp lại commonrooms.yml"));
            }

            default -> showInfo(player, p);
        }
        return true;
    }

    private void showInfo(Player player, StudentProfile p) {
        if (p.getHouse() == null) {
            player.sendMessage(Msg.msg("&7Con chưa được Phân Loại nên chưa có Phòng Sinh hoạt chung."));
            return;
        }
        CommonRoom room = plugin.getCommonRooms().get(p.getHouse());
        player.sendMessage(Msg.line());
        player.sendMessage(Msg.color("  " + p.getHouse().getLegacyColor() + "&lPHÒNG SINH HOẠT CHUNG"));
        player.sendMessage(Msg.color(""));
        player.sendMessage(Msg.color("  &7Người gác cửa   " + room.getGuardianName()));
        player.sendMessage(Msg.color("  &7Cách vào        &f" + switch (room.getEntryType()) {
            case PASSWORD -> "Nói mật khẩu với chân dung";
            case PASSWORD_WALL -> "Nói mật khẩu trước bức tường đá";
            case RIDDLE -> "Trả lời câu đố của con đại bàng";
            case KNOCK -> "Gõ thùng gỗ " + room.getKnockCount() + " nhịp";
        }));
        if (room.getEntryType() != CommonRoom.EntryType.RIDDLE
                && room.getEntryType() != CommonRoom.EntryType.KNOCK) {
            player.sendMessage(Msg.color("  &8Dùng &f/cr password &8để xem mật khẩu tuần này"));
        }
        player.sendMessage(Msg.line());
    }

    private boolean admin(Player p) {
        if (p.hasPermission("hogwarts.admin")) return true;
        p.sendMessage(Msg.msg("&cCon không có quyền dùng lệnh này."));
        return false;
    }

    private House house(String[] args, int idx, Player p) {
        if (args.length <= idx) {
            p.sendMessage(Msg.msg("&cThiếu tên Nhà. Ví dụ: &fgryffindor"));
            return null;
        }
        House h = House.fromString(args[idx]);
        if (h == null) p.sendMessage(Msg.msg("&cNhà không hợp lệ."));
        return h;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> base = new ArrayList<>(List.of("password"));
            if (sender.hasPermission("hogwarts.admin")) {
                base.addAll(List.of("setentrance", "setspawn", "pos1", "pos2",
                        "newpassword", "tp", "status", "reload"));
            }
            return base.stream().filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        if (args.length == 2) {
            return Arrays.stream(House.values()).map(Enum::name).map(String::toLowerCase)
                    .filter(s -> s.startsWith(args[1].toLowerCase())).toList();
        }
        return List.of();
    }
}
