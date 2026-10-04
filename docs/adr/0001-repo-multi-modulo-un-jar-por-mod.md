---
id: ADR-0001
title: "Un repo Gradle multi-módulo, un jar por mod; un mod depende de otro solo si lo extiende"
status: accepted
date: 2026-10-03
deciders: [dueño del repo]
tags: [build, arquitectura]
paths:
  - settings.gradle
  - build.gradle
  - gradle.properties
  - "*/src/main/resources/fabric.mod.json"
supersedes: []
superseded_by: []
---

# ADR-0001: Un repo Gradle multi-módulo, un jar por mod; un mod depende de otro solo si lo extiende

- Enforced by: `docs/adr/compliance.py` (`adr_0001`) para la estructura de build; que nadie dependa de un mod
  sin necesitarlo, por revisión.

## Context

El repo tiene varios mods. Se pidió escribir el mínimo de código por mod y que instalar todos no ocupe mucho
disco. Un mod chico pesa unos KB y Fabric API, que todos requieren, unos MB. Lo que más se repite entre mods
es la configuración de build, no el código Java. Algunos mods sirven de base a otros (exponen stats, eventos
u opciones), y copiar esa API en cada mod que la usa no tiene sentido.

## Decision

Un solo repo con un subproyecto Gradle por mod. Las versiones (Minecraft, Loader, Loom, Fabric API) y la
configuración de build viven una vez en la raíz. Cada mod tiene su carpeta con `gradle.properties`
(`version`), su código, su `fabric.mod.json` y sus assets, y genera su propio jar. Un mod depende de otro del
repo solo cuando lo extiende o consume su API, y lo declara en `depends` de su `fabric.mod.json`. No hay mod
núcleo del que dependan todos. Código compartido sin relación de dependencia se extrae a un módulo común
cuando lo repite un tercer mod, empaquetado con jar-in-jar de Loom.

## Options considered

- **Multi-módulo, dependencias solo cuando un mod extiende a otro.** Saca el boilerplate de build y permite
  mods base sin obligar a instalar nada extra al resto.
- **Mods siempre independientes.** Obliga a duplicar la API de un mod base en cada mod que la usa.
- **Mod núcleo del que dependen todos.** Obliga a instalarlo y a calzar versiones en cada mod.
- **Un repo por mod.** Repite build y versiones en cada repo.

## Consequences

- **Good:** un mod nuevo es una carpeta; subir de versión de Minecraft es editar `gradle.properties` raíz.
- **Bad:** todos los mods suben de versión juntos. Instalar un mod dependiente exige su base con versión
  compatible. El build de la raíz aún no compila un mod contra otro: el primer mod dependiente lo agrega.
- **Watch:** si un mod necesita otra versión de Minecraft, o si varios mods dependen del mismo sin
  necesitarlo (se está formando un mod núcleo).

## Rationale status

`confirmed`. El dueño del repo pidió menos código por mod, bajo uso de disco y permitir dependencias cuando
un mod es notoriamente dependiente de otro.
