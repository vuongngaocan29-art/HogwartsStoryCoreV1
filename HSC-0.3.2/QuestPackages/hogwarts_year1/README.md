# Hogwarts — Package BetonQuest Năm I

Cốt truyện đầy đủ của năm thứ nhất, nối trực tiếp với HogwartsStoryCore.

## Cài đặt

1. Copy cả thư mục `hogwarts_year1` vào `plugins/BetonQuest/QuestPackages/`
2. Dựng NPC bằng Citizens rồi sửa mục `npcs:` trong `package.yml` cho khớp ID (`/npc list`)
3. Sửa toạ độ trong mục `variables:` của `package.yml` cho khớp bản đồ
4. Mở `plugins/HogwartsStoryCore/config.yml` và giữ nguyên mục `cast-hooks` (đã trỏ sẵn sang package này)
5. `/q reload`

Cần PlaceholderAPI + expansion `hogwarts` (plugin tự đăng ký) để các điều kiện `%ph.hogwarts_house%` hoạt động.

## Mạch truyện

| Chương | NPC | Nội dung | Thưởng |
|---|---|---|---|
| 1. Hẻm Xéo | Hagrid, Ollivander | Nhận thư, mua đũa phép | đũa phép |
| 2. Lễ Phân Loại | (tự động ở Đại Sảnh) | Nón Phân Loại, vào Nhà | +5 điểm, học Lumos |
| 3. Bùa chú | Flitwick | Wingardium Leviosa × 5 | +20 điểm |
| 4. Biến hình | McGonagall | Que diêm → cây kim | +20 điểm |
| 5. Độc dược | Snape | Hái nguyên liệu, nấu Thuốc Trị Nhọt | +20 điểm |
| 6. Lớp Bay | Madam Hooch | Gọi chổi, bay lên 30m | +20 điểm |
| 7. DADA | Quirrell | Petrificus Totalus × 3 | +20 điểm |
| 8. Cấm túc | Filch, Hagrid | Rừng Cấm, kỳ lân, bóng đen | mở khoá thử thách |
| 9. Dưới cửa sập | — | 5 phòng bảo vệ + boss | +50 điểm, lên Năm II |

Thử thách cuối năm chỉ mở khi đã xong **cả 6 lớp học và buổi cấm túc** (điều kiện `du_dieu_kien_thu_thach`).

## Cách đếm số lần niệm phép

HogwartsStoryCore không biết BetonQuest tồn tại. Cầu nối là `cast-hooks` trong config:

```
người chơi niệm Wingardium Leviosa
  → HogwartsStoryCore chạy: q event <player> hogwarts_year1.charms_practice_tick
  → BetonQuest cộng: point charms_practice 1
  → điều kiện du_5_lan_bua_chu kiểm tra point >= 5
```

Muốn thêm phép nào vào việc đếm, chỉ cần thêm một mục trong `cast-hooks` và một event `point` tương ứng.

## Mob cần dựng bằng MythicMobs

Các objective đang tạm dùng mob vanilla có tên. Khi bạn dựng MythicMobs riêng thì đổi lại trong `objectives.yml`:

| Vai | Đang dùng tạm | Nên thay bằng |
|---|---|---|
| Bóng đen trong rừng | `ZOMBIE` tên `Bóng_Đen` | mob trùm áo choàng, bay, hút máu |
| Dây Tơ Hồng | `CAVE_SPIDER` tên `Dây_Tơ_Hồng` | mob bám chân, sợ lửa/ánh sáng |
| Quân cờ vua | `IRON_GOLEM` tên `Quân_Vua_Trắng` | armor stand cầm kiếm |
| Quirrell | `ZOMBIE_VILLAGER` tên `Quirrell` | boss 2 phase, phase 2 lộ mặt sau gáy |
| Fluffy | (chưa có) | chó ba đầu, ngủ khi nghe note block |

## Còn thiếu

- Trận Quidditch giữa các Nhà (định để sang Năm II)
- Thư viện khu Cấm ban đêm
- Gương Ảo Ảnh trong một phòng bỏ trống
