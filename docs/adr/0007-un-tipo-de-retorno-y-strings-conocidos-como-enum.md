---
id: ADR-0007
title: "Un método retorna un solo tipo (más null) y los strings conocidos son enums"
status: accepted
date: 2026-10-03
deciders: [dueño del repo]
tags: [convención, tipos]
paths:
  - "*/src/**"
supersedes: []
superseded_by: []
---

# ADR-0007: Un método retorna un solo tipo (más null) y los strings conocidos son enums

- Enforced by: `docs/adr/compliance.py` (`adr_0007`), probado en `docs/adr/test_compliance.py`.

## Context

Un método que retorna "una lista o un mapa" obliga a quien lo llama a preguntar el tipo (ADR-0006). Un
método que retorna un `String` de un conjunto conocido ("light", "heavy") esconde ese conjunto: el
compilador no avisa si falta un caso o hay un typo.

## Decision

Un método retorna un solo tipo concreto; la única segunda salida permitida es `null` u `Optional`. No se
retorna `Object`, `Either` ni genéricos con `Object` o `?`. Si todos los `return` de un método `String` son
literales, retorna un enum (el equivalente en Java de un tipo literal).

## Options considered

- **Un tipo por retorno y enums para valores conocidos.** El compilador revisa los casos.
- **Permitir `Object` y strings libres.** Menos tipos que declarar; los errores aparecen en runtime.

## Consequences

- **Good:** quien llama no adivina qué recibe; un `switch` sobre el enum avisa si falta un caso.
- **Bad:** un enum o un record extra donde antes bastaba un `String` u `Object`.
- **Watch:** strings conocidos que entran por parámetro o se comparan con `equals("…")`; el check solo mira
  retornos.

## Rationale status

`confirmed`. Lo pidió el dueño del repo.
