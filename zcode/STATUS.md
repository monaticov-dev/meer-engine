# STATUS

Обновляется после каждой значимой работы. Дата: 2026-11-10.

## Фаза
Phase A (аудит/консолидация) → Phase B (GT-реестры) → Phase C (benchmark) → Phase D (S0–S10).

## Сделано и верифицировано
- Сеть сборки доступна: Mojang/Forge/Maven/GitHub/Adoptium — HTTP 200.
- Чистый client-1.7.10.jar: SHA1 `e80d9b3bf5085002218d4be59e668bac718abbc6` = piston-meta ✓ (перепроверено 2026-11-10), SHA256 `a4fc2284...9780cce` в DEPENDENCIES.
- Forge 1.7.10-10.13.4.1614 installer вскрыт; universal (976 записей) + binpatches.pack.lzma (527 патчей client) + fml.srg + deobfuscation_data-1.7.10.lzma.
- StaticPatcher: static-merged.jar = vanilla+binpatch+universal overlay, 3744 записи, БЕЗ launchwrapper. Повторный прогон: те же 527/512/898/3744 — воспроизводимо по логике. Побайтовая детерминированность (фикс. timestamps jar) — TODO S0.
- MCP stable-12 (fields/methods/params), Vineflower 1.12.0, SpecialSource 1.11.6, xz 1.9 — скачаны, SHA256 зафиксированы.
- JDK 21.0.13 (runtime/jdk21), JDK 27.0.1 (runtime/jdk27, работает ✓), MinGit 2.47.1 (tools/mingit).
- Git-репо инициализировано (main), baseline commit `e36bf97`.
- GT5u клон tag 5.09.33.02 — в процессе (фон).
- Лицензии референсов: GT5u LGPL-3.0, lwjgl3ify MIT, Angelica LGPL-3.0, Neodymium LGPL-3.0, Beddium LGPL-3.0 (WebSearch; LICENSE-файлы — на проверке в Phase A/B).

## Текущая работа
- zcode-документы (README/STATUS/WORK_LOG/DECISIONS готовятся; ARCHITECTURE/ROADMAP/RISKS/AUDIT/RESEARCH — далее).
- Пиннинг GT 5.07.07 (оригинал закрыт; jar-зеркало ищется; GT5u начальный коммит = декомпил 5.07.07 — подтверждено IC2-тредом).

## Дальше
1. DEPENDENCIES.md + GT_REGISTRY/GT_MATRIX skeleton (Phase B).
2. Benchmark-каркас (Phase C).
3. S0: remap → Vineflower → game-src; bootstrap; запуск пары на JDK 27.

## Открытые вопросы к пользователю
- Нет блокирующих. D-003 (GT baseline 5.09.33.02 вместо несуществующего тега 5.09.31) — отклонение зафиксировано, вето возможна.
