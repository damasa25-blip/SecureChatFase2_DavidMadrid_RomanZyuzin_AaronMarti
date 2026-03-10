@echo off
REM ============================================================
REM Script de Compilación - FASE 4 SecureChat
REM ============================================================

echo ╔════════════════════════════════════════════════════════╗
echo ║     Compilando FASE 4 - SecureChat 2.0                ║
echo ╚════════════════════════════════════════════════════════╝
echo.

REM Directorio base
set BASE_DIR=src\main\java\securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti\fase4

echo [1/3] Compilando FITA 1 (AES con clave precompartida)...
javac %BASE_DIR%\fita1\*.java
if %ERRORLEVEL% EQU 0 (
    echo ✓ FITA 1 compilada exitosamente
) else (
    echo ✗ Error al compilar FITA 1
    exit /b 1
)
echo.

echo [2/3] Compilando FITA 2 (Intercambio RSA + Validaciones)...
javac %BASE_DIR%\fita2\*.java
if %ERRORLEVEL% EQU 0 (
    echo ✓ FITA 2 compilada exitosamente
) else (
    echo ✗ Error al compilar FITA 2
    exit /b 1
)
echo.

echo [3/3] Verificando Mini-Laboratorio...
if exist "%BASE_DIR%\fita1\MiniLaboratorioAES.class" (
    echo ✓ Mini-Laboratorio disponible
) else (
    echo ⚠️ Mini-Laboratorio no encontrado
)
echo.

echo ════════════════════════════════════════════════════════
echo ✓ Compilación completada exitosamente
echo ════════════════════════════════════════════════════════
echo.
echo Opciones disponibles:
echo   1. Ejecutar Mini-Laboratorio AES
echo   2. Ejecutar Servidor FITA 1
echo   3. Ejecutar Cliente FITA 1
echo   4. Ejecutar Servidor FITA 2
echo   5. Ejecutar Cliente FITA 2
echo.
echo Ver README_FASE4.md para más información
echo.

pause
