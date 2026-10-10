# โครงสร้างโค้ดเกมสำหรับทีม

รายละเอียดการเชื่อม Ken/M/Sati/Liquid Cat เดิมกับ cleanup อยู่ใน [INTEGRATION.md](INTEGRATION.md).

## เริ่มอ่านตรงนี้: เข้าใจภาพรวมด้วย 6 ไฟล์

ปัจจุบันมี Java ฝั่งเกมหลัก 48 ไฟล์ใต้ `core/src/main/java/io/github/kirinojuju/` อีก 2 ไฟล์เป็นตัวเปิดเกมบนคอมพิวเตอร์ และ 3 ไฟล์เป็นชุดตรวจเกม จัดโฟลเดอร์และ Java package ตามระบบแล้ว โดยมี `schoolLife.java` เป็นจุดเริ่มที่โฟลเดอร์หลัก ให้เริ่มตามลำดับนี้:

| ลำดับ | ไฟล์ | คำถามที่ไฟล์นี้ตอบ |
| --- | --- | --- |
| 1 | `schoolLife.java` | เกมสร้างระบบอะไรบ้าง และเรียกอะไรในแต่ละเฟรม? |
| 2 | `flow/RunDirector.java` | ตอนนี้อยู่หน้าไหน ห้องไหน และจะเปลี่ยนไปไหนต่อ? |
| 3 | `flow/RunPlan.java` | อยู่ลูป/ด่านไหน เจอบอสอะไร และชนะบอสตัวไหนแล้ว? |
| 4 | `player/PlayerCharacter.java` | รับปุ่มอย่างไร เดิน Dash ใช้สกิล เลือกอาวุธ และเก็บสถานะผู้เล่นอย่างไร? |
| 5 | `combat/CombatRoom.java` | อัปเดตศัตรู กระสุน การชน ดาเมจ และลบสิ่งที่ตายอย่างไร? |
| 6 | `render/GameRenderer.java` | นำสถานะเหล่านั้นไปวาดฉาก ตัวละคร เอฟเฟกต์ และหน้าจออย่างไร? |

ภาพรวมหนึ่งเฟรมคือ `schoolLife.render()` → `RunDirector` ประมวลผลตามสถานะเกม → ระหว่างต่อสู้เรียก `PlayerCharacter` และ `CombatRoom` → `GameRenderer.draw()` วาดผลลัพธ์ เมื่อเคลียร์ห้องแล้ว `RunDirector` เลือกขั้นตอนถัดไปโดยใช้ข้อมูลจาก `RunPlan`

## ไฟล์ที่เหลือเปิดเมื่อทำระบบนั้น

ตารางนี้แสดงตำแหน่งครบทั้ง 48 ไฟล์ โดยชื่อในช่องไฟล์คือชื่อก่อน `.java`:

| โฟลเดอร์ | ไฟล์ | หน้าที่ |
| --- | --- | --- |
| โฟลเดอร์หลัก (1) | `schoolLife` | เริ่มเกมและเรียกระบบแต่ละเฟรม |
| `flow/` (2) | `RunDirector`, `RunPlan` | หน้าจอ ลำดับห้อง ด่าน ลูป และความคืบหน้า |
| `player/` (2) | `PlayerCharacter`, `Upgrade` | ผู้เล่นและตัวเลือกอัปเกรด |
| `weapon/` (4) | `WeaponAttack`, `WeaponAbility`, `LanWeaponAttack`, `NotebookWeaponAttack` | การโจมตีและช่องต่อ Ability ของอาวุธ |
| `combat/` (1) | `CombatRoom` | ศัตรูและกระสุนในห้อง การชน และผลดาเมจ |
| `enemy/` (7) | `EnemyBehavior`, `StandardEnemyBehavior`, `WarperMonsterBehavior`, `EnemyFactory`, `EnemySpacing`, `AlgoMonster`, `NetworkMonster` | พฤติกรรมร่วม มอนทั่วไป การสร้างศัตรู และการแยกตัวที่ซ้อนกัน |
| `enemy/boss/` (11) | `AjarnBehavior`, `SatiBossBehavior`, `TalgoBossBehavior`, `DomeBossBehavior`, `KenBoss`, `KenBossBehavior`, `AeBoss`, `AeBossBehavior`, `MBoss`, `MBossBehavior`, `SatiBoss` | พฤติกรรมบอสอาจารย์และท่าเฉพาะ |
| `enemy/ex/` (5) | `ExBehavior`, `LiquidCatBehavior`, `AllergicVacuumBehavior`, `InsecureSwordBehavior`, `LiquidCatBoss` | พฤติกรรม EX Monster |
| `render/` (11) | `GameRenderer`, `StudentSprite`, `SatiSprite`, `DomeSprite`, `LiquidCatSprite`, `InsecureSwordSprite`, `OptionalAssetTexture`, `KenSprite`, `AeSprite`, `MSprite`, `AlgoMonSprite` | วาดเกม ภาพตัวละคร และภาพสำรอง |
| `world/` (3) | `GameSpace`, `EntranceScene`, `RoomObstacles` | ขอบพื้นที่ ทางเข้าอาคาร โต๊ะ และทางเดินอ้อม |
| `audio/` (1) | `GameAudio` | เสียงเกม |

Java package ตรงกับโฟลเดอร์ เช่น `player/PlayerCharacter.java` ใช้ `package io.github.kirinojuju.player;` เมื่อเพื่อนเพิ่มไฟล์ ให้ใช้ package ของระบบนั้นและ import คลาสจากระบบอื่นตามต้องการ ชุดตรวจยังอยู่ที่ `core/src/test/java/io/github/kirinojuju/GameRulesCheck.java` และตัวเปิดเกมยังอยู่ในโมดูล `lwjgl3`

## อยากแก้อะไร ให้เริ่มที่ไฟล์ไหน

| งาน | เริ่มอ่าน | เปิดเพิ่มเมื่อจำเป็น |
| --- | --- | --- |
| ความเร็วเดิน / Dash / PowerBall | `PlayerCharacter` | `CombatRoom` สำหรับผลดาเมจ และ `RoomObstacles` สำหรับการชนโต๊ะ |
| Notebook ยิงอย่างไร | `NotebookWeaponAttack` | `WeaponAttack`, `WeaponAbility`, `CombatRoom` |
| ท่าสายฟ้า / เลเซอร์ Dome | `DomeBossBehavior` | `DomeSprite` สำหรับภาพ และ `GameRenderer` สำหรับสัญญาณเตือน |
| มอนเดินไล่ / ยิง / วาร์ป | `StandardEnemyBehavior`, `WarperMonsterBehavior` | `EnemyFactory` สำหรับค่าสถานะตอนเกิด |
| มอนไม่ยืนทับกัน | `EnemySpacing` | `CombatRoom` สำหรับจังหวะที่เรียกใช้ |
| โต๊ะวางตรงไหน / เดินอ้อมอย่างไร | `RoomObstacles` | `PlayerCharacter`, `StandardEnemyBehavior`, `CombatRoom` |
| จบห้องแล้วไปไหน / เข้า FINAL_EXAM เมื่อไร | `RunDirector` | `RunPlan` สำหรับเงื่อนไขความคืบหน้า |
| HP หน้าจอ / ข้อความ / ฉาก | `GameRenderer` | คลาส `*Sprite` เมื่อต้องแก้ภาพตัวละคร |

`*Behavior` คือการตัดสินใจและกติกาของศัตรู ส่วน `*Sprite` คือภาพของตัวนั้น เช่น แก้เวลาชาร์จเลเซอร์ใน `DomeBossBehavior` และแก้เฟรมเลเซอร์ใน `DomeSprite` การแยกแบบนี้ช่วยให้แก้ระบบหนึ่งได้โดยตามเฉพาะไฟล์ที่เกี่ยวข้อง

จำนวนไฟล์หลักยังเป็น 48 ไฟล์ ตัวช่วยสั้น ๆ ยังคงอยู่ในไฟล์เจ้าของระบบ โดยตัวที่ต้องเรียกข้าม package ใช้ nested class เช่น `WeaponAttack.WeaponAttackSpec`, `NotebookWeaponAttack.NotebookSprite` และ `RoomObstacles.TableSprites` ส่วนท่าของบอสแต่ละตัวยังแยกไว้เพื่อเพิ่มบอสในอนาคตได้ง่าย คลาสแม่ การสืบทอด และ interface ยังใช้โครงสร้างเดิม

## รายละเอียดการต่อสู้และ OOP

เกมยังใช้ `schoolLife` เป็นจุดเริ่มและ `RunDirector` คุมลำดับห้อง ส่วนการต่อสู้แยกงานดังนี้

`RunPlan` จำว่าชนะ Ae, Far (Fa) และ Dome ครบในรันเดียวกันหรือยัง `RunDirector` จัดคิวห้อง `FINAL_EXAM` หนึ่งครั้ง โดยให้ EX Monster เดิมมาก่อนถ้าตรงกับปลายลูปเลขคู่ จากนั้นแสดงจอดำข้อความ "Good...Let's us prepare the final EXAM" และค่อยเริ่มห้องตู้เซิร์ฟเวอร์ `EnemyFactory` วางบอสสามตัวตรงตู้ใน `ServerRoom.png`; `PlayerCharacter` ล็อกตำแหน่งแกน Y ให้ผู้เล่นเดินและ dash ได้เฉพาะซ้าย–ขวาในห้องนี้

| ส่วน | หน้าที่ |
| --- | --- |
| `EnemyFactory` | เลือกชนิดศัตรูและสร้างค่าสถานะสำหรับมอน บอส และ EX |
| `EnemyBehavior` | สัญญาว่าศัตรูทุกชนิดต้องอัปเดตพฤติกรรมได้ |
| `StandardEnemyBehavior`, `AjarnBehavior` | กติกาเดิน สัมผัสผู้เล่น และโจมตีที่มอนกับบอสใช้ร่วมกัน; มอนทั่วไปใช้ `StandardEnemyBehavior` ตรง ๆ |
| `SatiBossBehavior`, `TalgoBossBehavior`, `DomeBossBehavior` | ท่าเฉพาะของ Ajarn แต่ละตัว |
| `ExBehavior` และคลาส EX ทั้งสาม | ท่าเฉพาะของ Liquid Cat, Allergic Vacuum และ Insecure Sword |
| `EnemyProjectilePatterns` | รูปแบบกระสุนของมอนและบอส |
| `EnemySpacing` | ดันศัตรูที่ซ้อนกันให้ออกจากกัน |
| `CombatRoom` | เก็บศัตรู กระสุน การชน ดาเมจ และเรียก `enemy.behavior.update(...)` |

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

ปรับชนิดได้ใน `BOSS_MINION_KINDS` จำนวนตาม Loop ใน `populateBossRoom()` และตำแหน่งใน `BOSS_MINION_POSITIONS` ซึ่งรองรับสูงสุด 5 จุดที่เว้นระยะจากจุดเข้าห้องของผู้เล่น และตรวจการชนโต๊ะก่อนเริ่มต่อสู้ ลูกน้องเกิดครั้งเดียวและไม่เกิดทดแทนเมื่อถูกกำจัด ต้องชนะทั้งบอสและลูกน้องจึงจบห้อง; HUD แสดง `MINIONS` ที่เหลือ และ `BOSS DOWN` หากบอสตายก่อน

นำระบบ Summon เดิมออกแล้ว ได้แก่ `summoner`, `summonTimer`, `queueSummons()`, `livingMinions()` และ `spawnQueue` จึงไม่มีการนับเวลา/ต่อคิวสร้างลูกน้องระหว่างอัปเดตศัตรู ห้อง `EX` ยังมี EX ตัวเดียว และ `FINAL_EXAM` ยังมีบอสบนตู้สามตัวตามกติกาเดิม ชุดตรวจ `checkBossMinions()` ครอบคลุมทุกบอส หลายลูป จุดเกิด การไม่เกิดเพิ่ม และเงื่อนไขจบห้อง

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
- `CombatRoom.updateShots()` ตรวจเส้นทางกระสุนจากตำแหน่งเดิมไปตำแหน่งใหม่ กระสุนผู้เล่นและมอนทั่วไปชนโต๊ะได้แม้เคลื่อนที่เร็ว Notebook เล่นภาพแตกที่จุดชน และ PowerBall ระเบิดเมื่อชนโต๊ะ
- กระสุนศัตรูทุกแบบสร้างผ่าน `addEnemyShot(source, shot)` ซึ่งตั้ง `Shot.ignoresTables` เมื่อผู้ยิงเป็นบอส (`kind >= 0`) บอสจึงยิงข้ามโต๊ะได้ รวมลูกเดี่ยว กระจาย รอบตัว ลูกใหญ่ คลื่น หอก Sati และกระสุนสะท้อน โต๊ะยังขวางการเดินตามเดิม
- โต๊ะไม่เพิ่มดาเมจหรือ Splash ให้ Notebook ส่วนดาเมจวงระเบิดของ PowerBall ยังใช้กติกาเดิม จึงสามารถโดนศัตรูใกล้โต๊ะได้
- สายฟ้า เลเซอร์ และ Heat Beam เป็นท่าที่แยกจากกระสุนทั่วไป และยังใช้การโจมตีเดิม โต๊ะยังทำลายไม่ได้
- `resolveEnemy()` จัดตำแหน่งศัตรูที่เกิดหรือวาร์ปเข้าโต๊ะ และตรวจหลังแยกศัตรูที่ซ้อนกันกับหลังทำดาเมจ

เปิดห้องมอนสเตอร์โดยตรงด้วย `gradlew.bat :lwjgl3:run --args="--test-obstacles" --no-daemon` ใช้ WASD/ลูกศรเดิน, Shift dash, 1/2 เลือกอาวุธ, คลิกซ้าย/Space โจมตี และ E ใช้ PowerBall เมื่อแพ้กด R จะเริ่มโหมดทดสอบนี้ใหม่ ชุดตรวจ `checkRoomObstacles()` ครอบคลุมการเดินเลียบโต๊ะ Dash เฉียง กระสุนกับที่กำบัง และการเปลี่ยนห้อง; `checkRandomTables()` ตรวจ 100 ห้องที่สุ่ม ตำแหน่งเกิด และการเดินข้ามห้อง; `checkBossShotsAcrossTables()` ตรวจบอส 7 รูปแบบยิงผ่านโต๊ะไปโดนผู้เล่นจริง

## Dome: ภาพและสกิล

- `DomeBossBehavior extends AjarnBehavior` คุมการสลับสายฟ้าและเลเซอร์ผ่าน `enemy.behavior.update(...)` โดยแทนการยิงคลื่นแบบเดิมของ Dome
- `CombatRoom.Enemy.dome` เก็บ `State` แยกต่อบอสแต่ละตัว เช่น `phase`, `remaining`, `elapsed`, จุดเป้าหมาย `aimX/aimY` และแนวลำแสง `originX/originY/endX/endY` จึงใช้ Behavior ตัวเดียวร่วมกันได้
- ลำดับคือ `IDLE → LIGHTNING_CHARGE → LIGHTNING_STRIKE → RECOVERY → IDLE → LASER_CHARGE → LASER_FIRE → RECOVERY` แล้ววนใหม่
- สายฟ้าใช้ 28 SP ล็อกตำแหน่งผู้เล่นตอนเริ่มเตือน 0.85 วินาที ก่อนฟาดในรัศมี `LIGHTNING_RADIUS` (ปัจจุบัน 100) ด้วยดาเมจฐาน 22; เคลื่อนที่ออกจากวงหรือ dash ตรงจังหวะฟาดเพื่อหลบได้ ภาพสายฟ้าอยู่ 0.4 วินาที
- เลเซอร์ใช้ 38 SP เตือนและชาร์จ 1 วินาที ยิง 0.6 วินาทีด้วยดาเมจฐาน 18 โดยเคารพช่วงอมตะหลังโดนโจมตี ในห้องปกติเล็งตำแหน่งผู้เล่นตอนเริ่มชาร์จ ใน `FINAL_EXAM` ยิงตรงลงจากตู้
- แต่ละท่ามีพักฟื้น 0.3 วินาที ก่อนพักระหว่างท่าประมาณ 0.9 วินาที (ลดลงตามความเร็วของลูป แต่ไม่ต่ำกว่า 0.55 วินาที) ดาเมจเพิ่มตามลูป ชุดตรวจจำลองเวลาจริงของลูปแรกและยืนยันว่าเริ่มร่ายได้อย่างน้อย 5 ครั้งใน 12 วินาที รวมช่วงชาร์จ โจมตี พัก และการฟื้น SP แล้ว
- `DomeSprite` อ่านเฟรมจาก `characters/boss/Tnetwork(Dome)/` เลือกภาพตาม `phase/elapsed` รวมภาพเตรียมร่าย ฟาดสายฟ้า ชาร์จเลเซอร์ ยิงด้านข้าง/ลง และพักฟื้น ส่วน `GameRenderer` วาดวงหรือเส้นเตือนก่อนโจมตี
- ภาพ Dome ที่ไม่มี assets จะถูกข้ามตามการตั้งค่าของ monsters-system; `DomeSprite.dispose()` คืนหน่วยความจำภาพเมื่อปิดเกม

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


`MBoss` เก็บค่าพลังและสถานะการแบ่งร่าง ส่วน `MBossBehavior` จัดการแบ่งร่างและยิงกระสุน โดยแต่ละร่างมีสถานะของตัวเอง สำหรับ `SatiBoss` เก็บค่าพลังและสถานะ ส่วน `SatiBossBehavior` จัดการชาร์จ ล็อกเป้าหมาย และปล่อยหอก และ `MSprite`/`SatiSprite` ทำหน้าที่วาดภาพ
