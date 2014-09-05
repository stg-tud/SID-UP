@echo off
IF EXIST tmp\ rd /s /q tmp
SET JOPTS=-Xms4G -Xmx4G
benchmark\target\start.bat
pause
