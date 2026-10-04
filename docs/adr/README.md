# Decisiones de arquitectura

Decisiones generales del repo, un archivo numerado por decisión (`NNNN-titulo.md`). Las de un mod viven en
`<mod>/docs/adr/`, con su propia numeración. Las reglas para leerlas y escribirlas están en
[`AGENTS.md`](../../AGENTS.md). Valida la estructura con `python3 docs/adr/validate-adrs.py --strict`.

<!-- ADR-INDEX:START -->

- [0001](0001-un-repo-multi-modulo-un-jar-por-mod.md) — *Reemplazado por 0006.* Mods sin dependencias entre
  sí.
- [0002](0002-menos-lineas-y-entender-antes-de-parchar.md) — Minimizar líneas de código y comentarios sin
  perder claridad; entender el problema antes de parchar. Aplica a todo el código.
- [0003](0003-codigo-muerto-se-borra-en-el-mismo-cambio.md) — Código muerto (incluidos assets) se borra en el
  mismo cambio que lo deja muerto.
- [0004](0004-target-minecraft-26-3-nombres-mojang.md) — *Reemplazado por 0005.* Apuntar a Minecraft 26.3.
- [0005](0005-seguir-la-proxima-version-de-minecraft.md) — Seguir la próxima versión de Minecraft desde sus
  snapshots, con nombres de Mojang y Java 25. Aplica a `gradle.properties` y `build.gradle`.
- [0006](0006-mods-independientes-con-dependencias-opcionales.md) — Un repo Gradle multi-módulo, un jar por
  mod; un mod puede depender de otro del repo si lo extiende o consume su API. Aplica al build en la raíz y a
  los `depends` de cada `fabric.mod.json`.

<!-- ADR-INDEX:END -->
