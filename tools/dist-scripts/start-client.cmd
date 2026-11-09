@echo off
rem MEER reference package: real Minecraft 1.7.10 client, auto-connects to the server process.
rem Portable: everything relative to this folder. No system dependencies.
cd /d "%~dp0"
if not exist data\client mkdir data\client
jre\bin\java.exe -Xmx1G -Dfile.encoding=UTF-8 --enable-native-access=ALL-UNNAMED -Djava.library.path=natives -cp "app\client-1.7.10.jar;libs\*" net.minecraft.client.main.Main --gameDir data\client --assetsDir assets --assetIndex 1.7.10 --username Player --version 1.7.10 --uuid 00000000-0000-0000-0000-000000000000 --accessToken 0 --userProperties {} --userType legacy --server 127.0.0.1 --port 25565
