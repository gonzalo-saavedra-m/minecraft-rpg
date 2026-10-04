---
id: ADR-0006
title: "Un repo Gradle multi-módulo, un jar por mod; un mod puede depender de otro del repo si lo necesita"
status: accepted
date: 2026-10-03
deciders: [dueño del repo]
tags: [build, arquitectura]
paths:
  - settings.gradle
  - build.gradle
  - gradle.properties
  - "*/src/main/resources/fabric.mod.json"
supersedes: [ADR-0001]
superseded_by: []
---

# ADR-0006: Un repo Gradle multi-módulo, un jar por mod; un mod puede depender de otro del repo si lo necesita

- Enforced by: `docs/adr/compliance.py` (`adr_0006`) para la estructura de build; que nadie dependa de un mod
  sin necesitarlo, por revisión.

## Context

ADR-0001 prohibía que un mod dependiera de otro del repo. El mod de niveles (`souls-stats`) expone stats y
eventos que un mod de habilidades posterior necesita consumir: copiarlos o reimplementarlos no tiene sentido.

## Decision

Sigue igual de ADR-0001: un solo repo con un subproyecto Gradle por mod, versiones y build en la raíz, cada
mod con su `gradle.properties` (`version`), su código, su `fabric.mod.json` y sus assets, y su propio jar.
Cambia que un mod puede depender de otro del repo cuando es notoriamente dependiente de él (lo extiende o
consume su API); lo declara en `depends` de su `fabric.mod.json`. No hay un mod núcleo del que dependan todos:
cada dependencia se justifica sola. Código compartido sin relación de dependencia se extrae a un módulo común
cuando lo repite un tercer mod, empaquetado con jar-in-jar de Loom.

## Options considered

- **Dependencias solo cuando un mod extiende a otro.** Permite mods base sin obligar a instalar nada extra
  al resto.
- **Mods siempre independientes (ADR-0001).** Obliga a duplicar la API del mod base en cada mod que la usa.
- **Mod núcleo del que dependen todos.** Obliga a instalarlo y a calzar versiones en cada mod.

## Consequences

- **Good:** un mod como `souls-stats` sirve de base para otros sin duplicar código.
- **Bad:** instalar el mod dependiente exige instalar su base, con versión compatible. El build de la raíz
  aún no compila un mod contra otro: el primer mod dependiente debe agregarlo.
- **Watch:** si varios mods empiezan a depender del mismo sin necesitarlo, se está formando un mod núcleo.

## Rationale status

`confirmed`. El dueño del repo pidió permitir dependencias cuando un mod es notoriamente dependiente de otro.
