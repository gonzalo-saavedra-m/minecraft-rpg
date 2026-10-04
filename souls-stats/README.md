# souls-stats (`soulsstats`)

Stats de personaje al estilo Dark Souls: peso del equipamiento, nivel por matar mobs y stats que deciden
cuánto cargas, cuánto pegas, qué armas usas y cuántos críticos sacas. Es la base para un mod de habilidades
posterior, que dependerá de este (ADR-0001 de la raíz). Nombre y modid provisorios.

Lo de abajo es la visión; se itera por partes (ver Pendientes). Primero todo funciona con comandos y mensajes
coloreados en el chat; una interfaz gráfica (HUD, pantallas) vendrá después, y el código se prepara para
eso: cada comando arma su salida en su clase de `command/` y los avisos viven en `display/` (ADR-0005 del
mod); la lógica no le muestra nada al jugador.

## Código

En `src/main/java/soulsstats/`, una carpeta por tema; `SoulsStats` es la entrada del mod y su API pública;
`SoulsStatsClient`, su entrada en el cliente.

- `event/`: `EventBus`, los eventos que emite el mod (`LevelUpEvent`, `StatsChangedEvent`) y `PlayerRefresh`
  (recalcular algo del jugador cuando sus stats pueden haber cambiado).
- `progress/`: nivel, XP y puntos del jugador (`PlayerProgress`), la XP por mob (`MobXpTable`) y
  `PlayerStatsPayload`, que le manda al cliente su nivel y stats.
- `stat/`: las stats (`Stat`, su registro), una clase por efecto de stat con el nombre `<Stat><Efecto>`
  (`VigorHealth`, `VigorLoad`, `DexteritySpeed`, `CriticalDamage`), el daño del arma según las stats
  (`WeaponScaling`) y el tótem del renacer
  (`RespecTotemItem`).
- `item/`: lo que `items.json` dice de cada ítem (`ItemProperties`), `ItemPropertiesPayload`, que lleva la
  tabla al cliente, y los topes por nivel (`LevelRequirements`).
- `weight/`: peso de un ítem y de lo equipado (`ItemWeight`) y carga del jugador con sus efectos (`Load`).
- `data/`: `Datapack` (cargador común de los JSON de datapack: tablas por id y objetos de config), `Config`
  (los números de `config.json`) y `Scaling` (una escala `base + per_point × puntos`).
- `command/`: un comando por clase (ADR-0005 del mod). `display/`: texto compartido, avisos y el tooltip de
  peso, stats, requisitos y escalado de los ítems (`ItemTooltip`).
- `mixin/`: cambios al código de Minecraft: el daño de un golpe pasa por el crítico (`LivingEntityMixin`) y,
  en el cliente, no se corre sobrecargado (`LocalPlayerMixin`).

## Qué hace hoy

- Cada jugador tiene nivel (desde 1) y XP propios, guardados en el jugador y conservados al morir.
- Matar un mob da la XP que dice la tabla `data/soulsstats/mob_xp.json` (ADR-0001 del mod): por defecto un
  zombie da 10, un creeper 20, un piglin 15, un piglin brute 60. Un mob que no está en la tabla (pasivos,
  golems, aldeanos, jugadores, mobs de otros mods) da 0. Cuenta si el jugador es quien mata, también con
  proyectiles.
- **Tope anti-granja.** Cada mob da XP solo hasta su `max_level`: un jugador de ese nivel o más ya no gana XP
  de él. Por defecto: mobs menores (slimes, silverfish, zombified piglin) hasta 10; zombies, esqueletos y
  arañas hasta 20; creepers, endermen, blazes, piglins hasta 30; ghasts, wither skeletons, breezes hasta 40;
  evokers, ravagers, piglin brutes hasta 50; los jefes (wither, warden, ender dragon, elder guardian) sin tope.
- El admin cambia la tabla con un datapack del mundo que trae el mismo archivo solo con los mobs a cambiar,
  por ejemplo `{"minecraft:creeper": {"xp": 50, "max_level": 60}, "minecraft:zombie": {"xp": 0}}`, y aplica
  con `/reload`. Cada mob se pisa entero. Las entradas inválidas se reportan en el log y se ignoran.
- Subir del nivel N al N+1 cuesta `level_xp` de `config.json`: por defecto `power`, 100 × N^1,2 (100 en el
  nivel 1, ~1585 en el 10, ~5923 en el 30), una curva que acelera como el costo en almas de Dark Souls. La XP
  sobrante pasa al nivel siguiente.
- Subir de nivel emite el evento `LevelUpEvent` (jugador y progreso nuevo) por `EventBus`, que llama a quienes lo
  escuchan con `EventBus.listen(LevelUpEvent.class, ...)`. `Notifications` lo escucha y avisa en el chat, en dorado.
- **Stats.** Vigor, fuerza, destreza y suerte (en comandos `vigor`, `strength`, `dexterity`, `luck`) parten en 10. Cada nivel sobre 1 da un punto para repartir
  (puntos libres = nivel − 1 + puntos extra − puntos ya repartidos). Otros mods suman stats con `Registry.register(Stat.REGISTRY, ...)` en su
  `onInitialize`; las de este mod no se pueden quitar (ADR-0003 del mod). Los puntos de una stat que ya no
  existe (mod desinstalado) vuelven a estar libres. Efectos, con sus números en
  `data/soulsstats/config.json` (ADR-0006 del mod; los valores por defecto son provisionales, por punto sobre la
  base de la stat):
  - **Vigor → vida** (`vigor_health`, `soft_caps`, como los soft caps de Dark Souls): +2 (un corazón) por
    punto hasta 15 puntos, +1 hasta 30 y +0,25 después.
  - **Vigor → carga máxima** (`vigor_load`, `linear`): 30 + 2 por punto (la carga de Dark Souls crece casi
    lineal).
  - **Fuerza (y cualquier stat) → daño del arma**, según cuánto escale el arma con ella; ver "Armas" abajo.
  - **Destreza → velocidad de ataque** (`dexterity_attack_speed`, `hyperbolic`): hasta +60 %, con +30 % a los 20
    puntos. **Y de movimiento** (`dexterity_movement`, `hyperbolic`): hasta +15 %, con +7,5 % a los 30, encima de
    la carga y sin zoom de poción.
  - **Suerte → crítico propio** (`luck_critical_chance`, `hyperbolic`; no el de Minecraft al golpear cayendo): 5 %
    de base, que tiende a 50 % (27,5 % a los 25 puntos), de multiplicar el daño del golpe por 1,5
    (`critical_damage`), con chispas azules y el sonido de crítico.

  Valores por defecto según los puntos sobre la base (vida en puntos de vida; el resto, el bonus):

  | Puntos | Vida | Carga | Curva de arma | Vel. ataque | Vel. movimiento | Crítico |
  |---|---|---|---|---|---|---|
  | 0 | +0 | 30 | 0 | +0 % | +0 % | 5 % |
  | 10 | +20 | 50 | 0,3 | +20 % | +3,8 % | 17,9 % |
  | 20 | +35 | 70 | 0,6 | +30 % | +6 % | 25 % |
  | 40 | +47,5 | 110 | 0,9 | +40 % | +8,6 % | 32,7 % |

  **Config.** El admin cambia cualquier efecto con un datapack que trae `config.json` solo con los campos a
  cambiar (se mezcla campo a campo, ver "Varios datapacks" abajo) y `/reload`, que recalcula a los conectados. Cada efecto elige su
  función con `"type"`; todas suman a `base` (opcional, 0) según los puntos p sobre la base de la stat, y con
  puntos negativos se aplican en espejo:

  | `type` | Campos | Da | Sirve para |
  |---|---|---|---|
  | `linear` | `per_point` | `per_point × p` | cada punto vale lo mismo |
  | `power` | `per_point`, `exponent` | `per_point × p^exponent` | curvas que aceleran (exponente > 1), como la de XP |
  | `logarithmic` | `scale` | `scale × ln(1 + p)` | rinde menos con cada punto, sin tope |
  | `hyperbolic` | `max`, `half` | `max × p / (p + half)` | tiende a `max` sin alcanzarlo; la mitad en `half` puntos |
  | `soft_caps` | `steps`: `[{"until": n, "per_point": x}, ..., {"per_point": y}]` | tramos lineales | soft caps de Dark Souls |

  Por ejemplo, `{"weapon_scaling": {"type": "linear", "per_point": 0.03}}` vuelve lineal la curva de armas.
- **Comandos.** Cada uno vive en su clase en `src/main/java/soulsstats/command/` (por ejemplo
  `RaiseCommand`), con lo que hace, lo que responde y su descripción (`COMMAND`, un `SubCommand`).
  `SoulsStatsCommand.SUBCOMMANDS` es el registro: de ahí se registran y de ahí lee `/soulsstats help`, que
  lista cada subcomando que el jugador puede usar con su sintaxis (sacada del árbol de comandos) y su
  descripción. Un comando nuevo se agrega a esa lista. `/soulsstats` muestra en colores nivel, XP, puntos libres y stats. `/soulsstats raise <stat>
  [amount]` reparte puntos (por ejemplo `/soulsstats raise vigor 3`); si no alcanzan, dice cuántos quedan.
  Para admins (nivel de permiso 2): `/soulsstats points <players> <amount>` da puntos libres que no
  vienen del nivel (comprados, de recompensa), y `/soulsstats points <players> <amount> <stat>` da
  puntos fijos en esa stat, que el tótem del renacer no devuelve. `/soulsstats levels <players> <amount>`
  sube niveles (avisa la subida como al matar) o los baja, sin bajar de 1; si el nivel nuevo pide menos XP,
  la XP se recorta. Una cantidad negativa los quita.
- **Clases base.** No hay clases propias; se arman con los puntos fijos. Un mago que parte con inteligencia
  14 y fuerza 8 es `/soulsstats points @p 4 intelligence` y `/soulsstats points @p -2 strength`, por ejemplo
  desde un bloque de comandos o un comando consumible.
- **Tótem del renacer** (`soulsstats:respec`). Consumible que devuelve todos los puntos repartidos (no los
  fijos ni los extra, que siguen libres). Sin receta
  ni drop: los admins deciden cómo se consigue (tiendas, drops de jefes, `/give`). No se gasta si no hay
  puntos repartidos.
- Cambiar stats (subir o reasignar) emite `StatsChangedEvent`; el vigor se recalcula con ese evento, al reaparecer
  y al entrar al server.
- **Peso de ítems** (ADR-0007 del mod). Cada ítem pesa lo que dice `data/soulsstats/items.json`, por ejemplo
  `{"minecraft:shield": {"weight": 4}}`. Un ítem sin
  entrada pesa lo que suma en armadura, dureza y daño de ataque (provisional): un peto de hierro 6, uno de
  diamante 10, una espada de hierro 5. Un arma pesa por su daño por golpe, no por su DPS: un hacha de diamante
  (8) pesa más que una espada de diamante (6), aunque pegue más lento; lo que no tiene esos atributos (bloques, élitra) pesa 0. Por defecto la tabla solo trae
  el escudo (4) y la maza (10). El tooltip de todo ítem que pese algo muestra "Peso N" (si el cliente tiene el
  mod): el servidor le manda la tabla al entrar y en cada `/reload`.
- **Ítems que suben stats.** Una entrada de `items.json` puede traer `stats`, que suman (o restan, si son
  negativas) mientras el ítem está equipado (armadura, manos o slots de un mod puente), por ejemplo un
  anillo de vigor: `{"othermod:vigor_ring": {"weight": 0.5, "stats": {"vigor": 3}}}`. Una stat sin namespace es
  de este mod; las de otros mods llevan el suyo (`"magic:faith": 2`); una stat desconocida se ignora. `weight`
  es opcional: sin él, el peso se deriva de los atributos. Cuentan para todos los efectos (vida, carga, daño,
  velocidades, crítico) y para lo que muestra `/soulsstats`, pero no para los puntos libres ni el tótem del
  renacer. El tooltip las muestra ("+3 Vigor"). El mod no trae ítems con stats: los define cada datapack.
- **Armas: escalado y requisitos.** Una entrada de `items.json` puede traer `scaling` (cuánto escala el arma
  con cada stat) y `requires` (stats mínimas para usarla bien), por ejemplo
  `{"othermod:great_axe": {"requires": {"strength": 18}, "scaling": {"strength": 1.2, "dexterity": 0.3}}}`.
  Como en Dark Souls, el arma en la mano suma daño del arma × Σ escalado × `weapon_scaling`(puntos de esa
  stat). La curva por defecto es `soft_caps` (0,3 a los 10 puntos, 0,6 a los 20, 0,9 a los 40 y +0,005 por
  punto después): una espada de diamante (7 de daño) con escalado S (1,75) pega 10,2 con 10 puntos, 13,3 con 20
  y 16,5 con 40 (+136 %); con C (0,7), 8,3, 9,5 y 10,8. Para un techo duro, `hyperbolic`. Un arma que hace
  daño y no trae `scaling` usa `default_weapon_scaling` (fuerza 0,7, una C), así la fuerza sirve con armas
  vanilla. Si no cumples `requires` (contando los ítems equipados), no hay bonus y el daño total se multiplica
  por `unmet_requirement_damage` (0,4). El tooltip agrega una sección "Requisitos:" con una stat por línea
  ("Strength: 18", en rojo si no la cumples: el servidor le manda al cliente el nivel y las stats del jugador,
  con lo equipado, cada vez que cambian) y otra "Escalado:" con su letra ("Strength: A"); con el tooltip avanzado (F3+H), cada letra
  lleva su número: "Strength: A (1.2)". Letras, de
  `scaling_grades` en `config.json` (los cortes de Elden Ring y Dark Souls 3): S desde 1,75, A desde 1,40, B
  desde 0,90, C desde 0,60, D desde 0,25 y E bajo eso. El cliente recibe `config.json` del servidor para esto.
- **Topes por nivel.** Una entrada de `items.json` puede traer `usable_at_level` (armas: bajo ese nivel el
  golpe se cancela y la barra de acción dice "Necesitas nivel N para usar esta arma") y `equipable_at_level`
  (armadura: bajo ese nivel la pieza no se queda puesta, vuelve al inventario o cae si está lleno, con "Necesitas
  nivel N para equipar esto"). Se revisa en cada cambio de equipo, así no importa cómo se equipó, y también al
  entrar, al reaparecer y al bajar de nivel por comando. El tooltip los suma a "Requisitos:" (" Nivel: 30", "
  Nivel para equipar: 25"). Las armas se limitan mejor por stats y la armadura por peso: estos topes quedan
  para el admin que los quiera, pero no se recomiendan.
- **Varios datapacks** (ADR-0007 del mod). `items.json` y `config.json` pueden venir de muchos lados: el jar
  de este mod, el de otros mods, datapacks de contenido y los datapacks del mundo con los que configura el
  admin. Se aplican de menor a mayor prioridad (mods primero, luego los del mundo en el orden de
  `/datapack list`; el admin puede subir uno con `/datapack enable <pack> last`) y se **mezclan campo a
  campo**: cada uno pisa solo lo que trae, también dentro de `stats` (una stat sin tocar las otras). Así un
  datapack de contenido define un ítem completo y el admin le cambia solo el peso sin perder sus stats. Para
  anular algo, se pone en 0. Cada campo pisado queda en el log del servidor, por ejemplo
  `items.json: file/admin pisa minecraft:mace.weight = 10 con 12`, y una entrada inválida se descarta sola.
  `mob_xp.json` sigue pisando cada mob entero.
- **Idioma** (ADR-0009 de la raíz). Todo lo que ve el jugador sigue el idioma del juego: inglés (en el código,
  así un cliente sin el mod lo lee igual), español, portugués de Brasil, ruso, alemán, francés, chino
  simplificado, japonés, coreano, polaco, italiano, turco y ucraniano, los más jugados. Cada idioma es un
  archivo en `assets/soulsstats/lang/`, y el build lo copia a sus variantes (el español a `es_mx`, `es_ar`,
  `es_cl`, `es_ec`, `es_uy` y `es_ve`; `pt_br` a `pt_pt`; `fr_fr` a `fr_ca` y `fr_ch`; `de_de` a `de_at` y
  `de_ch`). Los comandos, sus argumentos y los ids de stats van siempre en inglés.
- **Peso equipado.** Suma la armadura y lo que hay en ambas manos; el resto del inventario no pesa. Un mod
  puente puede sumar los slots de un mod de accesorios.
- **Carga.** Peso equipado / carga máxima (`vigor_load`, por defecto 30 + 2 × puntos de vigor; 30 con vigor 10: hierro
  completo con espada es carga normal y diamante completo con espada, sobrecarga). Cambia velocidad al
  caminar y correr, y salto (valores provisionales):

  | Carga | Peso / máximo | Velocidad | Salto |
  |---|---|---|---|
  | Ligera | hasta 30 % | +15 % | ~1,37 bloques (no alcanza una valla) |
  | Normal | hasta 70 % | vanilla | ~1,25 bloques (vanilla) |
  | Pesada | hasta 100 % | −20 % | ~1,1 bloques (siempre sube un bloque) |
  | Sobrecarga | sobre 100 % | −40 %, sin correr | ~0,9 bloques (no sube un bloque) |

  Se recalcula al cambiar equipo (incluido cambiar de ítem en la mano), stats, al reaparecer y al entrar.
  La velocidad no se aplica como modificador sino en la velocidad base del jugador (`walkingSpeed`), así no
  hay zoom del campo de visión como con una poción, aunque el cliente no tenga el mod. Ojo: otro mod que
  cambie `walkingSpeed` choca con este, y si se desinstala el mod el jugador queda con su última velocidad
  guardada. Sobrecargado no se puede correr: lo decide el cliente, así que eso solo rige si el cliente tiene
  el mod. `/soulsstats weight` muestra peso, carga máxima y nivel de carga.

## Qué hace

- **Sin stamina.** Correr, saltar, atacar y nadar quedan como en vanilla: limitarlos le quita la esencia a
  Minecraft. Tampoco hay almas, hogueras, roll ni inventario infinito.
- **Peso.** Cuenta la armadura equipada, lo que está en la mano principal y la secundaria y, con un mod
  puente, los slots de un mod de accesorios (anillos, amuletos). El resto de la hotbar y el inventario no pesan. La carga es peso
  actual / carga máxima, y cambia velocidad y salto de forma levemente perceptible:
  - **Ligera** (bajo X%): algo más rápido y un poco más de salto, sin llegar a saltar una valla.
  - **Normal**: velocidad y salto vanilla.
  - **Pesada** (sobre Y%): más lento y menos salto, pero siempre salta un bloque.
  - **Sobrecarga** (sobre 100%): muy lento y no alcanza a saltar un bloque.

  La velocidad no debe sentirse como un efecto de poción (sin zoom del campo de visión ni partículas).
- **Stats** (se suben con puntos al pasar de nivel):
  - **Vigor**: vida máxima y carga máxima.
  - **Fuerza**: daño de armas que escalan con fuerza y requisito de armas pesadas.
  - **Destreza**: daño de armas que escalan con destreza y requisito de armas que la piden.
  - **Suerte**: probabilidad de crítico aleatorio (hecho: crítico propio, no el de Minecraft).
- **Equipamiento.** Cada ítem registra en el datapack:
  - **Peso** (armas y armadura).
  - **Stats requeridas** y **escalado por stat** (armas, opcional): hecho, ver "Armas" arriba.
  - **`usable_at_level`** y **`equipable_at_level`** (opcionales): hecho, ver "Topes por nivel" arriba.

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
  ítem sin configurar usa un valor por defecto derivado de sus atributos vanilla (armadura → peso, daño de
  ataque → requisitos); un mob sin configurar no da XP.
- **API para otros mods.** Un evento de Fabric (`Event`) al subir de nivel y métodos para consultar nivel,
  XP y stats de un jugador. La usará un mod de habilidades que se hará después.

## Extender desde otro mod

Guía para integradores. Un mod que extiende este declara `"soulsstats"` en `depends` de su `fabric.mod.json`
(ADR-0001 de la raíz) y usa lo de abajo en su `onInitialize`. Lo marcado **(pendiente)** aún no existe.

- **Escuchar eventos.** `EventBus.listen(<Evento>.class, event -> ...)`; corren en el hilo del servidor, en
  orden de registro.
  - `LevelUpEvent(player, progress)`: el jugador subió uno o más niveles; `progress` es su estado nuevo.
  - `StatsChangedEvent(player, progress)`: cambiaron sus stats (subió una o usó el tótem del renacer).
- **Dar peso y stats a los ítems de un mod.** Sin hacer nada, pesan lo que suman en armadura, dureza y daño de
  ataque y no dan stats. Para otra cosa, el mod trae `data/soulsstats/items.json` en su jar con sus ítems, por
  ejemplo `{"othermod:great_axe": {"weight": 12}, "othermod:vigor_ring": {"stats": {"vigor": 3}}}`; un
  datapack del mundo lo puede pisar, campo a campo.
- **Leer una stat con lo equipado.** `SoulsStats.stat(player, stat)` es el valor que usan los efectos: el del
  progreso más lo que suman sus ítems equipados. `stat.points(player)` es lo mismo menos la base.
- **Sumar slots de equipamiento (mod puente).** `SoulsStats.addEquipment(entity -> <ítems equipados>)`
  suma ítems a la armadura y las manos, por ejemplo los slots de un mod de accesorios. Cuentan para el peso
  y para las stats que den los ítems. **(Pendiente)** un método público para recalcular la
  carga: hoy un cambio en esos slots se aplica en el próximo cambio de equipo vanilla.

  ```java
  SoulsStats.addEquipment(entity -> { List<ItemStack> items = new ArrayList<>();
      TrinketsApi.getAttachment(entity).forEach((slot, stack) -> items.add(stack)); return items; });
  ```
- **Registrar una stat.** Va al registro `soulsstats:stat` con su base; su path es el nombre en
  `/soulsstats raise`. Las stats de este mod no se pueden quitar (ADR-0003 del mod). El efecto de la stat es
  del mod que la registra: `PlayerRefresh.register(player -> ...)` lo recalcula al cambiar stats, al
  reaparecer, al entrar, al cambiar de equipo y con `/reload`, como hacen `VigorHealth` y `WeaponScaling`.
  Su nombre visible también lo pone el mod que la registra, en sus archivos de idioma con la clave
  `stat.<namespace>.<id>`, por ejemplo `"stat.magic.intelligence": "Inteligencia"` en
  `assets/magic/lang/es_es.json`; sin traducción se muestra su id con mayúscula ("Intelligence").

  ```java
  Stat INTELLIGENCE = Registry.register(Stat.REGISTRY, Identifier.fromNamespaceAndPath("magic", "intelligence"), new Stat(10));
  ```
- **Leer el progreso de un jugador.** `SoulsStats.progress(player)` devuelve su `PlayerProgress`: `level()`,
  `xp()`, `nextLevelXp()`, `stat(stat)` (valor final), `freePoints()`, `points()` (repartidos por stat),
  `extraPoints()` y `bonus()` (fijos por stat). Es inmutable: para cambiarlo, los métodos de abajo.
- **Dar niveles.** `SoulsStats.giveLevels(player, cantidad)`, por ejemplo niveles comprados. Negativo los
  quita, sin bajar de 1. Si sube, emite `LevelUpEvent`.
- **Dar puntos libres fuera del nivel.** `SoulsStats.giveFreePoints(player, cantidad)`, para puntos
  comprados o de recompensa. Negativo los quita; si los libres quedan bajo 0, el jugador los recupera
  subiendo de nivel. Emite `StatsChangedEvent`.
- **Dar puntos fijos en una stat.** `SoulsStats.giveBonus(player, stat, cantidad)`: suman al valor de la stat
  y el tótem del renacer no los devuelve. Sirve para clases base (un mago con más inteligencia) o
  recompensas permanentes. Negativo la baja. Emite `StatsChangedEvent`.
- **Clases base como contenido (pendiente).** Hoy una clase es una serie de `giveBonus` (o del comando
  `/soulsstats points`). Si hace falta elegir clase al entrar o listarlas, definirlas en un datapack.

## Referencia

[SoulsCrafter](https://modrinth.com/mod/soulscrafter) (`minesouls`, Fabric 26.2) implementa peso, stats
VIT/END/STR/DEX, requisitos y escalado de armas. Se descartó hacer fork: no publica código fuente (solo el
jar y `"license": "MIT"` en su metadata), sus stats están acopladas a almas y hogueras, calcula la stamina
en el cliente y adivina el perfil de cada arma por el nombre del ítem. Sirve como referencia de diseño y
números (`inv/EquipLoad`, `combat/WeaponStats`, `stats/PlayerStats`); no copiar código descompilado.

## Pendientes

Decisiones abiertas, para el dueño del repo:

- **Sprite del tótem del renacer.** Hoy usa el del tótem vanilla; hacer uno propio.
- **Interfaz gráfica.** HUD de nivel y XP, pantalla para repartir puntos y ver stats. Mientras tanto,
  todo por comandos y chat coloreado (ADR-0005 del mod); un comando podrá abrir una pantalla.
- **Revisar traducciones.** Las de idiomas distintos del español son una primera versión; conviene que las
  revise un hablante nativo.
- **Nivel máximo.** No hay; la curva de XP (`level_xp`) acelera, pero no tiene tope.
- **Niveles de carga en config.** Umbrales, velocidades y saltos siguen en `Load`, porque el cliente los usa
  para detectar la sobrecarga; pasarlos a `config.json` requiere mandárselos al cliente.

Orden sugerido para iterar, un paso por cambio:

1. Peso, en tres partes:
   1. ~~Peso por ítem con datapack~~ (hecho).
   2. ~~Peso del jugador: armadura y manos~~ (hecho). Los accesorios entran por un mod puente aparte (ver
      `PENDINGS.md` de la raíz).
   3. ~~Efecto de la carga en velocidad y salto~~ (hecho; probar y calibrar en el cliente).
   4. ~~Ítems equipados que suben stats~~ (hecho: `stats` en `items.json`).
2. ~~Stats requeridas, escalado con su letra en el tooltip y topes por nivel~~ (hecho).
3. API pública (evento de subida de nivel y consultas).
