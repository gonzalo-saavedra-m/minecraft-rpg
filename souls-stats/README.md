# souls-stats (`soulsstats`)

Stats de personaje al estilo Dark Souls: peso del equipamiento, nivel por matar mobs y stats que deciden
cuánto cargas, cuánto pegas, qué armas usas y cuántos críticos sacas. Es la base para un mod de habilidades
posterior, que dependerá de este (ADR-0006 de la raíz). Nombre y modid provisorios.

Hoy es solo un esqueleto: al iniciar escribe `Hello world` en el log. Lo de abajo es la visión; se itera
por partes (ver Pendientes).

## Qué hace

- **Sin stamina.** Correr, saltar, atacar y nadar quedan como en vanilla: limitarlos le quita la esencia a
  Minecraft. Tampoco hay almas, hogueras, roll ni inventario infinito.
- **Peso.** Cuenta la armadura equipada y lo que está en la mano principal y la secundaria. El resto de la
  hotbar y el inventario no pesan. La carga es peso actual / carga máxima:
  - **Ligera** (bajo X%): más velocidad que vanilla.
  - **Normal**: velocidad vanilla.
  - **Pesada** (sobre Y%): más lento.
  - **Sobrecarga** (sobre 100%): muy lento.
- **Stats** (se suben con puntos al pasar de nivel):
  - **Vigor**: vida máxima.
  - **Fuerza**: daño de armas que escalan con fuerza y requisito de armas pesadas.
  - **Destreza**: daño de armas que escalan con destreza y requisito de armas que la piden.
  - **Suerte**: probabilidad de crítico aleatorio.
  - La carga máxima sube con un stat (por decidir, ver Pendientes).
- **Equipamiento.** Cada ítem registra en el datapack:
  - **Peso** (armas y armadura).
  - **Stats requeridas** (armas, opcional), por ejemplo fuerza 18 y destreza 8. Usar un arma es tenerla en
    la mano y golpear con ella. Si no cumples las stats, el daño se multiplica por un factor de la config
    global (por ejemplo `0.1`).
  - **Escalado por stat** (armas, opcional): un número por stat que suma daño según esa stat. La UI (tooltip
    del ítem) lo muestra como letra, estilo Dark Souls (S, A, B, C, D, E).
  - **`usable_at_level`** (armas, opcional): bajo ese nivel no se puede golpear con el arma.
  - **`equipable_at_level`** (armadura, opcional): bajo ese nivel no se puede equipar.

  Las armas se limitan por stats y la armadura por peso. Los topes por nivel quedan a disposición del admin,
  pero no se recomiendan: la documentación oficial del mod debe decirlo.

  ```json
  { "item": "othermod:great_axe", "weight": 12,
    "requires": { "strength": 18, "dexterity": 8 }, "scaling": { "strength": 0.8, "dexterity": 0.1 } }
  ```
- **Nivel por matar mobs.** XP propia del mod, separada de la XP vanilla. Cada tipo de mob da una cantidad
  configurable. Para frenar las granjas sin anularlas, cada tipo de mob tiene un tope configurable (ver
  Pendientes).
- **Todo configurable con datapack.** JSON en `data/<namespace>/...`, recargable con `/reload`, por id de
  ítem o de entidad, así sirve igual para ítems y mobs de otros mods. Valores globales (umbrales de carga,
  curva de nivel, puntos por nivel, crítico, factor de daño sin stats) en un JSON del mismo datapack. Un
  ítem o mob sin configurar usa un valor por defecto derivado de sus atributos vanilla (armadura → peso,
  daño de ataque → requisitos, vida máxima → XP).
- **API para otros mods.** Un evento de Fabric (`Event`) al subir de nivel y métodos para consultar nivel,
  XP y stats de un jugador. La usará un mod de habilidades que se hará después.

## Referencia

[SoulsCrafter](https://modrinth.com/mod/soulscrafter) (`minesouls`, Fabric 26.2) implementa peso, stats
VIT/END/STR/DEX, requisitos y escalado de armas. Se descartó hacer fork: no publica código fuente (solo el
jar y `"license": "MIT"` en su metadata), sus stats están acopladas a almas y hogueras, calcula la stamina
en el cliente y adivina el perfil de cada arma por el nombre del ítem. Sirve como referencia de diseño y
números (`inv/EquipLoad`, `combat/WeaponStats`, `stats/PlayerStats`); no copiar código descompilado.

## Pendientes

Decisiones abiertas, para el dueño del repo:

- **Tope de XP por tipo de mob.** Opción A: XP total que un jugador puede ganar de ese tipo (ej. 5000 de
  creepers). Opción B: nivel hasta el que ese tipo da XP (ej. creepers dan XP hasta nivel 30). ¿Por jugador?
  ¿Se reinicia?
- **Qué stat sube la carga máxima.** Vigor, fuerza o uno propio (como Endurance/Vitality en Dark Souls).
- **Rangos de las letras de escalado.** Qué número corresponde a cada letra.
- **Formato del datapack.** Un archivo por ítem/mob o un archivo por namespace con una lista. Es formato de
  datos: va como ADR `proposed` en `souls-stats/docs/adr/`.
- **Nivel máximo y curva de XP.**

Orden sugerido para iterar, un paso por cambio:

1. Esqueleto del mod, nivel y XP guardados en el jugador (attachment de Fabric), XP por matar mobs con
   valores fijos y comando para ver nivel y stats.
2. Datapack de XP por mob y topes anti-granja.
3. Puntos por nivel, stats y pantalla o comando para repartirlos; vigor aplicado a la vida.
4. Peso con datapack y niveles de carga aplicados a la velocidad.
5. Nivel y stats requeridas, escalado con su letra en el tooltip; crítico por suerte.
6. API pública (evento de subida de nivel y consultas).
