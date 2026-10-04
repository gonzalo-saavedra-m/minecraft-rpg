---
id: ADR-0001
title: "La XP por mob y su tope viven en un solo archivo por datapack, que se mezcla por prioridad"
status: accepted
date: 2026-10-03
deciders: [dueño del repo]
tags: [souls-stats, datapack, formato]
paths:
  - src/main/resources/data/soulsstats/mob_xp.json
  - src/main/java/soulsstats/progress/MobXpTable.java
supersedes: []
superseded_by: []
---

# ADR-0001: La XP por mob y su tope viven en un solo archivo por datapack, que se mezcla por prioridad

- Enforced by: `docs/adr/compliance.py` de este mod (formato del archivo por defecto); al cargar, el codec
  de `MobXpTable` reporta en el log las entradas inválidas.

## Context

El dueño del repo quiere que la XP de cada mob la decida una tabla del server: un creeper da más que un
zombie, un piglin brute más que un piglin, un iron golem nada. Para frenar granjas sin anularlas, cada tipo
de mob da XP solo hasta cierto nivel del jugador. Debe servir para mobs de otros mods y recargarse con
`/reload`. El admin normalmente cambia unos pocos mobs, no la tabla entera.

## Decision

La tabla es `data/soulsstats/mob_xp.json`: `{"<id de entidad>": {"xp": <XP>, "max_level": <nivel>}}`.
`max_level` es opcional: es el nivel más alto al que lleva ese mob; un jugador de ese nivel o más ya no gana
XP de él. Sin `max_level` no hay tope. Un mob sin entrada da 0. Se leen todas las copias del archivo, de
menor a mayor prioridad de datapack, y cada una pisa solo los mobs que trae (la entrada completa). El tope no
se guarda por jugador: depende solo de su nivel.

## Options considered

- **Un archivo, un objeto por mob, mezclado por prioridad.** Todo lo de un mob en una línea; cambiar un mob
  es una línea en un datapack chico.
- **Tope en un archivo aparte.** Dos tablas paralelas que mantener.
- **Un archivo por mob (`mob_xp/<id>.json`, como las loot tables).** Formato vanilla, pero decenas de
  archivos en el mod y uno por cada cambio.
- **Tope como XP total por jugador y tipo, con o sin reinicio.** Hay que guardar un contador por jugador y
  por tipo; descartado por el dueño del repo.

## Consequences

- **Good:** un datapack de pocas líneas ajusta lo que quiera; los mobs débiles dejan de servir al subir de
  nivel, como en un RPG. Mods de terceros agregan sus mobs con el mismo archivo.
- **Bad:** un datapack no puede borrar una entrada, solo ponerla en `"xp": 0`. Cambiar solo el tope de un
  mob obliga a repetir su `xp`. Un mob de otro mod no da XP hasta que alguien lo configura.
- **Watch:** cuando llegue la XP o el peso de ítems, decidir si siguen este mismo formato.

## Rationale status

`confirmed`. El dueño del repo pidió la tabla por server, eligió el tope por nivel, el objeto por mob y que
un mob sin entrada dé 0.
