# Instrucciones para agentes

Repo de mods de Fabric para Minecraft. Antes de cambiar algo lee [SPEC.md](SPEC.md) (objetivo y lista de
mods), [PENDINGS.md](PENDINGS.md) (pendientes generales) y el índice [docs/adr/README.md](docs/adr/README.md)
(decisiones generales).

Para trabajar en un mod, lee además `<mod>/README.md` (qué hace y sus pendientes) y `<mod>/docs/adr/README.md`
(sus decisiones). No cargues los README ni ADRs de mods que no vas a tocar.

## Principios

- **Menos líneas, sin perder claridad** (ADR-0002). Las líneas de código y comentarios se minimizan mientras
  no ensucien la comprensión. Un comentario explica solo lo que el código no dice. Al cerrar un cambio,
  reporta la métrica: `git diff --shortstat` del cambio y lo que imprime `python3 docs/adr/compliance.py`.
- **Entender antes de parchar** (ADR-0002). Lee el flujo completo de Minecraft que tocas y corrige la causa,
  no el síntoma.
- **Código muerto se borra en el mismo cambio** (ADR-0003), incluidos assets sin uso. Sin linters de código.
- Cambios chicos y explícitos. Si cambias comportamiento, pruébalo (ver Probar) y actualiza el `README.md` del mod.
- No toques cambios ajenos sin commitear.

## Estructura

Proyecto Gradle multi-módulo (ADR-0006). La raíz define versiones (`gradle.properties`) y build
(`build.gradle`) para todos. Cada carpeta con `src/main/resources/fabric.mod.json` es un mod y entra sola al
build (`settings.gradle`). Un mod tiene `gradle.properties` (`version`), código en
`src/main/java/<modid>/` (paquete = modid, sin datos personales), assets en `src/main/resources/`, un
`README.md` (qué hace y pendientes) y, si tiene decisiones propias, `docs/adr/` con su numeración desde 0001,
su índice y opcionalmente su `compliance.py`. Un mod puede depender de otro del repo solo si lo extiende o
consume su API; lo declara en `depends` de su `fabric.mod.json`. Un `@decision ADR-NNNN` dentro de un mod
apunta a los ADRs de ese mod.

Mod nuevo: copia un mod existente (el primero, desde `chunk-loader/` de
`gonzalo-saavedra-m/minecraft-utility-mods`), renombra carpeta, paquete, `id` del `fabric.mod.json` y carpetas
de assets, y borra lo que no uses (incluidos sus ADRs). En `fabric.mod.json` usa `${version}`,
`${minecraft_dependency}` y `${loader_version}`. Agrega una línea en `SPEC.md`.

## Setup y comandos

Requiere Java 25: `export JAVA_HOME=/opt/homebrew/opt/openjdk@25`. Activa el hook de pre-commit una vez por
clon: `git config core.hooksPath .githooks`.

```sh
./gradlew build                      # jars en <mod>/build/libs/
./gradlew :<mod>:runClient           # cliente con el mod
./gradlew :<mod>:runServer           # servidor dedicado; mundo en <mod>/run/
```

## Cambiar de versión

Se sigue la próxima versión de Minecraft desde sus snapshots (ADR-0005). Para subir:

1. Busca la última versión de juego en `https://meta.fabricmc.net/v2/versions/game` que tenga Fabric API en
   `https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/maven-metadata.xml` (sufijo `+<versión
   base>`). Revisa también Loader y Loom en el `gradle.properties` del template oficial
   (`FabricMC/fabric-example-mod`).
2. Actualiza `gradle.properties` raíz. Si cambia la versión de Java, actualiza `build.gradle` y ADR-0005.
3. `./gradlew build`, arregla lo que rompa entendiendo el cambio de API (ADR-0002) y prueba cada mod en
   servidor (ver Probar).

## Minecraft 26.x: trampas conocidas

- Desde 26.1 se usan los nombres de Mojang. Casi todo lo que hay en internet es de 1.21.x con nombres de
  Yarn: no lo copies sin verificar. Verifica firmas en el jar:
  `javap -cp ~/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-common-deobf/<versión>/minecraft-common-deobf-<versión>.jar -p <clase>`.
  Para leer código fuente, `./gradlew genSources`.
- `Block.Properties` e `Item.Properties` necesitan `.setId(ResourceKey)`. `Blocks.register` es privado e
  `Items` no tiene helpers: se registra con `Registry.register`.
- El hook de remoción de un bloque es `affectNeighborsAfterRemoval` (no existe `onRemove`). Corre solo con el
  flag `UPDATE_NEIGHBORS` o por pistón.
- Loot tables: `"condition": {"type": ...}` por pool, no `"conditions": [...]`. Ante la duda, copia el
  formato de un archivo vanilla del jar (`unzip -p <jar> data/minecraft/...`).
- Los ítems necesitan `assets/<modid>/items/<item>.json` además del modelo.

## Probar

Sin jugadores no hay chunks cargados, así que los comandos sobre bloques fallan con "That position is not
loaded": antes, `forceload add 0 0`. Servidor headless con consola por FIFO:

```sh
mkfifo /tmp/mc && (sleep 99999 > /tmp/mc &) && ./gradlew :<mod>:runServer < /tmp/mc > /tmp/mc.log 2>&1 &
# cuando /tmp/mc.log diga "Done":
echo 'forceload add 0 0' > /tmp/mc
echo '<comando a probar>' > /tmp/mc
echo stop > /tmp/mc
```

Lo que dependa de un jugador (stats, XP al matar, peso) se prueba en el cliente con `runClient`.

La primera vez hay que aceptar la EULA en `<mod>/run/eula.txt`. El servidor se pausa a los 60 s sin
jugadores.

## Mantener el contexto

- `PENDINGS.md`: borra lo que cierres en el mismo commit, agrega lo que dejes pendiente.
- `<mod>/README.md`: comportamiento observable y pendientes del mod, siempre al día.
- `SPEC.md`: objetivo del repo y una línea por mod.

<!-- ADR:START -->
## Decisiones de arquitectura

Las decisiones generales viven en `docs/adr/` y las de cada mod en `<mod>/docs/adr/`. Antes de cambiar una
dependencia, la estructura de módulos, un formato de
datos o algo marcado con `@decision ADR-NNNN`, lee el índice que corresponda y los ADRs cuyos
`paths` calcen con lo que tocas. Si un cambio rompe un ADR, para y avísale al dueño del repo.

- Un ADR nuevo nace `proposed`; solo una persona lo pasa a `accepted` o `rejected`.
- Un ADR `accepted` no se reescribe: se reemplaza con uno nuevo (`supersedes` / `superseded_by`).
- Un ADR que solo afecta a un mod va en `<mod>/docs/adr/`, no en la raíz.
- Cada ADR nombra en "Enforced by" el check más chico que falla si se rompe, o dice que es por revisión. Los
  checks son funciones `adr_NNNN()` en el `compliance.py` de su carpeta de ADRs y corren en pre-commit. Nada de linters de
  código (ADR-0003): los checks miran build, metadata y assets.
- Tras escribir o cambiar uno, actualiza el índice y corre `.githooks/pre-commit`.
<!-- ADR:END -->
