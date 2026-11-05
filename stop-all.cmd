@echo off
chcp 65001 >nul
rem ============================================================
rem  blog 后端一键停止（只杀本项目的 7 个服务，不动 Nacos/ES/Seata）
rem ============================================================
powershell -NoProfile -Command "Get-CimInstance Win32_Process -Filter \"Name='java.exe'\" | Where-Object { $_.CommandLine -like '*blog\\*-service*SNAPSHOT.jar*' } | ForEach-Object { Write-Host ('stop ' + $_.ProcessId); Stop-Process -Id $_.ProcessId -Force }"
echo 已停止。
