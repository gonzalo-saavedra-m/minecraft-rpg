---
id: ADR-0003
title: "Las stats viven en un registro de Minecraft: trae las de este mod y otros mods suman las suyas, sin quitar"
status: accepted
date: 2026-10-04
deciders: [dueño del repo]
tags: [souls-stats, api]
paths:
  - src/main/java/soulsstats/stat/Stat.java
  - src/main/java/soulsstats/progress/PlayerProgress.java
supersedes: []
superseded_by: []
---

# ADR-0003: Las stats viven en un registro de Minecraft: trae las de este mod y otros mods suman las suyas, sin quitar

- Enforced by: revisión.

## Context

Un mod futuro, pegado a este, sumará stats de magia (inteligencia, quizás sabiduría y fe) con hechizos,
milagros o piromancias. El dueño del repo quiere que otros mods puedan sumar stats sin tocar souls-stats.
Con un enum, la lista de stats es fija y el guardado falla ante una stat desconocida.

## Decision

`Stat.REGISTRY` es un registro de Minecraft (`soulsstats:stat`, creado con `FabricRegistryBuilder`). Trae por
defecto `vigor`, `strength`, `dexterity` y `luck`; otros mods suman las suyas con `Registry.register` en su
`onInitialize`. Un registro no permite quitar entradas, y no se busca otra forma: las stats de este mod
siempre están. `Stat` es una clase (identidad única), no un record, para que dos stats con la misma base no
sean iguales. Comandos y resumen leen el registro al iniciar el servidor. `PlayerProgress` guarda los puntos
invertidos por id de stat (no el valor final), así cada stat tiene su base y esta puede cambiar sin mover
puntos. Al cargar un jugador se descartan los puntos de stats que ya no existen (un mod desinstalado) y
vuelven a estar libres. El efecto de una stat (como `VigorHealth`) es del mod que la registra.

## Options considered

- **Registro de Minecraft, sin quitar.** Ids, iteración en orden y el mecanismo estándar de Fabric; queda la
  puerta abierta a sincronizarlo con el cliente (`RegistryAttribute.SYNCED`).
- **Registro propio con `register` y `remove`.** Permite quitar, pero es un mecanismo a mano y un mod podría
  quitar stats de las que dependen otros.
- **Registro dinámico por datapack.** Las stats serían datos, pero su efecto es código.

## Consequences

- **Good:** un mod de magia suma cada stat en una línea; las stats base están garantizadas para quien las
  use; desinstalar un mod no rompe ni bloquea los puntos de nadie.
- **Bad:** un modpack que no quiera una stat base no puede sacarla; reinstalar un mod no devuelve los puntos
  que tenía su stat.
- **Watch:** cuando llegue la interfaz gráfica, el cliente necesitará la lista de stats; ahí evaluar
  `RegistryAttribute.SYNCED`.

## Rationale status

`confirmed`. El dueño del repo pidió el 2026-10-04 que otros mods sumen stats, que no se puedan quitar y que
estén por defecto las de este mod.
