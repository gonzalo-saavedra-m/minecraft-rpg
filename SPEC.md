# Especificación

Mods de Fabric que agregan progresión RPG al estilo Dark Souls, desarrollados en la próxima versión de Minecraft
(ADR-0004) y publicados para cada versión desde 26.1 (ADR-0010). Cada mod es un jar; un mod puede depender de otro del repo si lo extiende (ADR-0001).

## Mods

Una línea por mod. El detalle (qué hace, pendientes, decisiones) está en el `README.md` de cada uno; léelo
solo si vas a trabajar en ese mod.

- [souls-stats](souls-stats/README.md): stats al estilo Dark Souls (peso, nivel por matar mobs, requisitos
  de armas, críticos) y API para otros mods. Por ahora: nivel y XP por matar mobs, stats con
  puntos por nivel y vigor aplicado a la vida.
