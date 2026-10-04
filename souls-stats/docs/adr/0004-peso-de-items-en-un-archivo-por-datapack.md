---
id: ADR-0004
title: "El peso de cada ítem vive en items.json con el formato de mob_xp.json; sin entrada, se deriva de sus atributos"
status: superseded
date: 2026-10-04
deciders: [dueño del repo]
tags: [souls-stats, datapack, formato]
paths:
  - src/main/resources/data/soulsstats/items.json
  - src/main/java/soulsstats/weight/ItemWeight.java
supersedes: []
superseded_by: [ADR-0007]
---

# ADR-0004: El peso de cada ítem vive en items.json con el formato de mob_xp.json; sin entrada, se deriva de sus atributos

- Enforced by: `docs/adr/compliance.py` de este mod (`adr_0004`, formato del archivo por defecto); al cargar,
  el codec de `ItemWeight` reporta en el log las entradas inválidas.

## Context

El peso del equipamiento decide la velocidad y el salto del jugador. Debe configurarse por servidor, servir
para ítems de otros mods y recargarse con `/reload`, igual que la XP por mob (ADR-0001). Después se sumarán
requisitos y escalado de armas por ítem. Hay cientos de ítems equipables y nadie quiere listarlos todos.

## Decision

La tabla es `data/soulsstats/items.json`: `{"<id de ítem>": {"weight": <peso>}}`, peso decimal >= 0. Se
mezcla como `mob_xp.json`: todas las copias del archivo, de menor a mayor prioridad, y cada una pisa solo los
ítems que trae (la entrada completa). Un ítem sin entrada pesa lo que suma en armadura, dureza de armadura y
daño de ataque (sus modificadores `add_value`); uno sin esos atributos pesa 0. En armas cuenta solo el daño
por golpe, no la velocidad de ataque ni el DPS: un hacha es más pesada que una espada. La fórmula es
provisional. Requisitos y escalado serán
campos opcionales de la misma entrada. Ambas tablas comparten el cargador `Datapack`.

## Options considered

- **Mismo formato que `mob_xp.json`, con peso derivado por defecto.** Un formato que aprender; la tabla solo
  lista excepciones (escudo, maza) y los ítems de otros mods tienen peso sin configurar nada.
- **Tabla explícita para todos los ítems vanilla.** Control total, pero cientos de líneas y los ítems de otros
  mods pesan 0.
- **Un archivo por ítem o tags de peso.** Formato vanilla, pero muchos archivos para un número por ítem.

## Consequences

- **Good:** el admin cambia un ítem con una línea; un mod de armaduras funciona sin integración.
- **Bad:** el peso derivado mezcla unidades (armadura y daño) y puede no calzar con la intuición: la élitra
  pesa 0 y una espada de diamante más que un peto de hierro.
- **Watch:** si los números derivados obligan a listar demasiadas excepciones, cambiar la fórmula.

## Rationale status

`confirmed`. El dueño del repo pidió el 2026-10-04 empezar el peso por un datapack de atributos de ítems y
aceptó este formato el mismo día.
