# DEPENDENCIES — реестр артефактов (версия / SHA256 / лицензия / происхождение)

Статус лицензии: ✅ проверено (реальный LICENSE/первичный источник) | ⚠️ вторичный источник/предположение.

## Игровое ядро (личное использование; распространение запрещено)
| Артефакт | Версия | SHA1 | SHA256 | Лицензия | Происхождение |
|---|---|---|---|---|---|
| client-1.7.10.jar | 1.7.10 | e80d9b3bf5085002218d4be59e668bac718abbc6 (=piston-meta ✓) | a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce | Mojang EULA — личное использование | launcher.mojang.com/v1/objects/e80d9b3b… |
| version-1.7.10.json | 1.7.10 | ed5d8789ed29872ea2ef1c348302b0c55e3f3468 | 265c7e47aad4c74f6085a49667d3a828f48d63f484def2ae70b5e426e5b7cba6 | Mojang meta | piston-meta.mojang.com |
| forge-1.7.10-installer.jar | 10.13.4.1614 | fccafccf8ad4ce6d9f008e786b48ff53172bf9de | a5d60757d8b305939233a942d5097fa78816d116ef9082c03066fe669e010c7f | Forge: LGPL-2.1 (FML), BSD/CC элементы — тексты в installer-x (LICENSE-fml.txt, MinecraftForge-License.txt) ⚠️ сверить полный список | maven.minecraftforge.net |

## Сборочные инструменты
| Артефакт | Версия | SHA256 | Лицензия | Происхождение |
|---|---|---|---|---|
| vineflower.jar | 1.12.0 | 1dfcfe974395734fa467ce620661c7623d05ba83670de0529b1fbd63ff548b9d | Apache-2.0/GPL-3 dual ⚠️ проверить LICENSE в jar | repo.maven.apache.org |
| specialsource.jar (shaded) | 1.11.6 | afabcba81cd474c5b21abdfe1bbc3888f70a32e782c03bffe13510078fa45a26 | BSD-3 ⚠️ | repo.maven.apache.org (net/md-5) |
| xz.jar | 1.9 | 211b306cfc44f8f96df3a0a3ddaf75ba8c5289eed77d60d72f889bb855f535e5 | Public Domain | repo.maven.apache.org (org.tukaani) |
| mcp_stable-12-1.7.10.zip | stable-12 | 83d303a80071582cd07d41b35aa77c178d609a27c69c8472b794636eb50905b0 | MCP mapping-условия (только dev-использование) ⚠️ | maven.minecraftforge.net (de/oceanlabs/mcp) |
| mingit.zip | MinGit 2.47.1.2 | (дописать при ревизии) | GPLv2 (только инструмент, не распространяется) | github git-for-windows |
| jdk21.zip | Temurin 21.0.13+9 | a4dbaa05fca4bd89d84584e58c459bd732efebb1722af80ab13c6516832004f9 | GPLv2+CE | api.adoptium.net |
| jdk27.zip | Temurin 27.0.1.0.2+1 | 5fac419f00d2256941a6b1253a827bb8b904da215a1ec0667035e5592811c1dc | GPLv2+CE | api.adoptium.net |

## GT-референс (читаем, не копируем код/ассеты — см. D-009/RISKS)
| Репозиторий | Пин | Commit | Лицензия |
|---|---|---|---|
| Blood-Asp/GT5-Unofficial | tag 5.09.33.02 (build.properties: gt.version=5.09.31 — пин пользователя совпадает с версией-строкой снапшота) | 77a3c956e510b7c8ef4654054bcb16a0fccb86f7 | LGPL-3.0 (README/CF ⚠️; LICENSE-файл в корне репо ОТСУТСТВУЕТ — помечено, проверяется доп. источники) |
| GregTech 5.07.07 (оригинал) | закрытый исходник; lineage = начальные коммиты GT5u (IC2-тред) | — | GregoriusT, закрытая (инвентаризация только через открытые источники/wiki) |

## Референсы-идеи (не включаются в продукт)
LWJGL3ify (GTNH) MIT ✅ · Angelica LGPL-3.0 ⚠️ · Neodymium LGPL-3.0 ✅ · Beddium LGPL-3.0 ✅ · TecTech/BartWorks/GT++/GoodGenerator — лицензии проверяются в Phase B.

## Исходящие соединения (аудит)
Сборка: piston-meta/launcher.mojang.com, maven.minecraftforge.net, repo.maven.apache.org, api.adoptium.net, github.com, codeload.github.com, forum/wiki-страницы. Рантайм: только игровые подключения + явно заданные пользователем сервисы (D-011).
