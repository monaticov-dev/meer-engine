# MEER — Minecraft Extreme Engine Rewrite

Собственная реализация игры на базе поведения Minecraft 1.7.10: авторитетный сервер
и лёгкий клиент-терминал, собственный бинарный протокол, нативный GregTech (канон
GT 5.07.07 + GT5u baseline), своя минимальная Java (JDK 27, jlink). Без модов,
без GTNH-пака, без телеметрии. Полностью portable.

## Структура
- `zcode/` — аудит, архитектура, решения, риски, статус, журнал, исследования
- `docs/` — DEPENDENCIES, GT_REGISTRY (реестр GT 1:1), GT_MATRIX (матрица соответствия)
- `src/` — shared / server / client / gt / bootstrap / compat
- `tools/` — сборочные инструменты (StaticPatcher, скрипты; MinGit — локальный, не в git)
- `downloads/` — все внешние артефакты + их SHA256 (изолированно, вне git)
- `runtime/` — JDK 21 (сборочный), JDK 27 (целевой), вне git
- `build/` — промежуточные артефакты; `dist/` — готовые сборки
- `tests/`, `benchmarks/`, `logs/`, `temp/`, `cache/`

## Быстрый статус
См. `zcode/STATUS.md` и `BUILD_REPORT.md`.

## Канон GregTech
GT 5.07.07 (GregoriusT) — первоисточник; GT5u (Blood-Asp) tag `5.09.33.02` —
baseline (тега 5.09.31 не существует — см. zcode/DECISIONS.md D-003).
GTNH — только идеи, происхождение документируется.
