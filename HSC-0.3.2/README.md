# HogwartsStoryCore 0.2

Lõi RPG học viện phù thuỷ cho Minecraft Paper 1.21.x. Bám sát nguyên tác Harry Potter: bốn Nhà, Chiếc Nón Phân Loại, đũa phép kiểu Ollivander, thần chú đúng tên, điểm Nhà và Cúp Nhà.

Bản 0.2 hoàn chỉnh **Năm I** và mở sẵn khung cho Năm II → VII.

---

## Build

```bash
mvn clean package
```

File ra: `target/HogwartsStoryCore-0.2.0.jar` → thả vào `plugins/` rồi khởi động lại server.

Yêu cầu: Java 21, Paper 1.21.4+.
Tuỳ chọn (softdepend): PlaceholderAPI, Citizens, MythicMobs, BetonQuest, Vault, PlayerPoints.

---

## Luồng chơi hiện có

```
Vào server lần đầu
   → cú đưa thư nhập học (sách viết sẵn 4 trang)
   → /hogwarts wand — Ollivander thử 3 cây rồi trao đũa
   → /hogwarts sorting — Nón Phân Loại hỏi 5 câu qua GUI
   → vào Nhà, Nhà được +5 điểm, học phép Lumos
   → cầm đũa: chuột phải niệm phép, shift+phải đổi phép
```

Đũa phép sinh ngẫu nhiên theo chuẩn Ollivander: **gỗ + lõi + chiều dài + độ dẻo**. Lõi ảnh hưởng chỉ số thật:

| Lõi | Ma lực | Sức mạnh | Hồi chiêu |
|---|---|---|---|
| Lông đuôi Phượng hoàng | +15% | — | tốt hơn 10% |
| Sợi tim Rồng | — | +25% | chậm hơn 10% |
| Lông Kỳ lân | +10% | −10% | tốt hơn 20% |

---

## Thần chú

`spells.yml` có sẵn **43 câu chú** chia theo 7 năm, mỗi phép có hiệu ứng thật trong game (không chỉ particle):

- **Năm I** — Lumos, Nox, Wingardium Leviosa, Alohomora, Incendio, Petrificus Totalus, Locomotor Mortis
- **Năm II** — Expelliarmus, Rictusempra, Serpensortia, Reparo, Immobulus, Finite Incantatem
- **Năm III** — Riddikulus, Expecto Patronum, Lumos Maxima, Impervius, Ferula
- **Năm IV** — Accio, Stupefy, Impedimenta, Reducto, Densaugeo, Point Me
- **Năm V** — Protego, Silencio, Levicorpus, Liberacorpus, Episkey
- **Năm VI** — Aguamenti, Muffliato, Confringo, Sectumsempra, Langlock
- **Năm VII** — Protego Totalum, Salvio Hexia, Cave Inimicum, Fiendfyre, Expelliarmus Maxima
- **Bị khoá mặc định** — Imperio, Crucio, Avada Kedavra (`allow-unforgivable: false`)

Người chơi Năm I biết command cũng **không** niệm được Expecto Patronum: hệ thống chặn theo `year` + danh sách `learned spells`. Đúng như bạn muốn.

Thêm phép mới chỉ cần sửa `spells.yml` rồi `/hogwarts reload` — trừ khi phép cần hiệu ứng riêng thì thêm một `case` trong `SpellManager.execute()`.

---

## Lệnh

| Lệnh | Quyền | Công dụng |
|---|---|---|
| `/hogwarts` | ai cũng được | Xem hồ sơ học sinh |
| `/hogwarts points` | ai cũng được | Bảng điểm 4 Nhà |
| `/hogwarts spells` | ai cũng được | Danh sách phép đã học |
| `/hogwarts sorting` | ai cũng được | Bắt đầu Lễ Phân Loại |
| `/hogwarts wand` | ai cũng được | Nhận đũa phép từ Ollivander |
| `/spell <phép>` | ai cũng được | Chọn phép đang dùng |
| `/spell cast <phép>` | ai cũng được | Niệm ngay |
| `/hogwarts award <nhà> <điểm> [lý do]` | admin | Cộng/trừ điểm Nhà |
| `/hogwarts teach <player> <phép\|all-year>` | admin | Dạy phép |
| `/hogwarts year <player> <1-7\|next>` | admin | Đổi năm học |
| `/hogwarts sethouse <player> <nhà>` | admin | Đổi Nhà |
| `/hogwarts cup` | admin | Trao Cúp Nhà + reset điểm |
| `/hogwarts reload` | admin | Nạp lại config & spells |

---

## Placeholder cho TAB

Đúng bố cục bạn phác trong tài liệu:

```
✦ HOGWARTS ✦
⚜ HỌC SINH
%player_name%
Nhà       %hogwarts_house_colored%
Năm       %hogwarts_year%
Magic Lv  %hogwarts_level%
✦ MA LỰC
Mana      %hogwarts_mana% / %hogwarts_max_mana%
Tiền      %vault_eco_balance%
Xu        %playerpoints_points%
♜ ĐIỂM NHÀ
%hogwarts_house%  %hogwarts_points%
```

Placeholder khác: `%hogwarts_house_color%`, `%hogwarts_wand%`, `%hogwarts_wand_core%`, `%hogwarts_spell%`, `%hogwarts_spells_count%`, `%hogwarts_contributed%`, `%hogwarts_sorted%`, `%hogwarts_leader%`, `%hogwarts_points_gryffindor%` (và 3 Nhà còn lại).

---

## Nối với BetonQuest

Tiến độ cốt truyện lưu dưới dạng **flag** trong hồ sơ học sinh: `year1.letter`, `year1.wand`, `year1.sorting`, `year2.start`...

Trong BetonQuest, dùng điều kiện/sự kiện gọi lệnh:

```yaml
events:
  day_lumos: "command hogwarts teach %player% lumos"
  cong_diem: "command hogwarts award gryffindor 20 hoàn thành tiết Bùa chú"
  len_nam:   "command hogwarts year %player% next"
```

Citizens giữ vai giáo sư (Flitwick, McGonagall, Snape, Sprout, Hagrid), MythicMobs lo Ông Kẹ / Giám ngục / Basilisk, còn HogwartsStoryCore quyết định người chơi **được phép** làm gì.

---

## Phòng Sinh hoạt chung

Bốn Nhà, bốn cách vào khác nhau đúng nguyên tác:

| Nhà | Người gác | Cách vào |
|---|---|---|
| Gryffindor | Bà Béo | Nói mật khẩu vào chat, sai 3 lần thì bà không mở nữa |
| Slytherin | Bức tường đá | Cũng mật khẩu, nhưng bức tường không nói gì |
| Hufflepuff | Dãy thùng gỗ | Gõ 7 nhịp theo vần "Hel-ga Huf-fle-puff" — gõ quá thì bị vòi giấm phun vào mặt |
| Ravenclaw | Con đại bàng đồng | Trả lời câu đố. **Nhà khác cũng vào được nếu trả lời đúng** — đúng như trong truyện |

Mật khẩu tự đổi mỗi 7 ngày và thông báo riêng cho người cùng Nhà. Người Nhà khác lọt vào trong vùng phòng sẽ bị đẩy ra sau tối đa 2 giây.

Thiết lập bằng lệnh trong game, không cần sửa file:

```
/cr setentrance gryffindor   # nhìn vào khối làm cửa rồi gõ
/cr setspawn gryffindor      # đứng ở chỗ muốn dịch chuyển tới
/cr pos1  →  /cr pos2 gryffindor   # chọn hai góc vùng phòng
/cr status                   # xem phòng nào đã xong
```

Học sinh dùng `/cr` để xem cách vào Nhà mình và `/cr password` để xem mật khẩu tuần này.

---

## Quest Năm I (kèm sẵn)

Thư mục `QuestPackages/hogwarts_year1/` là package BetonQuest hoàn chỉnh cho năm thứ nhất: 9 chương, 6 giáo sư có hội thoại riêng, cấm túc trong Rừng Cấm và chuỗi 5 phòng bảo vệ + boss cuối năm. Xem `QuestPackages/hogwarts_year1/README.md`.

Cầu nối giữa hai plugin là mục `cast-hooks` trong `config.yml`: mỗi lần niệm phép thành công, plugin chạy một lệnh console (mặc định là `q event ...`) để BetonQuest đếm số lần luyện tập. Plugin cũng bắn `SpellCastEvent` cho ai muốn viết addon riêng.

---

## Còn thiếu (làm tiếp ở 0.3)

1. Nội dung quest Năm II → VII
2. Common Room theo Nhà + hệ thống warp/quyền vào phòng
3. Thời khoá biểu — lớp học mở theo giờ trong game
4. Boss cuối năm + thi cuối năm (O.W.L. / N.E.W.T.)
5. Cửa hàng Hẻm Xéo, Hogsmeade cuối tuần
6. Quest Tracker tự ẩn/hiện trên TAB (cần AetheriaTAB đọc flag)
7. Đổi CAVE_SPIDER của Serpensortia sang mob rắn MythicMobs riêng

---

*Server nội bộ, không thu phí. Toàn bộ tên riêng thuộc về J.K. Rowling và Warner Bros.*
