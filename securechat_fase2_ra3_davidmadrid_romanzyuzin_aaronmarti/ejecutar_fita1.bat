@echo off
REM ============================================================
REM Script de Ejecución - FASE 4 FITA 1
REM ============================================================

set PACKAGE=securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase4.fita1

:menu
cls
echo ╔════════════════════════════════════════════════════════╗
echo ║           FASE 4 - FITA 1 (AES Precompartido)        ║
echo ╚════════════════════════════════════════════════════════╝
echo.
echo Selecciona una opción:
echo.
echo   1. Ejecutar Mini-Laboratorio AES
echo   2. Ejecutar Servidor (Puerto 12348)
echo   3. Ejecutar Cliente
echo   4. Ejecutar Servidor + Cliente
echo   5. Salir
echo.
set /p option="Opción: "

if "%option%"=="1" goto laboratorio
if "%option%"=="2" goto servidor
if "%option%"=="3" goto cliente
if "%option%"=="4" goto ambos
if "%option%"=="5" goto fin

echo Opción inválida
timeout /t 2 >nul
goto menu

:laboratorio
cls
echo ════════════════════════════════════════════════════════
echo Ejecutando Mini-Laboratorio AES/GCM
echo ════════════════════════════════════════════════════════
echo.
java %PACKAGE%.MiniLaboratorioAES
echo.
pause
goto menu

:servidor
cls
echo ════════════════════════════════════════════════════════
echo Iniciando Servidor FITA 1 (Puerto 12348)
echo ════════════════════════════════════════════════════════
echo Presiona Ctrl+C para detener
echo.
java %PACKAGE%.ServidorEscalable
pause
goto menu

:cliente
cls
echo ════════════════════════════════════════════════════════
echo Iniciando Cliente FITA 1
echo ════════════════════════════════════════════════════════
echo Conectando a localhost:12348
echo.
java %PACKAGE%.Client
echo.
pause
goto menu

:ambos
echo Iniciando Servidor en nueva ventana...
start "FITA 1 - Servidor" cmd /k java %PACKAGE%.ServidorEscalable
timeout /t 2 >nul
echo.
echo Iniciando Cliente en nueva ventana...
start "FITA 1 - Cliente" cmd /k java %PACKAGE%.Client
echo.
echo ✓ Servidor y Cliente iniciados
echo.
pause
goto menu

:fin
echo.
echo Hasta pronto!
timeout /t 1 >nul
exit
