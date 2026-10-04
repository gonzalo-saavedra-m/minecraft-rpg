---
id: ADR-0007
title: "items.json describe peso, stats, requisitos y escalado de cada ítem y se mezcla campo a campo entre datapacks"
status: proposed
date: 2026-10-04
deciders: [dueño del repo]
tags: [souls-stats, datapack, formato]
paths:
  - src/main/resources/data/soulsstats/items.json
  - src/main/java/soulsstats/item/ItemProperties.java
  - src/main/java/soulsstats/data/Datapack.java
supersedes: [ADR-0004]
superseded_by: []
---

# ADR-0007: items.json describe peso, stats, requisitos y escalado de cada ítem y se mezcla campo a campo entre datapacks

- Enforced by: `docs/adr/compliance.py` de este mod (`adr_0007`, formato del archivo por defecto); al cargar, el
  codec de `ItemProperties` reporta en el log las entradas inválidas y `Datapack` cada campo que un datapack pisa.

## Context

ADR-0004 dejó en `items.json` el peso de cada ítem, y cada datapack pisaba la entrada entera. Ahora una entrada
también trae stats que suma el ítem equipado, y vendrán requisitos y escalados. Varios datapacks (de mods, de
contenido, del admin) pueden describir el mismo ítem: si el admin solo quiere cambiar un peso, pisar la entrada
entera borra sin aviso las stats y escalados que puso otro datapack.

## Decision

Una entrada de `data/soulsstats/items.json` es `{"<id de ítem>": {"weight": n, "stats": {"<stat>": n}, "requires":
{"<stat>": n}, "scaling": {"<stat>": x}}}`, todos los campos opcionales: sin `weight`, el peso se deriva de los
atributos del ítem (armadura, dureza y daño por golpe); `stats` suma mientras el ítem está equipado; en un arma,
`scaling` dice cuánto suma cada stat a su daño (daño × Σ escalado × curva de `config.json`, como en Dark Souls; sin
`scaling`, un arma usa el escalado por defecto del config) y `requires` las stats sin las que no hay bonus y el daño
cae; `usable_at_level` y `equipable_at_level` son el nivel desde el que se puede golpear con el ítem o
llevarlo puesto. Una stat sin namespace es de este mod. Se leen todas las
copias del archivo, de menor a mayor prioridad de datapack (los de mods primero, los del mundo después, en el
orden de `/datapack list`), y se mezclan campo a campo: cada datapack pisa solo los campos que trae, también
dentro de objetos (una stat sin tocar las otras); números y listas se reemplazan. Para anular algo se pone en 0.
Cada campo pisado queda en el log con el datapack que lo pisó, y una entrada inválida se descarta sin afectar a
las demás. `config.json` (ADR-0006) se mezcla igual. `mob_xp.json` sigue pisando la entrada entera (ADR-0001).

## Options considered

- **Mezcla campo a campo, con log de cada pisada.** El admin cambia lo justo y ve los choques.
- **Entrada entera (ADR-0004).** Simple, pero cambiar un campo exige copiar todos los demás de quien definió el
  ítem.
- **Un archivo de admin aparte, fuera de los datapacks.** Prioridad clara, pero otro formato y otra carga para lo
  mismo; los datapacks del mundo ya son la herramienta del admin y ya tienen orden.

## Consequences

- **Good:** un datapack de contenido define ítems completos y el admin ajusta un campo sin pisar el resto.
- **Bad:** no hay forma de borrar un campo de otro datapack, solo de ponerlo en 0.
- **Watch:** logs ruidosos si muchos datapacks pisan lo mismo; si molesta, bajar el nivel de log.

## Rationale status

`confirmed`. Lo pidió el dueño del repo el 2026-10-04 para que datapacks de contenido y la config del admin no
choquen.
