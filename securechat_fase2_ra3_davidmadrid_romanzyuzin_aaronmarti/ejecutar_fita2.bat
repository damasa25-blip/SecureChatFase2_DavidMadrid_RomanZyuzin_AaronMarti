@echo off
REM ============================================================
REM Script de Ejecución - FASE 4 FITA 2
REM ============================================================

set PACKAGE=securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase4.fita2

:menu
cls
echo ╔════════════════════════════════════════════════════════╗
echo ║      FASE 4 - FITA 2 (RSA + AES + Validaciones)      ║
echo ╚════════════════════════════════════════════════════════╝
echo.
echo Selecciona una opción:
echo.
echo   1. Ejecutar Servidor (Puerto 12349)
echo   2. Ejecutar Cliente
echo   3. Ejecutar Servidor + Cliente
echo   4. Ejecutar Múltiples Clientes (Prueba de carga)
echo   5. Salir
echo.
set /p option="Opción: "

if "%option%"=="1" goto servidor
if "%option%"=="2" goto cliente
if "%option%"=="3" goto ambos
if "%option%"=="4" goto multiples
if "%option%"=="5" goto fin

echo Opción inválida
timeout /t 2 >nul
goto menu

:servidor
cls
echo ════════════════════════════════════════════════════════
echo Iniciando Servidor FITA 2 (Puerto 12349)
echo ════════════════════════════════════════════════════════
echo Cifrado: RSA-2048 + AES-256/GCM
echo Presiona Ctrl+C para detener
echo.
java %PACKAGE%.ServidorEscalable
pause
goto menu

:cliente
cls
echo ════════════════════════════════════════════════════════
echo Iniciando Cliente FITA 2
echo ════════════════════════════════════════════════════════
echo Conectando a localhost:12349
echo Intercambio de claves automático...
echo.
java %PACKAGE%.Client
echo.
pause
goto menu

:ambos
echo Iniciando Servidor en nueva ventana...
start "FITA 2 - Servidor" cmd /k java %PACKAGE%.ServidorEscalable
timeout /t 3 >nul
echo.
echo Iniciando Cliente en nueva ventana...
start "FITA 2 - Cliente" cmd /k java %PACKAGE%.Client
echo.
echo ✓ Servidor y Cliente iniciados
echo ✓ Intercambio de claves RSA en proceso...
echo.
pause
goto menu

:multiples
cls
echo ════════════════════════════════════════════════════════
echo Prueba de Carga - Múltiples Clientes
echo ════════════════════════════════════════════════════════
echo.
set /p num="¿Cuántos clientes? (2-5): "
echo.
echo Iniciando Servidor...
start "FITA 2 - Servidor" cmd /k java %PACKAGE%.ServidorEscalable
timeout /t 3 >nul

for /L %%i in (1,1,%num%) do (
    echo Iniciando Cliente %%i...
    start "Cliente %%i" cmd /k java %PACKAGE%.Client
    timeout /t 1 >nul
)

echo.
echo ✓ Servidor y %num% clientes iniciados
echo ✓ Cada cliente tiene su propia clave AES
echo.
pause
goto menu

:fin
echo.
echo Hasta pronto!
timeout /t 1 >nul
exit
