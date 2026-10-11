# มาทั้งที่ก็เป็นนักเรียนติดลูปไปซะได้

A small playable libGDX action roguelike prototype about surviving computer engineering school life. This is a student-themed spin-off of Pusenbi. Its visual direction carries over Pusenbi's simple pixel-art style and adapts the characters, clothing, enemies, and settings to ISNE student life.

The displayed game name is defined in `schoolLife.GAME_TITLE`. Thai title lettering is bundled in `core/src/main/resources/ui/game-title.png`, so it also works without the local art pack. When VS Code has not copied that resource yet, the renderer reads it directly from the source folder; a missing title image does not prevent the game from opening. After editing the name, regenerate its lettering with a Thai-capable font installed:

```powershell
java "-Djava.awt.headless=true" tools/GenerateGameTitle.java
```

## Where to find the code

Start with `core/src/main/java/io/github/kirinojuju/schoolLife.java`. The 54 main Java files are organized into matching folders and Java packages:

| Folder | Responsibility |
| --- | --- |
| `flow/` | Screens, room progression, stages and loops |
| `player/` | Player movement, skills and upgrades |
| `weapon/` | LAN, Notebook and weapon abilities |
| `combat/` | `CombatRoom` coordinates room state, `CombatDamage` applies hits, and `ProjectileSystem` moves and collides shots |
| `enemy/` | Shared AI, monster behaviors, spawning and spacing |
| `enemy/boss/` | Ajarn boss behaviors |
| `enemy/ex/` | EX monster behaviors |
| `render/` | Drawing, character animations and fallback textures |
| `world/` | Arena bounds, entrance scene and table obstacles |
| `audio/` | Game sounds |

See [CODE_ARCHITECTURE.md](CODE_ARCHITECTURE.md) for the six-file reading order, the three combat files, frame order and a guide to finding the code for each feature. The desktop launcher remains in `lwjgl3`; the rules check remains in `core/src/test/java`.

## Clone and play

The team's original art and sound files are kept locally and are not included in Git. A fresh clone includes an empty `assets/` folder and runs with simple generated placeholder art and no sound. If you have the original files, put them under `assets/` using the paths shown in the art and sound sections below; the game will use each file that exists and fall back only for missing files. Do not commit those local files.

Install a JDK (17 or newer) and clone the repository. The first Gradle run needs internet access to download dependencies.

```powershell
git clone https://github.com/kirinojuju/survive_isne_no_escape.git
cd survive_isne_no_escape
.\gradlew.bat :lwjgl3:run
```

On macOS or Linux, use `./gradlew :lwjgl3:run`. To verify the rules and compile without opening the game, run `.\gradlew.bat :core:verifyGameRules :lwjgl3:classes` on Windows.

Each teammate should create a branch from the latest shared branch before coding, for example:

```powershell
git switch -c feature/dim-monster-ability
# Make and test your own change.
git add core/src/main/java core/src/test/java
git commit -m "Add telegraphed monster ability"
git push -u origin feature/dim-monster-ability
```

Use a branch name for your own feature and open a pull request to combine it with the team's work. Access to a private GitHub repository must be granted by its owner before teammates can clone or push.

At the title screen, press **Enter** to arrive at Building 30. Use **WASD** or the arrow keys to walk. Reach the left door to enter the core game. Near the car, press **E** or click the car to choose whether to go home; **1/Y/Enter** confirms the joke ending and **2/N/Esc** returns to the entrance. The opening scene has no combat. Scene changes fade through black.

Inside the core game, both the LAN cable and notebook are available. Press **Tab** to toggle weapons or **1**/**2** to select one directly during combat. Use **WASD** or the arrow keys to move, hold the **left mouse button** (or **Space**) for a normal attack, and press **Shift** to dash. Clicking near an enemy locks that target while the button is held; clicking empty ground locks the nearest living enemy. The student turns toward the target, a green ring shows the lock, and notebook shots home toward it. Press **E**, or tap the on-screen **BALL** button, to stand still and cast PowerBall. The skill locks the nearest living enemy, the student turns to follow it during the cast, and the PowerBall homes toward it. If that enemy falls, the ball locks another living enemy in the room. The cast costs 30 SP and has a 2-second cooldown. A dash costs 25 SP. Ranged shots cost 10 SP; close-range attacks are free. SP regenerates over time and on kills. Press **Enter** after clearing a monster or EX room. After each Ajarn, click one of three upgrades or press **1**, **2**, or **3** to advance. If you fall, press **R** to start a new run.

Press **F**, or click **ICE**, to raise Ice Wall exactly 92 units in the direction the student is facing. Its position stays fixed until it expires. An outward cast with its center outside the floor fails without spending SP or starting cooldown. It costs 25 SP, has a 7-second cooldown and lasts 4.5 seconds. The barrier blocks movement, dashes and enemy projectiles, including boss shots; friendly shots pass through. Raising it deals 24 damage and briefly stuns nearby enemies. The wall and cooldown reset on room entry. The ten frames in `assets/weapons/icewall/` grow upright along the barrier instead of rotating the artwork onto the floor.

Potions are consumable inventory items: press **Z** for **+40 HP** or **X** for **+35 SP**. A new run starts with two of each; remaining bottles carry between rooms. Using a potion at full HP/SP does not consume it. The HUD shows the real HP/SP bottle PNGs and remaining counts. Replenishment and monster drops are not implemented yet.

The LAN cable now sweeps a wide arc in the direction the student faces. Its reach is 125 game units and it swings every 0.44 seconds; the notebook attacks every 0.28 seconds. PowerBall is larger and explodes on impact, damaging and knocking back nearby enemies. Ordinary monsters are pushed farther than bosses. The blast briefly stuns and then slows enemies; bosses recover faster.

When a cyan binder shot catches you, movement stops. Alternate **A** and **D** (or **Left** and **Right**) six times to break free early. The bind also expires after 3.5 seconds. Purple warpers mark a red target on the floor before teleporting in for a heavy hit; move out of the marker before they arrive.

The upper wall contains windows. The student and monsters are restricted to the floor below them, including during dashes and teleports.

## Run structure

### Direct EX playtest

Run `.\gradlew.bat :lwjgl3:run --args="--test-ex" --no-daemon` to enter a Liquid Cat fight immediately. In EX test mode, **F1** selects Liquid Cat, **F2** selects Allergic Vacuum, and **F3** selects Insecure Sword. **R** retries the current boss at any time; **Enter** after a win also starts a fresh fight against that boss. Each reset restores 100 HP/SP, both weapons, and the room's initial pickups. Notebook starts equipped; use **1/2** or **Q** to switch weapons. LAN Tether and Notebook Heat Beam are available during the EX fight.

To start with a specific boss, replace `--test-ex` with `--test-liquid-cat`, `--test-allergic-vacuum`, or `--test-insecure-sword`. These shortcuts are active only when an EX test flag is supplied.

### Other direct tests

To test table cover directly, run `gradlew.bat :lwjgl3:run --args="--test-obstacles"`. Monster and boss rooms use `scene/sceneroom_not_table.png` with three separate tables, randomized once on room entry. Left, middle and right zones leave wide detours; the middle table alternates between the top and bottom of the room to keep the player's spawn clear. Table art also varies among table00, table01 and table04; the middle uses a low table. Walk around them with WASD/arrow keys; Shift stops before a table. Tables block player and ordinary monster projectiles. Boss projectiles travel over tables, including reflected shots and Sati's spear. Press 2 to try Notebook impacts against cover, or E to explode PowerBall against a table. Monsters route around the furniture. Tables are permanent; EX and Final Exam rooms keep their existing scene rules.

To test the Final Exam directly, run `gradlew.bat :lwjgl3:run --args="--test-final-exam"`. Press Enter on the black cutscene to begin. The test starts with Notebook equipped; use A/D or Left/Right to move, Shift for a horizontal dash, and click/Space or E to attack. Restarting with R returns to this test cutscene.

- Each loop shuffles four school-themed stages and picks four distinct Ajarns from a pool of eight, with one from each combat family.
- Each stage has a regular monster room, then an Ajarn boss room. Normal bosses start with two different minions in loop 1, three in loop 2 and five from loop 3 onward. First-loop bosses introduce minion types gradually, starting with basic and ranged monsters; all six types can appear from loop 2. Initial minions spawn once; Ken and Sati can summon additional armored helpers while fewer than four regular minions are alive. Defeat both the boss and its minions to clear the room.
- Each defeated Ajarn offers three random upgrades; choose one permanent upgrade for that run before the next room.
- After stage four and any queued EX or Final Exam fight, the next loop begins. Each additional loop adds 15% of base HP/armor, 10% SP/regen, 10% damage and 4% speed.
- The first random EX monster appears after the final Ajarn of loop 1; subsequent EX encounters retain the schedule after loops 2, 4, 6 and so on. Defeating Liquid Cat, Allergic Vacuum, or Insecure Sword grants its corresponding permanent buff for that run.
- Defeating Ajarn Ae, Far (Fa), and Dome during the same run unlocks the separate Final Exam server room once. If the third victory coincides with an even-loop EX encounter, the EX monster is fought first. During loop 1, a ready Final Exam waits until after the introductory EX, even when its three required victories are reached before stage four. A black cutscene then says "Good...Let's us prepare the final EXAM" before the three Ajarns appear together.
- In the Final Exam, the three Ajarns stay on the three upper server cabinets. The student stays in the lower lane and can move or dash only left and right; Notebook and PowerBall can reach the bosses above. Press Enter on the cutscene to start the fight. Clearing all three returns to the normal room sequence.
- LAN Tether and Notebook Heat Beam become available only during the EX fight. They switch off when that encounter ends and do not appear among the normal boss upgrade cards.
- Monster rooms restore 12 HP on victory; Ajarn defeats restore 35 HP. The run continues until the player is defeated.

## Enemy encounters

- Regular monsters move apart when they crowd each other. Green ranged monsters keep their distance, blue wave monsters fire expanding energy waves, white shielded monsters absorb damage with armor, cyan binders stop movement, and purple warpers teleport to a marked spot.
- Loop 1 introduces these enemies gradually: ranged in stage 1, waves in stage 2, shields and binders in stage 3, then warpers in stage 4. Later loops add random special monsters more often.
- Ajarns appear at the far right and hold their ground between attacks. Shared traits include reflecting armor, warp strikes, huge projectiles and energy waves. Every boss room has initial minions; Ken and Sati also use a capped summon queue. Sati transforms his C++ element into a spear. Ken places 3–5 warned bombs around the arena. M splits once at half HP and both bodies fire aimed projectiles. Algorithm monsters have their own sprite and ball projectile. Far uses the existing artillery family; Voltage and Thesis remain placeholder bosses.
- Ajarn Ae waits six seconds before his first warp warning, then marks a radius-75 area for 0.8 seconds. Walk out of the fixed circle or dash at impact to avoid the alternating one-second bind / base-14-damage hit. He returns home after 0.38 seconds and waits a seven-second cooldown before warning again (scaling down to at least five seconds in later loops). His regular spread fires every 1.1 seconds, with base speed 220 and damage 8 per shot. Final Exam Ae uses the slower spread while staying on his cabinet. In normal boss rooms, Ae also warns for 0.9 seconds before sweeping a vertical laser from left to right over 5 seconds, then resting for 5 seconds. The sweep uses base damage 16 and width 32; dash invulnerability protects against it. Final Exam Ae retains his cabinet behavior.
- Ajarn Dome uses the `Tnetwork(Dome)` character and effect animations. He alternates a lightning strike marked for 0.85 seconds (28 SP, base damage 22, radius set by `LIGHTNING_RADIUS`, currently 100) and a laser charged for 1 second (38 SP, base damage 18, active for 0.6 seconds). Lightning and the normal-room laser target the player's position at cast start, allowing a dodge. In Final Exam, his laser fires straight down from the server cabinet. Dash invulnerability protects against both attacks. Dome keeps reflecting armor in his normal boss room, and these skills replace his generic wave attacks. Recovery lasts 0.3 seconds and the pause between casts is 0.9 seconds (scaling down to 0.55 seconds in later loops).

### EX encounters

- **Liquid Cat:** alternates between full cat and a slower, smaller compact form. The small form takes reduced damage and leaves slowing ooze. Transformation locks movement and attacks. Divide locks its aim, then releases one bullet halfway through the animation. In full cat form it marks a fixed landing point before a heavy orbital leap; the attack finishes before changing form and briefly recovers after landing. Walking respects Ice Wall; the leap can cross it, but the landing body is separated from the wall. The warning marker remains the damage center.
- **Allergic Vacuum:** draws loose dust and the nearby student toward its nozzle. Each consumed dust piece fills its allergy meter. At 100%, suction stops for a sneeze warning, then it recoils and ejects its collected dust as damaging projectiles. The dust settles and can be sucked up again.
- **Insecure Sword:** drops Post-it notes. Walk over a note to pick it up, then approach the sword to apply it automatically. Each note lowers its armor but raises its confidence and chance of a powerful marked slash. A missed slash leaves the sword open to extra damage. At low confidence it often wobbles and deals zero damage.

## Art handoff

The student now renders from transparent 64 × 64 PNG frames in `assets/characters/student_player/`: `idle` (left/right), `walk` (four frames per side), and `skill_case` (eight left / nine right). The sprite uses nearest-neighbor filtering at 2× size. `skill_case` plays only during the E/touch skill; `power_ball.png` is released partway through that animation. Normal weapon attacks do not trigger the skill animation. The character source is `../characters/main/aseprite/student001.aseprite`.

The title screen uses `assets/scene/ปกเกม/Whatzz.png` as a full cover with a readable Enter prompt. The Building 30 entrance uses `assets/scene/scene30building_entrance_withoutcar.png` with the separate `scene30bCar_object.png` overlay at a crisp 3× scale. Monster and boss rooms use `assets/scene/sceneroom_not_table.png`, repeated across the wide arena, with three separate table objects and matching collision. EX encounters still use `assets/scene/sceneroom.png`. Sati's `cpp_element` uses six idle frames, sixteen transformation frames, and the spear sprite. Liquid Cat now uses the team's 62 exported PNG frames in `assets/characters/boss_ex/liquid-cat/` for idle, compact-form walking, transformation, divide, orbital strike and bullet attacks. The `skill` folder is still empty. Insecure Sword uses five idle and five attack PNG frames in `assets/characters/boss_ex/insecure-sword/`, at a crisp 2x scale. Its attack animation starts on the slash or zero-damage whiff; applying a Post-it interrupts it. Allergic Vacuum still uses temporary game shapes. The team's `Frame.png` and `Concept.png` files guide their behavior; they are presentation boards rather than game sprite frames. Sati's main body still uses a placeholder. Ordinary enemies use Slime (Basic/Shielded), Blue Blob (Binder/Wave) and Green Blob (Warper/Ranged); their six behaviors and colored role markers are preserved. Blue/Green sheets contain 64x32 cells, with six idle and eight run frames. `MonsterSprite` selects an individual cell, then crops only its transparent side padding. Algorithm monsters use their dedicated art if provided, otherwise the same monster sprites. Tables in monster and boss rooms block movement, dashes and ordinary projectiles.

The on-screen skill button accepts touch or mouse input. This repository currently has a desktop launcher; an iPad build and its full touch control scheme are future platform work.

## Sound effects

The team's original `.m4a` files remain in `assets/characters/soundfx/`. The matching `.ogg` copies are used by the desktop game for menu choices, LAN and notebook attacks, hits, armor hits, weapon switching, PowerBall casting and impact, and room transitions. Sati's `boss_fx/cpp_transform_sound.ogg` plays when the C++ element begins transforming into a spear.

Dome uses four cues from `boss_fx/Dome/`: `lighting_efx_cast_sound.ogg` on lightning charge, `lighting_efx_hit_sound.ogg` on the strike, `laser_efx_cast_sound.ogg` on laser charge, and `laser_efx_hit_sound.ogg` when firing. They play once per phase in both normal and Final Exam encounters, including when the player dodges. The two cast copies are shortened with tempo adjustment, preserving pitch, to match 0.85 and 1 second respectively; the original M4A files are retained. The other six Dome clips have OGG copies prepared but are not currently triggered. Adjust cast/fire volume in `audio/GameAudio.java`. Missing audio files remain optional.

## Weapon upgrades

- **LAN cable:** gain +7 hit damage, a wider and longer sweep, or a 1.5-second slow.
- **Notebook:** gain +6 hit damage, a poison effect over three seconds, or a splash hit around the target.
- **Shared/skill:** gain 18% faster weapon attacks, +20 movement speed, or +10 PowerBall damage. Effects unlock once; damage upgrades can be selected again on later stages.

These upgrades are permanent for the current run. During an EX fight, LAN also gains a tether: it marks enemies for 35% extra PowerBall damage, while ordinary monsters are pulled together and held briefly. Notebook gains a heat meter; four throws charge a wide piercing beam on the next attack. The beam applies poison if the regular poison upgrade is owned. Both EX techniques expire after that fight. Summoned notebook helpers remain an idea for a later version.

## Team development

See [TEAM_PLAN.md](TEAM_PLAN.md) for the four-person code split and integration checklist. Run `gradlew.bat :core:verifyGameRules` to check weapon unlocks, loop/EX sequencing, and combat rules.

## Integrated team updates

The current workspace includes `monsters-system` at `581089f` and the Ice Wall feature from `ability-weapon` at `997308f`, adapted to the current packages and collision systems. See [INTEGRATION.md](INTEGRATION.md) for provenance, art paths and verification. M and Far have distinct IDs; only Ae, Far and Dome count toward Final Exam progress.

Verify the integrated rules with `.\gradlew.bat :core:verifyGameRules :core:verifyCustomBosses :core:verifyDomeBoss :lwjgl3:classes --no-daemon`. Use `--test-ex` or `--test-obstacles` to try Ice Wall immediately.
