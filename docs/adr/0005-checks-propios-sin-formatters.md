---
id: ADR-0005
title: "Los ADRs se verifican con checks propios, sin formatters ni reglas de estilo; toda violación se salta con un comentario"
status: accepted
date: 2026-10-03
deciders: [dueño del repo]
tags: [convención, calidad]
paths:
  - docs/adr/compliance.py
  - docs/adr/test_compliance.py
  - "*/src/**"
supersedes: []
superseded_by: []
---

# ADR-0005: Los ADRs se verifican con checks propios, sin formatters ni reglas de estilo; toda violación se salta con un comentario

- Enforced by: `docs/adr/test_compliance.py` prueba cada check y su marca de skip; ambos corren en
  pre-commit.

## Context

El código lo escribe y lo mantiene un agente. Un formatter que reordena líneas o un check que rechaza commits
por largo de línea o espacios solo agrega ruido. Un check que entiende la decisión (código muerto, tipos de
retorno) sí evita que el agente la rompa. Ningún check acierta siempre: hace falta una salida explícita.

## Decision

Cada ADR que se pueda verificar tiene una función `adr_NNNN()` en el `compliance.py` de su carpeta (Python,
sin dependencias), y los checks no triviales tienen un caso en `test_compliance.py`. No hay formatters ni
reglas de estilo (largo de línea, orden de imports, espacios). Una violación se salta con
`// adr-skip ADR-NNNN: <motivo>` (`#` en `.properties`) en la misma línea o en la anterior; el motivo es
obligatorio. Los JSON no admiten comentarios: sus checks no se saltan.

## Options considered

- **Checks propios por ADR, con skip por comentario.** Cada regla vive junto a su decisión y se puede
  saltar dejando el porqué.
- **Herramientas externas (PMD, Checkstyle, Spotless).** Mezclan reglas de estilo con las de diseño y
  agregan dependencias.
- **Solo revisión.** Un agente rompe la misma regla una y otra vez.

## Consequences

- **Good:** las decisiones se cumplen sin depender de que alguien las recuerde; los skips dejan el motivo.
- **Bad:** los checks son heurísticos (regex sobre el código sin comentarios ni strings) y hay que
  mantenerlos.
- **Watch:** skips que se vuelven costumbre; si un ADR acumula muchos, el check o la decisión están mal.

## Rationale status

`confirmed`. El dueño del repo aclaró que "sin linters" siempre significó sin formatters ni reglas de
estilo, y pidió la marca de skip.
