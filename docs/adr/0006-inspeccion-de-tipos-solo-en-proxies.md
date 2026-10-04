---
id: ADR-0006
title: "La inspección de tipos en runtime solo vive en un proxy marcado"
status: accepted
date: 2026-10-03
deciders: [dueño del repo]
tags: [convención, tipos]
paths:
  - "*/src/**"
supersedes: []
superseded_by: []
---

# ADR-0006: La inspección de tipos en runtime solo vive en un proxy marcado

- Enforced by: `docs/adr/compliance.py` (`adr_0006`), probado en `docs/adr/test_compliance.py`.

## Context

Preguntar en runtime de qué tipo es algo (`instanceof`, `getClass()`) reparte decisiones de tipo por todo el
código y esconde ramas que el compilador no ve. En Minecraft a veces es inevitable: los eventos entregan un
`Entity` o un `Level` y hay que saber si es un jugador o el lado servidor.

## Decision

`instanceof`, `getClass()`, `Class.isInstance`, `Class.isAssignableFrom` y los patrones de tipo en `case`
solo se usan en un proxy: un método chico cuyo trabajo es esconder esa pregunta, por ejemplo
`Optional<ServerPlayer> player(Entity)`. El proxy se marca con `// adr-skip ADR-0006: <qué esconde>`
(ADR-0005); el resto del código usa el proxy.

## Options considered

- **Proxy marcado con comentario.** La pregunta de tipo queda en un lugar, visible y con su porqué.
- **Una clase de proxies fija por mod.** Más rígida; un proxy de una sola línea no justifica un archivo.
- **Permitirlo en todo el código.** Las ramas por tipo se esparcen.

## Consequences

- **Good:** las preguntas de tipo se encuentran buscando `adr-skip ADR-0006`.
- **Bad:** un `if (x instanceof Y)` de una vez pide un método o una marca.
- **Watch:** los casts (`(ServerLevel) level`) no entran en el check; si se usan para esquivarlo, sumarlos.

## Rationale status

`confirmed`. Lo pidió el dueño del repo, que eligió la marca por comentario para definir el proxy.
