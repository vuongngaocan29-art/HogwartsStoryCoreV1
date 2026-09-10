#!/usr/bin/env bash
set -e

echo "============================================"
echo "  HogwartsStoryCore - Tạo file .jar"
echo "============================================"
echo

echo "[1/3] Kiểm tra Java..."
if ! command -v java >/dev/null 2>&1; then
    echo "LỖI: Không tìm thấy Java."
    echo "Cài JDK 21:  sudo apt install openjdk-21-jdk"
    exit 1
fi
JAVA_VER=$(java -version 2>&1 | head -n1 | grep -oP '"\K[0-9]+')
echo "Java $JAVA_VER"
if [ "$JAVA_VER" -lt 21 ]; then
    echo "LỖI: Cần Java 21 trở lên, máy đang có Java $JAVA_VER."
    exit 1
fi
echo

echo "[2/3] Kiểm tra Maven..."
if ! command -v mvn >/dev/null 2>&1; then
    echo "LỖI: Không tìm thấy Maven."
    echo "Cài Maven:  sudo apt install maven"
    exit 1
fi
echo "OK"
echo

echo "[3/3] Đang build... (lần đầu sẽ tải thư viện, mất vài phút)"
mvn clean package

echo
echo "============================================"
echo "  XONG!"
echo "  File .jar: target/HogwartsStoryCore-0.3.1.jar"
echo "  Chép nó vào thư mục plugins/ của server."
echo "============================================"
