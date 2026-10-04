---
id: ADR-0003
title: "Código muerto se borra en el mismo cambio que lo deja muerto"
status: accepted
date: 2026-10-03
deciders: [dueño del repo]
tags: [convención, calidad]
paths:
  - "*/src/**"
supersedes: []
superseded_by: []
---

# ADR-0003: Código muerto se borra en el mismo cambio que lo deja muerto

- Enforced by: `docs/adr/compliance.py` (`adr_0003`), probado en `docs/adr/test_compliance.py`.

## Context

Adaptado de revi-mono ADR-0005, donde el código muerto rompe el build. El código lo escriben agentes, que
tienden a dejar helpers, imports y assets que nadie usa. Los assets se cruzan con lo registrado; el Java se
revisa por nombre dentro del mod, sin compilar ni depender de herramientas externas (ADR-0005).

## Decision

Código que nada usa se borra. Quitar el último uso de algo obliga a quitar ese algo en el mismo cambio. No se
deja código comentado ni "para después". El check marca:

- Un asset (blockstate, ítem, loot table, lang, modelo, textura) sin bloque o ítem que lo use.
- Un import cuyo nombre no aparece en el resto del archivo.
- Una declaración (tipo, constante, método, campo, variable local) cuyo nombre aparece una sola vez en el
  mod, contando sus `.java` y su metadata (`fabric.mod.json`, `*.mixins.json`). Quedan fuera constructores,
  parámetros (los callbacks de Fabric fijan la firma) y métodos con `@Override` o anotaciones de mixin.
- Un comentario `//` que termina en `;`, `{` o `}`: código comentado.

La API pública que otro mod consume y este no usa se marca con `adr-skip` (ADR-0005).

## Options considered

- **Check propio por nombre, en Python.** Corto, sin dependencias, cubre lo que más se olvida.
- **PMD u otra herramienta externa.** Agrega una dependencia y solo ve miembros privados.
- **Solo revisión.** El código muerto pasa revisiones repetidas.

## Consequences

- **Good:** el tamaño del repo refleja lo que hace.
- **Bad:** el check es por nombre: dos cosas con el mismo nombre se tapan entre sí, y código usado solo por
  sí mismo (recursión, dos métodos que se llaman entre ellos) pasa.
- **Watch:** falsos positivos que obliguen a muchos `adr-skip`; ahí conviene afinar el check.

## Rationale status

`confirmed`. Lo pidió el dueño del repo, que siempre quiso checks propios sobre el código (ADR-0005).
