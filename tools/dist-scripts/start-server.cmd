@echo off
rem MEER reference package: real Minecraft 1.7.10 dedicated server (two-process architecture).
rem Portable: everything relative to this folder. No system dependencies.
cd /d "%~dp0"
if not exist data mkdir data
jre\bin\java.exe -Xms512M -Xmx1G -Dfile.encoding=UTF-8 --enable-native-access=ALL-UNNAMED -cp "app\server-1.7.10.jar;libs\*" net.minecraft.server.MinecraftServer nogui
