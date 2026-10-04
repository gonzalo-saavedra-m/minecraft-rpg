---
id: ADR-0002
title: "Minimizar líneas de código y comentarios sin perder claridad; entender el problema antes de parchar"
status: accepted
date: 2026-10-02
deciders: [dueño del repo]
tags: [convención, calidad]
paths:
  - "*/src/**"
supersedes: []
superseded_by: []
---

# ADR-0002: Minimizar líneas de código y comentarios sin perder claridad; entender el problema antes de parchar

- Enforced by: revisión. `docs/adr/compliance.py` (`adr_0002`) imprime la métrica en cada commit.

## Context

El repo es de mods chicos que mantiene una persona con ayuda de agentes. Cada línea y cada comentario se lee
y se mantiene, y un parche al síntoma deja el bug real vivo en otro camino.

## Decision

Las líneas de código y de comentarios son una métrica a minimizar, mientras no ensucien la comprensión. Un
comentario existe solo si explica algo que el código no dice (el porqué, una trampa de Minecraft). Antes de
cambiar código se entiende el problema a fondo: se lee el flujo completo de Minecraft que se toca (con `javap`
o `genSources`) y se corrige la causa, no el síntoma. Entre una solución corta que parcha y una más larga que
resuelve la causa, gana la segunda.

## Options considered

- **Minimizar líneas, con la comprensión como límite.** Menos que leer y mantener.
- **Minimizar líneas a toda costa.** Produce código críptico; descartado por el límite de claridad.

## Consequences

- **Good:** mods cortos que se entienden de una leída.
- **Bad:** la métrica no se valida sola; depende de que cada cambio la reporte.
- **Watch:** comentarios que repiten el código o fixes que vuelven a fallar por otro camino.

## Rationale status

`confirmed`. Lo pidió el dueño del repo el 2026-10-02.
