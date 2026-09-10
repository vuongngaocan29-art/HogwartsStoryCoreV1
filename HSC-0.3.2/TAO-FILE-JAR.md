# CÁCH TẠO FILE .JAR

Thư mục bạn đang có là **mã nguồn**, không phải plugin. Máy chủ Minecraft không đọc được file `.java` — phải biên dịch chúng thành một file `.jar` trước.

Việc này chỉ làm **một lần**. Sau đó mỗi khi sửa code thì chạy lại đúng một câu lệnh.

---

## CÁCH 0 — Không cài gì cả, để GitHub build hộ

Nếu bạn không muốn cài Java và Maven lên máy, có thể đẩy mã nguồn lên GitHub và để họ biên dịch giúp, rồi tải file `.jar` về. Miễn phí, mất khoảng 5 phút.

Xem hướng dẫn riêng: **HUONG-DAN-GITHUB.md**

---

## CÁCH 1 — Dùng script có sẵn (nhanh nhất)

**Windows:** nhấp đúp vào `build.bat`

**Linux / macOS:**
```bash
chmod +x build.sh
./build.sh
```

Script sẽ tự kiểm tra Java và Maven, báo cho bạn biết thiếu cái gì. Nếu chạy được tới cuối, file `.jar` sẽ nằm ở `target/HogwartsStoryCore-0.3.1.jar`.

Nếu script báo thiếu Java hoặc Maven thì đọc tiếp phần dưới.

---

## CÁCH 2 — Làm thủ công từng bước

### Bước 1 — Cài JDK 21

Cần **JDK** (bộ công cụ lập trình), không phải JRE (bộ chạy chương trình). Server Minecraft chỉ cần JRE, nhưng để biên dịch thì phải có JDK.

**Windows / macOS:** tải bản **Temurin JDK 21 (LTS)** tại https://adoptium.net — chọn đúng hệ điều hành, tải file cài đặt rồi bấm Next tới hết.

**Ubuntu / Debian:**
```bash
sudo apt update
sudo apt install openjdk-21-jdk
```

**Kiểm tra:**
```bash
javac -version
```

Phải hiện `javac 21.x.x`. Nếu báo "không tìm thấy lệnh" nghĩa là chưa cài đúng JDK, hoặc trên Windows là chưa thêm vào PATH.

> Lưu ý cho Windows: nếu gõ `java -version` thì được nhưng `javac -version` thì không, nghĩa là máy bạn chỉ có JRE. Phải cài JDK.

### Bước 2 — Cài Maven

Maven là công cụ tự tải thư viện và gộp code thành file `.jar`.

**Ubuntu / Debian:**
```bash
sudo apt install maven
```

**macOS** (nếu có Homebrew):
```bash
brew install maven
```

**Windows:**
1. Tải bản **Binary zip archive** tại https://maven.apache.org/download.cgi
2. Giải nén ra, ví dụ `C:\maven`
3. Mở menu Start, gõ "biến môi trường" → **Chỉnh sửa biến môi trường hệ thống**
4. Bấm **Biến môi trường** → chọn dòng `Path` ở khung dưới → **Chỉnh sửa** → **Mới**
5. Dán vào: `C:\maven\bin`
6. Bấm OK hết, rồi **mở lại cửa sổ Command Prompt** (cửa sổ cũ không nhận biến mới)

**Kiểm tra:**
```bash
mvn -version
```

Phải hiện `Apache Maven 3.x` và dòng `Java version: 21`.

> Nếu dòng `Java version` hiện số khác 21, Maven đang dùng nhầm Java. Đặt biến môi trường `JAVA_HOME` trỏ vào thư mục JDK 21.

### Bước 3 — Build

Mở terminal (Windows: Command Prompt), đi vào thư mục chứa file `pom.xml`:

```bash
cd đường/dẫn/tới/HogwartsStoryCore
```

> Mẹo trên Windows: mở thư mục đó trong File Explorer, gõ `cmd` vào thanh địa chỉ rồi Enter — sẽ mở Command Prompt ngay tại thư mục đó.

Chạy:

```bash
mvn clean package
```

**Lần đầu sẽ mất 2–5 phút** vì Maven phải tải toàn bộ thư viện Paper về máy. Những lần sau chỉ mất khoảng 10 giây.

Chạy xong phải thấy:

```
[INFO] BUILD SUCCESS
```

### Bước 4 — Lấy file .jar

File nằm ở:

```
target/HogwartsStoryCore-0.3.1.jar
```

Chép **đúng file này** vào thư mục `plugins/` của server, rồi khởi động lại server.

> Trong thư mục `target/` còn có file `original-HogwartsStoryCore-0.3.1.jar`. **Đừng dùng file đó** — nó là bản chưa gộp thư viện.

---

## LỖI HAY GẶP KHI BUILD

**`invalid target release: 21`**
Maven đang dùng Java cũ hơn 21. Chạy `mvn -version` để xem nó đang dùng bản nào, rồi đặt `JAVA_HOME` trỏ vào JDK 21.

Linux/macOS:
```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
mvn clean package
```

Windows (trong Command Prompt):
```
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21
mvn clean package
```

**`Could not resolve dependencies for project ... paper-api`**
Máy không tải được thư viện Paper. Ba nguyên nhân thường gặp:
- Không có mạng, hoặc mạng chặn `repo.papermc.io`
- Phiên bản `paper-api` trong `pom.xml` không tồn tại. Mở https://repo.papermc.io/repository/maven-public/io/papermc/paper/paper-api/ xem danh sách có sẵn rồi sửa dòng `<version>` cho khớp
- Đang sau proxy công ty → cần cấu hình proxy cho Maven trong `~/.m2/settings.xml`

**`package io.papermc.paper.event.player does not exist`**
Phiên bản `paper-api` bạn đặt quá cũ. Plugin này cần từ 1.19 trở lên.

**`cannot find symbol: variable MAX_HEALTH`**
Cần Paper 1.21.3 trở lên (trước đó thuộc tính này tên `GENERIC_MAX_HEALTH`).

**Build xong nhưng server báo `UnsupportedClassVersionError`**
Server đang chạy Java thấp hơn 21. Nâng Java của server lên, hoặc hạ `maven.compiler.source/target` trong `pom.xml` xuống bằng Java của server.

---

## SỬA CODE RỒI BUILD LẠI

Mỗi lần sửa file `.java`:

```bash
mvn clean package
```

Rồi chép lại file `.jar` mới vào `plugins/` và **restart server** (không dùng `/reload` — nó hay gây lỗi khó hiểu).

Riêng các file `.yml` trong `plugins/HogwartsStoryCore/` thì không cần build lại, chỉ cần:

```
/hogwarts reload
```

---

## PHIÊN BẢN PAPER

`pom.xml` hiện đang để:

```xml
<version>1.21.11-R0.1-SNAPSHOT</version>
```

Nếu bạn đổi sang phiên bản Paper khác, sửa dòng này cho khớp. Danh sách phiên bản có sẵn xem tại:
https://repo.papermc.io/repository/maven-public/io/papermc/paper/paper-api/

Không cần khớp tuyệt đối — biên dịch bằng 1.21.4 vẫn chạy được trên 1.21.11. Nhưng khớp đúng thì an toàn nhất, vì trình biên dịch sẽ báo ngay nếu có API nào bị đổi.
