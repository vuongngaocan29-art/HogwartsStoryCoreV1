@echo off
chcp 65001 >nul
echo ============================================
echo   HogwartsStoryCore - Tao file .jar
echo ============================================
echo.

echo [1/3] Kiem tra Java...
java -version 2>&1 | findstr /C:"version" 
if errorlevel 1 (
    echo.
    echo LOI: Khong tim thay Java.
    echo Hay cai JDK 21 tai: https://adoptium.net
    pause
    exit /b 1
)
echo.

echo [2/3] Kiem tra Maven...
call mvn -version >nul 2>&1
if errorlevel 1 (
    echo.
    echo LOI: Khong tim thay Maven.
    echo Hay cai Maven tai: https://maven.apache.org/download.cgi
    echo Roi them thu muc bin cua Maven vao bien moi truong PATH.
    pause
    exit /b 1
)
echo OK
echo.

echo [3/3] Dang build... (lan dau se tai thu vien, mat vai phut)
call mvn clean package

if errorlevel 1 (
    echo.
    echo ============================================
    echo   BUILD THAT BAI
    echo   Doc dong loi mau do o tren de biet nguyen nhan.
    echo ============================================
    pause
    exit /b 1
)

echo.
echo ============================================
echo   XONG!
echo   File .jar nam o: target\HogwartsStoryCore-0.3.1.jar
echo   Chep no vao thu muc plugins/ cua server.
echo ============================================
pause
