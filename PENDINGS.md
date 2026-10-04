# Pendientes

Pendientes del repo en general. Los de cada mod están en su `README.md`. Quien cierre un ítem lo borra en el
mismo commit; quien deje algo pendiente lo agrega aquí. Sin historial: eso está en git.

- **Mod de habilidades.** Consume la API de `souls-stats` (ADR-0001). Definir qué hace cuando exista esa API.
- **Mod puente souls-stats ↔ Trinkets Updated.** Trinkets Updated (`eu.pb4:trinkets`, maven
  `https://maven.nucleoid.xyz/releases`, MIT) es el mod de accesorios activo para Fabric en 26.x; Accessories
  quedó en 1.21.x y Curios en 26.x es solo NeoForge. Su última versión (`4.2.1+26.3`) exige Minecraft 26.3.x
  y no carga en los snapshots de 26.4 que sigue el repo (ADR-0004): hacer el puente cuando publique para la
  versión del repo. El puente usa `SoulsStats.addEquipment` (README de souls-stats, "Extender desde otro mod").
- **Compilar un mod contra otro.** El `build.gradle` raíz aún no lo soporta; agregarlo con el mod de
  habilidades.
