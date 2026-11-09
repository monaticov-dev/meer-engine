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
