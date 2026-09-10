package com.donnnsleep.hogwarts.sorting;

import com.donnnsleep.hogwarts.HogwartsStoryCore;
import com.donnnsleep.hogwarts.model.House;
import com.donnnsleep.hogwarts.model.StudentProfile;
import com.donnnsleep.hogwarts.util.Msg;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.*;

/**
 * Lễ Phân Loại.
 *
 * Người chơi có ba lựa chọn:
 *   1. Trả lời 5 câu hỏi — Nón chấm điểm bốn Nhà rồi quyết định
 *   2. Để Nón tự quyết định — ngẫu nhiên hoàn toàn, cho ai không muốn nghĩ
 *   3. Xin Nón cân nhắc một Nhà — như Harry xin đừng vào Slytherin.
 *      Nón thường nghe theo, nhưng không phải lúc nào cũng vậy.
 *
 * Trong lúc trả lời, mỗi câu đều có nút "Không biết chọn gì" — câu đó được
 * tính đều cho cả bốn Nhà, nên ai lười nghĩ vẫn đi tới cuối được.
 */
public class SortingCeremony implements Listener {

    private final HogwartsStoryCore plugin;
    private final Map<UUID, Session> sessions = new HashMap<>();
    private final Random rng = new Random();

    private static final String TIEU_DE = "✦ Chiếc Nón Phân Loại ✦";

    /** Tỉ lệ Nón nghe theo nguyện vọng của học sinh. */
    private static final int TI_LE_NGHE_THEO = 75;

    public SortingCeremony(HogwartsStoryCore plugin) {
        this.plugin = plugin;
    }

    // ============================================================
    //  BỘ CÂU HỎI
    // ============================================================
    private record Choice(String text, Map<House, Integer> weights) {}
    private record Question(String prompt, List<Choice> choices) {}

    private static Map<House, Integer> w(int g, int h, int r, int s) {
        Map<House, Integer> m = new EnumMap<>(House.class);
        m.put(House.GRYFFINDOR, g);
        m.put(House.HUFFLEPUFF, h);
        m.put(House.RAVENCLAW, r);
        m.put(House.SLYTHERIN, s);
        return m;
    }

    private final List<Question> QUESTIONS = List.of(
            new Question("Điều gì khiến con sợ nhất?", List.of(
                    new Choice("Bị người khác coi là hèn nhát", w(3, 0, 0, 1)),
                    new Choice("Bị bạn bè bỏ rơi", w(0, 3, 0, 1)),
                    new Choice("Sống một đời tầm thường, vô danh", w(1, 0, 0, 3)),
                    new Choice("Không hiểu được thế giới quanh mình", w(0, 0, 3, 1)))),

            new Question("Con muốn được nhớ đến vì điều gì?", List.of(
                    new Choice("Lòng dũng cảm", w(3, 0, 0, 0)),
                    new Choice("Sự tử tế", w(0, 3, 1, 0)),
                    new Choice("Trí tuệ", w(0, 0, 3, 1)),
                    new Choice("Quyền lực và danh vọng", w(0, 0, 0, 3)))),

            new Question("Bốn cái rương. Con mở cái nào?", List.of(
                    new Choice("Rương phát ra tiếng gầm", w(3, 0, 0, 1)),
                    new Choice("Rương ấm áp mùi bánh mì", w(0, 3, 0, 0)),
                    new Choice("Rương khắc đầy chữ cổ", w(0, 0, 3, 1)),
                    new Choice("Rương khoá bằng bảy ổ khoá bạc", w(1, 0, 1, 3)))),

            new Question("Một bạn học bị bắt nạt trong hành lang. Con làm gì?", List.of(
                    new Choice("Lao vào can ngay lập tức", w(3, 1, 0, 0)),
                    new Choice("Đứng cạnh nạn nhân, không rời đi", w(0, 3, 0, 0)),
                    new Choice("Đi gọi giáo sư — cách chắc chắn nhất", w(0, 1, 3, 0)),
                    new Choice("Ghi nhớ mọi thứ để dùng về sau", w(0, 0, 1, 3)))),

            new Question("Con tin điều gì dẫn tới thành công?", List.of(
                    new Choice("Dám làm điều người khác không dám", w(3, 0, 0, 1)),
                    new Choice("Làm việc bền bỉ mỗi ngày", w(0, 3, 1, 0)),
                    new Choice("Hiểu rõ hơn mọi người", w(0, 0, 3, 1)),
                    new Choice("Biết mình muốn gì và đi tới cùng", w(1, 0, 0, 3))))
    );

    // ============================================================
    //  PHIÊN LÀM VIỆC
    // ============================================================
    private enum Stage { MO_DAU, NGUYEN_VONG, CAU_HOI }

    private static class Session {
        Stage stage = Stage.MO_DAU;
        int index = 0;
        int daBoQua = 0;
        final EnumMap<House, Integer> score = new EnumMap<>(House.class);
        Session() { for (House h : House.values()) score.put(h, 0); }
    }

    // ============================================================
    //  BẮT ĐẦU
    // ============================================================
    public void begin(Player player) {
        StudentProfile p = plugin.getProfiles().get(player);
        if (p == null) return;
        if (p.isSorted()) {
            player.sendMessage(Msg.color("&7Con đã được phân vào Nhà "
                    + p.getHouse().getLegacyColor() + p.getHouse().getDisplayName() + "&7 rồi."));
            return;
        }

        sessions.put(player.getUniqueId(), new Session());
        player.showTitle(Title.title(
                Msg.color("&#E8C25A✦ Lễ Phân Loại ✦"),
                Msg.color("&7Chiếc Nón Phân Loại được đặt lên đầu con..."),
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500))));
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 0.8f);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            player.sendMessage(Msg.color(""));
            player.sendMessage(Msg.color("&#E8C25A&oHừm... khó đây... rất khó đây..."));
            player.sendMessage(Msg.color(""));
            moManHinhMoDau(player);
        }, 70L);
    }

    // ============================================================
    //  MÀN HÌNH 1 — CHỌN CÁCH PHÂN LOẠI
    // ============================================================
    private void moManHinhMoDau(Player player) {
        Session s = sessions.get(player.getUniqueId());
        if (s == null) return;
        s.stage = Stage.MO_DAU;

        Inventory inv = taoKhung();

        inv.setItem(4, vatPham(Material.LEATHER_HELMET,
                "&#E8C25A&lNÓN PHÂN LOẠI",
                List.of("&7Con muốn ta quyết định thế nào?")));

        inv.setItem(11, vatPham(Material.WRITABLE_BOOK,
                "&b&lTrả lời năm câu hỏi",
                List.of("&7Ta sẽ hỏi con năm điều.",
                        "&7Trả lời thật lòng, ta sẽ đọc được con.",
                        "",
                        "&8Cách chuẩn nhất, mất khoảng một phút.")));

        inv.setItem(13, vatPham(Material.ENDER_PEARL,
                "&d&lĐể ta tự quyết định",
                List.of("&7Con không cần nghĩ gì cả.",
                        "&7Ta sẽ chọn một Nhà cho con.",
                        "",
                        "&8Hoàn toàn ngẫu nhiên, bốn Nhà đều nhau.")));

        inv.setItem(15, vatPham(Material.NAME_TAG,
                "&6&lXin ta cân nhắc một Nhà",
                List.of("&7Con nói ra Nhà con mong muốn.",
                        "&7Ta thường nghe theo... nhưng không phải luôn luôn.",
                        "",
                        "&8Khoảng " + TI_LE_NGHE_THEO + "% được như ý.")));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
    }

    // ============================================================
    //  MÀN HÌNH 2 — CHỌN NGUYỆN VỌNG
    // ============================================================
    private void moManHinhNguyenVong(Player player) {
        Session s = sessions.get(player.getUniqueId());
        if (s == null) return;
        s.stage = Stage.NGUYEN_VONG;

        Inventory inv = taoKhung();
        inv.setItem(4, vatPham(Material.LEATHER_HELMET,
                "&#E8C25A&lCon mong vào Nhà nào?",
                List.of("&7Nói đi, ta đang nghe.")));

        int[] slots = {10, 12, 14, 16};
        House[] houses = {House.GRYFFINDOR, House.HUFFLEPUFF, House.RAVENCLAW, House.SLYTHERIN};
        for (int i = 0; i < 4; i++) {
            House h = houses[i];
            inv.setItem(slots[i], vatPham(h.getBanner(),
                    h.getLegacyColor() + "&l" + h.getDisplayName(),
                    List.of("&7Biểu tượng: &f" + h.getAnimal(),
                            "&8" + String.join(", ", h.getTraits()))));
        }

        inv.setItem(22, vatPham(Material.BARRIER,
                "&7Thôi, để con nghĩ lại",
                List.of("&8Quay về màn hình trước")));

        player.openInventory(inv);
    }

    // ============================================================
    //  MÀN HÌNH 3 — CÂU HỎI
    // ============================================================
    private void moCauHoi(Player player) {
        Session s = sessions.get(player.getUniqueId());
        if (s == null) return;
        s.stage = Stage.CAU_HOI;

        if (s.index >= QUESTIONS.size()) {
            ketThuc(player, s);
            return;
        }

        Question q = QUESTIONS.get(s.index);
        Inventory inv = taoKhung();

        inv.setItem(4, vatPham(Material.LEATHER_HELMET,
                "&#E8C25A&l" + q.prompt(),
                List.of("&7Câu " + (s.index + 1) + " trên " + QUESTIONS.size())));

        int[] slots = {10, 12, 14, 16};
        Material[] mats = {Material.RED_DYE, Material.YELLOW_DYE, Material.BLUE_DYE, Material.LIME_DYE};
        for (int i = 0; i < q.choices().size() && i < 4; i++) {
            inv.setItem(slots[i], vatPham(mats[i], "&f" + q.choices().get(i).text(),
                    List.of("&8Bấm để chọn")));
        }

        inv.setItem(22, vatPham(Material.ENDER_PEARL,
                "&d&lKhông biết chọn gì",
                List.of("&7Để ta tự cân nhắc câu này.",
                        "&8Câu này được tính đều cho cả bốn Nhà.")));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
    }

    // ============================================================
    //  XỬ LÝ BẤM CHUỘT
    // ============================================================
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        Session s = sessions.get(player.getUniqueId());
        if (s == null) return;
        if (!e.getView().title().equals(Msg.color("&5" + TIEU_DE))) return;

        e.setCancelled(true);
        int slot = e.getRawSlot();
        if (slot < 0 || slot >= 27) return;

        switch (s.stage) {
            case MO_DAU -> xuLyMoDau(player, slot);
            case NGUYEN_VONG -> xuLyNguyenVong(player, slot);
            case CAU_HOI -> xuLyCauHoi(player, s, slot);
        }
    }

    private void xuLyMoDau(Player player, int slot) {
        switch (slot) {
            case 11 -> {
                bam(player);
                player.closeInventory();
                sau(player, () -> moCauHoi(player), 8L);
            }
            case 13 -> {
                bam(player);
                player.closeInventory();
                player.sendMessage(Msg.color("&#E8C25A&oĐược thôi. Để ta tự xem vậy..."));
                House chon = House.values()[rng.nextInt(House.values().length)];
                sau(player, () -> phanNha(player, chon, "ngau_nhien"), 20L);
            }
            case 15 -> {
                bam(player);
                player.closeInventory();
                sau(player, () -> moManHinhNguyenVong(player), 8L);
            }
            default -> {}
        }
    }

    private void xuLyNguyenVong(Player player, int slot) {
        if (slot == 22) {
            bam(player);
            player.closeInventory();
            sau(player, () -> moManHinhMoDau(player), 8L);
            return;
        }

        House chon = switch (slot) {
            case 10 -> House.GRYFFINDOR;
            case 12 -> House.HUFFLEPUFF;
            case 14 -> House.RAVENCLAW;
            case 16 -> House.SLYTHERIN;
            default -> null;
        };
        if (chon == null) return;

        bam(player);
        player.closeInventory();

        boolean ngheTheo = rng.nextInt(100) < TI_LE_NGHE_THEO;
        House ketQua;
        if (ngheTheo) {
            ketQua = chon;
            player.sendMessage(Msg.color("&#E8C25A&oCon chắc chứ? Được... nếu con đã muốn vậy..."));
        } else {
            List<House> khac = new ArrayList<>(List.of(House.values()));
            khac.remove(chon);
            ketQua = khac.get(rng.nextInt(khac.size()));
            player.sendMessage(Msg.color("&#E8C25A&oCon muốn vào đó ư? Không, không... ta thấy rõ hơn con."));
        }
        House cuoi = ketQua;
        sau(player, () -> phanNha(player, cuoi, ngheTheo ? "nguyen_vong" : "trai_nguyen_vong"), 30L);
    }

    private void xuLyCauHoi(Player player, Session s, int slot) {
        Question q = QUESTIONS.get(s.index);

        if (slot == 22) {
            bam(player);
            for (House h : House.values()) s.score.merge(h, 1, Integer::sum);
            s.daBoQua++;
            s.index++;
            player.closeInventory();
            sau(player, () -> moCauHoi(player), 10L);
            return;
        }

        int idx = switch (slot) {
            case 10 -> 0; case 12 -> 1; case 14 -> 2; case 16 -> 3;
            default -> -1;
        };
        if (idx < 0 || idx >= q.choices().size()) return;

        bam(player);
        q.choices().get(idx).weights().forEach((h, v) -> s.score.merge(h, v, Integer::sum));
        s.index++;
        player.closeInventory();
        sau(player, () -> moCauHoi(player), 10L);
    }

    /** Không cho bỏ dở giữa chừng — mở lại màn hình đang dang dở. */
    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player player)) return;
        Session s = sessions.get(player.getUniqueId());
        if (s == null) return;
        if (!e.getView().title().equals(Msg.color("&5" + TIEU_DE))) return;
        if (s.stage == Stage.CAU_HOI && s.index >= QUESTIONS.size()) return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            Session cur = sessions.get(player.getUniqueId());
            if (cur == null) return;
            switch (cur.stage) {
                case MO_DAU -> moManHinhMoDau(player);
                case NGUYEN_VONG -> moManHinhNguyenVong(player);
                case CAU_HOI -> moCauHoi(player);
            }
        }, 20L);
    }

    // ============================================================
    //  KẾT THÚC
    // ============================================================
    private void ketThuc(Player player, Session s) {
        int best = s.score.values().stream().max(Integer::compareTo).orElse(0);
        List<House> hoa = s.score.entrySet().stream()
                .filter(en -> en.getValue() == best)
                .map(Map.Entry::getKey)
                .toList();
        House chon = hoa.get(rng.nextInt(hoa.size()));

        String kieu = s.daBoQua >= 3 ? "ngau_nhien" : (hoa.size() > 1 ? "hoa_diem" : "cau_hoi");
        phanNha(player, chon, kieu);
    }

    /** Chốt Nhà và diễn hoạt cảnh Nón hét lên. */
    private void phanNha(Player player, House chon, String kieu) {
        sessions.remove(player.getUniqueId());

        StudentProfile p = plugin.getProfiles().get(player);
        if (p == null) return;
        p.setHouse(chon);
        p.addFlag("year1.sorting");
        plugin.getProfiles().save(p);

        String[] loiThoai = switch (kieu) {
            case "ngau_nhien" -> new String[]{
                    "Con để ta tự quyết ư? Ít đứa dám thế...",
                    "Nhưng ta đã nhìn thấy thứ ta cần thấy."};
            case "nguyen_vong" -> new String[]{
                    "Con biết mình muốn gì. Điều đó cũng nói lên nhiều thứ...",
                    "Chính lựa chọn làm nên con người ta, hơn cả năng lực."};
            case "trai_nguyen_vong" -> new String[]{
                    "Ta biết con muốn gì. Nhưng ta thấy thứ khác trong con...",
                    "Rồi con sẽ hiểu vì sao ta chọn như vậy."};
            case "hoa_diem" -> new String[]{
                    "Khó thật đấy... con hợp với hai Nhà cùng lúc...",
                    "Nhưng ta phải chọn một. Và ta đã chọn."};
            default -> new String[]{
                    "Can đảm... có. Trí tuệ... cũng có...",
                    "Nhưng ta thấy ở con một thứ khác nữa..."};
        };

        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                tick++;
                if (tick == 1) player.sendMessage(Msg.color("&#E8C25A&o" + loiThoai[0]));
                if (tick == 3) player.sendMessage(Msg.color("&#E8C25A&o" + loiThoai[1]));
                if (tick == 5) {
                    player.sendMessage(Msg.color(""));
                    player.sendMessage(Msg.color("&#E8C25A&lNÓN PHÂN LOẠI: &r" + chon.getLegacyColor()
                            + "&l" + chon.getDisplayName().toUpperCase() + "!"));
                    player.sendMessage(Msg.color(""));
                    player.showTitle(Title.title(
                            Msg.color(chon.getLegacyColor() + "&l" + chon.getDisplayName()),
                            Msg.color("&7Nhà " + chon.getAnimal() + " chào đón con"),
                            Title.Times.times(Duration.ofMillis(300), Duration.ofSeconds(4), Duration.ofSeconds(1))));
                    player.getWorld().playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                    player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING,
                            player.getLocation().add(0, 1, 0), 120, 0.6, 1, 0.6, 0.3);

                    Bukkit.broadcast(Msg.color("&7» &f" + player.getName() + " &7được phân vào Nhà "
                            + chon.getLegacyColor() + "&l" + chon.getDisplayName()));

                    plugin.getStory().onSorted(player, chon);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 20L, 25L);
    }

    /** Xoá Nhà để phân loại lại. Dùng cho lệnh quản trị. */
    public void resetSorting(Player player) {
        StudentProfile p = plugin.getProfiles().get(player);
        if (p == null) return;
        p.setHouse(null);
        p.removeFlag("year1.sorting");
        plugin.getProfiles().save(p);
        sessions.remove(player.getUniqueId());
        player.sendMessage(Msg.msg("&7Nhà của con đã được xoá. Lễ Phân Loại có thể làm lại."));
    }

    public void clear(UUID uuid) {
        sessions.remove(uuid);
    }

    // ============================================================
    //  TIỆN ÍCH
    // ============================================================
    private Inventory taoKhung() {
        Inventory inv = Bukkit.createInventory(null, 27, Msg.color("&5" + TIEU_DE));
        ItemStack vien = vatPham(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 27; i++) inv.setItem(i, vien);
        return inv;
    }

    private void bam(Player p) {
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.2f);
    }

    private void sau(Player p, Runnable r, long ticks) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (p.isOnline()) r.run();
        }, ticks);
    }

    private ItemStack vatPham(Material mat, String ten, List<String> lore) {
        ItemStack is = new ItemStack(mat);
        ItemMeta meta = is.getItemMeta();
        meta.displayName(Msg.color(ten).decoration(TextDecoration.ITALIC, false));
        if (lore != null) {
            meta.lore(lore.stream()
                    .map(l -> Msg.color(l).decoration(TextDecoration.ITALIC, false))
                    .toList());
        }
        is.setItemMeta(meta);
        return is;
    }
}
