# Hogwarts — Cốt truyện phụ

Bảy tuyến phụ chạy song song với mạch chính, **không cản trở và không bị cản trở**.

## Cách giữ cho nó không đụng mạch chính

| Ranh giới | Cách thực hiện |
|---|---|
| Tag tách biệt | Mọi tag ở đây đều có tiền tố `side_`. Package `hogwarts_year1` không có điều kiện nào đọc tiền tố này. |
| Không dạy phép | Không quest phụ nào chạy `hogwarts teach`. Danh sách phép chỉ do lớp học quyết định. |
| Không đẩy năm học | Không quest phụ nào chạy `hogwarts year next`. |
| Thưởng nhỏ | Tối đa 15 điểm Nhà, so với 20–50 của mạch chính. |
| Không khoá ngược | Điều kiện mở khoá duy nhất là `%ph.hogwarts_sorted%` — đã Phân Loại là làm được, bất kể Năm mấy. |

Xoá cả thư mục này đi thì mạch chính vẫn chạy nguyên vẹn.

## Bảy tuyến

**1. Con cóc Trevor** — Neville lại làm mất cóc. Ra bờ hồ, chuột phải vào con ếch.
*Thưởng: 5 điểm, kẹo Bertie Bott. Làm lại được nhiều lần — Trevor luôn trốn tiếp.*

**2. Peeves** — Con yêu tinh rủ bạn thả bom thối vào thư viện. **Có nhánh:**
- *Hùa theo* → +10 điểm rồi **−10 điểm** khi Bà Bình Bích phát hiện, nhưng Peeves từ đó không phá bạn nữa
- *Đi mách Bà Béo* → +15 điểm và mật khẩu tuần này, nhưng Peeves đổ mực lên đầu bạn

Hai nhánh loại trừ nhau và ghi tag khác nhau — về sau muốn dùng để chia nhánh tiếp thì đã có sẵn.

**3. Khu Cấm** — Lấy trộm sách. Ban ngày bị Bà Bình Bích chặn. Ban đêm có **35% bị Filch tóm** (`chance 35`) → mất 20 điểm. Đây là quest phụ duy nhất có rủi ro thật.

**4. Gương Ảo Ảnh** — Chỉ vào được ban đêm, chỉ làm được **một lần**. Người chơi chọn điều mình khao khát (gia đình / vinh quang / tri thức / bình yên), Dumbledore trả lời khác nhau cho từng lựa chọn và cho một buff nhỏ. Không có đáp án nào đúng hơn đáp án nào.

**5. Nhà bếp** — Cù lét quả lê trong bức tranh để mở cửa. Hái 10 quả dâu cho gia tinh Bấc.
*Thưởng: 10 điểm và **quyền vào bếp vĩnh viễn** — từ đó luôn xin được đồ ăn.*

**6. Nick Suýt Mất Đầu** — Tìm lá thư từ chối của Hội Săn Không Đầu, cất ở tháp cũ từ năm 1492.
*Thưởng: 15 điểm. Đây là tuyến có sức nặng cảm xúc nhất trong bảy tuyến.*

**7. Thẻ Sô cô la Ếch nhái** — Sưu tầm dài hạn chạy suốt 7 năm. Mua sô cô la 5 đồng/thanh từ bà bán hàng, mỗi thanh ra một trong 12 tấm thẻ theo tỉ lệ (Gunhilda và Bowman hiếm, 5% mỗi tấm). Đủ bộ được hộp gỗ + 15 điểm.

## Cài đặt

1. Copy thư mục vào `plugins/BetonQuest/QuestPackages/`
2. Sửa `npcs:` (ID 20–26) và `variables:` trong `package.yml`
3. Cần Vault cho tuyến thẻ sô cô la (`money -5`)
4. `/q reload`

## Gợi ý mở rộng

Tag `side_peeves_branch_help` / `side_peeves_branch_report` đang chưa dùng lại ở đâu. Sang Năm II có thể để Peeves nhớ lựa chọn cũ: nếu từng hùa theo thì nó chỉ đường bí mật, nếu từng mách thì nó cố tình chỉ sai.

Tương tự, `side_mirror_family/glory/knowledge/peace` có thể dùng cho một khoảnh khắc Năm VII, khi nhân vật đối diện lại chính điều mình từng khao khát năm thứ nhất.
