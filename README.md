# School Life: Survive Year Four

A small playable libGDX action roguelike prototype about surviving computer engineering school life. This is a student-themed spin-off of Pusenbi. Its visual direction carries over Pusenbi's simple pixel-art style and adapts the characters, clothing, enemies, and settings to ISNE student life.

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

Inside the core game, both the LAN cable and notebook are available. Press **Q** to toggle weapons or **1**/**2** to select one directly during combat. Use **WASD** or the arrow keys to move, hold the **left mouse button** (or **Space**) for a normal attack, and press **Shift** to dash. Clicking near an enemy locks that target while the button is held; clicking empty ground locks the nearest living enemy. The student turns toward the target, a green ring shows the lock, and notebook shots home toward it. Press **E**, or tap the on-screen **SKILL** button, to stand still and cast PowerBall. The skill locks the nearest living enemy, the student turns to follow it during the cast, and the PowerBall homes toward it. If that enemy falls, the ball locks another living enemy in the room. The cast costs 30 SP and has a 2-second cooldown. A dash costs 25 SP. Ranged shots cost 10 SP; close-range attacks are free. SP regenerates over time and on kills. Press **Enter** after clearing a monster or EX room. After each Ajarn, click one of three upgrades or press **1**, **2**, or **3** to advance. If you fall, press **R** to start a new run.

The LAN cable now sweeps a wide arc in the direction the student faces. Its reach is 125 game units and it swings every 0.44 seconds; the notebook attacks every 0.28 seconds. PowerBall is larger and explodes on impact, damaging and knocking back nearby enemies. Ordinary monsters are pushed farther than bosses. The blast briefly stuns and then slows enemies; bosses recover faster.

When a cyan binder shot catches you, movement stops. Alternate **A** and **D** (or **Left** and **Right**) six times to break free early. The bind also expires after 3.5 seconds. Purple warpers mark a red target on the floor before teleporting in for a heavy hit; move out of the marker before they arrive.

The upper wall contains windows. The student and monsters are restricted to the floor below them, including during dashes and teleports.

## Run structure

- Each loop shuffles four school-themed stages and picks four distinct Ajarns from a pool of eight, with one from each combat family.
- Each stage has a regular monster room, then an Ajarn boss room.
- Each defeated Ajarn offers three random upgrades; choose one permanent upgrade for that run before the next room.
- Clearing stage four starts a new loop. Enemy HP, SP, speed, and damage increase every loop.
- Immediately after the final Ajarn of every even-numbered loop, one random EX monster appears before the next loop. Defeating Liquid Cat, Allergic Vacuum, or Insecure Sword grants its corresponding permanent buff for that run.
- LAN Tether and Notebook Heat Beam become available only during the EX fight. They switch off when that encounter ends and do not appear among the normal boss upgrade cards.
- Ajarn defeats restore 24 HP. The run continues until the player is defeated.

## Enemy encounters

- Regular monsters move apart when they crowd each other. Green ranged monsters keep their distance, blue wave monsters fire expanding energy waves, white shielded monsters absorb damage with armor, cyan binders stop movement, and purple warpers teleport to a marked spot.
- Loop 1 introduces these enemies gradually: ranged in stage 1, waves in stage 2, shields and binders in stage 3, then warpers in stage 4. Later loops add random special monsters more often.
- Ajarns appear at the far right and hold their ground between attacks. Their projectiles hit harder and fire more often. The four families are summon + reflecting shield, summon + warp strike, long-range huge projectile, and energy waves + reflecting shield. Summoners call skilled monsters with shields: one at a time in loop 1, then up to two at a time, with four living minions as the limit. Boss warps are sudden: the Ajarn appears beside the student, alternates between binding their legs and a heavy hit, then returns to the original position. Regular warping monsters still mark their destination first. From loop 3 onward, a random extra trait may combine with a boss's main pattern; from loop 5 onward, some gain waves too. Ajarn Sati has an additional C++ element that transforms into a spear. A line marks the target point while the spear charges, so the player can dodge before it launches. Ajarn Talgo (Ken), linked to the `Talgorithm(Ken)` boss folder, marks a green safe circle before a full-room AOE. Reach the circle during the 1.8-second cast or time a dash for the impact. His other attacks pause while he casts. Other boss names and graphics are still placeholders.

### EX encounters

- **Liquid Cat:** alternates between full cat and a slower, smaller compact form. The small form takes reduced damage, leaves ooze that slows the student, and fires a liquid bullet with its divide animation. In full cat form it marks a landing point before a heavy orbital leap.
- **Allergic Vacuum:** draws loose dust and the nearby student toward its nozzle. Each consumed dust piece fills its allergy meter. At 100%, suction stops for a sneeze warning, then it recoils and ejects its collected dust as damaging projectiles. The dust settles and can be sucked up again.
- **Insecure Sword:** drops Post-it notes. Walk over a note to pick it up, then approach the sword to apply it automatically. Each note lowers its armor but raises its confidence and chance of a powerful marked slash. A missed slash leaves the sword open to extra damage. At low confidence it often wobbles and deals zero damage.

## Art handoff

The student now renders from transparent 64 × 64 PNG frames in `assets/characters/student_player/`: `idle` (left/right), `walk` (four frames per side), and `skill_case` (eight left / nine right). The sprite uses nearest-neighbor filtering at 2× size. `skill_case` plays only during the E/touch skill; `power_ball.png` is released partway through that animation. Normal weapon attacks do not trigger the skill animation. The character source is `../characters/main/aseprite/student001.aseprite`.

The Building 30 entrance uses `assets/scene/scene30building_entrance_withoutcar.png` with the separate `scene30bCar_object.png` overlay at a crisp 3× scale. Monster and boss rooms use the team's `assets/scene/sceneroom.png` and `sceneroom_boss.png` art, repeated across the wide arena. Sati's `cpp_element` uses six idle frames, sixteen transformation frames, and the spear sprite. Liquid Cat now uses the team's 62 exported PNG frames in `assets/characters/boss_ex/liquid-cat/` for idle, compact-form walking, transformation, divide, orbital strike and bullet attacks. The `skill` folder is still empty. Allergic Vacuum and Insecure Sword still use temporary game shapes. The team's `Frame.png` and `Concept.png` files guide their behavior; they are presentation boards rather than game sprite frames. Sati's main body and ordinary enemies still use placeholder blocks. The classroom furniture is currently visual background art, without collision.

The on-screen skill button accepts touch or mouse input. This repository currently has a desktop launcher; an iPad build and its full touch control scheme are future platform work.

## Sound effects

The team's original `.m4a` files remain in `assets/characters/soundfx/`. The matching `.ogg` copies are used by the desktop game for menu choices, LAN and notebook attacks, hits, armor hits, weapon switching, PowerBall casting and impact, and room transitions. Sati's `boss_fx/cpp_transform_sound.ogg` plays when the C++ element begins transforming into a spear.

## Weapon upgrades

- **LAN cable:** gain +7 hit damage, a wider and longer sweep, or a 1.5-second slow.
- **Notebook:** gain +6 hit damage, a poison effect over three seconds, or a splash hit around the target.
- **Shared/skill:** gain 18% faster weapon attacks, +20 movement speed, or +10 PowerBall damage. Effects unlock once; damage upgrades can be selected again on later stages.

These upgrades are permanent for the current run. During an EX fight, LAN also gains a tether: it marks enemies for 35% extra PowerBall damage, while ordinary monsters are pulled together and held briefly. Notebook gains a heat meter; four throws charge a wide piercing beam on the next attack. The beam applies poison if the regular poison upgrade is owned. Both EX techniques expire after that fight. Summoned notebook helpers remain an idea for a later version.

## Team development

See [TEAM_PLAN.md](TEAM_PLAN.md) for the four-person code split and integration checklist. Run `gradlew.bat :core:verifyGameRules` to check weapon unlocks, loop/EX sequencing, and combat rules.
