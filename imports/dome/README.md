# Dome import

Original files copied from `origin/game-cleanup-and-ex-updates` at `e746aac`.
After full cleanup integration, the active classes are
`core/src/main/java/io/github/kirinojuju/enemy/boss/DomeBossBehavior.java` and
`core/src/main/java/io/github/kirinojuju/render/DomeSprite.java`.
Dome uses the shared `CombatRoom.Enemy` state in both ordinary rooms and Final Exam.

Dome uses boss ID 3 and is selected through the normal randomized stage plan.
It alternates lightning and laser, with a warning before each attack.
Missing character/effect PNGs are skipped, without placeholder images.
Missing sounds are silent. Restart the game after adding assets.

Put PNGs in `assets/characters/boss/Tnetwork(Dome)/`, using these exact names
(including the original spelling and capitalization). Numbers have no leading zero.

| Prefix | First / last filename |
| --- | --- |
| Prepare | `Casting_state_loop1.png` / `Casting_state_loop5.png` |
| Lightning cast | `Casting_lighting_loop1.png` / `Casting_lighting_loop6.png` |
| Lightning recovery | `Recovery_Lighting1.png` / `Recovery_Lighting5.png` |
| Laser cast | `Laser_casting_loop1.png` / `Laser_casting_loop19.png` |
| Side laser | `Laser_shooting_1.png` / `Laser_shooting_5.png` |
| Downward laser (reserved for anchored encounters) | `Laser_shooting_down_loop1.png` / `Laser_shooting_down_loop5.png` |
| Lightning effect | `Elements/Lighting_strom1.png` / `Elements/Lighting_strom4.png` |
| Laser effect | `Elements/Laser1.png` / `Elements/Laser4.png` |

Put OGG sounds in `assets/characters/soundfx/boss_fx/Dome/`:

- `lighting_efx_cast_sound.ogg`
- `lighting_efx_hit_sound.ogg`
- `laser_efx_cast_sound.ogg`
- `laser_efx_hit_sound.ogg`

Assets remain excluded by the project's existing `.gitignore`.
