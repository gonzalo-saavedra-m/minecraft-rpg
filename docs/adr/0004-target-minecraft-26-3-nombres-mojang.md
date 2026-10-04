---
id: ADR-0004
title: "Apuntar a Minecraft 26.3 con nombres oficiales de Mojang y Java 25"
status: superseded
date: 2026-10-02
deciders: [dueño del repo]
tags: [build, versiones]
paths:
  - gradle.properties
supersedes: []
superseded_by: [ADR-0005]
---

# ADR-0004: Apuntar a Minecraft 26.3 con nombres oficiales de Mojang y Java 25

- Enforced by: `docs/adr/compliance.py` (`adr_0004`) y el compilador.

## Context

Desde 26.1 Minecraft se publica sin ofuscar y Fabric dejó Yarn: el template oficial
(`FabricMC/fabric-example-mod`) usa los nombres de Mojang (`Level`, `BlockBehaviour`, `Identifier`) y Java 25.
Casi todos los tutoriales y respuestas en internet son de 1.21.x o antes y usan nombres de Yarn
(`World`, `AbstractBlock`).

## Decision

Todos los mods apuntan a la última versión estable disponible al crear el repo, 26.3, con los nombres de Mojang
y Java 25. Las firmas se verifican contra el jar de Minecraft (ver `AGENTS.md`) y no contra tutoriales.

## Options considered

- **26.3.** Última estable; el template oficial apunta ahí.
- **1.21.x con Yarn.** Más tutoriales, pero es una versión vieja y la API de Fabric cambió de nombres.

## Consequences

- **Good:** API actual, sin capa de mappings.
- **Bad:** pocos ejemplos en internet; hay que leer el código de Minecraft.
- **Watch:** si el mundo o servidor donde se juega usa otra versión, este ADR se reemplaza.

## Rationale status

`confirmed`. La propuso el agente por ser la última estable y la aceptó el dueño del repo.
