# BUILD_REPORT — фактическое состояние сборок и тестов (только факты)

## ГОТОВО К ИГРЕ ПРЯМО СЕЙЧАС (2026-11-10)
**`dist\minecraft-1.7.10-real\`** — настоящий Minecraft 1.7.10, portable, два процесса:
1. `start-server.cmd` → vanilla dedicated server (JDK 27 внутри пакета, порт 25565, world в `data\world`). Проверено: `Done (3,228s)!`
2. `start-client.cmd` → vanilla клиент **с автоподключением** (`--server 127.0.0.1 --port 25565`). Проверено: лог сервера `Player joined the game`, скриншот `benchmarks/real-minecraft-joined.png` (мир с текстурами, мобы, hotbar).
- Внутри: `app\` (оба jar, SHA1=piston-meta ✓), `libs\` (31), `natives\`, `jre\` (своя Java 27 — ничего ставить не нужно), `assets\` (674 объекта, 107 МБ звуки/музыка), `data\` (миры/логи), `server.properties` (online-mode=false, оффлайн-режим), eula принята.
- Интернет-игра: на сервере пробрось 25565, в `start-client.cmd` поменяй `--server` на внешний IP (D-006 auto-UPnP появится в собственном движке).
- Скрипты пакета в git: `tools/dist-scripts/`. Папка `dist\` вне git (бинарные артефакты Mojang, личное использование).

## Собственный движок MEER (проверенные вехи)
| Веха | Факт | Commit |
|---|---|---|
| M0 | vanilla 1.7.10 client+server на JDK 27 (титул-скриншот; сервер Done 3,0s) | 2f9990c |
| S1-proto | свой GLFW 3.3.6-клиент (61 fps, 506 ms), свой сервер bind 0.0.0.0+JOIN_ADDRESS | 42b0bd9 |
| M1-core | свой протокол v1 → серверный мир 128×64×128 → рендер снапшота в своём клиенте (скриншот) | 7610e8e |
| HW probe (D-012) | по факту: AMD Radeon iGPU, **GL 3.3.0 Core** (26.5.2) — modern-core путь под 3.3+, не 4.6 | — |

## Baseline-узкие места (измерены, цели S2/S3)
- 14,011,764 вершин в одном draw call → 20–38 FPS iGPU (чанковые VBO+culling дадут рост).
- Снапшот = полный мир 1 МиБ (дельты/AOI снизят трафик).

## GT 1:1 (реестр, скан #1–2, снапшот 77a3c956 = gt.version 5.09.31)
- Материалов **726**, рецептурных карт **59** (GT_RecipeAdder), жил-конструкторов **68**, машин **80** (multi 45/basic 19/steam 12) + генераторы 9 + storage 3; 626 java-файлов. Таблицы: `docs/gt/`. GT_MATRIX: 0% IMPL (честно) — реализация волнами с S6.

## Пайплайн сборки (все шаги воспроизводимы)
- Deobf: SrgTools (lzma→joined.srg→deobf.srg CL=1836/FD=6900/MD=13263) → Vineflower --deobfuscation-map → `build/decomp/game-client|game-server` (оракул).
- StaticPatcher: static-merged.jar (512 binpatch + 898 overlay, 3744 записи, без launchwrapper), воспроизводим; jar-детерминизм (фикс-таймстампы) — TODO.
- Специальное: SpecialSource снят с крит-пути (D-013); ассеты 686 записей → 674 уникальных хеша = 100% скачано.

## Что НЕ сделано (честно)
- Собственный движок ещё не заменяет Minecraft: интерактив (блоки/инвентарь), GT-реализация (0%), jlink-минимизация (текущий jre = полный JDK 27), сервер-пакет на реальном Anvil-мире с GT.
- Ролик YouTube: сеть закрыта; субтитры не получены.
