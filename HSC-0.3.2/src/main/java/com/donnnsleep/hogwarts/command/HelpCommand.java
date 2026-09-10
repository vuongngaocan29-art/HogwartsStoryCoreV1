package com.donnnsleep.hogwarts.command;

import com.donnnsleep.hogwarts.HogwartsStoryCore;
import com.donnnsleep.hogwarts.model.StudentProfile;
import com.donnnsleep.hogwarts.story.StoryManager;
import com.donnnsleep.hogwarts.util.Msg;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Lệnh trợ giúp dành cho học sinh mới.
 * Menu có thể bấm trực tiếp trong chat, không cần gõ lại lệnh.
 */
public class HelpCommand implements CommandExecutor, TabCompleter {

    private final HogwartsStoryCore plugin;

    public HelpCommand(HogwartsStoryCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Lệnh này chỉ dùng được trong game.");
            return true;
        }

        String topic = args.length > 0 ? args[0].toLowerCase() : "menu";

        switch (topic) {
            case "batdau", "bắtđầu" -> batDau(player);
            case "dua", "đũa" -> dua(player);
            case "phep", "phép" -> phep(player);
            case "nha", "nhà" -> nha(player);
            case "phong", "phòng" -> phong(player);
            case "diem", "điểm" -> diem(player);
            case "quest", "nhiemvu" -> quest(player);
            case "lenh", "lệnh" -> lenh(player);
            default -> menu(player);
        }
        return true;
    }

    // ------------------------------------------------------------
    //  MENU CHÍNH
    // ------------------------------------------------------------
    private void menu(Player player) {
        StudentProfile p = plugin.getProfiles().get(player);

        player.sendMessage(Msg.line());
        player.sendMessage(Msg.color("&5&l          ✦ SỔ TAY HỌC SINH ✦"));
        player.sendMessage(Msg.color(""));

        // Gợi ý việc cần làm tiếp theo, tuỳ tiến độ
        if (p != null) {
            if (!p.hasWand()) {
                player.sendMessage(Msg.color("  &e➤ Việc cần làm: &fđi mua đũa phép ở tiệm Ollivander"));
            } else if (!p.isSorted()) {
                player.sendMessage(Msg.color("  &e➤ Việc cần làm: &ftới Đại Sảnh Đường dự Lễ Phân Loại"));
            } else {
                player.sendMessage(Msg.color("  &7Học sinh Nhà " + p.getHouse().getLegacyColor()
                        + p.getHouse().getDisplayName() + " &8· &fNăm " + StoryManager.roman(p.getYear())));
            }
            player.sendMessage(Msg.color(""));
        }

        player.sendMessage(muc("batdau", "Bắt đầu chơi", "Ba bước đầu tiên khi mới vào server"));
        player.sendMessage(muc("dua", "Đũa phép", "Cách nhận và sử dụng đũa"));
        player.sendMessage(muc("phep", "Thần chú", "Cách học và niệm phép"));
        player.sendMessage(muc("nha", "Bốn Nhà", "Lễ Phân Loại và ý nghĩa từng Nhà"));
        player.sendMessage(muc("phong", "Phòng Sinh hoạt chung", "Cách vào phòng của Nhà mình"));
        player.sendMessage(muc("diem", "Điểm Nhà", "Cách kiếm điểm và Cúp Nhà"));
        player.sendMessage(muc("quest", "Nhiệm vụ", "Lớp học và cốt truyện"));
        player.sendMessage(muc("lenh", "Danh sách lệnh", "Toàn bộ lệnh có thể dùng"));

        player.sendMessage(Msg.color(""));
        player.sendMessage(Msg.color("  &8Bấm vào dòng bất kỳ để xem chi tiết"));
        player.sendMessage(Msg.line());
    }

    /** Một dòng menu bấm được. */
    private Component muc(String topic, String ten, String moTa) {
        return Msg.color("  &8▸ &b" + ten)
                .clickEvent(ClickEvent.runCommand("/trogiup " + topic))
                .hoverEvent(HoverEvent.showText(Msg.color("&7" + moTa + "\n&8Bấm để mở")));
    }

    // ------------------------------------------------------------
    //  CÁC MỤC
    // ------------------------------------------------------------
    private void batDau(Player player) {
        header(player, "&e", "BẮT ĐẦU CHƠI");
        player.sendMessage(Msg.color("  &fBa bước đầu tiên:"));
        player.sendMessage(Msg.color(""));
        player.sendMessage(Msg.color("  &e1. &7Đọc &fthư nhập học &7trong túi đồ"));
        player.sendMessage(Msg.color("     &8Con cú thả nó xuống khi bạn vào server lần đầu."));
        player.sendMessage(Msg.color("     &8Nếu mất thư, gõ &f/hogwarts letter"));
        player.sendMessage(Msg.color(""));
        player.sendMessage(Msg.color("  &e2. &7Tới &fTiệm Ollivander &7ở Hẻm Xéo mua đũa phép"));
        player.sendMessage(Msg.color("     &8Nói chuyện với ông Ollivander, hoặc gõ &f/hogwarts wand"));
        player.sendMessage(Msg.color(""));
        player.sendMessage(Msg.color("  &e3. &7Tới &fĐại Sảnh Đường &7dự Lễ Phân Loại"));
        player.sendMessage(Msg.color("     &8Nón Phân Loại hỏi 5 câu. Trả lời thật lòng."));
        player.sendMessage(Msg.color("     &8Nhà của bạn sẽ theo bạn suốt 7 năm."));
        player.sendMessage(Msg.color(""));
        player.sendMessage(Msg.color("  &7Xong ba bước là bạn chính thức thành học sinh Hogwarts."));
        quayLai(player);
    }

    private void dua(Player player) {
        StudentProfile p = plugin.getProfiles().get(player);
        header(player, "&d", "ĐŨA PHÉP");

        if (p != null && p.hasWand()) {
            player.sendMessage(Msg.color("  &7Đũa của bạn: &f" + p.getWand().describe()));
            player.sendMessage(Msg.color(""));
        }

        player.sendMessage(Msg.color("  &f&lCÁCH DÙNG"));
        player.sendMessage(Msg.color("  &7Cầm đũa (que màu vàng) trên tay:"));
        player.sendMessage(Msg.color("    &b• Chuột phải &8— niệm phép đang chọn"));
        player.sendMessage(Msg.color("    &b• Shift + chuột phải &8— đổi sang phép khác"));
        player.sendMessage(Msg.color(""));
        player.sendMessage(Msg.color("  &f&lLÕI ĐŨA"));
        player.sendMessage(Msg.color("  &7Mỗi đũa có một lõi, ảnh hưởng tới chỉ số của bạn:"));
        player.sendMessage(Msg.color("    &6• Lông đuôi Phượng hoàng &8— nhiều ma lực, hồi chiêu nhanh"));
        player.sendMessage(Msg.color("    &c• Sợi tim Rồng &8— phép mạnh nhất, nhưng hồi chiêu chậm"));
        player.sendMessage(Msg.color("    &f• Lông Kỳ lân &8— hồi chiêu rất nhanh, phép yếu hơn"));
        player.sendMessage(Msg.color(""));
        player.sendMessage(Msg.color("  &8Đũa phép chọn phù thuỷ, không phải ngược lại."));
        player.sendMessage(Msg.color("  &8Bạn không đổi được đũa, và cũng không nên muốn đổi."));
        quayLai(player);
    }

    private void phep(Player player) {
        StudentProfile p = plugin.getProfiles().get(player);
        header(player, "&b", "THẦN CHÚ");
        player.sendMessage(Msg.color("  &f&lCÁCH HỌC"));
        player.sendMessage(Msg.color("  &7Phép chỉ học được ở &flớp học&7, không mua được."));
        player.sendMessage(Msg.color("  &7Mỗi phép thuộc một năm học. Năm I không niệm"));
        player.sendMessage(Msg.color("  &7được phép của Năm V, dù có biết tên phép."));
        player.sendMessage(Msg.color(""));
        player.sendMessage(Msg.color("  &f&lMA LỰC"));
        player.sendMessage(Msg.color("  &7Mỗi phép tốn ma lực. Hết ma lực thì không niệm được."));
        player.sendMessage(Msg.color("  &7Ma lực tự hồi theo thời gian. Lên cấp thì có thêm."));
        if (p != null) {
            player.sendMessage(Msg.color("  &7Của bạn: &b" + (int) p.getMana()
                    + "&8/&b" + (int) p.effectiveMaxMana()));
        }
        player.sendMessage(Msg.color(""));
        player.sendMessage(Msg.color("  &f&lLỆNH"));
        player.sendMessage(Msg.color("  &f/hogwarts spells &8— xem phép đã học")
                .clickEvent(ClickEvent.runCommand("/hogwarts spells")));
        player.sendMessage(Msg.color("  &f/spell <tên phép> &8— chọn phép để dùng"));
        player.sendMessage(Msg.color("  &f/spell cast <tên phép> &8— niệm ngay"));
        quayLai(player);
    }

    private void nha(Player player) {
        header(player, "&6", "BỐN NHÀ");
        player.sendMessage(Msg.color("  &c&lGryffindor &8— &7Sư Tử"));
        player.sendMessage(Msg.color("    &8can đảm, gan dạ, hào hiệp, liều lĩnh"));
        player.sendMessage(Msg.color("  &e&lHufflepuff &8— &7Lửng"));
        player.sendMessage(Msg.color("    &8trung thành, kiên nhẫn, công bằng, chăm chỉ"));
        player.sendMessage(Msg.color("  &9&lRavenclaw &8— &7Đại Bàng"));
        player.sendMessage(Msg.color("    &8thông thái, sáng tạo, ham học, sắc sảo"));
        player.sendMessage(Msg.color("  &a&lSlytherin &8— &7Rắn"));
        player.sendMessage(Msg.color("    &8tham vọng, mưu lược, quyết đoán, tháo vát"));
        player.sendMessage(Msg.color(""));
        player.sendMessage(Msg.color("  &f&lBA CÁCH ĐƯỢC PHÂN NHÀ"));
        player.sendMessage(Msg.color("  &b• Trả lời 5 câu hỏi &8— Nón đọc tính cách của bạn"));
        player.sendMessage(Msg.color("  &d• Để Nón tự quyết &8— ngẫu nhiên, không cần nghĩ"));
        player.sendMessage(Msg.color("  &6• Xin Nón cân nhắc &8— nói Nhà bạn muốn, ~75% được như ý"));
        player.sendMessage(Msg.color(""));
        player.sendMessage(Msg.color("  &7Đang trả lời mà bí câu nào thì bấm &fKhông biết chọn gì&7,"));
        player.sendMessage(Msg.color("  &7câu đó sẽ tính đều cho cả bốn Nhà."));
        player.sendMessage(Msg.color(""));
        player.sendMessage(Msg.color("  &7Không có Nhà nào tốt hơn Nhà nào."));
        player.sendMessage(Msg.color("  &8Đã phân Nhà rồi thì không đổi được."));
        quayLai(player);
    }

    private void phong(Player player) {
        StudentProfile p = plugin.getProfiles().get(player);
        header(player, "&2", "PHÒNG SINH HOẠT CHUNG");
        player.sendMessage(Msg.color("  &7Mỗi Nhà vào phòng theo một cách riêng:"));
        player.sendMessage(Msg.color(""));
        player.sendMessage(Msg.color("  &c• Gryffindor &8— &7Bà Béo hỏi mật khẩu, gõ vào chat"));
        player.sendMessage(Msg.color("  &a• Slytherin &8— &7nói mật khẩu trước bức tường đá"));
        player.sendMessage(Msg.color("  &e• Hufflepuff &8— &7gõ thùng gỗ 7 nhịp liên tiếp"));
        player.sendMessage(Msg.color("     &8theo vần \"Hel-ga Huf-fle-puff\". Gõ dư thì bị giấm phun."));
        player.sendMessage(Msg.color("  &9• Ravenclaw &8— &7trả lời câu đố của con đại bàng đồng"));
        player.sendMessage(Msg.color("     &8Nhà khác trả lời đúng cũng vào được."));
        player.sendMessage(Msg.color(""));
        if (p != null && p.isSorted()) {
            player.sendMessage(Msg.color("  &f/cr &8— xem cách vào phòng của bạn")
                    .clickEvent(ClickEvent.runCommand("/cr")));
            player.sendMessage(Msg.color("  &f/cr password &8— xem mật khẩu tuần này")
                    .clickEvent(ClickEvent.runCommand("/cr password")));
        }
        player.sendMessage(Msg.color("  &8Vào nhầm phòng Nhà khác sẽ bị đẩy ra ngay."));
        quayLai(player);
    }

    private void diem(Player player) {
        header(player, "&6", "ĐIỂM NHÀ");
        player.sendMessage(Msg.color("  &f&lCÁCH KIẾM ĐIỂM"));
        player.sendMessage(Msg.color("    &a+20 &8— hoàn thành một tiết học"));
        player.sendMessage(Msg.color("    &a+50 &8— vượt thử thách cuối năm"));
        player.sendMessage(Msg.color("    &a+5  &8— học được một phép mới"));
        player.sendMessage(Msg.color("    &a+5 đến +15 &8— làm việc tốt ngoài giờ"));
        player.sendMessage(Msg.color(""));
        player.sendMessage(Msg.color("  &f&lCÁCH MẤT ĐIỂM"));
        player.sendMessage(Msg.color("    &c-5  &8— trả lời sai câu hỏi của giáo sư"));
        player.sendMessage(Msg.color("    &c-10 &8— ra ngoài sau giờ giới nghiêm"));
        player.sendMessage(Msg.color("    &c-20 &8— vi phạm nghiêm trọng nội quy"));
        player.sendMessage(Msg.color(""));
        player.sendMessage(Msg.color("  &7Cuối năm, Nhà nhiều điểm nhất giành &6Cúp Nhà&7."));
        player.sendMessage(Msg.color("  &f/hogwarts points &8— xem bảng xếp hạng")
                .clickEvent(ClickEvent.runCommand("/hogwarts points")));
        quayLai(player);
    }

    private void quest(Player player) {
        header(player, "&e", "NHIỆM VỤ");
        player.sendMessage(Msg.color("  &7Hành trình kéo dài &f7 năm học&7. Mỗi năm có các"));
        player.sendMessage(Msg.color("  &7tiết học, nhiệm vụ và một thử thách cuối năm."));
        player.sendMessage(Msg.color(""));
        player.sendMessage(Msg.color("  &f&lNĂM I &7gồm:"));
        player.sendMessage(Msg.color("    &8• Bùa chú — Giáo sư Flitwick"));
        player.sendMessage(Msg.color("    &8• Biến hình — Giáo sư McGonagall"));
        player.sendMessage(Msg.color("    &8• Độc dược — Giáo sư Snape"));
        player.sendMessage(Msg.color("    &8• Lớp Bay — Madam Hooch"));
        player.sendMessage(Msg.color("    &8• Phòng chống Nghệ thuật Hắc ám — Giáo sư Quirrell"));
        player.sendMessage(Msg.color("    &8• Cấm túc trong Rừng Cấm"));
        player.sendMessage(Msg.color("    &8• Thử thách dưới cửa sập"));
        player.sendMessage(Msg.color(""));
        player.sendMessage(Msg.color("  &7Ngoài ra còn có nhiệm vụ phụ: tìm con cóc của Neville,"));
        player.sendMessage(Msg.color("  &7sưu tầm thẻ Sô cô la Ếch nhái, Gương Ảo Ảnh..."));
        player.sendMessage(Msg.color("  &8Nhiệm vụ phụ không bắt buộc, làm hay không tuỳ bạn."));
        player.sendMessage(Msg.color(""));
        player.sendMessage(Msg.color("  &f/j &8— mở nhật ký xem nhiệm vụ đang làm"));
        quayLai(player);
    }

    private void lenh(Player player) {
        header(player, "&f", "DANH SÁCH LỆNH");
        player.sendMessage(Msg.color("  &f/trogiup &8— mở sổ tay này"));
        player.sendMessage(Msg.color("  &f/hogwarts &8— xem hồ sơ học sinh của bạn"));
        player.sendMessage(Msg.color("  &f/hogwarts points &8— bảng điểm bốn Nhà"));
        player.sendMessage(Msg.color("  &f/hogwarts spells &8— danh sách phép đã học"));
        player.sendMessage(Msg.color("  &f/hogwarts letter &8— nhận lại thư nhập học"));
        player.sendMessage(Msg.color("  &f/hogwarts wand &8— nhận đũa phép (nếu chưa có)"));
        player.sendMessage(Msg.color("  &f/hogwarts sorting &8— dự Lễ Phân Loại"));
        player.sendMessage(Msg.color("  &f/spell <tên phép> &8— chọn phép đang dùng"));
        player.sendMessage(Msg.color("  &f/spell cast <tên phép> &8— niệm phép ngay"));
        player.sendMessage(Msg.color("  &f/cr &8— thông tin Phòng Sinh hoạt chung"));
        player.sendMessage(Msg.color("  &f/cr password &8— mật khẩu tuần này"));
        player.sendMessage(Msg.color("  &f/j &8— nhật ký nhiệm vụ"));
        player.sendMessage(Msg.color(""));
        player.sendMessage(Msg.color("  &8Tên lệnh cũng dùng được bằng tiếng Việt:"));
        player.sendMessage(Msg.color("  &8/phep, /phong, /hoso"));
        quayLai(player);
    }

    // ------------------------------------------------------------
    private void header(Player p, String mau, String ten) {
        p.sendMessage(Msg.line());
        p.sendMessage(Msg.color("  " + mau + "&l" + ten));
        p.sendMessage(Msg.color(""));
    }

    private void quayLai(Player p) {
        p.sendMessage(Msg.color(""));
        p.sendMessage(Msg.color("  &8« &7Quay lại mục lục")
                .clickEvent(ClickEvent.runCommand("/trogiup")));
        p.sendMessage(Msg.line());
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("batdau", "dua", "phep", "nha", "phong", "diem", "quest", "lenh")
                    .stream().filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        return List.of();
    }
}
