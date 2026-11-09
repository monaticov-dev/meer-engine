# BUILD_REPORT — фактическое состояние сборок и тестов (только факты)

## Что реально существует и проверено (2026-11-10)
1. **Артефакты** (DEPENDENCIES.md): client-1.7.10.jar SHA1=piston-meta ✓; Forge installer; MCP-маппинги; Vineflower/SpecialSource/xz; JDK21/27; MinGit.
2. **StaticPatcher** (tools/StaticPatcher.java): static-merged.jar 3744 записи = vanilla 2847 + binpatch (512 классов, adler32 OK) + universal overlay 898. Без launchwrapper. Повторный прогон — идентичные счётчики. Побайтовая детерминированность — TODO (фикс-таймстампы jar, S0).
3. **GT5u-снапшот**: tag 5.09.33.02, commit 77a3c956e510b7c8ef4654054bcb16a0fccb86f7, gt.version=5.09.31 (пин пользователя), 7824 файла. Первый скан: tileentities 111, machines 83 (multi 45, basic 19), Materials/OrePrefixes/Tier, worldgen-жилы.
4. **Git**: main, baseline e36bf97; документы фазы A закоммичены.
5. **Запуск игры**: НЕ СОБРАН ещё — S0 в работе (remap → decompile → bootstrap). Никаких заявлений о запускаемости без факта.

## Что НЕ сделано (честно)
- game-src (деобф-исходники) — S0.
- Собственные клиент/сервер — S1+.
- GT-реализация — S6 (волны); GT_MATRIX: 0% по всем категориям.
- Benchmark-каркас — Phase C.
- jlink/AOT — S8. Portable-сборка — S10.

## Сборка/запуск (по мере готовности будет дополняться)
- Команды пересборки static-merged.jar: javac/java 8 (Pack200) + universal-x + xz.jar.
- Сборочные скрипты консолидируются в tools/build.cmd (TODO S0).
