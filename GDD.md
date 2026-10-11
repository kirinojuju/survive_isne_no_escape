# Game Design Document — มาทั้งที่ก็เป็นนักเรียนติดลูปไปซะได้

**Version:** 0.2 · **Updated:** October 1, 2026 · **Status:** Design document based on the current prototype

> **Status labels:** **Implemented** describes systems found in the current code and README. **Planned** describes work that remains incomplete. **Team decision needed** marks details the game and project documents have not settled. Vision and design goals are not claims that a feature is finished.

## 1. Game overview

| Topic | Description |
| --- | --- |
| Title | **มาทั้งที่ก็เป็นนักเรียนติดลูปไปซะได้** |
| Genre | Single-player, 2D room-based action roguelike |
| Premise | A computer engineering student fights through monsters and Ajarns while trying to survive a fourth year that repeats and grows harder. |
| Hooks | Two student-themed weapons, four Ajarn combat styles per loop, EX enemies with distinct mechanics, and a joke ending in which the student chooses to go home before fighting. |
| Current platform | Desktop via libGDX/LWJGL3. An iPad build and complete touch controls are future work. |
| Run structure | A run continues until the player's HP reaches zero. The main loop has no final ending in the current prototype. |
| Visual direction | Simple pixel art carried over from Pusenbi, adapted to ISNE student life through the characters, clothing, enemies, and locations. |

### Intended player experience

The player should feel that each new school year is more demanding, while their growing skill and upgrades help them keep up. Combat alternates between reading attack warnings, dodging, choosing the right weapon for the distance, and building a distinctive set of upgrades during each run. The tone is playful and rooted in student life. Dangerous attacks should give players a clear chance to react.

### Design pillars

1. **Readable danger:** Powerful attacks use cues such as floor markers, aim lines, or charge animations.
2. **Distinct weapons:** The LAN cable covers a wide area at close range without using SP. The notebook attacks from a distance and consumes SP.
3. **Meaningful victories:** Each Ajarn offers a choice of one upgrade from three. EX enemies grant a unique buff.
4. **Varied repetition:** Stage order and Ajarn selection change between loops, while difficulty rises each time.

## 2. Core game loop

```text
Title screen → Building 30 entrance → Left door
             ↘ Car: choose to go home → Joke ending → New run

Monster room → Ajarn room → Choose 1 of 3 upgrades
      ↑                            ↓
      └──────── Next stage ─────────┘

Clear 4 stages → Start a harder loop
Clear loop 1 or an even-numbered loop → Choose the final Ajarn upgrade
                            → Fight 1 random EX → Gain its buff → Next loop
HP reaches zero → Results screen → New run
```

**Implemented:** Each loop shuffles four unique stage locations and selects four of eight Ajarns, including one from each combat family. A stage contains one monster room followed by one Ajarn room. Clearing a monster room restores 12 HP. Defeating an Ajarn restores 35 HP and presents an upgrade choice. One of three EX enemies appears after loop 1, then after loops 2, 4, 6, and so on.

### Four stage locations

| Location | Theme |
| --- | --- |
| Programming Lab | Programming laboratory |
| Circuit Workshop | Algorithms workshop |
| Network Center | Network laboratory |
| Web Programming | Final-year project |

**Prototype limitation:** Stage names affect the displayed location and shuffled order, but combat currently reuses the same regular-room and boss-room backgrounds. The locations do not yet have distinct visuals or mechanics.

## 3. Player and controls

| Action | Desktop control | Effect |
| --- | --- | --- |
| Move | WASD or arrow keys | Walk within the room's playable floor area. |
| Normal attack | Hold left mouse button or Space | Attack repeatedly with the active weapon. |
| Select a target | Click near an enemy | Lock that enemy. Clicking empty space locks the nearest living enemy. |
| Switch weapons | Tab or 1/2 | Select the LAN cable or notebook. |
| Dash | Shift | Move quickly with a brief period of damage immunity. |
| Cast PowerBall | E or the on-screen SKILL button | Stop and cast a homing projectile. |
| Ice Wall | F or ICE button | Raise a temporary barrier in the facing direction. |
| HP / SP potion | Z / X | Consume a stored bottle for 40 HP / 35 SP. |
| Escape a bind | Alternate A/D or left/right six times | Break free before the bind expires. |
| Choose an upgrade | Click a card or press 1/2/3 | Take one upgrade. |
| Continue | Enter | Move on after clearing a room. |
| Restart after defeat | R | Begin a new run. |

**Implemented:** The player enters combat with both weapons, 100/100 HP, and 100/100 SP. SP regenerates at 14 per second and increases on kills. The player stays on the floor below the room's windows, including while dashing. Starting a new room restores up to 30 SP and provides a short period of immunity.

### Weapons and abilities

| Tool | Role | Prototype base values |
| --- | --- | --- |
| LAN cable | Close-range arc attack | 27 damage, 125-unit reach, one swing every 0.44 seconds, no SP cost. |
| Notebook | Ranged attack that homes toward a locked target | 20 damage, one attack every 0.28 seconds, 10 SP per shot. |
| PowerBall | Homing skill with an area explosion | 42 base projectile damage before bonuses, 130-unit blast radius, 30 SP cost, 2-second cooldown. |
| Dash | Dodge or reposition | 115-unit movement, 25 SP cost, 0.75-second cooldown. |

PowerBall's cast animation lasts about 0.72 seconds and releases the projectile about 0.4 seconds after casting starts. Movement, dashing, weapon switching, and normal attacks are unavailable during the cast. The projectile tracks its target and switches to another living enemy if that target dies. Its explosion knocks ordinary monsters back farther than bosses, briefly stuns them, and then slows them.

## 4. Progression and balance

### Ajarn rewards

**Implemented:** Defeating an Ajarn presents three different upgrades. The player chooses one, and its effect lasts for that run. There is no progression system that carries upgrades between runs.

| Category | Upgrade choices |
| --- | --- |
| LAN cable | +7 damage; wider and longer sweep; slow enemies for 1.5 seconds. |
| Notebook | +6 damage; poison over 3 seconds; small splash attack. |
| Shared / skill | +18% attack speed; +20 movement speed; +10 PowerBall damage. |

**Optional ideas (not implemented):** An ultimate skill and special weapon drops from EX monsters.

Trait upgrades such as slow, poison, and splash can be selected once. Damage bonuses can be selected again. Attack speed and movement speed have caps in the code.

### Difficulty scaling

| Enemy stat | Multiplier in loop `n` |
| --- | --- |
| HP | `1 + 0.15 × (n − 1)` |
| SP | `1 + 0.10 × (n − 1)` |
| Speed | `1 + 0.04 × (n − 1)` |
| Damage | `1 + 0.10 × (n − 1)` |

Monster-room population increases with the stage and loop, up to a cap on the loop-based increase. Loop 1 introduces enemy types gradually. Later loops can spawn additional special monsters more often. From loop 3 onward, Ajarns may gain an extra trait; from loop 5 onward, some may also gain wave attacks.

## 5. Regular enemies

| Type | Behavior to read | Player response |
| --- | --- | --- |
| Basic | Approaches and attacks. | Maintain distance, sweep with the LAN cable, or attack before it closes in. |
| Green ranged | Keeps its distance and fires. | Close the gap or use the notebook or PowerBall. |
| Blue wave | Fires expanding energy waves. | Read the wave's path and move clear. |
| White shielded | Armor absorbs damage. | Break the armor before depleting HP. |
| Cyan binder | A projectile prevents movement for a short time. | Dodge the shot or alternate left and right to escape. |
| Purple warper | Marks a landing point before teleporting in for a heavy hit. | Leave the marked area before it arrives. |

Regular monsters spread apart when crowded. Loop 1 introduces ranged enemies in stage 1, waves in stage 2, shielded enemies and binders in stage 3, and warpers in stage 4.

## 6. Ajarns and special encounters

### Eight Ajarns across four combat families

| Family | Current names | Main pattern |
| --- | --- | --- |
| Summoning + reflecting shield | Ajarn Sati, Ajarn Talgo (Ken) | Summon armored minions and reflect attacks with a shield. |
| Summoning + warp strike | Ajarn Ohm, Ajarn Voltage | Summon minions and teleport toward the player. |
| Long-range artillery | Ajarn Packet, Ajarn Firewall | Fire large projectiles from a distance. |
| Energy waves + reflecting shield | Ajarn Deadline, Ajarn Thesis | Create energy waves and defend with a shield. |

Ajarns hold a position toward the right side of the room between attacks. Summoners call armored minions, with a limit on living minions. Boss warps are sudden and alternate between binding the player's legs and dealing heavy damage. Regular warping monsters, by contrast, mark their destination before arriving.

**Implemented signature attacks:** Ajarn Sati has a C++ element that transforms into a spear, with an aim line before launch. Ajarn Talgo (Ken) marks a green safe circle before a room-wide attack. The player has about 1.8 seconds to reach the circle or time a dash through the impact. Talgo's other attacks pause during the cast.

**Planned:** Most Ajarn names and main character art are still placeholders. Each needs a distinct appearance, signature move, and short character voice while keeping the combat family readable.

### EX encounters after loop 1 and even-numbered loops

| EX | Encounter mechanic | Reward on defeat |
| --- | --- | --- |
| Liquid Cat | Switches between a full cat and a slower, smaller form that takes reduced damage. The compact form leaves slowing ooze and fires liquid bullets; the full form marks a landing point before a heavy leap. | +25 maximum HP and restore 45 HP. |
| Allergic Vacuum | Pulls loose dust and a nearby player toward its nozzle. Dust raises its allergy meter. At 100%, suction stops, a sneeze warning appears, and collected dust is launched as damaging projectiles. | +30 movement speed and restore 20 HP. |
| Insecure Sword | Drops Post-it notes. The player picks them up and brings them close to the sword. Notes lower its armor but raise its confidence and chance of a strong slash. A missed slash leaves it open to extra damage. | +8 LAN damage, +6 magic damage, and restore 20 HP. |

During an EX encounter only, the LAN cable gains **Tether**, which marks enemies for 35% extra PowerBall damage and pulls ordinary monsters together. The notebook gains **Heat**: four throws charge a wide piercing beam for the next attack. Both techniques turn off when the EX fight ends. The buff earned by defeating the EX remains for the rest of the run.

## 7. Screens and player communication

**Implemented:** Title screen; Building 30 entrance; prompt to take the car home; combat rooms; cleared-room interstitial; upgrade selection; defeat screen; and the car's joke ending. Scene changes fade through black.

The HUD shows HP, SP, loop, stage, room or primary enemy, active weapon, and current bonuses. A bind adds an escape-progress bar. EX fights show relevant state such as Heat, Allergy, or Confidence. Warning markers and safe areas must remain readable over the background, especially when several enemies are on screen.

**UX proposal:** Before the first combat room, give players short prompts for attacking, switching weapons, dashing, and using the skill. On the first encounter with a binder, warper, or EX, explain its counterplay through brief in-game text and symbols. Then test whether a new player understands those mechanics without consulting the README.

## 8. Art, audio, and technology

| Area | Current status |
| --- | --- |
| Player character | Transparent 64×64 PNG frames for idle, walk, and skill_case; displayed at 2× scale with nearest-neighbor filtering. |
| Environments | Building 30 entrance with a separate car layer; team-made regular and boss room backgrounds. |
| Bosses and EX | Liquid Cat and Insecure Sword have in-game animation frames. Sati's C++ element has art and sound. Allergic Vacuum, Sati's main body, and regular monsters still use some temporary visuals. |
| Audio | Sound effects cover menu choices, weapons, hits, room changes, PowerBall, and the C++ element's transformation. |
| Engine | Java and libGDX, desktop via LWJGL3, 1280×720 logical game space. |

**Art direction for remaining work:** Keep pixel scale and nearest-neighbor rendering consistent. Give threats distinct effect colors, and prevent furniture or room art from obscuring attack warnings. Classroom furniture is currently part of the background and has no collision.

## 9. Demo scope and acceptance criteria

The team demo should play continuously from the entrance through four stages, include upgrade choices after Ajarns, present an EX encounter after loop 1, and enter loop 2 without blocking progress.

| Area | Acceptance criterion |
| --- | --- |
| Run flow | Correct room order; four unique locations per loop; four boss families; EX after loop 1 and even-numbered loops. |
| Combat | Both weapons and PowerBall work; major status effects have readable counters; HP, SP, and targeting are shown correctly. |
| Progression | Choosing one of three upgrades changes gameplay; EX buffs last for the run; EX-only techniques disappear after the encounter. |
| Presentation | The player character and all three EX enemies are visually distinct; major attacks have readable visual and audio timing; temporary art is replaced in content presented as finished. |
| Stability | A player can launch the game, finish loop 2, lose, and restart without a freeze or progress-blocking error. |

**Prototype status:** Core systems and the behavior of all three EX encounters are implemented. Some character art and screen polish remain incomplete, so the presentation criteria describe work to finish rather than completed features.

## 10. Team decisions needed

1. Final Thai title, short description, and story reason for the repeating fourth year.
2. Final identities, art, and signature moves for Ajarns that still use placeholder names or visuals, including how each relates to the four stage themes.
3. Target run length and what counts as a satisfying demo ending, since the prototype's main loop can continue indefinitely.
4. Whether in-game controls and text remain in English or use Thai or both languages.
5. Priority order for character art, sound, location-specific rooms, and touch controls after the desktop demo.

## Project references

- [README.md](README.md): Controls, game content, and current art and audio status.
- [TEAM_PLAN.md](TEAM_PLAN.md): Team demo goal and division of work.
- `core/src/main/java/io/github/kirinojuju/`: Current rules in `RunPlan`, `RunDirector`, `PlayerCharacter`, `CombatRoom`, `Upgrade`, and `GameRenderer`.
- `core/src/test/java/io/github/kirinojuju/GameRulesCheck.java`: Regression checks for loop order, weapons, EX encounters, and combat rules.
