---
id: ADR-0008
title: "Sin números mágicos: todo número con significado es una constante con nombre"
status: proposed
date: 2026-10-04
deciders: [dueño del repo]
tags: [codigo, java]
paths:
  - "*/src/main/java/**"
supersedes: []
superseded_by: []
---

# ADR-0008: Sin números mágicos: todo número con significado es una constante con nombre

- Enforced by: `docs/adr/compliance.py` (`adr_0008`), con casos en `test_compliance.py`: un número distinto
  de 0 y 1 solo puede aparecer en la declaración de una constante `static final` o en los argumentos de las
  constantes de un enum.

## Context

Los mods tienen muchos números de diseño (carga máxima, umbrales, vida por punto, XP por nivel) que se
calibran jugando. Escritos sueltos en una fórmula (`10 + 2 * vigor`) no dicen qué son, se repiten sin
saberlo y cuesta encontrarlos al ajustar.

## Decision

Todo número con significado se define una vez como constante con nombre (`private static final float
BASE_MAX = 10`) o como argumento de una constante de enum cuando describe una fila de una tabla
(`HEAVY(1, 0.9f, -0.03)`). 0 y 1 se permiten sueltos porque son idiomas (`== 0`, `level - 1`), no valores de
diseño. Un número que viene de otro lado y no se ajusta (una fórmula vanilla) se salta con
`// adr-skip ADR-0008: <motivo>`.

## Options considered

- **Constantes con nombre, check propio.** El número se busca por nombre y se cambia en un lugar.
- **Por revisión.** Sin check, se cuelan en cada cambio.
- **Todo a datapack o config.** Lo que deba configurar el admin irá ahí; el resto no necesita esa maquinaria.

## Consequences

- **Good:** las fórmulas se leen por nombre y la calibración está junta.
- **Bad:** algunas líneas más por constantes que se usan una vez.
- **Watch:** constantes con nombres que no dicen nada (`TWO`); el nombre debe decir qué es, no cuánto vale.

## Rationale status

`confirmed`. Lo pidió el dueño del repo el 2026-10-04.
