# PROJECT_AUDIT — независимый аудит (спека §4)

## 0. Текущее состояние проекта (факты)
Артефакты верифицированы хешами (DEPENDENCIES.md); StaticPatcher воспроизводим; git-репо с baseline; JDK21/27 на месте. Код новой игры: ещё не написан (только пайплайн и документы) — никаких фиктивных «готовностей» не заявляется.

## 1. Цепочка запуска 1.7.10+FML (как есть)
Сейчас: launchwrapper → tweakers (FML) → LaunchClassLoader → рантайм-цепочка трансформеров (AT по fml_at/forge_at, Side-фильтр, деобф-ремаппер FMLDeobfuscatingRemapper c deobfuscation_data-1.7.10.lzma, binpatcher ClassPatchManager c adler32-проверкой) → Forge патчит vanilla-классы в памяти.
**Ограничения**: секундный оверхед старта (lzma+pack200 распаковка на каждый запуск, remap-проход), двойной ASM-проход классов, хрупкость, мусорные пути (realms/twitch).
**Наше решение (D-007)**: всё статически выпекается (binpatches применены — 512 классов; AT — следующий шаг в StaticPatcher); рантайм-трансформеров нет; свои bootstrap/main.
**Замер**: время до меню до/после (benchmark-каркас Phase C).

## 2. JVM/память
Как есть: эпоха Java 8, полный JDK, GC по умолчанию, строковые аллокации в горячих путях (à la java.lang.String повсеместно в ваниль-классах), объектные обёртки массивов.
Альтернативы: primitive-oriented структуры (long-keyed open addressing), пулы, escape-analysis-дружелюбный код, JDK27 функции (AOT cache, jlink), GC-выбор измерением.
**Замер**: JFR allocation rate, GC pauses, RSS.

## 3. Мир/чанки
Как есть 1.7.10: RegionFile 32×32 на регион, Anvil-секции 16³, однопоточный тик мира, свет — BFS пересчёт на чанковых границах в главном потоке,scheduledTick списки.
Ограничения: IO-стопы, свет в тик-потоке, отсутствие dirty-регионов.
Альтернативы: async gen/load/save (worker pools), packed palettes, dirty regions, дельта-BFS свет, бюджеты.
**Замер**: чанки/с, тик p50/p95/p99, время сохранения.

## 4. Рендер
Как есть 1.7.10: display lists + Tessellator на каждый кадр-проход, фиксированный пайплайн, 1 draw call на чанк-слой, ручной frustum culling.
Ограничения: перезагрузка геометрии на изменение блока — целиком чанк; CPU-bound tessellation.
Альтернативы: VBO+persistent mapping, multithreaded meshing, batching по материалу, occlusion.
**Замер**: FPS/1%low/draw calls. GL-возможности iGPU — probe, не предположение.

## 5. Сеть
Как есть: netty 4.0.10, протокол 1.7.10 (варп-фреймы, сжатие zle), клиент-сервер в одном JVM для SP.
Наш вариант: свой TCP-протокол (D-006), AOI, дельты, квантование, backpressure; интернет-сервер с авто-пробросом порта.
**Замер**: байты/с, пакеты/с, размер снапшотов, задержка.

## 6. GT (канон)
Источник: GT5u 5.09.31-снапшот (tag 5.09.33.02, commit 77a3c956). Фактура: 83 machine-класса пути (basic 19, multi 45 — счётчики файлов+каталогов, точная статистика .java в реестре), Materials.java (реестр материалов), OrePrefixes/Tier/ItemList, worldgen: GT_Worldgen_GT_Ore_Layer (жилы), GT_Worldgen_GT_Ore_SmallPieces, GT_UndergroundOil, GT_Pollution, GT_Worldgenerator.
Ограничения подхода «мод»: регистрация через oredictionary/reflection, CI-цепочки на EventBus.
Наш вариант: нативное industrial-engine ядро на сервере (спека §14 1:1), GUI-схемы клиенту через снапшоты.
**Замер**: время обработки машин/энергосети за тик.

## 7. Лицензии/совместимость
Сводка в DEPENDENCIES.md; ключевые риски в RISKS.md (LICENSE-файл GT5u отсутствует в корне — помечено ⚠️).

## 8. Что удалено/вырезано (лог решений)
realms/twitch/httpclient-стек, launchwrapper-цепочка, рантайм-патчер/деобф — план вырезки на S0–S1; факт-подтверждение после вырезки (linkage-тест).
