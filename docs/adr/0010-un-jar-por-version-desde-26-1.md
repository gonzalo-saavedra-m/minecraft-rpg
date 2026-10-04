---
id: ADR-0010
title: "Publicar un jar por versión de Minecraft, desde 26.1 hasta la más nueva, con el mismo código"
status: accepted
date: 2026-10-04
deciders: [dueño del repo]
tags: [build, versiones, publicación]
paths:
  - build.gradle
  - .github/workflows/build.yml
supersedes: []
superseded_by: []
---

# ADR-0010: Publicar un jar por versión de Minecraft, desde 26.1 hasta la más nueva, con el mismo código

- Enforced by: `.github/workflows/build.yml`, que compila cada mod contra cada versión de su matriz. Que la matriz
  tenga todas las versiones desde 26.1 es por revisión.

## Context

ADR-0004 fija la versión de desarrollo: siempre la más nueva, aunque sea un snapshot. Los jugadores, en cambio,
están repartidos entre las versiones estables. Desde 26.1 Minecraft sale sin ofuscar, con nombres de Mojang y Java
25; antes (1.21.x) estaba ofuscado, con otro toolchain, Java 21 y otros nombres (`ResourceLocation`): soportarlo
sería otro código. El 2026-10-04, `souls-stats` compilaba contra 26.1, 26.2, 26.3 y 26.4-snapshot-2 salvo una
línea (`placeItemBackInInventory`, que cambió de firma en 26.3), resuelta con métodos que existen en todas.

## Decision

El código es uno solo y se escribe contra la versión de ADR-0004. Además se compila y publica un jar por versión
menor, desde 26.1 hasta la más nueva, pasando `-Pminecraft_version` y `-Pfabric_api_version` al build. Cada jar se
compila contra el primer parche de su versión (26.1, no 26.1.2), así su `~26.1-` cubre todos los parches, y lleva la
versión en el nombre (`1.0.0+26.1`). La matriz del workflow de CI es la lista de versiones soportadas. Si una API
difiere entre versiones, se usa una que exista en todas; solo si no la hay se evalúa un preprocesador
(Stonecutter), con un ADR nuevo.

## Options considered

- **Un jar por versión menor, del mismo código.** Casi sin código extra; un build por versión.
- **Un solo jar con rango amplio (`>=26.1`).** Un archivo, pero compilar contra una versión no garantiza enlazar en
  otra: un método que cambia de firma revienta en runtime.
- **Stonecutter desde ya.** Permite divergir por versión, pero suma tooling para una sola diferencia.
- **Incluir 1.21.x.** Más jugadores, pero otro toolchain y otra API: es mantener dos mods.

## Consequences

- **Good:** los jugadores de 26.1 en adelante tienen el mod sin mantener ramas.
- **Bad:** cada API nueva se elige también por existir en la versión más vieja; cada versión suma un build en CI.
- **Watch:** si una diferencia no tiene reemplazo común, decidir entre preprocesador y subir la versión mínima.

## Rationale status

`confirmed`. El dueño del repo pidió publicar desde la versión más nueva hasta la más vieja posible sin escribir
mucho código extra; el mecanismo lo propuso el agente y el dueño lo aceptó el 2026-10-04.
