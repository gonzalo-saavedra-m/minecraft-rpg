# Decisiones de souls-stats

Solo aplican a este mod. Las generales están en [`docs/adr/`](../../../docs/adr/README.md) de la raíz.

<!-- ADR-INDEX:START -->

- [0001](0001-xp-por-mob-en-un-archivo-por-datapack.md) — La XP por mob y su tope (`max_level`) viven en
  `data/soulsstats/mob_xp.json`; cada datapack pisa solo los mobs que trae.
- [0002](0002-todo-lo-visible-pasa-por-display.md) — Todo lo que ve el jugador pasa por `Display`.
  Reemplazado por 0005.
- [0003](0003-stats-abiertas-a-otros-mods.md) — Las stats viven en el registro `soulsstats:stat`: trae las
  de este mod y otros mods suman las suyas, sin quitar; se guardan los puntos por id.

- [0004](0004-peso-de-items-en-un-archivo-por-datapack.md) — El peso de cada ítem vive en `items.json`.
  Reemplazado por 0007.
- [0005](0005-cada-comando-arma-su-salida.md) — Cada comando arma su propia salida en su clase de
  `command/`; lo compartido y los avisos en `display/`; la lógica no le muestra nada al jugador.

- [0006](0006-numeros-de-stats-en-config-json.md) — Los números de las stats y la curva de nivel viven en
  `data/soulsstats/config.json`, cada efecto con una función a elegir (`linear`, `power`, `logarithmic`,
  `hyperbolic`, `soft_caps`); un datapack pisa campos (proposed).

- [0007](0007-items-json-se-mezcla-campo-a-campo.md) — `items.json` describe peso, stats, requisitos y escalado
  de cada ítem y se mezcla campo a campo entre datapacks; el log dice quién pisó qué (proposed).

<!-- ADR-INDEX:END -->
