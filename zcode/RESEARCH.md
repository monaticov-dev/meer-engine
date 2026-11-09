# RESEARCH

## Лицензии (проверено WebSearch 2026-11-10; ⚠️ = перепроверить по реальным LICENSE-файлам в Phase B)
- GT5-Unofficial: LGPL-3.0 (README/CurseForge). LICENSE-файла в корне репо НЕТ — противоречие помечено; вывод «LGPL-3.0» пока вторичный источник.
- GregTech 5.07.07: закрытый исходник GregoriusT; GT5u — «decompiled and modified version of GT 5.07.07, approved by GregoriusT» (IC2 forum thread 11488). Значит: оригинал нельзя копировать (нет публичной лицензии); lineage-факты GT5.07.07 получаем из открытых источников/wiki + структуры GT5u.
- lwjgl3ify (GTNH): MIT. Angelica: LGPL-3.0 (+NotFine LGPL, части Forge LGPL-2.1). Neodymium: LGPL-3.0. Beddium: LGPL-3.0 (fork Embeddium/Sodium-линии).
- LWJGL3: BSD-3. Vineflower: dual Apache-2.0/GPL-3 ⚠️. SpecialSource: BSD-3 ⚠️. xz-java: Public Domain. MinGit: GPLv2 (только инструмент). Temurin JDK: GPLv2+CE.
- Правило проекта (D-009): код LGPL-проектов в единый jar не переносится; ассеты — только свои или предоставленные пользователем.

## Технические находки
- Binpatch формат 1.7.10 FML: binpatches.pack.lzma → LZMA → Pack200 unpack → jar с binpatch/<side>/*.binpatch; внутри: UTF name, sourceClass (obf), targetClass (searge), existsAtTarget bool, adler32 int, длина int, GDiff-дельта. Подтверждено декомпилом ClassPatchManager (build/decomp/fml-universal).
- Тег-версия GT5u: build.properties на 5.09.33.02 = gt.version 5.09.31 → пин пользователя = этот снапшот.
- Теги Blood-Asp: серия 5.09.30.01–30.10 → 5.09.33.01/.02; релизы Alk-1/2 поверх (не для канона — фиксируется в реестре аддонов как «непроверенная ветка»).
- Forge installer 1.7.10: --installClient нет (только серверная опция) → клиент ставится статическим пайплайном.
- lzma-0.0.1 с forge-maven = HTML-заглушка 404 → xz-1.9.

## YouTube-ролик (спека «обязательно»)
Все прямые/прокси-маршруты закрыты песочницей (таймаут/403/502, 8 попыток, 2026-11-09/10). Заголовок: «Почему графика Minecraft сложнее, чем кажется?» (найден поиском ID). Тема учтена в S3 (рендер). Субтитры: одна попытка yt-dlp; если сеть не пустит — пользователь может положить .srt в проект.
