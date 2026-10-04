---
id: ADR-0009
title: "El texto visible es traducible con respaldo en inglés en el código; cada idioma se copia a sus variantes"
status: proposed
date: 2026-10-04
deciders: [dueño del repo]
tags: [convención, idioma]
paths:
  - "*/src/main/resources/assets/*/lang/**"
  - build.gradle
supersedes: []
superseded_by: []
---

# ADR-0009: El texto visible es traducible con respaldo en inglés en el código; cada idioma se copia a sus variantes

- Enforced by: `docs/adr/compliance.py` (`adr_0009`): todos los archivos de idioma de un mod, salvo `en_us`, traducen
  las mismas claves. El check de ADR-0003 marca las claves que el código no usa. Que no quede texto literal visible
  es por revisión.

## Context

Los mods deben seguir el idioma del juego. Los mensajes de comandos y avisos salen del servidor, y un cliente sin el
mod no tiene sus archivos de idioma: con `Component.translatable` vería claves sueltas. Minecraft Java tiene 142
idiomas, el español en siete variantes (`es_es`, `es_mx`, `es_ar`, `es_cl`, `es_ec`, `es_uy`, `es_ve`), y no cae de
una variante a su idioma base: un jugador en `es_mx` sin `es_mx.json` ve inglés. Nadie quiere traducir "Fuerza" siete
veces.

## Decision

Todo texto que ve el jugador es `Component.translatableWithFallback(clave, inglés)`: el inglés vive en el código, así
que `en_us.json` solo trae lo que no tiene respaldo (nombres de ítems), y un cliente sin el mod lee inglés. Cada
idioma traducido es un archivo; se traducen los idiomas más jugados (por país: Estados Unidos, Brasil, Rusia, Reino
Unido, Alemania): español, portugués de Brasil, ruso, alemán, francés, chino simplificado, japonés, coreano, polaco,
italiano, turco y ucraniano. El build copia cada idioma base a sus variantes (`es_es` a las seis de Latinoamérica,
`pt_br` a `pt_pt`, `fr_fr` a `fr_ca` y `fr_ch`, `de_de` a `de_at` y `de_ch`), salvo a las que el mod ya traiga. Los
comandos, sus argumentos y los ids van siempre en inglés y no se traducen.

## Options considered

- **Respaldo en inglés en el código y copias de variantes en el build.** Una traducción por idioma, legible sin el mod.
- **`Component.translatable` con `en_us.json`.** El estándar, pero un cliente sin el mod ve claves.
- **Un archivo por variante en el repo.** Siete copias del español que mantener a mano.

## Consequences

- **Good:** un idioma nuevo es un archivo; las variantes y el inglés salen solos.
- **Bad:** el inglés vive en el código y no en `en_us.json`; corregirlo es tocar Java.
- **Watch:** variantes que de verdad difieran (un término distinto en `es_mx`): se agrega su archivo y deja de copiarse.

## Rationale status

`confirmed`. Lo pidió el dueño del repo el 2026-10-04, incluidos los idiomas más jugados y no traducir cada variante.
