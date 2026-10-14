# Game Design Document — มาทั้งที่ก็เป็นนักเรียนติดลูปไปซะได้

**Version:** 0.5 · **Updated:** October 14, 2026 · **Status:** Prototype design and current implementation reference

> **Implemented** means present in the current workspace source. **Planned** means proposed work. **Team decision needed** marks unresolved choices. Design goals and acceptance criteria are not playtest results.

## Overview

### Theme, setting and genre

| Topic | Design |
| --- | --- |
| Game title | มาทั้งที่ก็เป็นนักเรียนติดลูปไปซะได้ |
| Theme | School life, academic pressure and the absurdity of repeating a school year. |
| Setting | An ISNE computer engineering campus, beginning at Building 30 and continuing through classrooms, laboratories and a server room. |
| Genre | Single-player, 2D room-based action roguelike. |
| Short pitch | Survive a looping fourth year with a LAN cable, a flying notebook and increasingly powerful upgrades. Fight Ajarns, outsmart unusual EX enemies and face two teachers together in the Final Exam. |
| Player experience | Read a threat, dodge it, choose a response and become stronger through each victory. The tone is playful, while attacks should remain understandable. |

### Design pillars

1. **Distinct weapons:** LAN offers free close-range control; Notebook trades SP for distance. PowerBall, Dash and Ice Wall support different responses to danger.
2. **Varied repetition:** Four shuffled locations, changing boss selections, EX encounters and rising difficulty keep the same school-year structure fresh.
3. **Meaningful victories:** Each Ajarn offers one of three upgrades; EX victories grant a lasting run buff; defeating Ae and Dome unlocks a special encounter.
4. **Readable danger:** Floor markers, safe zones, aiming lines, charge animations and sound cues give players a chance to react.

### Influences

- **Pusenbi — established visual influence:** Simple pixel art adapted to ISNE student clothing, objects and environments.
- **Student life — thematic reference:** LAN cables, notebooks, programming, networking and deadlines turn familiar academic objects into combat ideas.
- **Room-based roguelike structure — design reference:** Clear an arena, select a reward and repeat with a stronger build. Additional named game references require team agreement.

### Project description

The player controls a computer engineering student trapped in a repeating school year. Entering Building 30 begins a run through four shuffled stages. Each stage contains a monster room and an Ajarn encounter. The student starts with both weapons and must manage HP, regenerating SP, positioning and short cooldowns.

Boss rewards let the player build around melee damage, ranged effects, speed or PowerBall. EX encounters introduce different problems: a two-phase cat, a dust-sensitive vacuum and a sword whose confidence changes its behavior. A separate Final Exam combines Ae and Dome in a server-room fight with horizontal movement. The prototype continues into harder loops after these milestones.

### Gameplay mechanics brief

- Move, lock a target, switch between LAN and Notebook, and attack.
- Spend SP on Notebook, PowerBall, Dash or Ice Wall; use free LAN attacks while recovering.
- Avoid marked hazards and use tables as cover against ordinary projectiles.
- Clear both an Ajarn and its minions, then choose one upgrade.
- Fight an EX after loop 1 and even-numbered loops; use temporary EX weapon techniques.
- Defeat Ae and Dome within one run to unlock the Final Exam once.

### Game loop

```text
Title → Building 30 entrance → Left door → Begin combat
                           ↘ Car → Go-home joke ending → Restart

Monster room → Ajarn + minions → Choose 1 of 3 upgrades
     ↑                                ↓
     └──────────── Next stage ────────┘

After stage 4:
  Loop 1, 2, 4, 6, ... → Random EX → Run buff
  Other loops         → Continue
  Final Exam queued?  → Cutscene → Ae + Dome
  Resume saved sequence → Next, harder loop

HP reaches zero → Defeat/results screen → New run
```

**Implemented:** Each loop uses all four locations once and selects four bosses from five active Ajarns: Ken or Sati; Ae; M; and Dome. Both location order and boss order are shuffled. Clearing a monster room restores **12 HP**. Clearing an Ajarn room restores **35 HP** and opens the upgrade screen. Healing cannot exceed maximum HP.

**Final Exam ordering:** Victories over Ae and Dome accumulate within the same run. In loop 1, a ready exam waits until after the introductory EX. In later loops it follows the qualifying boss reward unless an end-of-loop EX is already next; that EX comes first. Clearing the exam resumes the saved room sequence. M does not count toward the Ae/Dome requirement.

## Project Scope

### Targeted platforms

**Implemented target:** Desktop through Java, libGDX and LWJGL3, using keyboard and mouse. The logical game space is 1280 × 720.

**Planned:** An iPad build and a complete touch control scheme. Existing on-screen skill buttons do not establish full mobile support. Other desktop operating systems need their own release testing before being advertised as verified.

### Targeted audience

**Design target:** Players who enjoy short arena fights, repeated attempts and choosing a build. Student-life humor is aimed especially at university and computer engineering audiences. These are intended audiences, not findings from user research.

### How to finish the loop

Completing four stages finishes one school-year loop. EX and queued Final Exam encounters resolve before the game resumes the next part of its sequence. Defeating the three-teacher Final Exam is a major milestone, but the prototype has **no final graduation ending**; the run continues until HP reaches zero.

**Demo goal:** Show one complete loop, an upgrade choice and the first EX, then reach loop 2. Demonstrate the Final Exam separately when random boss selection does not unlock it during that route.

**Team decision needed:** Decide whether the release should end at graduation or retain endless survival, and set a target run duration after playtesting.

### Technologies and delivery

- Java 17 or newer; libGDX framework; LWJGL3 desktop launcher; Gradle build.
- PNG pixel-art frames, nearest-neighbor filtering and optional local audio.
- Separate systems for flow, player, weapons, combat, enemy behavior, rendering, world geometry and sound.
- Original art and audio remain local. A source-only clone uses fallback visuals and missing sounds stay optional.
- No implemented multiplayer, account system, persistent unlock progression or save/resume system is claimed by this design.

## Story

### Player POV

You are a computer engineering student returning to Building 30. Familiar lessons have become dangerous arenas, Ajarns stand between you and the next stage, and even the everyday objects around you behave strangely. You fight to survive the year and understand why it keeps repeating.

### Summary

**Narrative direction:** Preserve the original Notion premise of a curse that returns the student to the first day of study, while presenting each gameplay loop as another attempt to survive year four. The exact connection between that first day and the repeating final year remains a team writing decision.

The immediate objective is to survive each room. Longer-term goals are to build stronger combinations, defeat EX enemies and qualify for the Final Exam. At the entrance, choosing the car lets the student abandon the day through a joke ending.

### In-depth

- **Arrival:** Building 30 establishes an ordinary campus setting before the student chooses the door or the car.
- **The repeating year:** Programming Lab, Circuit Workshop, Network Center and Capstone Studio represent areas of academic pressure. These are the current stage names; distinct room identities remain future art and level work.
- **Ajarns as tests:** Teachers create different combat problems. Their attacks transform academic themes into exaggerated challenges.
- **EX interruptions:** The cat, vacuum and sword interrupt the school-year rhythm with their own encounter rules and rewards.
- **Final Exam:** Ae and Dome appear together on server cabinets. Restricting the student to a lower horizontal lane changes the player's usual movement habits.
- **After the exam:** Current gameplay returns to the continuing run. Curse resolution, dialogue, graduation and a definitive story ending are **planned or undecided**, rather than implemented narrative content.

## Gameplay

### Core gameplay loops

**Moment to moment:** Observe enemy warnings → reposition or dash → attack with the suitable weapon → recover SP → respond again.

**Room to room:** Defeat all required enemies → receive healing/reward → choose an upgrade after an Ajarn → continue.

**Run progression:** Complete four stages → resolve scheduled special encounters → enter a harder loop → survive until defeated → start with a fresh build.

### Controls

| Action | Input |
| --- | --- |
| Move | WASD or arrow keys |
| Normal attack | Hold left mouse button or Space |
| Lock target | Click near an enemy; empty ground selects the nearest living enemy |
| Switch weapon | Tab; 1 for LAN, 2 for Notebook |
| Dash | Shift |
| PowerBall | E or BALL button |
| Ice Wall | F or ICE button |
| LAN Sword Wave | R or LAN button, with LAN equipped |
| HP / SP potion | Z / X |
| Escape bind | Alternate A/D or Left/Right six times |
| Choose upgrade | Click a card or press 1/2/3 |
| Reroll upgrades | R, once per upgrade screen |
| Continue / begin exam | Enter |
| Restart after defeat | R |

R is context-sensitive: Sword Wave during combat, reroll on the upgrade screen and restart after defeat.

### Player resources and targeting

**Implemented:** Begin with **100 HP**, **100 SP**, both weapons and two HP/two SP potions. SP regenerates at **14 per second** and is also recovered on kills. Room entry restores up to **30 SP** and gives **0.9 seconds** of initial immunity. HP potions restore **40 HP**; SP potions restore **35 SP**. Using a potion at full capacity does not consume it. Bottles carry between rooms; replenishment and monster drops are not implemented.

Normal attacks use the held target; Notebook projectiles home toward it. PowerBall initially locks the nearest living enemy and retargets if that enemy dies. Player and enemy movement stays inside the playable floor area.

### Weapons and abilities

| Tool | Role | Current base values before upgrades |
| --- | --- | --- |
| LAN cable | Close-range arc and resource recovery option | 27 damage; 125-unit reach; 0.44-second interval; 0 SP |
| Notebook | Homing ranged attack | 20 damage; 0.28-second interval; 10 SP |
| PowerBall | Homing projectile with area damage and crowd control | 42 base damage; 130-unit explosion radius; 30 SP; 2-second cooldown |
| Dash | Reposition or time immunity against impact | 115-unit movement; 25 SP; 0.75-second cooldown; 0.22-second immunity |
| Ice Wall | Temporary barrier and nearby disruption | 25 SP; 7-second cooldown; 4.5-second duration; 24 damage on raising |
| LAN Sword Wave | Straight projectile with LAN equipped | 38 damage plus physical bonus; 0 SP; 0.1-second cooldown |

PowerBall locks movement and ordinary attacks during its **0.72-second** cast, releasing at about **0.4 seconds**. Its explosion uses distance falloff, knocks back ordinary enemies more strongly than bosses, briefly stuns and then slows them. The base value is not a promise of identical damage to every target.

Ice Wall appears **92 units** ahead and remains fixed. It blocks movement, dashes and enemy projectiles, including boss shots; friendly shots pass through. An out-of-bounds cast fails without spending SP or starting cooldown. Its wall and cooldown reset on room entry.

Sword Wave follows the locked aim or facing direction and hits one enemy. Tables stop it; friendly Ice Wall lets it through. Its current free cost and short cooldown require balance review against the intended melee/ranged distinction.

### Arena obstacles and status effects

Normal monster and boss rooms have **three permanent tables**, randomized once on entry with space to move around them. Tables block movement, dashes and ordinary projectiles, including player shots. Boss projectiles travel over tables. EX and Final Exam rooms retain their own layouts.

Binder shots stop movement for up to **3.5 seconds**; alternating left/right inputs breaks the bind early. A warper marks its destination before striking. Slow, poison, armor, reflected attacks and safe-zone effects are communicated through effects and HUD cues.

### Upgrades and build choices

After an Ajarn room, choose **one of three different available upgrades**. Effects last for the current run.

| Upgrade group | Choices |
| --- | --- |
| LAN | +7 damage; longer/wider sweep; 1.5-second slow on hit |
| Notebook | +6 damage; poison over 3 seconds; splash damage |
| Shared / skill | +18% attack-speed bonus; +20 movement speed; +10 PowerBall damage |

Slow, wide sweep, poison and splash unlock once. Damage upgrades can repeat. Attack-speed bonus caps at **+180%** and the movement upgrade bonus at **+160**. EX movement rewards also use the speed-bonus field, so final speed can depend on both systems.

One reroll is available per upgrade screen. It favors eligible upgrades outside the previous three and does not consume the reroll when no alternative exists. Upgrades reset on a new run; no permanent progression between runs is implemented.

### Regular enemies

| Role | Behavior | Counterplay |
| --- | --- | --- |
| Basic | Approaches and attacks | Keep spacing or use LAN sweeps |
| Ranged | Keeps distance and fires | Close in, use cover or return ranged attacks |
| Wave | Sends expanding energy waves | Read the path and reposition |
| Shielded | Armor absorbs damage | Break armor before HP |
| Binder | Fires a movement-locking projectile | Dodge, then alternate directions if caught |
| Warper | Marks a point before teleporting into an attack | Leave the marker before impact |

Loop 1 introduces ranged enemies in stage 1, waves in stage 2, shields/binders in stage 3 and warpers in stage 4. Ordinary monsters separate when crowded. Algorithm, Network and Programming artwork also exists; visual variants should not be confused with the six combat roles.

### Ajarns

| Active boss | Distinct behavior in normal rooms | Player response |
| --- | --- | --- |
| Sati | Summons armored helpers; C++ element transforms into a spear | Manage minions and avoid the spear aim line |
| Ken | Summons helpers; places 7–9 warned bombs | Move through safe gaps before explosions |
| Ae | Warned warp alternating bind/damage, aimed spread and sweeping vertical laser | Leave the warp circle and time movement/dash across the sweep |
| M | Splits once at half HP; both bodies fire aimed shots | Track both bodies and avoid converging fire |
| Dome | Alternates marked lightning and a charged laser | Leave the locked target position before impact |

Ae's warp warning lasts **0.8 seconds**; his laser warns for **0.9 seconds** before a **5-second** sweep. Dome's lightning charges for **0.85 seconds**, and his laser for **1 second**. These normal-room abilities differ from the anchored Final Exam patterns.

Normal Ajarn rooms begin with **2 minions in loop 1**, **3 in loop 2**, and **5 from loop 3 onward**. Defeat the boss and remaining minions to clear. Sati and Ken can summon extra armored helpers while fewer than four regular minions are alive.

### EX encounters

EX appears after **loop 1**, then **loops 2, 4, 6, ...**. One of three enemies is selected randomly.

| EX | Encounter rule | Reward for the current run |
| --- | --- | --- |
| Liquid Cat | Stationary first phase; one protected transformation into a moving compact phase. Uses marked Orbital strikes; phase two takes 55% damage, leaves slowing ooze and creates up to two damageable copies. Each original phase has 190 base HP before loop scaling. | +25 maximum HP and restore 45 HP |
| Allergic Vacuum | Suction gathers dust and pulls the nearby player. At full allergy it warns, sneezes collected dust as projectiles and releases it to settle again. | +30 movement speed and restore 20 HP |
| Insecure Sword | Collect Post-it notes and approach to apply them. Notes reduce armor but raise confidence and stronger-slash chance. A missed slash creates a damage opportunity. | +8 physical damage, +6 magic damage and restore 20 HP |

During EX only, LAN gains **Tether**, marking enemies for **35% more PowerBall explosion damage** and pulling ordinary monsters together. Notebook gains **Heat**: four throws charge a piercing beam on the next attack. These techniques turn off after EX; earned EX buffs remain until the run ends.

### Final Exam

**Unlock:** Defeat Ae and Dome during the same run. This encounter is available **once per run**.

After a black cutscene and Enter input, both bosses occupy the outer upper server cabinets. The student moves or dashes **left and right only** in the lower lane. Notebook and PowerBall can reach the bosses above. The bosses stay anchored while their attacks combine across the room; Ae fires his aimed spread and Dome fires his cabinet laser downward.

Defeat both bosses to clear the exam and return to the saved normal sequence. This encounter is a combat milestone, not an implemented final ending.

### Difficulty scaling

For loop number n, enemy base stats use the following multipliers:

| Stat | Multiplier |
| --- | --- |
| HP / applicable armor | 1 + 0.15 × (n − 1) |
| SP / applicable regeneration | 1 + 0.10 × (n − 1) |
| Damage | 1 + 0.10 × (n − 1) |
| Speed | 1 + 0.04 × (n − 1) |

These are linear additions relative to base values. Room populations and special-monster frequency also increase. Encounter-specific timings may have their own caps. Balance remains subject to playtesting.

### Screens, HUD and feedback

**Implemented:** Title, Building 30 entrance, car prompt/joke ending, combat, room-clear transition, upgrade selection, Final Exam cutscene and defeat/results. Scene changes fade through black.

The HUD communicates HP/SP, potions, current weapon, loop/stage, enemy status, bonuses and encounter-specific meters such as Heat, Allergy and Confidence. Binding shows escape progress. Enemy markers and safe zones must stay readable over room art.

**Planned UX work:** Short first-use prompts for weapon switching, SP, Dash, Ice Wall, bind escape and EX mechanics. Test whether new players understand these without consulting project documentation.

## Art

### Art style and aesthetic

Simple **2D pixel art** with a playful campus identity. Student sprites use transparent **64 × 64** frames, displayed at 2× scale with nearest-neighbor filtering. Keep pixel scale consistent, silhouettes distinct and attack warnings visible above decorative detail.

**Current assets:** Building 30 entrance and separate car; classroom scenes and collision tables; student idle/walk/cast; several Ajarn sprites/effects; Liquid Cat and Insecure Sword animation. Sati has seven body and ten attack frames. Network and Programming monsters use the exported Network_Kiki (15 frames) and Program_Kiki (8 frames) series. Allergic Vacuum still uses temporary visuals. Missing optional exports use fallback sprites.

**Planned:** Replace remaining temporary visuals, give the four stage themes stronger identities and polish instructional feedback. Distinct art does not imply each location already has unique mechanics.

### Sound design

**Implemented:** Menu selection, LAN and Notebook attacks, hits/armor, weapon switching, PowerBall, room transitions and Sati's C++ transformation. Dome has separate lightning/laser cast and impact cues. Sword Wave alternates two clips, restarting the cue on a successful cast.

**Direction:** Charge sounds should announce danger before impact; hits and armor should sound different; reward/transition cues should support the combat rhythm. Missing local sounds remain optional.

**Planned:** A consistent campus ambience and music direction, a complete volume mix and any missing encounter cues. No completed soundtrack is claimed.

### Moodboard direction

Use original project art as the visual reference set:

| Reference | Intended feeling or design use |
| --- | --- |
| Title artwork | Playful school-life premise and identity |
| Building 30 entrance | Familiar campus arrival and the door/car choice |
| Classroom with tables | Readable arena layout and usable cover |
| Student idle/walk/cast frames | Consistent scale, clear motion and casting poses |
| Liquid Cat / Insecure Sword art | Absurd EX personalities with readable phases |
| Server room and Dome effects | Concentrated exam pressure and clear attack warnings |

These describe available reference subjects. The Notion moodboard should use reviewed exports from the team; this revision does not claim to have attached image files.

## Demo Acceptance and Team Decisions

### Acceptance criteria

| Area | Evidence needed |
| --- | --- |
| Run flow | Play entrance → four stages → upgrade choices → first EX → loop 2 |
| Combat | Demonstrate LAN, Notebook, PowerBall, Dash, Ice Wall, Sword Wave and cover rules |
| Progression | Show one upgrade changing behavior, a valid reroll and an EX buff carrying forward |
| Final Exam | Show correct Ae/Dome unlock, EX ordering, horizontal movement and return to the saved sequence |
| Presentation | Read major warnings, distinguish enemies and identify remaining placeholder assets |
| Stability | Complete the route, lose and restart without a progress-blocking error |

Automated rule checks support system verification; they do not replace live playtesting or establish a target session duration.

### Team responsibilities

The existing team plan assigns **Dim** to monster abilities, **Ju** to player abilities, **Apple** to upgrades and **Jj** to scene transitions/weapon abilities. These are responsibility areas, not proof of authorship of every existing feature. Each member should demonstrate and explain their own changes using actual implementation and commit evidence.

### Decisions still needed

1. Confirm the curse origin and the relationship between the first-day premise and the looping fourth year.
2. Choose endless survival or a graduation ending and set a tested target run length.
3. Review Sword Wave's free rapid-fire balance and its effect on weapon roles.
4. Finalize tutorials, language support and accessibility/readability checks.
5. Prioritize remaining art/audio, distinct stage layouts and future touch controls.

### Project references

- README.md — controls, content, asset status and run instructions.
- CODE_ARCHITECTURE.md — system layout and execution flow.
- TEAM_PLAN.md — responsibility areas and demonstration expectations.
- INTEGRATION.md — integration provenance and verification.
- Core source: RunPlan, RunDirector, PlayerCharacter, Upgrade, PotionInventory, EnemyFactory, boss/EX behaviors, combat and world systems.
- Existing checks: GameRulesCheck, CustomBossCheck and DomeBossCheck.
