# โครงสร้างโค้ดเกมสำหรับทีม

## เริ่มอ่านตรงนี้: เข้าใจภาพรวมด้วย 6 ไฟล์

ปัจจุบันมี Java ฝั่งเกมหลัก 54 ไฟล์ใต้ `core/src/main/java/io/github/kirinojuju/` อีก 2 ไฟล์เป็นตัวเปิดเกมบนคอมพิวเตอร์ และ 3 ไฟล์เป็นชุดตรวจเกม จัดโฟลเดอร์และ Java package ตามระบบแล้ว โดยมี `schoolLife.java` เป็นจุดเริ่มที่โฟลเดอร์หลัก ให้เริ่มตามลำดับนี้:

| ลำดับ | ไฟล์ | คำถามที่ไฟล์นี้ตอบ |
| --- | --- | --- |
| 1 | `schoolLife.java` | เกมสร้างระบบอะไรบ้าง และเรียกอะไรในแต่ละเฟรม? |
| 2 | `flow/RunDirector.java` | ตอนนี้อยู่หน้าไหน ห้องไหน และจะเปลี่ยนไปไหนต่อ? |
| 3 | `flow/RunPlan.java` | อยู่ลูป/ด่านไหน เจอบอสอะไร และชนะบอสตัวไหนแล้ว? |
| 4 | `player/PlayerCharacter.java` | รับปุ่มอย่างไร เดิน Dash ใช้สกิล เลือกอาวุธ และเก็บสถานะผู้เล่นอย่างไร? |
| 5 | `combat/CombatRoom.java` | ห้องเก็บข้อมูลอะไร และเรียกระบบต่อสู้แต่ละส่วนตามลำดับไหน? |
| 6 | `render/GameRenderer.java` | นำสถานะเหล่านั้นไปวาดฉาก ตัวละคร เอฟเฟกต์ และหน้าจออย่างไร? |

ภาพรวมหนึ่งเฟรมคือ `schoolLife.render()` → `RunDirector` ประมวลผลตามสถานะเกม → ระหว่างต่อสู้เรียก `PlayerCharacter` และ `CombatRoom` → `GameRenderer.draw()` วาดผลลัพธ์ เมื่อเคลียร์ห้องแล้ว `RunDirector` เลือกขั้นตอนถัดไปโดยใช้ข้อมูลจาก `RunPlan`

### อ่านเมธอดหลักหลังจัดโค้ด

| จุดเริ่ม | ลำดับการทำงาน |
| --- | --- |
| `schoolLife.render()` | รับปุ่มโหมดทดสอบ → อัปเดตเสียง → `updateRun()` → วาดเกม |
| `schoolLife.updateRun()` | รับสกิลและการโจมตี → `handleMenuClicks()` รับคลิกตามหน้าจอ → `RunDirector.update()` |
| `RunDirector.update()` | ทำฉาก Fade ก่อน แล้วใช้ `switch (phase)` เลือกงานของหน้าจอ; การเดินทางเข้าอาคารอยู่ใน `updateEntrance()` และการต่อสู้อยู่ใน `updateFight()` |
| `GameRenderer.drawCombatShapes()` | วาดสนาม → กระสุนและเอฟเฟกต์ → วัตถุ EX → สัญญาณเตือน → สาย Tether → ศัตรู → แถบสถานะและเมนู |
| `GameRenderer.drawHud()` | วาด HP/SP และข้อมูลด่าน → `drawEncounterHud()` ข้อมูลศัตรู → `drawWeaponHud()` ข้อมูลอาวุธ |

แถบ HP/เกราะของศัตรูใช้ `drawEnemyBar()` ร่วมกัน และการแปลงตำแหน่งเมาส์เป็นพิกัดเกมใช้ `updateTouchPosition()` จุดเดียว เมธอดย่อยอยู่ในไฟล์เจ้าของระบบ ส่วนการต่อสู้มี `CombatDamage` และ `ProjectileSystem` ช่วย `CombatRoom` รวมไฟล์หลักเป็น 54 ไฟล์

## ไฟล์ที่เหลือเปิดเมื่อทำระบบนั้น

ตารางนี้แสดงตำแหน่งครบทั้ง 54 ไฟล์ โดยชื่อในช่องไฟล์คือชื่อก่อน `.java`:

| โฟลเดอร์ | ไฟล์ | หน้าที่ |
| --- | --- | --- |
| โฟลเดอร์หลัก (1) | `schoolLife` | เริ่มเกมและเรียกระบบแต่ละเฟรม |
| `flow/` (2) | `RunDirector`, `RunPlan` | หน้าจอ ลำดับห้อง ด่าน ลูป และความคืบหน้า |
| `player/` (3) | `PlayerCharacter`, `Upgrade`, `PotionInventory` | ผู้เล่น ตัวเลือกอัปเกรด และขวดยาที่ใช้แล้วหมด |
| `weapon/` (4) | `WeaponAttack`, `WeaponAbility`, `LanWeaponAttack`, `NotebookWeaponAttack` | การโจมตีและช่องต่อ Ability ของอาวุธ |
| `combat/` (3) | `CombatRoom`, `CombatDamage`, `ProjectileSystem` | เก็บสถานะและลำดับเฟรม ดาเมจ และการเคลื่อน/ชนของกระสุน |
| `enemy/` (7) | `EnemyBehavior`, `StandardEnemyBehavior`, `WarperMonsterBehavior`, `EnemyFactory`, `EnemySpacing`, `AlgoMonster`, `NetworkMonster` | พฤติกรรมร่วม การเกิด/แยกตัว และคลาสมอนสเตอร์ |
| `enemy/boss/` (11) | `AjarnBehavior`, `SatiBossBehavior`, `TalgoBossBehavior`, `DomeBossBehavior`, `AeBoss`, `AeBossBehavior`, `KenBoss`, `KenBossBehavior`, `MBoss`, `MBossBehavior`, `SatiBoss` | คลาสบอสเก็บสถานะ และ Behavior จัดการท่าเฉพาะ |
| `enemy/ex/` (5) | `ExBehavior`, `LiquidCatBehavior`, `AllergicVacuumBehavior`, `InsecureSwordBehavior`, `LiquidCatBoss` | คลาสและพฤติกรรม EX Monster |
| `render/` (13) | `GameRenderer`, `StudentSprite`, `SatiSprite`, `DomeSprite`, `LiquidCatSprite`, `InsecureSwordSprite`, `OptionalAssetTexture`, `AeSprite`, `KenSprite`, `MSprite`, `AlgoMonSprite`, `IceWallSprite`, `MonsterSprite` | วาดเกม ตัวละคร เอฟเฟกต์ และภาพสำรอง |
| `world/` (4) | `GameSpace`, `EntranceScene`, `RoomObstacles`, `IceWall` | ขอบพื้นที่ ทางเข้าอาคาร โต๊ะ ทางเดินอ้อม และกำแพงชั่วคราว |
| `audio/` (1) | `GameAudio` | เสียงเกม |

Java package ตรงกับโฟลเดอร์ เช่น `player/PlayerCharacter.java` ใช้ `package io.github.kirinojuju.player;` เมื่อเพื่อนเพิ่มไฟล์ ให้ใช้ package ของระบบนั้นและ import คลาสจากระบบอื่นตามต้องการ ชุดตรวจยังอยู่ที่ `core/src/test/java/io/github/kirinojuju/GameRulesCheck.java` และตัวเปิดเกมยังอยู่ในโมดูล `lwjgl3`

## อยากแก้อะไร ให้เริ่มที่ไฟล์ไหน

| งาน | เริ่มอ่าน | เปิดเพิ่มเมื่อจำเป็น |
| --- | --- | --- |
| ความเร็วเดิน / Dash / PowerBall | `PlayerCharacter` | `CombatDamage` สำหรับผลดาเมจ และ `RoomObstacles` สำหรับการชนโต๊ะ |
| Notebook ยิงอย่างไร | `NotebookWeaponAttack` | `WeaponAttack`, `WeaponAbility`, `ProjectileSystem`, `CombatDamage` |
| ท่าสายฟ้า / เลเซอร์ Dome | `DomeBossBehavior` | `DomeSprite` สำหรับภาพ และ `GameRenderer` สำหรับสัญญาณเตือน |
| มอนเดินไล่ / ยิง / วาร์ป | `StandardEnemyBehavior`, `WarperMonsterBehavior` | `EnemyFactory` สำหรับค่าสถานะตอนเกิด |
| มอนไม่ยืนทับกัน | `EnemySpacing` | `CombatRoom` สำหรับจังหวะที่เรียกใช้ |
| โต๊ะวางตรงไหน / เดินอ้อมอย่างไร | `RoomObstacles` | `PlayerCharacter`, `StandardEnemyBehavior`, `CombatRoom` |
| จบห้องแล้วไปไหน / เข้า FINAL_EXAM เมื่อไร | `RunDirector` | `RunPlan` สำหรับเงื่อนไขความคืบหน้า |
| HP หน้าจอ / ข้อความ / ฉาก | `GameRenderer` | คลาส `*Sprite` เมื่อต้องแก้ภาพตัวละคร |

`*Behavior` คือการตัดสินใจและกติกาของศัตรู ส่วน `*Sprite` คือภาพของตัวนั้น เช่น แก้เวลาชาร์จเลเซอร์ใน `DomeBossBehavior` และแก้เฟรมเลเซอร์ใน `DomeSprite` การแยกแบบนี้ช่วยให้แก้ระบบหนึ่งได้โดยตามเฉพาะไฟล์ที่เกี่ยวข้อง

หลังรวมงานบอส มอนสเตอร์ Ice Wall, Potion และแยกงานต่อสู้ มีไฟล์หลัก 54 ไฟล์ ตัวช่วยสั้น ๆ ยังคงอยู่ในไฟล์เจ้าของระบบ โดยตัวที่ต้องเรียกข้าม package ใช้ nested class เช่น `WeaponAttack.WeaponAttackSpec`, `NotebookWeaponAttack.NotebookSprite` และ `RoomObstacles.TableSprites` ส่วนท่าของบอสแต่ละตัวยังแยกไว้เพื่อเพิ่มบอสในอนาคตได้ง่าย คลาสแม่ การสืบทอด และ interface ยังใช้โครงสร้างเดิม

## CombatRoom หลังแยกหน้าที่: อ่านเพียง 3 ไฟล์

รอบนี้ลด `CombatRoom.java` จาก 601 เหลือประมาณ 320 บรรทัด โดยเพิ่ม Java เพียง 2 ไฟล์ใน `combat/`:

| ไฟล์ | หน้าที่ | เมธอดที่เริ่มอ่าน |
| --- | --- | --- |
| `CombatRoom.java` | เก็บสิ่งที่อยู่ในห้อง เริ่มห้องใหม่ และเรียกงานแต่ละส่วนตามลำดับ | `begin()`, `update()` |
| `CombatDamage.java` | คิดดาเมจ เกราะ การสะท้อน LAN/Tether, PowerBall และ Notebook Poison/Splash/Beam | `applyToEnemy()`, `explodePowerBall()` |
| `ProjectileSystem.java` | เคลื่อนกระสุน เล็งตามเป้าหมาย ตรวจชนโต๊ะ/กำแพง/ตัวละคร และลบเมื่อหมดอายุ | `update()` → `updateShot()` |

`CombatDamage` และ `ProjectileSystem` ใช้ภายใน package `combat` เท่านั้น อาวุธและสกิลเรียกผ่าน `CombatRoom` เช่นเดิม เช่น `combat.damageArc(...)`, `combat.fireFriendly(...)` และ `combat.fireNotebookBeam(...)` จึงตามโค้ดจากจุดเรียกเดิมได้

### หนึ่งเฟรมทำงานอย่างไร

อ่าน `CombatRoom.update()` จากบนลงล่าง:

1. `updateEffects()` ลดเวลาเอฟเฟกต์ กำแพงน้ำแข็ง และภาพ Notebook แตก
2. `ExEnvironment.updateObjects()` อัปเดตฝุ่น/สไลม์และเก็บ Post-it
3. `updateEnemies()` คิดพิษ ลดเวลาสถานะ ฟื้น SP เรียก Behavior และเพิ่มศัตรูที่รออยู่ในคิว
4. `EnemySpacing.separate()` แยกมอนที่ยืนซ้อนกัน แล้ว `resolveObstacles()` จัดตำแหน่งให้อยู่นอกโต๊ะ/กำแพง
5. `projectiles.update()` เคลื่อนกระสุนและส่งผลการโดนให้ `CombatDamage`
6. `resolveObstacles()` อีกครั้ง เพราะ PowerBall อาจผลักศัตรูไปชิดสิ่งกีดขวาง
7. `removeDefeatedEnemies()` ลบตัวที่ตายและคืน SP ให้ผู้เล่นครั้งละ 5
8. คืน `true` เมื่อไม่มีศัตรูเหลือ เพื่อให้ `RunDirector` ไปขั้นตอนถัดไป

จังหวะเหล่านี้มีผลต่อเกม: ตัวที่ตายจากพิษจะไม่ได้โจมตีปิดท้าย, Post-it ที่เพิ่งเก็บนำไปใช้กับดาบในเฟรมเดียวกันได้ และกระสุนสะท้อนที่เพิ่งเกิดระหว่างตรวจชนเริ่มเคลื่อนในเฟรมถัดไป

### เก็บข้อมูลอะไรไว้ใน CombatRoom

| ข้อมูล | ความหมาย |
| --- | --- |
| `enemies` | ศัตรูทั้งหมดที่อยู่ในห้อง |
| `spawnQueue` | ศัตรูใหม่ที่รอเพิ่มหลังจบการวนอัปเดต เช่น ลูกน้องและร่างแยก M |
| `shots` | กระสุนผู้เล่นและศัตรู |
| `notebookImpacts` | ภาพ Notebook แตกแต่ละครั้ง; เป็นภาพอย่างเดียว |
| `exObjects`, `carriedNotes` | วัตถุในห้อง EX และจำนวน Post-it ที่ถือ |
| `obstacles`, `iceWall` | โต๊ะถาวรและกำแพงน้ำแข็งชั่วคราว |
| `blast*`, `splash*`, `beam*` | ตำแหน่งและเวลาของเอฟเฟกต์ที่ `GameRenderer` ใช้วาด |

`Enemy`, `Shot`, `ExObject` และ `NotebookImpact` ยังเป็น nested class ในไฟล์นี้เพื่อรวมข้อมูลของห้องไว้ด้วยกัน คลาสบอสเดิมยัง `extends CombatRoom.Enemy` ได้ ส่วนการตัดสินใจเดินหรือใช้สกิลอยู่ใน `*Behavior`

### ต้องแก้อะไร เปิดตรงไหน

- **ดาเมจ / เกราะ / กระสุนสะท้อน:** `CombatDamage.applyToEnemy()`
- **วงระเบิดและแรงผลัก PowerBall:** `CombatDamage.explodePowerBall()`
- **LAN ดึงมอน / Notebook พิษและ Splash:** `CombatDamage`
- **ความเร็ว การเลี้ยว หรือการชนของกระสุน:** `ProjectileSystem`; ค่าสร้างกระสุนยังอยู่ที่อาวุธหรือ Behavior ที่ยิง
- **การเรียกลูกน้องและเพดานจำนวนมอน:** `EnemyFactory.queueSummons()`
- **ฝุ่น สไลม์ และการเก็บ Post-it:** `ExBehavior.ExEnvironment`
- **เวลาท่า Cat:** `LiquidCatBehavior.LEAP_WINDUP`, `TRANSFORM_DURATION`, `DIVIDE_DURATION`
- **เวลาชาร์จและตำแหน่งธาตุ Sati:** `SatiBossBehavior.CHARGE_DURATION`, `elementX()`, `elementY()`
- **เวลาร่ายและวงปลอดภัย Talgo แบบเดิม:** `TalgoBossBehavior.CAST_DURATION`, `SAFE_RADIUS`

### Polymorphism ที่ใช้จริง

`EnemyBehavior.incomingDamageMultiplier(enemy)` คืนตัวคูณดาเมจที่ได้รับ ค่าเริ่มต้นเป็น `1` และ Behavior เฉพาะ override ได้:

- Liquid Cat ร่างย่อ: `0.55`
- Insecure Sword ช่วงเปิดช่องโหว่: `1.5`
- ศัตรูและช่วงอื่น: `1`

`CombatDamage` จึงเรียกกติกาของตัวนั้นผ่าน interface เดียวกัน การเพิ่มรูปแบบรับดาเมจใหม่ทำใน Behavior เจ้าของตัวนั้นได้

ชุดตรวจ `GameRulesCheck.checkCombatFrameOrder()` ตรวจเกราะแตก/สะท้อน การตายจากพิษ Post-it ที่หมดอายุในจังหวะเก็บ และตัวคูณดาเมจทั้งสองบอส โดยตรวจผ่านกับโค้ดก่อนและหลังแยกแล้ว

## รายละเอียดการต่อสู้และ OOP

เกมยังใช้ `schoolLife` เป็นจุดเริ่มและ `RunDirector` คุมลำดับห้อง ส่วนการต่อสู้แยกงานดังนี้

`RunPlan` จำว่าชนะ Ae, Far (Fa) และ Dome ครบในรันเดียวกันหรือยัง `RunDirector` จัดคิวห้อง `FINAL_EXAM` หนึ่งครั้ง โดยให้ EX Monster เดิมมาก่อนเมื่อจบ Loop 1 หรือเมื่อคิวตรงกับ EX ปลายลูปเลขคู่ จากนั้นแสดงจอดำข้อความ "Good...Let's us prepare the final EXAM" และค่อยเริ่มห้องตู้เซิร์ฟเวอร์ `EnemyFactory` วางบอสสามตัวตรงตู้ใน `ServerRoom.png`; `PlayerCharacter` ล็อกตำแหน่งแกน Y ให้ผู้เล่นเดินและ dash ได้เฉพาะซ้าย–ขวาในห้องนี้

| ส่วน | หน้าที่ |
| --- | --- |
| `EnemyFactory` | เลือกชนิดศัตรูและสร้างค่าสถานะสำหรับมอน บอส และ EX |
| `EnemyBehavior` | สัญญาว่าศัตรูทุกชนิดต้องอัปเดตพฤติกรรมได้ |
| `StandardEnemyBehavior`, `AjarnBehavior` | กติกาเดิน สัมผัสผู้เล่น และโจมตีที่มอนกับบอสใช้ร่วมกัน; มอนทั่วไปใช้ `StandardEnemyBehavior` ตรง ๆ |
| `AeBossBehavior`, `KenBossBehavior`, `MBossBehavior`, `SatiBossBehavior`, `DomeBossBehavior` | เลเซอร์ Ae, ระเบิด Ken, ร่างแยก M, หอก Sati และสายฟ้า/เลเซอร์ Dome |
| `ExBehavior` และคลาส EX ทั้งสาม | ท่าเฉพาะของ Liquid Cat, Allergic Vacuum และ Insecure Sword |
| `EnemyProjectilePatterns` | รูปแบบกระสุนของมอนและบอส |
| `EnemySpacing` | ดันศัตรูที่ซ้อนกันให้ออกจากกัน |
| `CombatRoom` | เก็บข้อมูลห้องและเรียก `enemy.updateBehavior(...)`; เพิ่มศัตรูจาก `spawnQueue` หลังจบลูปอัปเดต |
| `CombatDamage`, `ProjectileSystem` | คิดผลการโจมตี และเคลื่อน/ตรวจชนกระสุน ตามลำดับ |

คลาสตัวช่วยสั้น ๆ อยู่ในไฟล์ของระบบที่ใช้มัน: `EnemyBehavior.EnemyBehaviors` อยู่ใน `enemy/EnemyBehavior.java`, `EnemyProjectilePatterns` อยู่ใน `enemy/StandardEnemyBehavior.java`, `ExBehavior.ExEnvironment` อยู่ใน `enemy/ex/ExBehavior.java` และ `AbstractWeaponAttack` กับ `WeaponAttack.WeaponAttackSpec` อยู่ใน `weapon/WeaponAttack.java` หากเรียก nested class จากไฟล์อื่น สามารถ import ชื่อเต็ม เช่น `import io.github.kirinojuju.weapon.WeaponAttack.WeaponAttackSpec;`

`EnemyBehavior` เป็น interface ที่พฤติกรรมแต่ละตัว implement ผ่านคลาสแม่ ส่วน `WarperMonsterBehavior extends StandardEnemyBehavior`, `SatiBossBehavior extends AjarnBehavior`, `TalgoBossBehavior extends AjarnBehavior` และ EX ทั้งสาม `extends ExBehavior` เป็นตัวอย่างการสืบทอด พฤติกรรมที่ต่างกันถูกเรียกผ่าน interface เดียวกัน จึงไม่ต้องเพิ่ม `if` ของท่าเฉพาะทุกตัวในลูปหลักของ `CombatRoom`

เมื่อต้องเพิ่มศัตรูใหม่ ให้เพิ่มคลาสพฤติกรรมของตัวนั้น แล้วเลือกคลาสใน `EnemyBehaviors.forKind(...)` กำหนดค่าตอนเกิดใน `EnemyFactory` และเพิ่มภาพหรือคำเตือนท่าใน `GameRenderer` ตามต้องการ พฤติกรรมที่เก็บสถานะรายตัวควรใช้ข้อมูลใน `CombatRoom.Enemy`; ออบเจ็กต์พฤติกรรมถูกใช้ร่วมกันและไม่ควรเก็บสถานะของศัตรูตัวใดตัวหนึ่งไว้ในฟิลด์ของตน

## ฉากหน้าอาคารและเลือกรถใน GameRenderer

โค้ดส่วนนี้อยู่ใน `render/GameRenderer.java` โดยแบ่งหน้าที่เป็น 5 เมธอด:

| เมธอด | หน้าที่ |
| --- | --- |
| `drawEntrance()` | คุมลำดับการวาดฉากเริ่มต้น หน้าอาคาร และตัวเลือกกลับบ้าน |
| `drawEntranceBackground()` | วาดอาคาร 30 และภาพรถ |
| `drawEntranceOverlay()` | วาดแถบคำแนะนำและพื้นหลังปุ่ม YES/NO ตาม Phase |
| `drawEntranceText()` | วาดข้อความหน้าเริ่มเกม คำแนะนำหน้าอาคาร และข้อความตัวเลือกรถ |
| `drawCarEnding()` | วาดฉากจบกลับบ้านบนพื้นหลังดำ |

ตำแหน่งปุ่มอยู่ที่ `CAR_YES_BUTTON` และ `CAR_NO_BUTTON` โดยใช้ Rectangle เดียวกันทั้งวาดปุ่มและตรวจคลิก ส่วน `CAR_TARGET` คือกรอบคลิกรถ `touchJustPressed()` แปลงตำแหน่งเมาส์จากหน้าจอเป็นพิกัดเกมก่อนตรวจกรอบเหล่านี้

แก้ข้อความฉากจบได้ที่ `CAR_ENDING_MESSAGE`; ข้อความไทยยังต้องใช้ฟอนต์ที่รองรับภาษาไทย ฟอนต์ BitmapFont ค่าเริ่มต้นที่ใช้ตอนนี้ยังแสดงข้อความไทยได้ไม่ครบ การเคลื่อนที่และระยะเข้าใกล้รถอยู่ใน `world/EntranceScene.java` ส่วนผลของการเลือก YES/NO อยู่ใน `RunDirector.openCarPrompt()` และ `chooseCar()`

## เปิดทดสอบ EX โดยตรง

เปิดด้วย `gradlew.bat :lwjgl3:run --args="--test-ex" --no-daemon` จะเริ่มที่ Liquid Cat กด F1/F2/F3 เลือก Liquid Cat / Allergic Vacuum / Insecure Sword, R เริ่มสู้ตัวเดิมใหม่ และ Enter หลังชนะเริ่มการทดสอบตัวเดิมอีกครั้ง ทุกครั้งสร้างผู้เล่นและห้องใหม่พร้อม HP/SP 100, LAN, Notebook และท่าเฉพาะ EX เลือกบอสตอนเปิดได้ด้วย `--test-liquid-cat`, `--test-allergic-vacuum`, `--test-insecure-sword`

`schoolLife.TestMode` เก็บหมายเลข EX ของโหมดทดสอบ; `Lwjgl3Launcher` อ่านตัวเลือกตอนเปิดเกม; `RunDirector.startExTest()` เตรียมอาวุธและเข้าห้องจริง; `schoolLife.updateExTestControls()` สลับบอสหรือเริ่มใหม่ และ `RunDirector.exTest` ใช้แสดงคำแนะนำบน HUD ชุดตรวจ `checkExTestEntry()` ตรวจบอสทั้งสาม อาวุธ วัตถุในห้อง และการเคลียร์ห้อง

## ภาพ Insecure Sword

`render/InsecureSwordSprite.java` โหลด `idle_1..5.png` และ `attack_1..5.png` จาก `assets/characters/boss_ex/insecure-sword/` วาดที่ขนาด 128×128 (ภาพต้นฉบับ 64×64) โดยปรับขนาดได้ที่ `SIZE` ส่วน Idle ใช้ `IDLE_FRAME_TIME` เฟรมละ 0.16 วินาที

`enemy/ex/InsecureSwordBehavior.java` เริ่ม `Enemy.swordAttackTime` เมื่อฟันหลังวงเตือนหรือฟันพลาด แล้วนับเวลาลงเพื่อเลือกเฟรม Attack ตั้งแต่ต้นทุกครั้ง การติด Post-it หยุดแอนิเมชันโจมตี ช่วงเตือนและช่วงพักที่เหลือกลับไปใช้ Idle; `GameRenderer` วาดภาพและแถบเลือด/เกราะที่อยู่เหนือดาบ และ `OptionalAssetTexture` มีภาพดาบสำรองเมื่อไม่มีไฟล์ PNG

## อ่าน Logic หลังจัด Clean Code

ใช้ไฟล์เดิม โดยแบ่งเมธอดตามงานที่ทำจริง:

| จุดเริ่ม | ลำดับที่ควรอ่าน |
| --- | --- |
| `CombatRoom.update()` | อัปเดตวัตถุ EX → ศัตรูและสถานะ → แยกศัตรูที่ซ้อนกัน → กระสุนและการชน → ลบศัตรูที่ตาย |
| `DomeBossBehavior.update()` | นับเวลา → อัปเดตแนวเลเซอร์ → ตรวจดาเมจเลเซอร์ → `advancePhase()` เปลี่ยนท่า |
| `DomeBossBehavior.startCast()` | ตรวจ SP → จ่าย SP → จำเป้าหมาย → เริ่มเตือนก่อนโจมตี |
| `DomeSprite.draw()` | `characterFrame()` เลือกภาพตามสถานะ → วาดตัวละคร; `drawEffect()` แยกวาดสายฟ้ากับเลเซอร์ |
| `EnemyFactory.createMonster()` | เลือกค่าสถานะพื้นฐานตามชนิดใน `switch` → คูณค่าตามลูป → เพิ่มเกราะถ้ามี |
| `EnemySpacing.pushShare()` | เลือกว่าตัวใดควรถูกดัน โดยบอสบนตู้อยู่กับที่และมอนทั่วไปหลีกให้บอส |
| `RunDirector.finishRoom()` | เลือกกติกาหลังจบห้อง MOBS / BOSS / EX / FINAL_EXAM โดยกติกาบอสกับ EX มีเมธอดของตัวเอง |
| `GameRenderer.draw()` | เลือกฉาก → วาดฉากหลัง → `drawCombatShapes()` → `drawCombatSprites()` → `drawCombatPanel()` → เฟดฉาก |

ค่าปรับสกิล Dome อยู่ด้านบน `DomeBossBehavior`: เวลา, SP, ดาเมจ, `LIGHTNING_RADIUS`, `LASER_HALF_WIDTH` และ `LASER_MAX_LENGTH` ภาพและพื้นที่โจมตีใช้ค่าขนาดเดียวกัน

### จังหวะโจมตี Ae

`AjarnBehavior.tryAeWarp()` ใช้สถานะวาร์ปเดิมของ `CombatRoom.Enemy` และวงเตือนเดิมใน `GameRenderer` โดยล็อก `warpX/warpY` ตอนเริ่มเตือน 0.8 วินาที ผู้เล่นเดินออกจากวงรัศมี 75 หรือ Dash หลบได้ เมื่อวาร์ปถึงจุดเตือนจะสลับจับ 1 วินาที / โจมตี 14 ดาเมจพื้นฐาน เฉพาะผู้เล่นที่ยังอยู่ในวงและไม่มีสถานะอมตะ จากนั้นกลับจุดเดิมใน 0.38 วินาที และพักคูลดาวน์ 7 วินาที (ลดตามลูปได้ต่ำสุด 5 วินาที) วาร์ปครั้งแรกรอ 6 วินาทีก่อนเตือน

กระสุน Ae ใน `StandardEnemyBehavior` ยิงทุก 1.1 วินาที ความเร็วพื้นฐาน 220 และดาเมจพื้นฐาน 8 ยังเป็นกระสุนกระจายสามนัดและยิงข้ามโต๊ะได้ ใน FINAL_EXAM ใช้จังหวะกระสุนนี้เช่นกันและยืนบนตู้ ชุดตรวจ `checkAeBalance()` ครอบคลุมวงเตือนล็อกตำแหน่ง การเดินหลบ การจับ ดาเมจ การ Dash ที่ติดโต๊ะ จังหวะยิง และบอสบนตู้

## ลูกน้องประจำห้อง Boss

ทุกห้อง `BOSS` เริ่มด้วยบอส 1 ตัวและมอนลูกน้องต่างชนิดกัน โดย Loop 1 มี 2 ตัว, Loop 2 มี 3 ตัว และ Loop 3 เป็นต้นไปมี 5 ตัว ใช้ `enemy/EnemyFactory.populateBossRoom(...)` ชุดเดียวสำหรับบอสทุกตัว หมุนเลือกจาก `BASIC`, `RANGED`, `WAVE`, `SHIELDED`, `BINDER`, `WARPER` ตาม ID ของบอส ใน Loop แรกค่อย ๆ เปิดชนิดตามด่าน: BASIC/RANGED → เพิ่ม WAVE → เพิ่ม SHIELDED → เพิ่ม BINDER; WARPER เข้ากลุ่มลูกน้องตั้งแต่ Loop 2 ทุกชนิดใช้ `createMonster(...)` และ AI เดิม รวมถึงการเพิ่มค่าสถานะตามด่าน/ลูป เฉพาะชนิด SHIELDED มีเกราะตามกติกามอนปกติ

ปรับชนิดได้ใน `BOSS_MINION_KINDS` จำนวนตาม Loop ใน `populateBossRoom()` และตำแหน่งใน `BOSS_MINION_POSITIONS` ซึ่งรองรับสูงสุด 5 จุดที่เว้นระยะจากจุดเข้าห้องของผู้เล่น และตรวจการชนโต๊ะก่อนเริ่มต่อสู้ ลูกน้องเริ่มห้องเกิดครั้งเดียว; Ken และ Sati สามารถเรียกลูกน้องเพิ่มตามระบบของเพื่อน ต้องชนะทั้งบอสและลูกน้องจึงจบห้อง; HUD แสดง `MINIONS` ที่เหลือ และ `BOSS DOWN` หากบอสตายก่อน

Ken และ Sati ใช้ `summoner`, `summonTimer` และ `queueSummons()` โดยเพิ่มลูกน้องผ่าน `spawnQueue` หลังอัปเดตศัตรู เพดานลูกน้องที่มีชีวิตสำหรับการเรียกเพิ่มคือ 4 ตัว; หากเริ่มห้องด้วย 5 ตัวจะรอจนต่ำกว่าเพดานก่อน M ใช้คิวเดียวกันสร้างร่างแยกหนึ่งครั้งเมื่อ HP ถึงครึ่ง ห้อง `EX` ยังมี EX ตัวเดียว และ `FINAL_EXAM` มีบอสบนตู้สามตัวตามเดิม ชุดตรวจ `checkBossMinions()` และ `CustomBossCheck` ตรวจการเกิด การจำกัดจำนวน และการเคลียร์ห้อง

### เส้นทางไป EX และค่าความยาก

`RunPlan.hasExAfterYear()` เปิด EX หลัง Loop 1 ครั้งแรก จากนั้นตามรอบ Loop 2, 4, 6, ... เลือกอัปเกรดหลังบอสตัวที่สี่ก่อนเข้า EX ใน Loop แรก หากชนะ Ae/Far/Dome ครบก่อนด่านสุดท้าย จะเก็บคิว FINAL_EXAM ไว้จนผ่าน EX แรกก่อน: EX → Cutscene → FINAL_EXAM → Loop ถัดไป รอบต่อไปที่ปลดล็อก FINAL_EXAM พร้อม EX ก็ใช้ลำดับนี้เช่นกัน

`EnemyFactory.populateMonsters()` สร้างมอน `4 + stage + min(2, loop - 1)` ตัว จึงมี 4–7 ตัวใน Loop แรก `RunDirector.MONSTER_CLEAR_HEAL` ฟื้น 12 HP เมื่อชนะห้องมอน และ `BOSS_CLEAR_HEAL` ฟื้น 35 HP เมื่อชนะบอส โดยไม่เกิน max HP; HUD ใช้ค่าชุดเดียวกัน อัตราเพิ่มต่อ Loop จากค่าพื้นฐานใน `RunPlan` คือ HP/เกราะ +15%, SP/การฟื้น SP +10%, ดาเมจ +10%, ความเร็ว +4% ชุดตรวจ `checkFirstExProgression()` ตรวจการเคลียร์สี่ด่านจนถึง EX และต่อไป Loop 2 หลาย seed ส่วน `checkFinalExamMechanics()` ตรวจลำดับ EX ก่อน FINAL_EXAM ทั้ง Loop 1 และ Loop 2

## โต๊ะและที่กำบังในห้องมอนสเตอร์และ Boss

`RoomObstacles.java` รวมข้อมูลโต๊ะ การชน และทางเดินอ้อม โดยมี `TableSprites` อยู่ในไฟล์เดียวกัน ห้อง `MOBS` และ `BOSS` สุ่มโต๊ะใหม่ 3 ตัวเมื่อเข้าห้อง: โซนซ้ายใกล้ (365, 310), โซนกลางเลือกด้านบน (640, 435) หรือด้านล่าง (640, 195), โซนขวาใกล้ (940, 300) แต่ละตำแหน่งสุ่มเพิ่ม X ±30 และ Y ±10 โดยเว้นช่องเดินระหว่างโซนกับจุดเกิดผู้เล่น ห้อง EX และ FINAL_EXAM ใช้กติกาฉากเดิม

ปรับโซนได้ใน `RoomObstacles.begin()` และระยะสุ่มใน `randomTable()` สุ่มภาพจาก `table00`, `table01`, `table04`; โซนกลางใช้เฉพาะโต๊ะเตี้ยเพื่อไม่บังจุดเกิดผู้เล่น `table03` เป็นภาพเงาจึงยังไม่ใช้สุ่ม โต๊ะจะอยู่ตำแหน่งเดิมตลอดการสู้ในห้องนั้น ใช้ `CombatRoom.random` เพื่อให้ทดสอบซ้ำด้วย seed เดิมได้ และจัดศัตรูที่ทับโต๊ะออกก่อนเริ่มสู้

- `GameRenderer` ใช้ `scene/sceneroom_not_table.png` เป็นพื้นห้องมอนสเตอร์และ Boss แล้ววาด Object โต๊ะแยก จึงไม่มีโต๊ะซ้ำกับพื้นหลัง และทุกโต๊ะที่เห็นในสองห้องนี้มีกรอบชนจริง
- `ART_BOUNDS` บอกขอบภาพโต๊ะภายใน canvas โปร่งใสของ PNG; ขนาดวาดเป็น 2 เท่า ส่วน `Table.solid` เป็นกรอบชนที่เว้นขอบภาพไว้เล็กน้อย
- `PlayerCharacter.moveBy()` ตรวจแกน X และ Y แยกกัน จึงเดินเลียบขอบโต๊ะได้ ส่วน `dash()` ตรวจตลอดแนวการพุ่งก่อนขยับ เพื่อไม่ให้ทะลุโต๊ะหรือมุมโต๊ะ
- `StandardEnemyBehavior` ใช้ `moveToward()` เลือกทางสั้นผ่านมุมโต๊ะที่เดินถึงได้ มอนยิงไกลจะเดินอ้อมหากโต๊ะบังแนวยิงอยู่
- `ProjectileSystem.updateShot()` ตรวจเส้นทางกระสุนจากตำแหน่งเดิมไปตำแหน่งใหม่ กระสุนผู้เล่นและมอนทั่วไปชนโต๊ะได้แม้เคลื่อนที่เร็ว Notebook เล่นภาพแตกที่จุดชน และ PowerBall ระเบิดเมื่อชนโต๊ะ
- กระสุนศัตรูทุกแบบสร้างผ่าน `addEnemyShot(source, shot)` ซึ่งตั้ง `Shot.ignoresTables` เมื่อผู้ยิงเป็นบอส (`kind >= 0`) บอสจึงยิงข้ามโต๊ะได้ รวมลูกเดี่ยว กระจาย รอบตัว ลูกใหญ่ คลื่น หอก Sati และกระสุนสะท้อน โต๊ะยังขวางการเดินตามเดิม
- โต๊ะไม่เพิ่มดาเมจหรือ Splash ให้ Notebook ส่วนดาเมจวงระเบิดของ PowerBall ยังใช้กติกาเดิม จึงสามารถโดนศัตรูใกล้โต๊ะได้
- สายฟ้า เลเซอร์ และ Heat Beam เป็นท่าที่แยกจากกระสุนทั่วไป และยังใช้การโจมตีเดิม โต๊ะยังทำลายไม่ได้
- `resolveEnemy()` จัดตำแหน่งศัตรูที่เกิดหรือวาร์ปเข้าโต๊ะ และตรวจหลังแยกศัตรูที่ซ้อนกันกับหลังทำดาเมจ

เปิดห้องมอนสเตอร์โดยตรงด้วย `gradlew.bat :lwjgl3:run --args="--test-obstacles" --no-daemon` ใช้ WASD/ลูกศรเดิน, Shift dash, 1/2 เลือกอาวุธ, คลิกซ้าย/Space โจมตี และ E ใช้ PowerBall เมื่อแพ้กด R จะเริ่มโหมดทดสอบนี้ใหม่ ชุดตรวจ `checkRoomObstacles()` ครอบคลุมการเดินเลียบโต๊ะ Dash เฉียง กระสุนกับที่กำบัง และการเปลี่ยนห้อง; `checkRandomTables()` ตรวจ 100 ห้องที่สุ่ม ตำแหน่งเกิด และการเดินข้ามห้อง; `checkBossShotsAcrossTables()` ตรวจบอส 7 รูปแบบยิงผ่านโต๊ะไปโดนผู้เล่นจริง

## Dome: ภาพและสกิล

- `DomeBossBehavior extends AjarnBehavior` คุมการสลับสายฟ้าและเลเซอร์ผ่าน `enemy.updateBehavior(...)` โดยแทนการยิงคลื่นแบบเดิมของ Dome
- `CombatRoom.Enemy.dome` เก็บ `State` แยกต่อบอสแต่ละตัว เช่น `phase`, `remaining`, `elapsed`, จุดเป้าหมาย `aimX/aimY` และแนวลำแสง `originX/originY/endX/endY` จึงใช้ Behavior ตัวเดียวร่วมกันได้
- ลำดับคือ `IDLE → LIGHTNING_CHARGE → LIGHTNING_STRIKE → RECOVERY → IDLE → LASER_CHARGE → LASER_FIRE → RECOVERY` แล้ววนใหม่
- สายฟ้าใช้ 28 SP ล็อกตำแหน่งผู้เล่นตอนเริ่มเตือน 0.85 วินาที ก่อนฟาดในรัศมี `LIGHTNING_RADIUS` (ปัจจุบัน 100) ด้วยดาเมจฐาน 22; เคลื่อนที่ออกจากวงหรือ dash ตรงจังหวะฟาดเพื่อหลบได้ ภาพสายฟ้าอยู่ 0.4 วินาที
- เลเซอร์ใช้ 38 SP เตือนและชาร์จ 1 วินาที ยิง 0.6 วินาทีด้วยดาเมจฐาน 18 โดยเคารพช่วงอมตะหลังโดนโจมตี ในห้องปกติเล็งตำแหน่งผู้เล่นตอนเริ่มชาร์จ ใน `FINAL_EXAM` ยิงตรงลงจากตู้
- แต่ละท่ามีพักฟื้น 0.3 วินาที ก่อนพักระหว่างท่าประมาณ 0.9 วินาที (ลดลงตามความเร็วของลูป แต่ไม่ต่ำกว่า 0.55 วินาที) ดาเมจเพิ่มตามลูป ชุดตรวจจำลองเวลาจริงของลูปแรกและยืนยันว่าเริ่มร่ายได้อย่างน้อย 5 ครั้งใน 12 วินาที รวมช่วงชาร์จ โจมตี พัก และการฟื้น SP แล้ว
- `DomeSprite` อ่านเฟรมจาก `characters/boss/Tnetwork(Dome)/` เลือกภาพตาม `phase/elapsed` รวมภาพเตรียมร่าย ฟาดสายฟ้า ชาร์จเลเซอร์ ยิงด้านข้าง/ลง และพักฟื้น ส่วน `GameRenderer` วาดวงหรือเส้นเตือนก่อนโจมตี
- `OptionalAssetTexture` มีภาพสำรองสำหรับ Dome เมื่อไม่มี assets; `DomeSprite.dispose()` คืนหน่วยความจำภาพเมื่อปิดเกม

ชุดตรวจ `checkDomeSkills()` ใน `GameRulesCheck` ครอบคลุมการเตือนก่อนโจมตี การหลบ ดาเมจ การสลับท่า สถานะบอสแยกกัน และเลเซอร์แนวดิ่งใน `FINAL_EXAM` ใช้ `--test-final-exam` เพื่อเปิดห้องสามบอสและดูภาพจริงได้

### เสียง Dome

ไฟล์อยู่ที่ `assets/characters/soundfx/boss_fx/Dome/` โดย `DomeBossBehavior.enter(...)` เรียก `GameAudio` ครั้งเดียวตอนเปลี่ยนสถานะ:

| สถานะ | เมธอดใน `audio/GameAudio.java` | ไฟล์ OGG |
| --- | --- | --- |
| `LIGHTNING_CHARGE` | `domeLightningCast()` | `lighting_efx_cast_sound.ogg` |
| `LIGHTNING_STRIKE` | `domeLightningStrike()` | `lighting_efx_hit_sound.ogg` |
| `LASER_CHARGE` | `domeLaserCast()` | `laser_efx_cast_sound.ogg` |
| `LASER_FIRE` | `domeLaserFire()` | `laser_efx_hit_sound.ogg` |

เสียงเอฟเฟกต์เล่นเมื่อปล่อยท่าแม้ผู้เล่นหลบได้ และไม่เรียกซ้ำระหว่างเฟรมของท่านั้น ใช้จังหวะเดียวกันในห้องปกติและ FINAL_EXAM หาก SP ไม่พอหรือบอสติด stun จนยังไม่เริ่มท่า จะยังไม่มีเสียงชาร์จ ระดับเสียงชาร์จเป็น `.45f` และตอนปล่อยท่า `.60f` ปรับได้ในเมธอดข้างต้น

แปลงเสียงใหม่ทั้ง 10 ไฟล์จาก M4A เป็น OGG แล้ว โดยคง M4A ต้นฉบับไว้ เสียงชาร์จ OGG สองไฟล์ย่อเวลาด้วยการปรับ tempo โดยคง pitch ให้ตรงกับช่วงชาร์จ 0.85 และ 1 วินาที อีก 6 ไฟล์ (glitch, bass และสายฟ้าตัวเลือกที่รวม impact) เตรียมไว้แต่ยังไม่มีจังหวะเรียกใช้ `GameAudio.dispose()` คืนเสียงทั้งสี่เมื่อปิดเกม และเกมยังเปิดได้เมื่อไม่มีไฟล์เสียงเหล่านี้

## อาวุธและ Ability

`PlayerCharacter` รับปุ่มและเลือกอาวุธ ส่วน `LanWeaponAttack` และ `NotebookWeaponAttack` คำนวณการโจมตีของตัวเอง ทั้งคู่ implement `WeaponAttack` และสืบทอด `AbstractWeaponAttack` เพื่อใช้ระบบลงทะเบียน Ability ร่วมกัน ค่าของการโจมตีครั้งหนึ่งอยู่ใน `WeaponAttackSpec` เช่น damage, interval, spCost, projectileSpeed, poison และ splash

ภาพ Notebook อยู่ใน `assets/weapons/notebook/`: `notebook.png` เป็นไอคอนอาวุธ, `notebook_flying1–9.png` วนตอนบิน และ `notebook_explode1–6.png` เล่นครั้งเดียวเมื่อยิงโดน `NotebookSprite` อยู่ในไฟล์ `NotebookWeaponAttack.java` และให้ `GameRenderer` เรียกวาด จึงใช้ไฟล์โค้ดเดิม

`NotebookWeaponAttack` ทำเครื่องหมาย `shot.notebook` บนกระสุนที่ยิง ส่วน `CombatRoom` เพิ่ม `shot.animationTime` และสร้าง `NotebookImpact` เมื่อชน เอฟเฟกต์แต่ละครั้งเก็บตำแหน่งและเวลาของตัวเอง เล่นจบใน 0.36 วินาทีแล้วถูกลบ และล้างทั้งหมดเมื่อเปลี่ยนห้อง ภาพแตกไม่มีดาเมจเพิ่ม; ดาเมจวงกว้างยังต้องมีอัปเกรด Splash ตามเดิม ส่วน Heat Beam ใช้ภาพลำแสงเดิม

ปรับขนาดและความเร็วภาพได้ที่ `NotebookSprite`: `PROJECTILE_SIZE = 48`, `IMPACT_SIZE = 64`, `FLYING_FRAME_TIME = .06f` ส่วนเวลาภาพแตกอยู่ที่ `CombatRoom.NOTEBOOK_IMPACT_DURATION` ภาพสำรองกรณีไม่มี assets อยู่ใน `OptionalAssetTexture.drawNotebook()`

เพื่อนที่ทำ Ability ของ Notebook สามารถสร้างคลาสที่ implement `WeaponAbility` แล้วลงทะเบียนเมื่อผู้เล่นปลดล็อก เช่นจาก `Upgrade.apply(...)`:

```java
package io.github.kirinojuju.weapon;

import io.github.kirinojuju.player.PlayerCharacter;
import io.github.kirinojuju.weapon.WeaponAttack.WeaponAttackSpec;

final class FasterNotebookAbility implements WeaponAbility {
    @Override
    public void modify(PlayerCharacter player, WeaponAttackSpec attack) {
        attack.projectileSpeed += 120;
    }
}
```

ลงทะเบียนจากโค้ดที่มีตัวแปร `player` โดย import `FasterNotebookAbility` หากอยู่คนละ package (กรณีนั้นให้ประกาศคลาส Ability เป็น `public`):

```java
player.addWeaponAbility(PlayerCharacter.Weapon.MAGICAL,
    new FasterNotebookAbility());
```

`modify(...)` ควรแก้เฉพาะ `WeaponAttackSpec` ของครั้งนั้น เพราะจะถูกเรียกก่อนตรวจ SP หากโจมตีสำเร็จและต้องการผลเพิ่มเติม ให้เขียน `afterAttack(...)` ด้วย Ability ของ LAN ลงทะเบียนกับ `Weapon.PHYSICAL` แบบเดียวกัน แต่ใช้ค่าที่มีผลกับ LAN เช่น `reach` หรือ `halfAngle`

อัปเกรดเดิม เช่น poison, splash, slow และ wide sweep ยังคงใช้ flag ของผู้เล่นเพื่อให้หน้าจอและกติกาเดิมทำงานเหมือนเดิม Ability ใหม่ใช้ช่องต่อด้านบนได้โดยไม่ต้องแก้เมธอด `PlayerCharacter.update()` หรือ `CombatRoom.update()`

ก่อนส่งงาน ให้รัน `gradlew.bat :core:verifyGameRules :lwjgl3:classes` แล้วลองเล่นท่าที่แก้จริง ชุดตรวจครอบคลุมลำดับห้อง AI บอสและ EX อาวุธ และตัวอย่าง Ability ที่ปรับการโจมตี Notebook

## งานที่รวมจาก GitHub วันที่ 11 ตุลาคม 2026

รายละเอียดที่มาและรายการภาพที่ต้องรับจากเพื่อนอยู่ใน `INTEGRATION.md` คลาส `KenBoss`, `AeBoss`, `MBoss`, `SatiBoss` และ `LiquidCatBoss` สืบทอดจาก `CombatRoom.Enemy`; คลาส `*BossBehavior` รับผิดชอบท่าเฉพาะ ส่วนคลาสบอสเก็บสถานะรายตัว `LiquidCatBoss` เรียก `LiquidCatBehavior` ร่วมกับ Enemy ปกติ จึงไม่มีสำเนาวงจรโจมตีสองชุด

- `AeBossBehavior`: เตือน 0.9 วินาที → เลเซอร์แนวตั้งกวาดจากซ้ายไปขวา 5 วินาที → พัก 5 วินาที พร้อมท่าวาร์ปและกระสุนเดิม; FINAL_EXAM ใช้บอสบนตู้ตามเดิม
- `KenBossBehavior`: สุ่มระเบิด 3–5 ลูก พร้อมภาพเตือนและระเบิด; `MBossBehavior`: แยกร่างหนึ่งครั้งเมื่อ HP ถึงครึ่ง
- ID ของ M คือ 2 และ Far คือ 6 ทั้งห้องปกติและ FINAL_EXAM; ชนะ M จะไม่เพิ่มความคืบหน้า Ae–Far–Dome

### Ice Wall

กด **F** หรือปุ่ม **ICE** → `PlayerCharacter.tryStartIceWall()` ตรวจ SP/คูลดาวน์ → `CombatRoom.raiseIceWall()` สร้างกำแพง ทำดาเมจ 24 และ Stun 0.8 วินาทีกับศัตรูในระยะ 112 → `world/IceWall` เก็บอายุและคำนวณการชน → `IceWallSprite` วาดภาพ

ใช้ 25 SP คูลดาวน์ 7 วินาที กำแพงอยู่ 4.5 วินาที ยาว 180 หน่วย วางข้างหน้า 92 หน่วยตาม `faceX/faceY` ขณะกด และหันตั้งฉากกับทิศนั้น ไม่เลือกมอนใกล้สุดเพื่อเปลี่ยนทิศให้ กำแพงอยู่ที่เดิมตลอดอายุ หากจุดกึ่งกลางออกนอกพื้นห้องจะคืน `false` โดยไม่หัก SP หรือเริ่มคูลดาวน์ ขวางการเดิน/Dash/มอนสเตอร์และกระสุนศัตรู รวมกระสุนบอสที่ข้ามโต๊ะได้ กระสุนผู้เล่นผ่านได้ การชนกระสุนใช้เส้นทางตลอดเฟรมจึงกันลูกที่เคลื่อนที่เร็วได้ บอสบนตู้ยังถูกล็อกตำแหน่งและผู้เล่นยังเดินได้เฉพาะซ้าย–ขวาใน FINAL_EXAM

`GameRenderer.iceWallButtonJustPressed()` ใช้ตัวแปลงพิกัดเมาส์ร่วมกับปุ่มอื่น และยึดการกดไว้เพื่อไม่ให้คลิกสกิลแล้วยิงอาวุธปกติพร้อมกัน เมื่อเปลี่ยนห้องหรือรีเซ็ต EX จะล้างกำแพงและคูลดาวน์


### ภาพมอนสเตอร์ ปกเกม และ Potion

- `render/MonsterSprite`: รวมตัวโหลด Slime แบบ PNG แยก และ Blob แบบ sprite sheet ในไฟล์เดียว; Basic/Shielded ใช้ Slime, Binder/Wave ใช้ Blue Blob, Warper/Ranged ใช้ Green Blob การเปลี่ยนภาพไม่เปลี่ยนชนิดหรือพฤติกรรมของมอน
- Blob หนึ่งเฟรมกว้าง 64 สูง 32: Idle 6 เฟรม, Run 8 เฟรม เลือก TextureRegion ของช่องนั้นก่อนตัดขอบใส จึงไม่มีเฟรมข้างเคียงติดมาด้วย `CombatRoom` เก็บ `moving` จากตำแหน่งที่ขยับจริงเพื่อเลือก Idle/Run
- `GameRenderer.drawTitleScreen()` อ่าน `scene/ปกเกม/Whatzz.png` รักษาสัดส่วน แล้ววาดชื่อเกม/Enter; หากไม่มีภาพยังใช้หน้าเริ่มเดิม
- `player/PotionInventory`: เก็บจำนวน HP/SP อย่างละ 2 ต่อ run, `useHp()` ฟื้น 40 และ `useSp()` ฟื้น 35 โดยจำกัดที่ค่าสูงสุด การใช้ตอนเต็มหรือแพ้จะคืน false และไม่เสียขวด จำนวนไม่รีเซ็ตตอนเปลี่ยนห้อง
- `PlayerCharacter.update()`: Tab สลับอาวุธ, Z/X เรียกขวดยา; Potion ใช้ผ่านแป้นพิมพ์ระหว่างต่อสู้ได้และ HUD แสดงจำนวน ยังไม่มีระบบดรอปหรือเติมขวด
- `LiquidCatBehavior`: จัดลำดับแปลงร่าง → ร่าย Divide/Leap ที่ยังค้าง → เปลี่ยนร่าง/เริ่มท่าใหม่ ไม่เดินหรือทำ contact damage ระหว่างร่าย Divide ปล่อยกระสุนนัดเดียวเมื่อตัวนับข้าม 0.3 วินาทีที่เหลือ จุดเล็งและทิศภาพล็อกตอนเริ่มท่า
- `LiquidCatSprite`: ขนาดร่างค่อย ๆ เปลี่ยนระหว่าง Transform, กลับภาพท่าที่ไม่มีซ้าย/ขวา และแสดงวัตถุโจมตีช่วงเตือน/กระแทก กระสุนตัดเฉพาะภาพจริง 10x10 แล้ววาดให้ตรงกับรัศมีชน
