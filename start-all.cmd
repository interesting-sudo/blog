@echo off
chcp 65001 >nul
setlocal
rem ============================================================
rem  blog 后端一键启动（7 个微服务）
rem  依赖：Nacos 8848 / MySQL 3306 / Redis 6379 / ES 9200 / Seata 8091 需先启动
rem  首次使用前先执行：mvn clean install -DskipTests
rem ============================================================

set "JAVA_HOME=E:\Program Files\Java\jdk17.0_5"
set "ROOT=E:\java_ideaProject\project-practice\blog"
rem 清掉 WorkBuddy 宿主注入的端口变量，否则所有服务会抢同一个端口
set "SERVER__PORT="
set "SERVER__HOST="

rem 加载本地密钥（不存在则使用配置里的开发默认值）
if exist "%ROOT%\config\secrets.local.cmd" call "%ROOT%\config\secrets.local.cmd"

if not exist "%ROOT%\logs" mkdir "%ROOT%\logs"

call :run user-service     8010 512m
call :run blog-service     8020 512m
call :run comment-service  8030 400m
call :run document-service 8040 400m
call :run search-service   8070 450m
call :run ai-service       8060 450m
timeout /t 5 /nobreak >nul
call :run gateway-service  8080 400m

echo.
echo 全部启动完成，网关地址 http://localhost:8080/api
echo 日志目录：%ROOT%\logs
goto :eof

:run
set "SVC=%~1"
set "HEAP=%~3"
echo [start] %SVC%  -Xmx%HEAP%
start "%SVC%" cmd /c ""%JAVA_HOME%\bin\java.exe" -Xmx%HEAP% -Dfile.encoding=UTF-8 -jar "%ROOT%\%SVC%\target\%SVC%-1.0-SNAPSHOT.jar" > "%ROOT%\logs\%SVC%.log" 2>&1"
goto :eof
