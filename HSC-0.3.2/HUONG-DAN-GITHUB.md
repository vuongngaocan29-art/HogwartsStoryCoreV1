# TẠO FILE .JAR QUA GITHUB

Cách này **không cần cài Java hay Maven** lên máy. GitHub sẽ biên dịch giúp bạn trên máy chủ của họ, bạn chỉ việc tải file `.jar` về.

Miễn phí hoàn toàn với kho lưu trữ công khai (public repository).

---

## BƯỚC 1 — Tạo tài khoản GitHub

Nếu chưa có, đăng ký tại https://github.com/signup — chỉ cần email.

---

## BƯỚC 2 — Tạo kho lưu trữ mới

1. Vào https://github.com/new
2. **Repository name:** gõ `HogwartsStoryCore`
3. Chọn **Public**

   > Chọn Private cũng được, nhưng tài khoản miễn phí bị giới hạn 2000 phút build mỗi tháng. Public thì không giới hạn.

4. **Không** tích ô "Add a README file"
5. Bấm **Create repository**

---

## BƯỚC 3 — Tải mã nguồn lên

Đây là bước dễ sai nhất, nên làm theo đúng thứ tự.

1. Ở trang kho vừa tạo, bấm dòng chữ **uploading an existing file**
   (nằm ở dòng "…or upload an existing file")

2. Mở thư mục `HogwartsStoryCore-0.3` trên máy

3. Chọn **toàn bộ** thứ bên trong — `src`, `QuestPackages`, `.github`, `pom.xml`, và các file `.md` — rồi kéo thả vào trang web

   > **Quan trọng:** kéo thả *nội dung bên trong* thư mục, không phải kéo cả thư mục `HogwartsStoryCore-0.3`. Nếu kéo cả thư mục, file `pom.xml` sẽ nằm sâu một cấp và GitHub sẽ không build được.

   > **Nếu không thấy thư mục `.github`:** nó bị ẩn vì tên bắt đầu bằng dấu chấm.
   > - Windows: mở File Explorer → thẻ **View** → tích ô **Hidden items**
   > - macOS: bấm `Cmd + Shift + .`

4. Kéo lên xong, cuộn xuống dưới bấm **Commit changes**

---

## BƯỚC 4 — Chờ GitHub build

1. Bấm thẻ **Actions** ở đầu trang
2. Sẽ thấy một dòng đang chạy, có vòng tròn màu vàng quay quay
3. Chờ khoảng **1–2 phút**
4. Vòng tròn chuyển thành **dấu tích xanh** ✅ là xong

Nếu ra **dấu X đỏ** ❌, bấm vào dòng đó để đọc lỗi. Xem phần "Lỗi thường gặp" ở cuối trang.

---

## BƯỚC 5 — Tải file về

1. Bấm vào dòng build vừa xong
2. Cuộn xuống cuối trang, phần **Artifacts**
3. Sẽ có hai mục:

| Tên | Nội dung |
|---|---|
| `HogwartsStoryCore` | Chỉ file `.jar` |
| `HogwartsStoryCore-day-du` | File `.jar` + package quest + hướng dẫn |

4. Bấm vào mục muốn tải, GitHub sẽ tải về một file `.zip`
5. Giải nén ra, lấy file `HogwartsStoryCore-0.3.1.jar` bên trong

Chép file `.jar` đó vào thư mục `plugins/` của server, khởi động lại. Xong.

> Artifact chỉ được giữ **90 ngày**. Cần lâu hơn thì tải về máy tự lưu.

---

## LẦN SAU SỬA CODE

Mỗi lần bạn sửa file trên GitHub và bấm **Commit changes**, GitHub tự build lại. Chỉ cần vào thẻ Actions chờ 2 phút rồi tải bản mới.

Muốn build lại mà không sửa gì: vào **Actions** → chọn **Tạo file plugin** ở cột trái → bấm **Run workflow**.

---

## LỖI THƯỜNG GẶP

**Thẻ Actions trống, không có gì chạy**
Thư mục `.github` chưa được tải lên. Nó bị ẩn nên hay bị bỏ sót. Bật hiển thị file ẩn rồi tải lại. Kiểm tra bằng cách vào kho lưu trữ, phải thấy đường dẫn `.github/workflows/build.yml`.

**`Could not find or load main class` / `no pom in this directory`**
Bạn kéo nhầm cả thư mục lên. Ở trang chính của kho, file `pom.xml` phải nằm ngay ngoài cùng, không nằm trong thư mục con nào. Nếu sai, xoá hết đi rồi tải lại theo Bước 3.

**`invalid target release: 21`**
File `build.yml` bị sửa nhầm phần `java-version`. Nó phải là `'21'`.

**`Could not resolve dependencies ... paper-api`**
Phiên bản Paper trong `pom.xml` không tồn tại. Mở https://repo.papermc.io/repository/maven-public/io/papermc/paper/paper-api/ xem danh sách rồi sửa dòng `<version>` trong `pom.xml` cho khớp.

**Build xanh nhưng không thấy Artifacts**
Cuộn xuống tận cuối trang chi tiết của lần build đó. Mục Artifacts nằm dưới cùng, dễ bị bỏ qua.

---

## CÁCH KHÁC — Codespaces

Nếu muốn gõ lệnh trực tiếp mà không cài gì lên máy:

1. Ở trang kho lưu trữ, bấm nút **Code** màu xanh
2. Chọn thẻ **Codespaces** → **Create codespace on main**
3. Chờ khoảng một phút, một cửa sổ giống VS Code sẽ mở ra ngay trong trình duyệt
4. Ở ô terminal phía dưới, gõ:

```bash
mvn clean package
```

5. Xong, file nằm ở `target/HogwartsStoryCore-0.3.1.jar` — nhấp chuột phải vào nó ở cột trái, chọn **Download**

Tài khoản miễn phí được 60 giờ Codespaces mỗi tháng, thừa sức dùng.
