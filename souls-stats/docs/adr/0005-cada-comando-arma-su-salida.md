---
id: ADR-0005
title: "Cada comando arma su propia salida en su clase; la lógica no le muestra nada al jugador"
status: accepted
date: 2026-10-04
deciders: [dueño del repo]
tags: [souls-stats, ui]
paths:
  - src/main/java/soulsstats/command/
  - src/main/java/soulsstats/display/
supersedes: [ADR-0002]
superseded_by: []
---

# ADR-0005: Cada comando arma su propia salida en su clase; la lógica no le muestra nada al jugador

- Enforced by: `docs/adr/compliance.py` de este mod (`adr_0005`): fuera de `command/` y `display/` no se arma
  texto para el jugador (`Component`, `ChatFormatting`) ni se le envía (`sendSystemMessage`, `sendSuccess`,
  `sendFailure`, `displayClientMessage`).

## Context

ADR-0002 juntaba en `Display` todo lo que ve el jugador, comandos incluidos. Con varios comandos, `Display`
mezclaba la lógica de cada uno y su texto, y para entender un comando había que leer dos clases. El dueño del
repo quiere una clase por comando donde se vea todo lo que hace, incluida su salida. Más adelante un comando
podrá abrir una interfaz gráfica en vez de responder en el chat.

## Decision

Cada comando vive en su clase en `command/` y define cómo se ve su respuesta, sea texto en el chat o, después,
abrir una pantalla. `display/` tiene lo que comparten varios: `Text` (colores y el resumen de progreso) y
`Notifications` (avisos por eventos de `EventBus`, como `LevelUpEvent`, que el jugador no pidió). Ahí irán también
las pantallas de la interfaz gráfica. La lógica (progreso, peso, carga) no le muestra nada al jugador: emite
eventos o expone datos.

## Options considered

- **Cada comando arma su salida; lo compartido en `display/`.** Un comando se entiende en una clase.
- **Un solo `Display` con todo el texto (ADR-0002).** Cambiar la presentación toca un archivo, pero cada
  comando queda partido en dos.
- **Una clase de texto por comando.** Separa salida y lógica, pero duplica las clases sin ganar nada.

## Consequences

- **Good:** leer un comando muestra qué hace y qué responde; una pantalla futura la abre el comando.
- **Bad:** cambiar el estilo de todos los mensajes toca varias clases (lo común está en `Text`).
- **Watch:** lógica de juego dentro de un comando; si crece, vuelve a su clase y el comando la llama.

## Rationale status

`confirmed`. Lo pidió el dueño del repo el 2026-10-04.
