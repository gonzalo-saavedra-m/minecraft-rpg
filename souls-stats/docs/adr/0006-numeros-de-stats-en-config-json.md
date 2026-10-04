---
id: ADR-0006
title: "Los números de las stats y la curva de nivel viven en config.json, cada efecto con una función a elegir"
status: proposed
date: 2026-10-04
deciders: [dueño del repo]
tags: [souls-stats, datapack, formato]
paths:
  - src/main/resources/data/soulsstats/config.json
  - src/main/java/soulsstats/data/Config.java
supersedes: []
superseded_by: []
---

# ADR-0006: Los números de las stats y la curva de nivel viven en config.json, cada efecto con una función a elegir

- Enforced by: `docs/adr/compliance.py` de este mod (`adr_0006`): el `config.json` por defecto trae todos los
  campos y cada escala tiene un `type` conocido con sus campos. Al cargar, el codec de `Config` reporta en
  el log una mezcla inválida y se mantiene la anterior.

## Context

Los efectos de las stats (vida y carga por vigor, daño por fuerza, velocidades por destreza, crítico por suerte) y
la curva de XP se calibran jugando y cada servidor puede querer otros. El dueño del repo quiere esos números fuera
del código y, si se puede, también la forma de la fórmul## Decision

`data/soulsstats/config.json` es un objeto con un campo por efecto. Cada efecto es una escala `{"type": ...}` que
elige una función de un conjunto predefinido y sus valores; todas suman a `base` (opcional, 0) lo que dan los
puntos de la stat sobre su base (en `level_xp`, el nivel actual):

- `linear`: `per_point × p`.
- `power`: `per_point × p^exponent` (curvas de nivel que aceleran).
- `logarithmic`: `scale × ln(1 + p)` (rinde menos con cada punto, sin tope).
- `hyperbolic`: `max × p / (p + half)` (llega a la mitad de `max` en `half` puntos y nunca lo alcanza).
- `soft_caps`: tramos lineales hasta `until` puntos con su propio `per_point` (los soft caps de Dark Souls).

Con puntos negativos la función se aplica en espejo. `critical_damage` es un multiplicador. Se leen todas las
copias del archivo, de menor a mayor prioridad, y se mezclan campo a campo como `items.json` (ADR-0007);
el archivo del mod trae todos con valores sugeridos. Se aplica con `/reload` y recalcula los efectos de los
jugadores conectados. Una función nueva es un tipo más. Los niveles de carga siguen en el código, porque el
cliente los usa para saber si el jugador está sobrecargado.

## Options considered

- **Un conjunto predefinido de funciones, elegido por `type`.** Cubre lo que usan Dark Souls (soft caps), League of
  Legends (hiperbólica, crecimiento que acelera) y una curva logarítmica; cada una con pocos parámetros.
- **Fórmulas como texto (`"2 * points"`) evaluadas al cargar.** Máxima libertad, pero un evaluador de expresiones
  que mantener y errores que solo aparecen jugando.
- **Solo escalas lineales.** Simple, pero sin rendimientos decrecientes: cada punto en vigor vale lo mismo siempre.

## Consequences

- **Good:** el admin calibra sin recompilar, elige la forma de cada curva y todos los números están en un archivo.
- **Bad:** una forma que no está en el conjunto requiere agregar un tipo en `Scaling`.
- **Watch:** campos que se agregan al código sin agregarse al `config.json` del mod (el check lo detecta).

).

## Rationale status

`confirmed`. Lo pidió el dueño del repo el 2026-10-04, incluido elegir la función de cada efecto; el conjunto
de funciones y los valores sugeridos son propuesta.
