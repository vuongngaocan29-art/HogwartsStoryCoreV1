# HƯỚNG DẪN CÀI ĐẶT

Làm đúng theo thứ tự dưới đây. Mỗi giai đoạn đều có bước kiểm tra — **đừng sang giai đoạn sau khi giai đoạn trước chưa chạy đúng**, vì nếu có lỗi bạn sẽ không biết nó đến từ đâu.

---

## GIAI ĐOẠN 0 — Yêu cầu

| Thứ | Phiên bản | Bắt buộc? |
|---|---|---|
| Java | **21 trở lên** | Bắt buộc |
| Paper | **1.21.4 trở lên** (đã chỉnh sẵn cho 1.21.11) | Bắt buộc (Spigot/Bukkit thường sẽ **không** chạy được) |
| Maven | 3.8+ | Chỉ cần khi build |
| PlaceholderAPI | mới nhất | Rất nên có |
| Vault + một plugin economy | mới nhất | Cần cho tuyến thẻ sô cô la |
| Citizens | 2.0.35+ | Cần cho quest |
| BetonQuest | **2.x** | Cần cho quest |
| MythicMobs | 5.x | Tuỳ chọn |

Kiểm tra Java:

```bash
java -version
```

Phải thấy `21` hoặc cao hơn. Nếu thấy `17` thì plugin sẽ báo `UnsupportedClassVersionError` khi khởi động.

---

## GIAI ĐOẠN 1 — Tạo file .jar

Thư mục này là **mã nguồn**, chưa phải plugin. Phải biên dịch thành `.jar` trước.

Cách nhanh nhất — Windows nhấp đúp `build.bat`, Linux/macOS chạy `./build.sh`.

Hoặc làm tay:

```bash
cd HogwartsStoryCore
mvn clean package
```

Xong sẽ có `target/HogwartsStoryCore-0.3.1.jar` — đây là file chép vào `plugins/`.

**Chưa có Java 21 hoặc Maven, hoặc build bị lỗi** → đọc **TAO-FILE-JAR.md**, trong đó hướng dẫn cài từng bước cho cả Windows lẫn Linux và liệt kê các lỗi build thường gặp.

**Không muốn cài gì lên máy** → đọc **HUONG-DAN-GITHUB.md**, đẩy mã nguồn lên GitHub và để họ build hộ, rồi tải file `.jar` về.

---

## GIAI ĐOẠN 2 — Cài plugin lõi (chưa cài quest)

Cố ý chạy plugin **một mình trước**, để nếu có lỗi thì biết chắc lỗi nằm ở đâu.

1. Chép `HogwartsStoryCore-0.3.1.jar` vào `plugins/`
2. Khởi động lại server (**restart**, không dùng `/reload`)
3. Xem console, phải thấy:

```
[HogwartsStoryCore] Đã nạp 43 câu thần chú.
[HogwartsStoryCore] HogwartsStoryCore đã bật. Chào mừng tới Hogwarts.
```

**Kiểm tra:**

```
/hogwarts
```

Phải hiện hồ sơ học sinh với Nhà là "Chưa phân loại".

Vào server bằng tài khoản mới, chờ 5 giây — con cú phải bay tới và bỏ thư nhập học vào túi đồ.

Sau đó chạy thử toàn bộ chuỗi:

```
/hogwarts wand        → Ollivander thử 3 cây rồi trao đũa
/hogwarts sorting     → Nón Phân Loại hỏi 5 câu
/hogwarts             → hồ sơ phải hiện Nhà và đũa phép
```

Cầm cây đũa (Blaze Rod) lên, **chuột phải** — phải thấy hiệu ứng Lumos.
**Shift + chuột phải** — đổi sang phép khác.

Nếu tới đây mọi thứ chạy đúng thì phần lõi đã ổn.

---

## GIAI ĐOẠN 3 — Phòng Sinh hoạt chung

Làm lần lượt cho từng Nhà. Ví dụ với Gryffindor:

**Bước 1 — đặt cửa vào.** Đứng cách khối cửa (bức tranh, cửa gỗ, khối đá...) **tối đa 6 khối**, nhìn thẳng vào nó rồi gõ:

```
/cr setentrance gryffindor
```

**Bước 2 — đặt điểm vào phòng.** Đi vào bên trong phòng, đứng đúng chỗ muốn người chơi xuất hiện:

```
/cr setspawn gryffindor
```

**Bước 3 — khoanh vùng phòng.** Đứng ở một góc phòng (nhớ tính cả chiều cao):

```
/cr pos1
```

Đi tới góc chéo đối diện:

```
/cr pos2 gryffindor
```

Lặp lại ba bước trên cho `slytherin`, `hufflepuff`, `ravenclaw`. Rồi kiểm tra:

```
/cr status
```

Cả bốn dòng phải có đủ ba dấu ✔.

**Thử nghiệm:** dùng một tài khoản không phải OP (vì OP có quyền `hogwarts.commonroom.bypass` nên đi xuyên được hết, sẽ không thấy hệ thống hoạt động).

| Nhà | Cách vào | Kết quả đúng |
|---|---|---|
| Gryffindor | Chuột phải cửa → gõ mật khẩu vào chat | Tin nhắn không hiện ra chat công cộng |
| Slytherin | Như trên | Bức tường không nói gì, chỉ hiện "..." |
| Hufflepuff | Chuột phải thùng **7 lần liên tiếp** (mỗi lần cách nhau dưới 2 giây) | Gõ lần thứ 8 sẽ bị vòi giấm phun vào mặt |
| Ravenclaw | Chuột phải → trả lời câu đố vào chat | Học sinh Nhà khác trả lời đúng cũng vào được |

Xem mật khẩu Nhà mình:

```
/cr password
```

---

## GIAI ĐOẠN 4 — Cài quest

### 4.1 Cài plugin phụ thuộc

Chép vào `plugins/` rồi **restart**:

```
PlaceholderAPI.jar
Citizens.jar
BetonQuest.jar
Vault.jar + plugin economy (EssentialsX, CMI...)
```

Kiểm tra PlaceholderAPI đã thấy expansion của mình chưa:

```
/papi list
```

Phải có `hogwarts` trong danh sách. Thử:

```
/papi parse me %hogwarts_house%
```

### 4.2 Dựng NPC bằng Citizens

Đứng ở nơi muốn đặt NPC rồi gõ:

```
/npc create Hagrid
```

Ghi lại số ID mà Citizens báo về. Làm tương tự cho **15 NPC**:

| Vai | Tên gợi ý | ID mặc định trong file |
|---|---|---|
| Hagrid | Rubeus Hagrid | 1 |
| Ollivander | Garrick Ollivander | 2 |
| McGonagall | Minerva McGonagall | 3 |
| Flitwick | Filius Flitwick | 4 |
| Snape | Severus Snape | 5 |
| Madam Hooch | Rolanda Hooch | 6 |
| Quirrell | Quirinus Quirrell | 7 |
| Filch | Argus Filch | 8 |
| Neville | Neville Longbottom | 20 |
| Peeves | Peeves | 21 |
| Bà Béo | Bà Béo | 22 |
| Dumbledore | Albus Dumbledore | 23 |
| Gia tinh Bấc | Bấc | 24 |
| Nick Suýt Mất Đầu | Ngài Nicholas | 25 |
| Bà bán hàng | Bà bán hàng | 26 |

Xem lại danh sách bất cứ lúc nào:

```
/npc list
```

### 4.3 Chép package quest

Chép **hai thư mục** vào `plugins/BetonQuest/QuestPackages/`:

```
QuestPackages/hogwarts_year1/
QuestPackages/hogwarts_sidequests/
```

### 4.4 Sửa ID và toạ độ

Mở `plugins/BetonQuest/QuestPackages/hogwarts_year1/package.yml`, sửa hai mục:

```yaml
npcs:
  "1": hagrid        # đổi số 1 thành ID thật của NPC Hagrid
  ...

variables:
  loc_charms: "40;70;20;world"   # x;y;z;tên_world
```

Lấy toạ độ nhanh: đứng đúng chỗ đó rồi bấm **F3**, đọc dòng `XYZ`.

Làm tương tự với `hogwarts_sidequests/package.yml` (ID 20–26).

### 4.5 Nạp quest

```
/q reload
```

Console **không được có dòng nào màu đỏ**. Nếu có, đọc kỹ tên event/condition mà nó báo thiếu — thường là do gõ sai tên NPC trong mục `npcs:`.

### 4.6 Bật cầu nối đếm phép

Đây là bước **cuối cùng**, làm sau khi `/q reload` đã sạch lỗi.

Mở `plugins/HogwartsStoryCore/config.yml`, tìm mục `cast-hooks` ở cuối file và **bỏ dấu `#`** ở ba nhóm dưới:

```yaml
cast-hooks:
  any: []

  wingardium-leviosa:
    - "q event %player% hogwarts_year1.charms_practice_tick"

  lumos:
    - "q event %player% hogwarts_year1.lumos_practice_tick"

  petrificus-totalus:
    - "q event %player% hogwarts_year1.dada_practice_tick"
```

Rồi:

```
/hogwarts reload
```

**Vì sao phải để cuối:** nếu bật cái này khi chưa cài package quest, console sẽ báo lỗi "event not found" mỗi lần có người niệm phép.

---

## GIAI ĐOẠN 5 — Chạy thử toàn bộ

Dùng một tài khoản mới, không OP:

1. Vào server → nhận thư nhập học
2. Nói chuyện với Hagrid → Ollivander → nhận đũa
3. Đi tới `loc_great_hall` → Lễ Phân Loại tự chạy
4. Tới lớp Bùa chú → nói chuyện Flitwick → niệm Wingardium Leviosa 5 lần → quay lại gặp thầy
5. `/hogwarts points` → Nhà bạn phải được cộng điểm
6. `/j` → nhật ký phải có mục "Tiết Bùa chú"

Nếu bước 4 mà đếm không lên, nghĩa là cast-hooks chưa chạy. Kiểm tra `/hogwarts reload` đã chạy chưa, và tên package trong lệnh có đúng `hogwarts_year1` không.

---

## GIAI ĐOẠN 6 — Hướng dẫn cho thành viên

Plugin có sẵn sổ tay trong game cho người mới. Bảo họ gõ:

```
/trogiup
```

Menu hiện ra bấm được trực tiếp trong chat, gồm 8 mục: bắt đầu chơi, đũa phép, thần chú, bốn Nhà, phòng sinh hoạt chung, điểm Nhà, nhiệm vụ và danh sách lệnh. Sổ tay còn tự nhắc việc cần làm tiếp theo tuỳ tiến độ của từng người.

Người chơi mới cũng được nhắc lệnh này ngay khi vào server lần đầu.

Ba lệnh thành viên dùng nhiều nhất:

| Lệnh | Công dụng |
|---|---|
| `/trogiup` | Sổ tay hướng dẫn |
| `/hogwarts` | Hồ sơ học sinh của mình |
| `/cr password` | Mật khẩu phòng Nhà mình |

---

## GIAI ĐOẠN 7 — TAB

Nếu dùng plugin TAB, dán vào cấu hình scoreboard:

```
✦ HOGWARTS ✦
⚜ HỌC SINH
%player%
Nhà       %hogwarts_house_colored%
Năm       %hogwarts_year%
Magic Lv  %hogwarts_level%
✦ MA LỰC
Mana      %hogwarts_mana% / %hogwarts_max_mana%
Tiền      %vault_eco_balance%
♜ ĐIỂM NHÀ
%hogwarts_house%  %hogwarts_points%
```

---

## LỖI THƯỜNG GẶP

**Plugin không bật, console báo `UnsupportedClassVersionError`**
Server đang chạy Java cũ hơn 21.

**`Đã nạp 0 câu thần chú`**
File `plugins/HogwartsStoryCore/spells.yml` bị hỏng. Xoá nó đi rồi restart, plugin sẽ tạo lại file mặc định.

**Chuột phải đũa phép không có gì xảy ra**
Cây đũa phải do plugin cấp (`/hogwarts wand`). Blaze Rod tự lấy từ creative sẽ không hoạt động vì thiếu dữ liệu ẩn bên trong.

**`Con chưa học phép này`**
Đúng như thiết kế. Phép phải học qua lớp, hoặc admin dạy bằng `/hogwarts teach <người chơi> <phép>`.

**Người Nhà khác vẫn vào được phòng**
Chưa khoanh vùng bằng `/cr pos1` + `/cr pos2`, hoặc tài khoản đang test là OP.

**Nón Phân Loại mở GUI rồi tự đóng lại liên tục**
Đúng như thiết kế — không cho bỏ dở giữa chừng. Cứ chọn hết 5 câu là xong.

**Mật khẩu hiện ra chat công cộng**
Có plugin chat khác bắt sự kiện trước. Đặt ưu tiên của plugin đó thấp hơn, hoặc tắt tính năng chat tuỳ chỉnh của nó.

**Console spam `event not found`**
Đã bật `cast-hooks` nhưng chưa cài package quest, hoặc gõ sai tên package.

---

## SAO LƯU

Ba thứ cần sao lưu định kỳ:

```
plugins/HogwartsStoryCore/players/     ← hồ sơ học sinh, mất là mất hết tiến độ
plugins/HogwartsStoryCore/houses.yml   ← điểm Nhà
plugins/BetonQuest/                    ← tiến độ quest
```

Plugin tự lưu mỗi 5 phút và khi tắt server. Nếu tắt server bằng cách kill process thì có thể mất tối đa 5 phút dữ liệu.
