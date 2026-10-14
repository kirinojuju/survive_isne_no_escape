# งานที่รวมเข้าพื้นที่ทำงาน

วันที่ 11 ตุลาคม 2026 บน branch `game-cleanup-and-ex-updates` ฐาน commit `e746aac`

## ที่มา

| Branch | Commit ที่ตรวจและนำโค้ดมารวม | ระบบ |
| --- | --- | --- |
| `monsters-system` | `581089f` | Ken/Ae/M/Sati, Algorithm/Network monsters, Liquid Cat, ภาพบอสและชุดตรวจ |
| `ability-weapon` | `997308f` | Ice Wall: ปุ่ม F/ICE, ทรัพยากร คูลดาวน์ กำแพง การชน และภาพเคลื่อนไหว |

รวมเป็นการแก้ไฟล์ใน workspace โดยเทียบกับฐาน `e746aac` และเก็บงาน Clean Code, โหมด EX test, ฉากรถ และค่าทางเดินรอบโต๊ะที่แก้ไว้ก่อนหน้า ประวัติ branch ยังอยู่ที่ commit เดิมจนกว่าจะ commit งานนี้

สำเนาไฟล์ที่ติดตามก่อนรวมอยู่ใน `build/integration-2026-10-11-before/` พร้อม `head.txt` และ `status.txt` ไฟล์ assets เดิมอยู่ที่เดิม

## จุดที่ปรับให้ทำงานร่วมกัน

- `CombatRoom.Enemy` รองรับคลาสลูกและการเรียก `updateBehavior()` แบบ polymorphism บอสเก็บสถานะรายตัวและใช้ Behavior ของตัวเอง
- `GameRenderer` ใช้เมธอดที่แยกไว้จาก Clean Code และเพิ่มภาพ/คำเตือนของบอสใหม่
- `LiquidCatBoss` ใช้ `LiquidCatBehavior` ร่วมกัน จึงมีวงจรโจมตีชุดเดียว
- M ใช้ ID 2 และ Far ใช้ ID 6; การปลดล็อกและบอสใน FINAL_EXAM อ้าง Far จริง การชนะ M ไม่เพิ่มเงื่อนไขของ Far
- Ken/Sati เรียกลูกน้องผ่าน `spawnQueue` หลังจบการอัปเดตศัตรู โดยมีเพดานการเรียกเพิ่ม 4 ตัว M ใช้คิวเดียวกันสร้างร่างแยก
- Ice Wall แยกอายุและรูปทรงการชนไว้ใน `world/IceWall.java` ใช้ร่วมกับโต๊ะเดิม ตรวจเส้นทางกระสุน/Dash และปล่อยกระสุนผู้เล่นผ่าน
- ปุ่ม BALL กับ ICE ใช้พิกัดเมาส์ชุดเดียวกัน และการกดสกิลไม่ทำให้ยิงอาวุธปกติพร้อมกัน
- ค่า Gradle/JDK เฉพาะเครื่อง และสำเนาโค้ดใต้ `imports/dome/` ไม่จำเป็นต่อระบบที่รวม

## ภาพที่ต้องรับจากเพื่อน

GitHub ทั้งสอง branch ไม่มีภาพใหม่เหล่านี้ และใน assets ของ workspace ยังไม่มีภาพ Ken/M/Algo/Ae จึงใช้ภาพสำรองสำหรับ Ken/M, Algo ใช้ Slime/Blob ตามชนิด และ Ae ใช้รูปทรงสำรองกับเลเซอร์ที่วาดด้วยโค้ด ส่วน Ice Wall ได้รับภาพจริงแล้วตามรายการด้านล่าง

| ตำแหน่งภายใน `assets/` | ไฟล์ที่ตัวโหลดใช้งาน |
| --- | --- |
| `characters/boss/Talgorithm(Ken)/` | `kenBody1.png` ถึง `kenBody17.png`, `kenBomb00.png` ถึง `kenBomb06.png`, `warningBomb00.png` ถึง `warningBomb02.png` |
| `characters/boss/Tnetwork(Ae)/` | `aeBody.png` หรือ `aeTest.jpg`; `aeLaser.png` เป็นภาพเลเซอร์เสริม |
| `characters/boss/Toop(M)/` | `mTest.jpg` |
| `characters/monsters/monster_algo/` | `gigiAlgo00.png`, `gigiAlgo01.png`, `ballgigiAlgo00.png` |

เมื่อวางภาพตามชื่อและตำแหน่ง ให้เปิดเกมใหม่ ตัวโหลดจะเลือกภาพจริงเอง ภาพนักเรียน, Notebook, Dome, Liquid Cat และ Insecure Sword เดิมยังใช้งานอยู่

## การตรวจสอบ

```powershell
.\gradlew.bat :core:verifyGameRules :core:verifyCustomBosses :core:verifyDomeBoss :lwjgl3:classes --no-daemon
```

- `GameRulesCheck`: ลำดับห้อง/ลูป/EX/FINAL_EXAM, M ไม่ปลดล็อกแทน Far, Ice Wall ใช้ SP/คูลดาวน์/ดาเมจ/หมดอายุ/รีเซ็ต ขวางเดินและ Dash กันกระสุนบอสเร็วหลายมุม ปล่อยกระสุนผู้เล่น และทำงานร่วมกับโต๊ะ
- `CustomBossCheck`: Ken วางระเบิดและเรียกลูกน้อง, Ae เตือน/กวาดเลเซอร์และโดน/หลบได้, M แยกร่างครั้งเดียว, Sati ใช้หอก และ Liquid Cat แปลงร่าง/สไลม์
- `DomeBossCheck`: ภาพที่ขาดไม่ทำให้เกมปิด และสกิล Dome ยังใช้กติกาที่กำหนด
- ฉากตรวจกราฟิกใน `build/integration-preview/`: วาดบอสทั้ง 8 แบบ, ระเบิด Ken, เลเซอร์ Ae, ร่างแยก M, กด F และคลิก ICE/BALL ที่ขนาด viewport 1280×720 และ 960×720 รวมถึง FINAL_EXAM บอสบนตู้และผู้เล่นแนวนอน
- ฉากตรวจเดิมใน `build/car-preview/` และ `build/ex-playtest/`: ฉากรถ 6 หน้าจอ/ตำแหน่งคลิก และโหมด EX ทั้งสาม/เปลี่ยนตัว/แพ้/ชนะ/รีเซ็ต


## ภาพที่เพิ่มและงานปรับร่วมกันรอบล่าสุด

| ภาพใน assets | การใช้งาน |
| --- | --- |
| `characters/monsters/Individual Sprites/slime-*.png` | Idle/Move/Attack/Hurt ของ Basic และ Shielded |
| `characters/monsters/Blue-Blob-sheets/Blue-Blob-{Idle,Run}.png` | Binder และ Wave; แบ่งช่อง 64x32 เป็น 6/8 เฟรม |
| `characters/monsters/Green-Blob-sheets/Green-Blob-{Idle,Run}.png` | Warper และ Ranged; แบ่งช่อง 64x32 เป็น 6/8 เฟรม |
| `scene/ปกเกม/Whatzz.png` | ปกหน้าเริ่มเกม รักษาสัดส่วน พร้อม Enter |
| `weapons/icewall/icewall1.png` ถึง `icewall10.png` | ภาพจริง 10 เฟรม วาดเป็นน้ำแข็งตั้งตรงตามแนวชน |
| `weapons/potion/Potion_hp.png`, `Potion_mp.png` | ไอเทมเก็บติดตัว ใช้ Z/X พร้อมจำนวนบน HUD |

- สลับอาวุธด้วย Tab หรือ 1/2; Q ไม่สลับแล้ว
- Ice Wall วางตามทิศหันที่ระยะ 92 คงที่ ไม่เลื่อนไปหามอนหรือดึงกลับจากขอบห้อง หากกึ่งกลางกำแพงออกนอกพื้นที่จะไม่ใช้สกิลและไม่เสีย SP/คูลดาวน์ ตรวจทางเดินมอนตลอดเส้นทางและเลือกจุดแยกออกจากกำแพงที่ไม่ติดโต๊ะ/ขอบห้อง
- Potion เริ่ม HP/SP อย่างละ 2 ขวดต่อ run; ใช้แล้วฟื้น 40 HP/35 SP จำนวนติดตัวข้ามห้อง ยังไม่มีการดรอปหรือหาเพิ่ม
- Liquid Cat หยุดระหว่างแปลงร่างและร่าย Divide, ปล่อยกระสุนนัดเดียวกลางแอนิเมชัน ล็อกจุด Leap และให้ท่าที่ค้างจบก่อนเปลี่ยนร่าง พร้อมช่วงพัก/ภาพกระแทก กระสุนแสดงตามขนาดชน
- เพิ่มคลาสใหม่ 2 ไฟล์: `MonsterSprite` และ `PotionInventory` รวม main Java 52 ไฟล์
- สำเนาก่อนปรับรอบนี้อยู่ใน `build/assets-polish-2026-10-11-before/`

ชุดตรวจ GameRules/CustomBoss/Dome และการคอมไพล์ผ่านแล้ว ฉากตรวจ OpenGL จริงตรวจปก, มอนทั้งหกชนิด, เฟรม Blob, ภาพ Ice Wall ทั้งสิบเฟรมและตำแหน่งสี่ทิศ, Tab/Q, Z/X และ Cat Transform/Divide/Bullet/Leap/Impact พร้อมภาพใน `build/integration-preview/new-*.png` ตรวจฉากรถและโหมด EX เดิมผ่านด้วย โดยหน้าปกใหม่ตรวจแยกจาก baseline หน้าเริ่มเก่า


## แยกหน้าที่ CombatRoom

- เพิ่มเฉพาะ `combat/CombatDamage.java` และ `combat/ProjectileSystem.java`; main Java รวม 54 ไฟล์
- `CombatRoom` เหลือประมาณ 320 บรรทัด เก็บข้อมูลห้อง เริ่มห้อง และคุมลำดับต่อสู้
- ย้ายการเรียกลูกน้องไป `EnemyFactory.queueSummons()` และฝุ่น/สไลม์/Post-it ไป `ExBehavior.ExEnvironment`
- เวลาท่าและตำแหน่งธาตุ Sati อยู่ใน Behavior เจ้าของตัวนั้น; การลด/เพิ่มดาเมจของ Cat/Sword ใช้ `EnemyBehavior.incomingDamageMultiplier()`
- กติกาและจังหวะเดิมตรวจผ่านทั้งโค้ดก่อนและหลังแยก ดูคำอธิบายและจุดแก้ใน `CODE_ARCHITECTURE.md`

## Sword skill effect จาก ability-weapon

วันที่ 11 ตุลาคม 2026 ตรวจ branch ล่าสุด `origin/ability-weapon` ที่ commit `6266900` (`effect sword`) เทียบกับฐานปัจจุบัน `053d635` แล้วนำส่วน LAN Wave มาปรับรวมใน workspace บน `game-cleanup-and-ex-updates`

- ใช้ภาพที่เพิ่มใน `assets/weapons/sword_power/effectsword1.png` ถึง `effectsword12.png` แก้ตำแหน่งโหลดภาพให้ตรงกับไฟล์จริง
- ใช้ `getsuga1.mp3` และ `getsuga2.mp3` ในโฟลเดอร์เดียวกัน สลับเสียงตอนปล่อยคลื่นผ่าน `GameAudio.lanWave()` โดยรอเสียงเดิมจบก่อน เสียงใช้ `Music.isPlaying()` ตรวจสถานะโดยไม่เปลี่ยนคูลดาวน์การยิง หากไฟล์ขาดใช้เสียง LAN ปกติ
- สกิลคลื่นฟันใช้ G / ปุ่ม LAN เมื่อถือ LAN: ดาเมจ 38 + physicalBonus, ไม่มีค่า SP, คูลดาวน์ 0.1 วินาทีตาม branch เพื่อน ยิงตรง ชนเป้าหนึ่งตัว โต๊ะกันได้ และผ่าน Ice Wall ของผู้เล่นได้
- เพิ่ม `render/LanWaveSprite.java` เพียงไฟล์เดียว รวม main Java ฝั่ง core เป็น 55 ไฟล์ ใช้ `ProjectileSystem` และ `CombatDamage` เดิม
- ปรับปุ่ม R ของสกิลจาก branch เพื่อนเป็น G เพื่อให้ EX Test ยัง retry ด้วย R และคง Tab/1/2 โดย Q ไม่สลับอาวุธ
- เก็บ Liquid Cat สองเฟส/ร่างแยกที่แก้ล่าสุด และใช้กติกา Ice Wall ตำแหน่งคงที่กับภาพ 10 เฟรมที่มีใน workspace การแก้ Ice Wall และไฟล์ตั้งค่าเฉพาะเครื่องที่ติดมากับ branch ไม่ได้อยู่ในส่วนสกิลที่นำมารวม
- สำเนาก่อนรวมอยู่ที่ `build/sword-integration-2026-10-11-before/` พร้อม commit ต้นทางและรายการไฟล์ที่แก้ค้าง

ตรวจผ่าน `verifyGameRules`, `verifyCustomBosses`, `verifyDomeBoss`, `lwjgl3:classes` และ OpenGL จริงสำหรับ Sword Wave, Liquid Cat สองเฟส และ EX Test ทั้งสาม ภาพตรวจอยู่ใน `build/sword-playtest/` ตรวจ G/LAN, Tab/Q/R, viewport 1280×720 และ 960×720, animation 12 เฟรม และยิงโดนบอสบนตู้ใน FINAL_EXAM แล้ว การรวมรอบนี้ยังเป็นไฟล์แก้ไขใน workspace

## อัปเดต monsters-system และ Reroll — 12 ตุลาคม 2026

ตรวจทุก remote branch แล้วพบงานใหม่ใน `origin/monsters-system` จาก `581089f` ถึง `ee006d7` (รวม `034b1f6`) และ branch ใหม่ `origin/Reroll` ที่ `a9b79ff` ส่วน `origin/ability-weapon` ยังอยู่ที่ `6266900`

นำส่วนที่เปลี่ยนพฤติกรรมมารวมกับไฟล์แก้ค้างบน `game-cleanup-and-ex-updates` ฐาน `053d635` โดยเก็บสำเนาก่อนรวมไว้ใน `build/branch-update-2026-10-12/before/` พร้อม `head.txt` และ `status.txt` การรวมนี้ยังเป็นการแก้ไฟล์ใน workspace ไม่ได้สร้าง commit หรือ push

- Hitbox บอสใช้ขอบสี่เหลี่ยมของพิกเซลที่มองเห็นในแต่ละเฟรม ตัดขอบโปร่งใสและกลับด้านตามภาพ; `BossBody` เก็บขนาดร่วม ส่วน `OptionalAssetTexture` อ่าน alpha ครั้งเดียวตอนโหลดภาพ
- ส่งกติกา Hitbox ไปยัง `CombatDamage` และ `ProjectileSystem` ที่แยกไว้แล้ว ครอบคลุม melee, Notebook/beam/splash, PowerBall, Ice Wall และ LAN Wave; การเล็งใช้จุดกึ่งกลางขอบภาพ
- Dome ยิงเลเซอร์จากพิกัดมือในเฟรมด้านข้าง/ยิงลง Ken วางระเบิด 7–9 ลูกตามอัปเดตต้นทาง
- เพิ่ม FarBoss/FarBossBehavior/FarSprite และย้ายท่าวงปลอดภัยจาก Talgo มาเป็น Far; ค่าร่าย 1.8 วินาที วงปลอดภัยรัศมี 76 บอส Far ใน FINAL_EXAM ยังคงโจมตีจากตู้โดยไม่ร่ายวงบนพื้น
- RunPlan เลือกจากบอสจริง 6 ตัว: Ken/Sati, Ae, Far/M และ Dome โดยคง ID เดิม การชนะ M ยังไม่ถูกนับเป็น Far
- เพิ่ม ProgramMonster/SlimeMonster และรองรับภาพ Network/Programming/slime ชุดใหม่ เมื่อไม่มีไฟล์ยังใช้ Slime และ Blue/Green Blob เดิม พร้อมภาพสำรองของบอสที่ยังไม่มีโมเดล
- กด R ในหน้า UPGRADE เพื่อสุ่มใหม่ได้หนึ่งครั้งต่อบอส ให้ตัวเลือกที่ยังใช้ได้และไม่ซ้ำชุดก่อนเมื่อมีพอ เมื่อไม่มีตัวเลือกใหม่ไม่หักสิทธิ์; R ใน EX test ยังเป็น retry และ G/Tab ยังทำงานเดิม
- เก็บ Liquid Cat สองเฟส แยกร่าง และ Orbital ลงจากฟ้า; ขนาด Hitbox ระหว่าง Transform ลดตามภาพที่กำลังหดตัว

ปัจจุบันมี main Java ฝั่ง core 60 ไฟล์ เพิ่มสุทธิ 5 ไฟล์จากก่อนรวมรอบนี้ ตรวจผ่าน `verifyGameRules`, `verifyCustomBosses`, `verifyDomeBoss` และการคอมไพล์ desktop ทดสอบ OpenGL จริงผ่าน Reroll ด้วยปุ่ม R, ขอบพิกเซล Ken/การเล็ง Sword, คำเตือน Far, จุดยิง Dome, Liquid Cat สองเฟส/ร่างแยก, Sword Wave/FINAL_EXAM และ EX test ทั้งสาม ภาพรอบใหม่อยู่ใน `build/branch-update-2026-10-12/`

### เปลี่ยนปุ่ม Sword — 12 ตุลาคม 2026

ตามคำขอผู้ใช้ เปลี่ยนสกิล Sword/LAN Wave จาก G เป็น **Q** พร้อมข้อความบนปุ่ม LAN, HUD และคู่มือ ปุ่ม Tab/1/2 ใช้สลับอาวุธ ส่วน R ใช้ Reroll หรือ retry ตามหน้าจอ ข้อความ G ในบันทึกด้านบนเป็นปุ่มที่ใช้ตอนรวมงานรอบก่อน

### เสียง Sword ตอบสนองตามการกด — 12 ตุลาคม 2026

เปลี่ยนไฟล์ MP3 ทั้งสองจาก `Music` เป็น `Sound` เพื่อโหลดเสียงล่วงหน้า ทุกครั้งที่ใช้สกิลสำเร็จจะหยุดเสียง Sword ก่อนหน้าและเริ่มเสียงถัดไปทันที จึงกดรัวตามคูลดาวน์ 0.1 วินาทีได้โดยไม่รอคลิปจบ การกดที่ติดคูลดาวน์หรือถือ Notebook ไม่เล่น/ตัดเสียง และยังใช้เสียง LAN ปกติเมื่อไม่มีไฟล์ MP3

### เปลี่ยน Sword เป็น R — 12 ตุลาคม 2026

ผู้ใช้ยืนยันเปลี่ยนสกิล Sword จาก Q เป็น **R** พร้อมป้ายบนปุ่ม LAN และ HUD ย้ายการ retry EX Test ระหว่างต่อสู้ไป **F4** เพื่อไม่ให้ใช้สกิลแล้วรีเซ็ตห้อง ปุ่ม R ในหน้า UPGRADE ยังสุ่มอัปเกรด และใน GAME_OVER ยังเริ่มใหม่ ส่วนหน้าปกเริ่มด้วย Enter

## Refactor ตาม OOP/SOLID และภาพ Kiki — 14 ตุลาคม 2026

ตรวจ GitHub แล้ว remote branches ยังอยู่ที่ commit เดิม งานรอบนี้แก้ใน `game-cleanup-and-ex-updates` ฐาน `053d635` สำเนาก่อนแก้อยู่ที่ `build/finalize-2026-10-14/before/` พร้อมสถานะ Git และ hash ฐาน โดยยังไม่ commit/push

### สิ่งที่เปลี่ยน

- เชื่อม `Network_Kiki1..15.png` และ `Program_Kiki1..8.png` กับตัวโหลดจริง แก้การอ้างชื่อ gigi เดิม และจัดภาพมอนสเตอร์ให้มีจุดกึ่งกลางตรงกับตำแหน่งชน
- รวม Algo/Network/Program/Slime ที่เดิมมีแต่ constructor เป็น `Monster` ซึ่งเก็บ Appearance เป็น private และเปิดผ่าน getter พฤติกรรมหกชนิดยังเลือกจาก combat role เดิม
- รวมโมเดลกับ nested `Behavior` ของ Ken/Ae/M/Sati/Far ในไฟล์บอสแต่ละตัว คงการตัดสินใจโจมตีผ่าน `EnemyBehavior` และการสืบทอดจาก AI แม่
- ซ่อน `Enemy.behavior`, สถานะเลเซอร์ Ae, สถานะแยกร่าง M และลิงก์ต้นฉบับของ Cat จากการเขียนภายนอก ย้ายการอัปเดตระเบิดเป็น instance method `KenBoss.advanceBombs()`
- ยกเลิกออบเจ็กต์ AI ส่วนกลางแบบ static 13 ตัว แต่ละศัตรูสร้าง strategy ของตัวเอง ส่วนค่าคงที่และ reference ของ dependency ที่ไม่เปลี่ยนยังใช้ static final/final ตามหน้าที่
- เพิ่ม abstract `EnemySprite implements Disposable` เพื่อใช้ตัวโหลดภาพ ขอบภาพ และการคืน texture ร่วมกัน คลาสภาพลูก override `draw()`/`supports()` และ `GameRenderer` เรียกแบบ polymorphism
- ใช้ `AnimatedEnemySprite` แทน MSprite/FarSprite และรวมภาพ/กระสุน Algorithm ใน MonsterSprite จึงไม่เพิ่มไฟล์เปล่าตามชนิดภาพ
- อัปเดต README/GDD ให้ตรงกับโมเดล Sati ที่มีอยู่ และเพิ่มตัวอย่าง OOP/SOLID พร้อมข้อจำกัดใน CODE_ARCHITECTURE.md โดยอ้างหัวข้อจากเอกสารเรียนที่ผู้ใช้ส่งมา

### ขนาดโค้ด

นับเฉพาะ `core/src/main/java` ก่อนแก้รอบนี้เทียบกับหลังแก้ ไม่รวม test, launcher, เอกสาร และ artifacts ใน build:

| รายการ | ก่อน | หลัง |
| --- | ---: | ---: |
| ไฟล์ Java | 60 | 51 |
| บรรทัดรวม | 6,621 | 6,360 |
| บรรทัดที่มีโค้ด ไม่รวมบรรทัดว่าง/comment | 5,843 | 5,633 |
| keyword static ในโค้ด | 323 | 290 |
| keyword final ในโค้ด | 421 | 389 |

รายละเอียดวิธีนับและตัวเลขอยู่ใน `build/finalize-2026-10-14/metrics.json` จำนวนไฟล์ไม่เท่ากับจำนวนคลาส เพราะ Behavior บางตัวถูกจัดกลุ่มเป็น nested class

### หลักฐานตรวจและข้อจำกัด

ตรวจผ่าน `verifyGameRules`, `verifyCustomBosses`, `verifyDomeBoss`, desktop compilation และ JAR build ตรวจ OpenGL จริงสำหรับภาพ Kiki ครบ 23 เฟรม, Sati/Ae/M, ขอบภาพ Ken, Dome laser, Cat สองเฟส/ร่างแยก, R/LAN, Tab/F4, EX ทั้งสาม และ FINAL_EXAM ตรวจ OpenAL จริงด้วยเสียง Sword กดรัว 11 ครั้งและกรณีไฟล์เสียงขาด

ตรวจ flow ผ่านตัวเกมจริง: หน้าปก → Building 30 → ห้องมอน/บอสครบสี่ด่าน → อัปเกรด → EX แรกหลัง Loop 1 → Loop 2 โดยใช้การจำลองชนะห้อง จึงเป็นหลักฐานเรื่องลำดับเกมและการโหลดทรัพยากร ไม่ใช่ผลวัดสมดุลความยากจากการเล่นของคน

สร้าง `build/finalize-2026-10-14/release/SchoolLife.jar` ที่รวม assets ปัจจุบัน และตรวจจากโฟลเดอร์ว่างที่ไม่มี assets ภายนอกแล้ว ภาพตรวจอยู่ใน `build/finalize-2026-10-14/packaged-preview/` ไฟล์เกมสำหรับส่งเล่นมี assets ส่วนตัว จึงเก็บใน build ที่ถูก ignore ส่วน Far/Allergic Vacuum ยังใช้ภาพสำรอง

ชุดส่งเล่นอยู่ที่ `build/finalize-2026-10-14/SchoolLife-playtest-2026-10-14.zip` มี JAR, START_GAME.bat และ README_TH.txt ตรวจ CRC แตก ZIP ใหม่ เทียบ hash ของ JAR แล้วรันทั้ง flow และเสียง Sword จากไฟล์ที่แตกออกมาผ่านแล้ว ต้องมี Java 17 หรือใหม่กว่า ชุดนี้เป็นตัวเกมสำหรับเล่น ไม่ใช่ ZIP ซอร์สโปรเจกต์ทั้งหมด


## 2026-10-14 — Direct inheritance for the OOP presentation (local workspace)

- Reused branch `game-cleanup-and-ex-updates`, HEAD `053d635`; these changes are not committed or pushed.
- Main Java sources: 51 files / 6,393 lines at resume -> 48 files / 6,318 lines. `CombatRoom` remains 250 lines after its earlier reduction from 378; `GameRenderer` remains 1,018 lines.
- `Enemy`, `Boss` and `ExBoss` are abstract bases. `Monster` and `WarperMonster` use inheritance; each normal/EX boss overrides its own behavior directly. Removed the separate EnemyBehavior / StandardEnemyBehavior / AjarnBehavior layers and renamed the warper/EX base files.
- Enemy HP/max HP are private. Getter, bounded setter and damage methods replace external field access. Armor overflow, poison, transformation and clone deaths retain their regression checks.
- Player weapons support constructor injection through `WeaponAttack`; the game entry point selects LAN/Notebook implementations. A regression check verifies dispatch to injected physical and magical implementations.
- No Map / HashMap / LinkedHashMap / WeakHashMap remain in main source. Boss names use switch and arrays; alpha bounds belong to the texture object.
- Far is removed. Five normal bosses remain: Ken, Ae, M, Sati, Dome. Final Exam unlocks after Ae/Dome victories, preserves introductory EX-before-exam ordering, and spawns AeBoss/DomeBoss on the outer cabinets. Anchored Ae retains spread attacks without the sweeping laser.
- Passed tracked checks: verifyGameRules, verifyCustomBosses, verifyDomeBoss; desktop classes compile.
- Passed actual OpenGL checks: BranchUpdateCheck (monster/new boss art plus normal flow), LiquidCatRenderCheck (two phases, real clones, orbital/reward), SwordWaveCheck (12 frames, R/Tab/F4, two viewport sizes and hits on both outer cabinets). The flow check simulates victories, so it does not establish human balance or play-session duration.
- Built the playable JAR and verified it contains new WarperMonster/ExBoss classes, no obsolete AI/Behavior classes, and no test Check classes.
- Packaged `build/oop-resume-2026-10-14/SchoolLife-OOP-demo-2026-10-14.zip` (7 entries), verified ZIP CRC and extracted-JAR SHA-256 equality, and ran BranchUpdateCheck from the extracted JAR without an external assets folder. Passed.
- Added OOP_PRESENTATION_TH.md with all eight lecture mappings, verified local source/PDF links, class relationships, SOLID limits and speaking notes. Replaced stale CODE_ARCHITECTURE.md with a current 48-file catalog; refreshed README/GDD/team references.

## Renderer / visible hitbox indicators — 14 October 2026

- Split `GameRenderer` from 1,018 to 174 lines. Added only `SceneRenderer` (268 lines), `CombatRenderer` (448 lines), and `GameHud` (267 lines). Current core main source totals: 51 files / 6,457 lines. This separates ownership; it does not claim the total drawing code disappeared.
- Target outline and tether endpoints now use the same visible body bounds as incoming damage. Enemy HP/status indicators follow the visible body top, including M and Liquid Cat. Indicators are drawn after the current sprite selects its frame bounds.
- Replaced dark filled telegraph centers with open ring edges for Cat/Sword/Vacuum warnings, Dome lightning, warps and blast/splash effects. Cat's warning radius reads `LiquidCatBoss.ORBITAL_RADIUS`, matching its damage check. Removed the decorative phase-two ellipse that floated above Cat's sprite; actual ooze objects remain.
- Integrated `scene/sceneroom_new_no_table.png` in MOBS/BOSS/EX. Falls back to the previous empty room when absent. Random tables still belong to MOBS/BOSS; FINAL_EXAM uses its server scene.
- Passed `verifyGameRules`, `verifyCustomBosses`, `verifyDomeBoss`, desktop classes/JAR build, and `git diff --check`.
- A focused graphical regression verified visible M hits, misses below its transparent image padding, target outline placement, Cat warning border/interior, safe/damaging orbital positions, and the actual new scene in all three room types.
- Verified the current standalone release JAR from its release directory, without external assets: the focused rendering check plus BranchUpdateCheck, LiquidCatRenderCheck and SwordWaveCheck all passed. Covers scripted cover → entrance → four stages → upgrades → EX → queued FINAL_EXAM → loop two, Cat transformation/copies/orbital/reward, R/Tab/F4 and Sword hits on both Final Exam cabinets. These are scripted functional checks, not a difficulty assessment.
- Refreshed the existing seven-file demo ZIP and presentation/architecture guides; copied current classes to `core/bin/main` for IDE launch compatibility. New screenshots: `build/render-hitbox-2026-10-14/` and its `jar-*` folders. Changes remain local on `game-cleanup-and-ex-updates`.

## Renderer package folders — 14 October 2026

- Moved 15 existing Java files into `render/scene`, `render/combat`, `render/hud`, `render/sprite`, and `sprite/boss`, `sprite/ex`, `sprite/effect`. `GameRenderer` and the shared `OptionalAssetTexture` remain directly under `render`.
- Updated package declarations, imports, required public constructors/methods, the Dome test import, documentation links and source anchors. Kept private sprite state and the existing EnemySprite inheritance.
- Core main source count remains 51 files. GameRenderer is now 178 lines due to explicit subpackage imports; other views: SceneRenderer 270, CombatRenderer 460, GameHud 268.
- All three rule checks and desktop compilation passed. Graphical checks cover hitbox indicators/new scenes, complete scripted run progression, Liquid Cat phases/copies and Sword/Final Exam controls.
- Updated the existing demo JAR/ZIP and IDE output folders to use the new package paths; removed obsolete generated classes from the previous flat IDE render folder.
- Remaining architectural limits are documented: renderer/player still have broad responsibilities, and some geometry/status fields remain public. The project does not claim complete SOLID compliance or custom implementations of every design pattern from the lecture.
