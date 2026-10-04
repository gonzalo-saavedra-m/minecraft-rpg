# Decisiones de arquitectura

Decisiones generales del repo, un archivo numerado por decisión (`NNNN-titulo.md`). Las de un mod viven en
`<mod>/docs/adr/`, con su propia numeración. Las reglas para leerlas y escribirlas están en
[`AGENTS.md`](../../AGENTS.md). Valida la estructura con `python3 docs/adr/validate-adrs.py --strict`.

<!-- ADR-INDEX:START -->

- [0001](0001-repo-multi-modulo-un-jar-por-mod.md) — Un repo Gradle multi-módulo, un jar por mod; un mod
  depende de otro solo si lo extiende o consume su API. Aplica al build en la raíz y a los `depends` de cada
  `fabric.mod.json`.
- [0002](0002-menos-lineas-y-entender-antes-de-parchar.md) — Minimizar líneas de código y comentarios sin
  perder claridad; entender el problema antes de parchar. Aplica a todo el código.
- [0003](0003-codigo-muerto-se-borra-en-el-mismo-cambio.md) — Código muerto (Java y assets) se borra en el
  mismo cambio que lo deja muerto.
- [0004](0004-seguir-la-proxima-version-de-minecraft.md) — Seguir la próxima versión de Minecraft desde sus
  snapshots, con nombres de Mojang y Java 25. Aplica a `gradle.properties` y `build.gradle`.
- [0005](0005-checks-propios-sin-formatters.md) — Los ADRs se verifican con checks propios, sin formatters
  ni reglas de estilo; toda violación se salta con `// adr-skip ADR-NNNN: <motivo>`.
- [0006](0006-inspeccion-de-tipos-solo-en-proxies.md) — `instanceof`, `getClass()` y similares solo en un
  proxy marcado.
- [0007](0007-un-tipo-de-retorno-y-strings-conocidos-como-enum.md) — Un método retorna un solo tipo (más
  null) y los strings conocidos son enums.

- [0008](0008-sin-numeros-magicos.md) — Sin números mágicos: un número distinto de 0 y 1 va en una constante
  `static final` con nombre o en los argumentos de una constante de enum (proposed).

- [0009](0009-idiomas-con-respaldo-en-ingles.md) — El texto visible es traducible con respaldo en inglés en el
  código; cada idioma base se copia a sus variantes regionales en el build (proposed).

- [0010](0010-un-jar-por-version-desde-26-1.md) — Un jar por versión de Minecraft, desde 26.1 hasta la más nueva,
  del mismo código; la matriz de CI es la lista de versiones. Aplica a `build.gradle` y `.github/workflows/build.yml`.

<!-- ADR-INDEX:END -->
