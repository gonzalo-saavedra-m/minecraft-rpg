---
id: ADR-0004
title: "Seguir la próxima versión de Minecraft desde sus snapshots, con nombres de Mojang y Java 25"
status: accepted
date: 2026-10-02
deciders: [dueño del repo]
tags: [build, versiones]
paths:
  - gradle.properties
  - build.gradle
supersedes: []
superseded_by: []
---

# ADR-0004: Seguir la próxima versión de Minecraft desde sus snapshots, con nombres de Mojang y Java 25

- Enforced by: `docs/adr/compliance.py` (`adr_0004`) y el compilador.

## Context

El objetivo es tener un servidor con estos mods listo el día que salga la próxima versión de Minecraft, no
jugar en la actual. Fabric publica Loader y Fabric API para los snapshots: el 2026-10-02 la última estable era
26.3 y ya existían `26.4-snapshot-2` y `fabric-api 0.161.2+26.4`. Fabric API declara `"minecraft": "~26.4-"`,
que acepta snapshots, pre-releases y la versión final. Desde 26.1 Minecraft sale sin ofuscar y Fabric dejó
Yarn por los nombres de Mojang (`Level`, `BlockBehaviour`, `Identifier`) con Java 25. Casi todo lo que hay en
internet es de 1.21.x con nombres de Yarn (`World`, `AbstractBlock`).

## Decision

Los mods apuntan al último snapshot, pre-release o release candidate de la próxima versión que tenga build de
Fabric API, con nombres de Mojang y Java 25. `minecraft_version` en `gradle.properties` es la versión exacta de
desarrollo; el `fabric.mod.json` declara `${minecraft_dependency}`, que el build deriva como
`~<versión base>-`. Cuando sale un snapshot nuevo, o la versión final, se sube y se prueba (`AGENTS.md`,
sección Cambiar de versión). Las firmas se verifican contra el jar de Minecraft, no contra tutoriales.

## Options considered

- **Seguir los snapshots de la próxima versión.** Los cambios de API llegan de a poco y el día del release
  solo queda subir a la versión final.
- **Apuntar a la última estable.** Deja todo el trabajo de migración para el día del release.
- **1.21.x con Yarn.** Más tutoriales, pero es una versión vieja con otra API.

## Consequences

- **Good:** el día del release, los mods ya compilan y funcionan contra casi la misma API.
- **Bad:** los snapshots rompen APIs sin aviso; a veces un snapshot no tiene Fabric API todavía. Hay pocos
  ejemplos en internet: hay que leer el código de Minecraft.
- **Watch:** si durante un tiempo largo hay que jugar en la versión estable, volver a apuntar a ella.

## Rationale status

`confirmed`. El dueño del repo quiere el servidor listo para la próxima versión; el mecanismo (snapshots y
dependencia derivada) lo propuso el agente.
