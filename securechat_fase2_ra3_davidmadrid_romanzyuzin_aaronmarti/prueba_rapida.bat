@echo off
REM ============================================================
REM Prueba Rápida - FASE 4
REM Compila y ejecuta el Mini-Laboratorio AES
REM ============================================================

echo ╔════════════════════════════════════════════════════════╗
echo ║         PRUEBA RÁPIDA - Mini-Laboratorio AES          ║
echo ╚════════════════════════════════════════════════════════╝
echo.

set BASE_DIR=src\main\java\securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti\fase4

echo [1/2] Compilando Mini-Laboratorio...
javac %BASE_DIR%\fita1\*.java

if %ERRORLEVEL% NEQ 0 (
    echo ✗ Error de compilación
    pause
    exit /b 1
)

echo ✓ Compilación exitosa
echo.
echo [2/2] Ejecutando Mini-Laboratorio AES/GCM...
echo.
echo ════════════════════════════════════════════════════════
java securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase4.fita1.MiniLaboratorioAES
echo ════════════════════════════════════════════════════════
echo.
echo Prueba completada
pause
