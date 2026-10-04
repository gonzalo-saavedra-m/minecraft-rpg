---
id: ADR-0001
title: "Un repo Gradle multi-módulo; cada mod es un jar independiente, sin mod núcleo"
status: superseded
date: 2026-10-02
deciders: [dueño del repo]
tags: [build, arquitectura]
paths:
  - settings.gradle
  - build.gradle
  - gradle.properties
supersedes: []
superseded_by: [ADR-0006]
---

# ADR-0001: Un repo Gradle multi-módulo; cada mod es un jar independiente, sin mod núcleo

- Enforced by: `docs/adr/compliance.py` (`adr_0001`); `settings.gradle` incluye toda carpeta con
  `src/main/resources/fabric.mod.json`.

## Context

El repo tendrá varios mods. Se pidió escribir el mínimo de código por mod y que instalar todos no ocupe
mucho disco. Medido en 26.3: `chunk-loader-1.0.0.jar` pesa 7 KB y Fabric API, que todos requieren, 2,6 MB.
Lo que más se repetía entre mods era la configuración de build, no el código Java.

## Decision

Usamos un solo repo con un subproyecto Gradle por mod. Las versiones (Minecraft, Loader, Loom, Fabric API)
y la configuración de build viven una vez en la raíz. Cada mod solo tiene su carpeta con `gradle.properties`
(`version`), su código, su `fabric.mod.json` y sus assets, y genera su propio jar. No hay mod núcleo de
runtime. Código compartido se extrae a un módulo común cuando lo repite un tercer mod, y se empaqueta con
jar-in-jar de Loom para que el jugador siga instalando un archivo por mod.

## Options considered

- **Multi-módulo con jars independientes.** Saca el boilerplate de build sin acoplar mods en runtime.
- **Mod núcleo del que dependen todos.** Obliga a instalarlo y a calzar versiones; ahorra kilobytes cuando
  Fabric API ya es la librería compartida.
- **Un repo por mod.** Repite build y versiones en cada repo.

## Consequences

- **Good:** un mod nuevo es una carpeta; subir de versión de Minecraft es editar `gradle.properties` raíz.
- **Bad:** todos los mods suben de versión de Minecraft juntos.
- **Watch:** si un mod necesita otra versión de Minecraft, o si el código común supera unos cientos de KB.

## Rationale status

`confirmed`. El dueño del repo pidió menos código por mod y bajo uso de disco, y aprobó esta estructura.
