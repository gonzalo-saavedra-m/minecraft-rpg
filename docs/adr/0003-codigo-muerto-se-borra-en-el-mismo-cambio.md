---
id: ADR-0003
title: "Código muerto se borra en el mismo cambio que lo deja muerto"
status: accepted
date: 2026-10-02
deciders: [dueño del repo]
tags: [convención, calidad]
paths:
  - "*/src/**"
supersedes: []
superseded_by: []
---

# ADR-0003: Código muerto se borra en el mismo cambio que lo deja muerto

- Enforced by: `docs/adr/compliance.py` (`adr_0003`) para assets; revisión para Java.

## Context

Adaptado de revi-mono ADR-0005, donde el código muerto rompe el build. El dueño del repo no quiere linters de código en
este repo. Los assets sí se pueden cruzar sin analizar Java: cada blockstate, ítem, loot table, lang, modelo y
textura debe corresponder a algo registrado o referenciado.

## Decision

Código que nada usa se borra. Quitar el último uso de algo obliga a quitar ese algo en el mismo cambio. Un
asset (modelo, textura, lang, loot table) sin bloque o ítem que lo use cuenta como código muerto. No se deja
código comentado ni "para después".

## Options considered

- **Assets por check, Java por revisión.** Detecta lo más fácil de olvidar sin analizar código.
- **Linter de Java (PMD o un test propio, como revi-mono).** Descartado: El dueño del repo no quiere linters de código.

## Consequences

- **Good:** el tamaño del repo refleja lo que hace.
- **Bad:** el código Java muerto depende de la revisión.
- **Watch:** código muerto en Java que pase revisiones repetidas.

## Rationale status

`confirmed` en revi-mono y en el rechazo a linters; la adaptación la propuso el agente y la aceptó el dueño del repo.
