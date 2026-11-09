# WORK LOG

Только значимые записи. Формат: дата — что сделано — результат/факты.

## 2026-11-09
- Проба среды: java 8 Temurin (системная), curl/tar/powershell; git в системе ОТСУТСТВУЕТ → принят portable MinGit.
- Сеть: Mojang/Forge/Maven/GitHub/Adoptium — 200. YouTube-эндпоинты закрыты из песочницы (таймаут/403/502 на 8 маршрутах); ролик недоступен — заголовок найден через поиск («Почему графика Minecraft сложнее, чем кажется?»), субтитры не получены; попытка yt-dlp запланирована.
- Загрузки с верификацией: version_manifest → 1.7.10 client.jar (SHA1 = piston-meta ✓); Forge 1.7.10-10.13.4.1614 installer; MCP stable-12 csv; Vineflower 1.12.0; SpecialSource 1.11.6; xz 1.9; lzma-0.0.1 с forge-maven оказался HTML-заглушкой (404) → удалён, заменён xz.
- JDK 21.0.13 скачан/распакован; Forge installer headless: опции --installClient нет (только серверная) → решение: свой статический пайплайн.
- Декомпиляция FML universal Vineflower — ОК (976 классов).
- Вскрыт формат binpatch (ClassPatchManager): LZMA→Pack200→jar→GDiff; adler32 входа; existsAtTarget. Написан StaticPatcher (build/tools→tools/): статическая выпечка vanilla+binpatch+universal в один jar без launchwrapper.
- Первый прогон: 15 lib-resident skip (realms, MinecraftServer, ClientBrandRetriever и др.), 512 запатчено, 898 overlay, 3744 записи. static-merged.jar готов.
- Требование пользователя: JDK 27 «самая новейшая» → Adoptium: доступен 27 GA → скачан. GT на 1.2.5 не существует → пользователь выбрал базу 1.7.10; позже зафиксировано: полный форк, свой сервер+клиент, GT нативно.

## 2026-11-10
- Спецификация §1–16 принята как мастер-документ; план v5 утверждён.
- Инвентаризация файлов, SHA1/SHA256 всех артефактов (tools/hash.ps1): client SHA1 повторно сверён ✓; все хеши в DEPENDENCIES.md.
- MinGit 2.47.1 скачан (downloads/mingit.zip, 47,2 МБ) → tools/mingit; git работает.
- JDK 27.0.1.0.2+1 распакован → runtime/jdk27; `java -version` ✓.
- Реструктуризация корня per план (zcode/docs/src/tools/runtime/cache/temp/logs/tests/benchmarks/dist/build).
- StaticPatcher повторный прогон: идентичные счётчики (527/512/898/3744) — логика воспроизводима.
- git init -b main; .gitignore (downloads/runtime/cache/temp/logs/build/tools/mingit, *.log); baseline commit e36bf97.
- GT5u: полный список тегов получен по git-протоколу — тега 5.09.31 НЕТ; серия 30.01–30.10, затем 33.01/33.02 (финал). Releases.atom: свежие = Alk-2, Alk-1, 5.09.30.10… → пиннинг: tag 5.09.33.02 как baseline (девиация от «5.09.31» зафиксирована в DECISIONS D-003).
- Спецификация §17 (GTNH = источник идей) и §18 (полностью свой клиент-терминал) + интернет-сервер (bind 0.0.0.0, авто-проброс порта, печать IP:порт в консоль) — приняты, внесены в DECISIONS D-004/D-005/D-006.
- Клон GT5u tag 5.09.33.02 запущен (фон).

## 2026-11-10 (продолжение) — S0, M0 GREEN
- GT5u клон завершён: commit 77a3c956e510b7c8ef4654054bcb16a0fccb86f7, 7824 файла; build.properties на теге: gt.version=5.09.31 → пин пользователя совпадает с снапшотом (D-003 закрыт как «совпадение по версии-строке»).
- Скачан server-1.7.10.jar (9,6 МБ, SHA1 952438ac4e01b4d115c5fc38f891710c4941df29 = piston-meta ✓).
- joined.srg распакован из deobfuscation_data-1.7.10.lzma (2 МБ); формат КОМПАКТНЫЙ (пробелы, без стрелок) — первый парсер (arrow) чинился дважды (guards, потом полный rewrite под компакт). Итог deobf.srg (arrow, для Vineflower): CL=1836 FD=6900 MD=13263 PK=7, skip=0; имена человеческие (getMapColor, canProvidePower...).
- SpecialSource 1.11.6 падает на дескриптор-парсинге MCP-srg ("Unrecognized type in method descriptor: ;") → снят с критического пути (D-013).
- Vineflower декомпиляция client+server с --deobfuscation-map: build/decomp/game-client, game-server (директории исходников net/...) ✓.
- getlibs.ps1: первый прогон скачал 0 (отсутствие url-поля) → фикс дефолт URL; итог 29/33 + 2 natives-jar вручную; 4 natives-only 404 — не критичны.
- Natives распакованы (lwjgl64/OpenAL64/jinput-dx8/raw/wintab) в runtime/natives.
- **M0-СЕРВЕР GREEN**: vanilla 1.7.10 dedicated server на JDK 27, "Done (3,016s)!", world/ создан. Первый запуск был с eula=false (сервер сам перезаписал файл) → Set-Content eula=true.
- **M0-КЛИЕНТ GREEN**: титульный экран рендерится (скриншот benchmarks/M0-title-screen.png): LWJGL 2.9.1 + атласы + звук + меню. Требуются флаги: --enable-native-access=ALL-UNNAMED, полный набор аргументов (--uuid/--accessToken/--userProperties/--userType). Twitch-ошибка безопасна (вырежем).
- Клиент процесс: 458 МБ RSS на -Xmx1G (замер зафиксирован).

## 2026-11-10 (продолжение 2) — S1-прототип GREEN
- GitHub: репо создан monaticov-dev/meer-engine (приватный), remote через токен, main запушен (7307663). Токен пользователя в истории чата — отозвать/ротировать (уведомлён).
- LWJGL3: metadata-листинг Central нестабилен в этой среде → HEAD-пробы: 3.3.6 доступна (3.3.7 нет). Скачано 6 jar (core/glfw/opengl + natives-windows) в runtime/libs3.
- GT5u скан #1 (фактура для реестра): tileentities/machines 80 .java (multi 45, basic 19, steam 12), generators 9, storage 3; всего 626 .java в src/main/java.
- Свой bootstrap написан и работает (src/bootstrap/net/meer/boot/): BootClient (GLFW 1280x720, vsync, loop 61 FPS, ESC-exit), BootServer (bind 0.0.0.0:port, LAN-IP перечисление, --public-ip, JOIN_ADDRESS в консоль, framed ping/pong, guard длины кадра), BootPing (тест-клиент, rtt_us=24368 первый хоп).
- HW probe по факту (D-012): cpu_cores=12, ram 15772 МБ, GPU = AMD Radeon Graphics, **GL 3.3.0 Core Profile**, драйвер 26.5.2 → modern-core путь строим под 3.3+ (не 4.6: драйвер не даёт), compat-фолбэк всё равно закладывается.
- Окно готово за 506 мс. Скриншот окна не удался (фокус) — лог-фактура полная; визуальное подтверждение окна переносим в M1 (там рендер сцены важнее).
- Специальные замечания JDK 27: Unsafe-warning от LWJGL (deprecated) — для финальной сборки jlink-рантайма это учтём (флаги/редакция загрузки нативов), сейчас не мешает.

## 2026-11-10 (продолжение 3) — M1-core GREEN
- B2 волна #2 (реестр): **материалов 726** (Materials.java), **59 adder-методов** рецептов (GT_RecipeAdder, gregtech/common), 92 map-строки в GT_Recipe.java, **68 ctor-вызовов жил** (layers+small) в GT_Worldgenloader.java. Таблицы в docs/gt/ (machines.csv 88 строк, materials.txt 726, recipeadder_methods.txt 59, loaders.txt).
- S1-core написан и проверен: src/shared/net/meer/proto/Proto.java (framing v1: [len][type][payload], MAX_FRAME guard), src/server/.../ServerMain.java (авторитетный мир 128×64×128, генерация heightmap, bind 0.0.0.0, LAN IPs, --public-ip JOIN_ADDRESS, virtual threads на соединения, HELLO→WELCOME→CHUNK full snapshot), src/client/.../ClientMain.java (GL 3.3 core renderer, шейдеры, MVP, fly-камера WASD+мышь, face culling по соседям).
- Баги и фиксы (для истории): (1) серверный hello-чек 7→8 байт; (2) клиентский парс WELCOME/CHUNK — ручные офсеты били мимо type-байта → переход на потоковый разбор.
- **M1-core результат**: клиент получил снапшот 128×64×128 (1 МиБ) от сервера и отрендерил его — скриншот benchmarks/M1-world-render.png (зелёные холмы в окне «MEER client»). Клиент ноль симуляции (D-005 соблюдён: display-only + MOVE-intent).
- Baseline-узкие места зафиксированы (для S2/S3): 14,011,764 вершин в одном draw call (наивный меш), 20–38 FPS на iGPU без frustum/occlusion culling; snapshot = полный мир 1 МиБ без дельт/AOI.
- Git push: monaticov-dev/meer-engine main.

## 2026-11-10 (продолжение 4) — REAL package + pivot к старейшим версиям
- dist\minecraft-1.7.10-real собран и ПРОВЕРЕН ИГРОЙ: сервер Done 3,228s; клиент автоподключился (--server/--port), «Player joined the game», скриншот real-minecraft-joined.png (текстуры, свиньи, hotbar). Ассеты 1.7.10: 686 записей индекса → 674 уникальных хеша — скачаны все (107 МБ). Инцидент git: dist-бинарники попали в индекс → коммит переписан (reset --soft), push отклонён GitHub по 139МБ jre-модулю до переписи — на GitHub чисто (3ddc521).
- Запрос пользователя: «меняем в корне все», все версии до 1.5.2, самая младшая, папки на Desktop\min, portable, только релизы.
- Выдан список: 84 версии rd-132211…1.5.2; после «только релизы» — 16 релизных папок (1.0…1.5.2) на Desktop\min\ (нерелизные удалены).
- Самая младшая релизная = **1.0 (2011-11-17)**: client.jar 2,36 МБ SHA1=b679fea2…✓; libs 9 (lwjgl 2.9.0, jinput, launchwrapper 1.5 — не нужен: в jar есть прямой main у net.minecraft.client.Minecraft); natives извлечены; подвох найден: lwjgl_util-2.9.0.jar на libraries.minecraft.net = артефакт с подчёркиванием (lwjgl-util… = HTML 404, 215 байт) → NoClassDefFoundError GLU → исправлено.
- **Minecraft 1.0 запущена portable** (`Desktop\min\1.0\start.cmd`, общая `min\_jre\` = JDK 27): 27 achievements / 174 recipes / OpenAL ок / процесс 570 МБ / скриншот пауза-меню в мире (minecraft-1.0-running.png). s3.amazonaws.com skin-404 — безвреден (dead CDN).
- D-015 зафиксирован: база = 1.0 release; GT-трек заморожен до решения пользователя (GT физически невозможен на 1.0).
