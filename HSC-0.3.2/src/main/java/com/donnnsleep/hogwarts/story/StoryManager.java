package com.donnnsleep.hogwarts.story;

import com.donnnsleep.hogwarts.HogwartsStoryCore;
import com.donnnsleep.hogwarts.model.House;
import com.donnnsleep.hogwarts.model.StudentProfile;
import com.donnnsleep.hogwarts.model.Wand;
import com.donnnsleep.hogwarts.util.Msg;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.List;
import java.util.Random;

/**
 * Quản lý tiến độ cốt truyện. Bản 0.2 hoàn thiện Năm I;
 * các năm sau móc nối bằng flag nên BetonQuest có thể đọc/ghi cùng hệ thống.
 */
public class StoryManager {

    private final HogwartsStoryCore plugin;
    private final Random rng = new Random();

    public StoryManager(HogwartsStoryCore plugin) {
        this.plugin = plugin;
    }

    // ---------------------------------------------------------------
    //  BƯỚC 1 — THƯ NHẬP HỌC
    // ---------------------------------------------------------------
    public void sendAcceptanceLetter(Player player) {
        StudentProfile p = plugin.getProfiles().get(player);
        if (p == null || p.hasFlag("year1.letter")) return;

        player.showTitle(Title.title(
                Msg.color("&f✉ &eMột con cú đưa thư"),
                Msg.color("&7Nó thả một phong thư xuống trước mặt bạn"),
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(4), Duration.ofSeconds(1))));
        player.playSound(player.getLocation(), Sound.ENTITY_PARROT_IMITATE_WITCH, 1f, 1.2f);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().add(0, 2, 0), 40, 0.5, 0.3, 0.5, 0.02);

        ItemStack letter = createLetter(player.getName());
        player.getInventory().addItem(letter);

        player.sendMessage(Msg.color(""));
        player.sendMessage(Msg.color("&e&lTHƯ NHẬP HỌC &7đã được gửi tới túi đồ của bạn."));
        player.sendMessage(Msg.color("&7Hãy mở thư ra đọc, rồi tới &fHẻm Xéo &7để sắm đồ."));
        player.sendMessage(Msg.color(""));

        p.addFlag("year1.letter");
        plugin.getProfiles().save(p);
    }

    private ItemStack createLetter(String name) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.title(Msg.color("Thư nhập học Hogwarts"));
        meta.author(Msg.color("Minerva McGonagall"));
        meta.displayName(Msg.color("&e✉ Thư nhập học Hogwarts")
                .decoration(TextDecoration.ITALIC, false));

        meta.addPages(Msg.color(
                "&0&lTRƯỜNG PHÁP THUẬT\n&0&lHOGWARTS\n\n" +
                "&8Hiệu trưởng: Albus Dumbledore\n\n" +
                "&0Kính gửi &8" + name + "&0,\n\n" +
                "&0Chúng tôi hân hạnh báo tin rằng em đã được nhận vào Trường Pháp thuật Hogwarts."));

        meta.addPages(Msg.color(
                "&0&lDANH MỤC ĐỒ DÙNG\n\n" +
                "&0• Ba bộ áo chùng đen\n" +
                "&0• Một chiếc nón chóp nhọn\n" +
                "&0• Một đôi găng tay bảo hộ\n" +
                "&0• Một áo choàng mùa đông\n" +
                "&0• Một cây đũa phép\n" +
                "&0• Một vạc thiếc cỡ 2\n" +
                "&0• Một bộ ống nghiệm thuỷ tinh"));

        meta.addPages(Msg.color(
                "&0&lSÁCH GIÁO KHOA\n\n" +
                "&0• Sách Chú thuật Căn bản, Quyển 1\n" +
                "&0• Lịch sử Pháp thuật\n" +
                "&0• Lý thuyết Pháp thuật\n" +
                "&0• Biến hình cho Người mới\n" +
                "&0• Một Ngàn Loại Thảo mộc\n" +
                "&0• Độc dược Ma thuật\n\n" +
                "&8Học sinh năm nhất không được\n&8mang theo chổi bay riêng."));

        meta.addPages(Msg.color(
                "&0Học kỳ bắt đầu vào ngày &l1 tháng 9&0.\n\n" +
                "&0Chuyến tàu Tốc hành Hogwarts khởi hành từ &lsân ga 9¾&0.\n\n" +
                "&0Trân trọng,\n&8Minerva McGonagall\n&8Phó Hiệu trưởng"));

        book.setItemMeta(meta);
        return book;
    }

    // ---------------------------------------------------------------
    //  BƯỚC 2 — ÔNG OLLIVANDER TRAO ĐŨA PHÉP
    // ---------------------------------------------------------------
    public void giveWand(Player player) {
        StudentProfile p = plugin.getProfiles().get(player);
        if (p == null) return;
        if (p.hasWand()) {
            player.sendMessage(Msg.color("&7Con đã có đũa phép rồi: &f" + p.getWand().describe()));
            return;
        }

        player.sendMessage(Msg.color(""));
        player.sendMessage(Msg.color("&f&lOLLIVANDER: &7&oKhông phải phù thuỷ chọn đũa phép..."));

        new BukkitRunnable() {
            int step = 0;
            @Override public void run() {
                step++;
                if (step == 1) {
                    player.sendMessage(Msg.color("&f&lOLLIVANDER: &7&oĐũa phép chọn phù thuỷ."));
                    player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_DOOR_OPEN, 1f, 1.4f);
                } else if (step == 2) {
                    player.sendMessage(Msg.color("&8Ông ấy thử một cây... rồi lắc đầu."));
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1.5f);
                } else if (step == 3) {
                    player.sendMessage(Msg.color("&8Một cây nữa... vẫn chưa phải."));
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1.2f);
                } else {
                    Wand wand = Wand.random(rng);
                    p.setWand(wand);
                    p.addFlag("year1.wand");
                    plugin.getProfiles().save(p);

                    player.getInventory().addItem(createWandItem(wand));
                    player.showTitle(Title.title(
                            Msg.color("&d✦ Đũa phép của bạn ✦"),
                            Msg.color("&f" + wand.describe()),
                            Title.Times.times(Duration.ofMillis(300), Duration.ofSeconds(4), Duration.ofSeconds(1))));
                    player.getWorld().spawnParticle(Particle.DUST,
                            player.getLocation().add(0, 1.5, 0), 90, 0.4, 0.7, 0.4,
                            new Particle.DustOptions(wand.getCore().getMauHat(), 1.2f));
                    player.getWorld().spawnParticle(Particle.END_ROD,
                            player.getLocation().add(0, 1.5, 0), 40, 0.3, 0.5, 0.3, 0.03);
                    player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.4f);

                    player.sendMessage(Msg.color("&f&lOLLIVANDER: &7&oPhải rồi... chính là cây này."));
                    player.sendMessage(Msg.color("&7Đũa phép: &" + wand.getCore().getHexPhu()
                            + wand.describe()));
                    player.sendMessage(Msg.color(""));
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 40L, 35L);
    }

    public ItemStack createWandItem(Wand wand) {
        ItemStack is = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = is.getItemMeta();

        String dam = "&" + wand.getCore().getHexChinh();   // ví dụ &#FF9E2C
        String nhat = "&" + wand.getCore().getHexPhu();

        // Tên đũa đổi màu theo lõi, có hai ngôi sao ôm hai bên
        meta.displayName(Msg.color(dam + "✦ " + nhat + "Đũa phép " + dam + "✦")
                .decoration(TextDecoration.ITALIC, false));

        meta.lore(List.of(
                khong(Msg.color("&8" + "─".repeat(22))),
                khong(Msg.color("&7Gỗ        " + nhat + Wand.woodDisplay(wand.getWood()))),
                khong(Msg.color("&7Lõi       " + dam + wand.getCore().getVi())),
                khong(Msg.color("&7Chiều dài " + nhat + String.format("%.2f", wand.getLength()) + " inch")),
                khong(Msg.color("&7Độ dẻo    " + nhat + wand.getFlexibility())),
                khong(Msg.color("&8" + "─".repeat(22))),
                khong(Msg.color(moTaLoi(wand))),
                khong(Msg.color("")),
                khong(Msg.color("&8▸ &7Chuột phải &8· &7niệm phép đang chọn")),
                khong(Msg.color("&8▸ &7Shift + phải &8· &7đổi phép")),
                khong(Msg.color("")),
                khong(Msg.color("&8&oĐũa phép chọn phù thuỷ."))
        ));

        // Ánh phép lấp lánh, không cần enchant thật
        meta.setEnchantmentGlintOverride(true);
        meta.setRarity(org.bukkit.inventory.ItemRarity.EPIC);
        meta.setMaxStackSize(1);

        meta.getPersistentDataContainer().set(plugin.getWandKey(),
                org.bukkit.persistence.PersistentDataType.STRING, wand.serialize());
        is.setItemMeta(meta);
        return is;
    }

    /** Một dòng mô tả ngắn về tính cách của lõi đũa. */
    private String moTaLoi(Wand wand) {
        return switch (wand.getCore()) {
            case PHOENIX_FEATHER -> "&#FFD98A&oTrung thành hiếm thấy, nhưng khó thuần";
            case DRAGON_HEARTSTRING -> "&#FF8A7A&oQuyền năng mạnh nhất, dễ ngả sang hắc ám";
            case UNICORN_HAIR -> "&#E8F7FF&oỔn định và trung thực, ít khi phản chủ";
        };
    }

    /** Tắt kiểu chữ nghiêng mặc định của lore. */
    private net.kyori.adventure.text.Component khong(net.kyori.adventure.text.Component c) {
        return c.decoration(TextDecoration.ITALIC, false);
    }

    // ---------------------------------------------------------------
    //  BƯỚC 3 — SAU LỄ PHÂN LOẠI
    // ---------------------------------------------------------------
    public void onSorted(Player player, House house) {
        StudentProfile p = plugin.getProfiles().get(player);
        if (p == null) return;

        plugin.getPoints().award(house, 5, "học sinh mới nhập Nhà");

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.sendMessage(Msg.color(""));
            player.sendMessage(Msg.color("&7Phòng Sinh hoạt chung của Nhà "
                    + house.getLegacyColor() + house.getDisplayName() + " &7đã mở cho con."));
            player.sendMessage(Msg.color("&7Ngày mai con có &fTiết Bùa chú &7đầu tiên với Giáo sư Flitwick."));
            player.sendMessage(Msg.color("&8Gõ &f/hogwarts &8để xem hồ sơ học sinh của con."));
            player.sendMessage(Msg.color(""));

            // Mở khoá phép đầu tiên của Năm I
            teachSpell(player, "lumos", false);
        }, 100L);
    }

    // ---------------------------------------------------------------
    //  DẠY PHÉP
    // ---------------------------------------------------------------
    public boolean teachSpell(Player player, String spellId, boolean silentIfKnown) {
        StudentProfile p = plugin.getProfiles().get(player);
        var spell = plugin.getSpells().get(spellId);
        if (p == null || spell == null) return false;

        if (p.hasLearned(spell.id())) {
            if (!silentIfKnown) player.sendMessage(Msg.color("&7Con đã học phép này rồi."));
            return false;
        }
        if (p.getYear() < spell.year()) {
            player.sendMessage(Msg.color("&cPhép này chỉ được dạy từ Năm " + roman(spell.year()) + " trở đi."));
            return false;
        }

        p.learn(spell.id());
        if (p.getActiveSpell() == null) p.setActiveSpell(spell.id());
        p.addXp(plugin.getConfig().getInt("magic.xp-per-spell-learned", 25));
        plugin.getProfiles().save(p);

        player.showTitle(Title.title(
                Msg.color("&d✦ Học được phép mới"),
                Msg.color(spell.displayColor() + spell.incantation() + " &8· &7" + spell.viName()),
                Title.Times.times(Duration.ofMillis(300), Duration.ofSeconds(3), Duration.ofMillis(700))));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.3f);
        player.sendMessage(Msg.color(""));
        player.sendMessage(Msg.color("&d✦ &f" + spell.incantation() + " &8— &7" + spell.description()));
        player.sendMessage(Msg.color("&8Dùng &f/spell " + spell.id() + " &8để chọn phép này."));
        player.sendMessage(Msg.color(""));

        if (p.getHouse() != null) {
            plugin.getPoints().awardPlayer(player, 5, "học được " + spell.incantation());
        }
        return true;
    }

    // ---------------------------------------------------------------
    //  LÊN NĂM
    // ---------------------------------------------------------------
    public void advanceYear(Player player) {
        StudentProfile p = plugin.getProfiles().get(player);
        if (p == null) return;
        if (p.getYear() >= 7) {
            player.sendMessage(Msg.color("&7Con đã ở Năm VII — năm cuối cùng."));
            return;
        }
        p.setYear(p.getYear() + 1);
        p.addFlag("year" + p.getYear() + ".start");
        plugin.getProfiles().save(p);

        player.showTitle(Title.title(
                Msg.color("&6&lNĂM " + roman(p.getYear())),
                Msg.color("&7Một năm học mới bắt đầu tại Hogwarts"),
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(4), Duration.ofSeconds(1))));
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        player.sendMessage(Msg.color("&7Các phép mới của &fNăm " + roman(p.getYear()) + " &7đã sẵn sàng để học."));
    }

    public static String roman(int n) {
        return switch (n) {
            case 1 -> "I"; case 2 -> "II"; case 3 -> "III"; case 4 -> "IV";
            case 5 -> "V"; case 6 -> "VI"; case 7 -> "VII";
            default -> String.valueOf(n);
        };
    }
}
