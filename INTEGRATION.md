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
| `icewall/icewall/icewall1.png` ถึง `icewall17.png` | ภาพ Ice Wall ของ ability-weapon จำนวน 17 เฟรม วาดเป็นแผ่นเดียวกว้าง 180 สูง 128 และหมุนตามแนวกำแพง |
| `weapons/potion/Potion_hp.png`, `Potion_mp.png` | ไอเทมเก็บติดตัว ใช้ Z/X พร้อมจำนวนบน HUD |

- สลับอาวุธด้วย Tab หรือ 1/2; Q ไม่สลับแล้ว
- Ice Wall ใช้ภาพ 17 เฟรมและรูปทรงชนยาว 180 หนา 20 วางไปตามทิศหันที่ระยะ 92 และจำกัดตำแหน่งกึ่งกลางให้อยู่ในพื้นที่เดิน ผู้เล่นชนแล้วเลื่อนขนานไปตามกำแพงได้ มอนสเตอร์ถูกแยกออกด้านตรงข้ามผู้เล่นโดยคงตำแหน่งตามแนวกำแพง และตรวจโต๊ะก่อนย้าย
- Potion เริ่ม HP/SP อย่างละ 2 ขวดต่อ run; ใช้แล้วฟื้น 40 HP/35 SP จำนวนติดตัวข้ามห้อง ยังไม่มีการดรอปหรือหาเพิ่ม
- Liquid Cat หยุดระหว่างแปลงร่างและร่าย Divide, ปล่อยกระสุนนัดเดียวกลางแอนิเมชัน ล็อกจุด Leap และให้ท่าที่ค้างจบก่อนเปลี่ยนร่าง พร้อมช่วงพัก/ภาพกระแทก กระสุนแสดงตามขนาดชน
- เพิ่มคลาสใหม่ 2 ไฟล์: `MonsterSprite` และ `PotionInventory` รวม main Java 52 ไฟล์
- สำเนาก่อนปรับรอบนี้อยู่ใน `build/assets-polish-2026-10-11-before/`

ชุดตรวจ GameRules/CustomBoss/Dome และการคอมไพล์ผ่านแล้ว ฉากตรวจ OpenGL จริงตรวจปก, มอนทั้งหกชนิด, เฟรม Blob, ภาพ Ice Wall 17 เฟรมและตำแหน่งสี่ทิศ, Tab/Q, Z/X และ Cat Transform/Divide/Bullet/Leap/Impact พร้อมภาพใน `build/integration-preview/new-*.png` ตรวจฉากรถและโหมด EX เดิมผ่านด้วย โดยหน้าปกใหม่ตรวจแยกจาก baseline หน้าเริ่มเก่า


## แยกหน้าที่ CombatRoom

- เพิ่มเฉพาะ `combat/CombatDamage.java` และ `combat/ProjectileSystem.java`; main Java รวม 54 ไฟล์
- `CombatRoom` เหลือประมาณ 320 บรรทัด เก็บข้อมูลห้อง เริ่มห้อง และคุมลำดับต่อสู้
- ย้ายการเรียกลูกน้องไป `EnemyFactory.queueSummons()` และฝุ่น/สไลม์/Post-it ไป `ExBehavior.ExEnvironment`
- เวลาท่าและตำแหน่งธาตุ Sati อยู่ใน Behavior เจ้าของตัวนั้น; การลด/เพิ่มดาเมจของ Cat/Sword ใช้ `EnemyBehavior.incomingDamageMultiplier()`
- กติกาและจังหวะเดิมตรวจผ่านทั้งโค้ดก่อนและหลังแยก ดูคำอธิบายและจุดแก้ใน `CODE_ARCHITECTURE.md`
