---
id: ADR-0002
title: "Todo lo que ve el jugador pasa por Display: primero comandos y chat coloreado, después una interfaz gráfica"
status: superseded
date: 2026-10-03
deciders: [dueño del repo]
tags: [souls-stats, ui]
paths:
  - src/main/java/soulsstats/Display.java
supersedes: []
superseded_by: [ADR-0005]
---

# ADR-0002: Todo lo que ve el jugador pasa por Display: primero comandos y chat coloreado, después una interfaz gráfica

- Enforced by: `docs/adr/compliance.py` de este mod (`adr_0002`): fuera de `Display.java` no se arma texto
  para el jugador (`Component`, `ChatFormatting`) ni se le envía (`sendSystemMessage`, `sendSuccess`,
  `sendFailure`, `displayClientMessage`).

## Context

El mod tendrá una interfaz gráfica (HUD de nivel, pantalla de stats), pero el dueño del repo quiere todo
funcional antes por comandos y mensajes coloreados en el chat. Si el texto se arma donde ocurre la lógica
(subir de nivel, repartir puntos), pasar a la interfaz obliga a tocar esa lógica en cada lugar.

## Decision

La lógica no le muestra nada al jugador: emite eventos por `EventBus` (como `LevelUp`) o expone datos
(como `Progress`). `Display` es el único que escucha esos eventos para avisar, registra los comandos que
muestran información y arma el texto con colores. Cuando llegue la interfaz gráfica, cambia `Display` (o
se suma una clase de cliente que escuche lo mismo), no la lógica.

## Options considered

- **Un solo punto de salida, alimentado por eventos y datos.** La interfaz reemplaza ese punto.
- **Texto donde ocurre la lógica.** Menos indirección hoy; cada pantalla futura toca la lógica.

## Consequences

- **Good:** el chat de hoy y la interfaz de mañana leen lo mismo; la lógica no cambia con la presentación.
- **Bad:** todo aviso nuevo necesita un evento o un dato expuesto, aunque sea una línea de chat.
- **Watch:** `Display` creciendo con lógica de juego; si pasa, esa lógica vuelve a su clase.

## Rationale status

`confirmed`. Lo pidió el dueño del repo el 2026-10-03.
