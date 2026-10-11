# การรวม cleanup กับ monsters-system

นำโค้ดจาก `origin/game-cleanup-and-ex-updates` commit `e746aac` มารวมกับ
`monsters-system` commit `9a67651` และงาน Dome ที่แก้ในเครื่องก่อนรวม
โดยคงไฟล์ assets ในเครื่องไว้ตามเดิม

## ระบบจาก cleanup

- แยก package เป็น `audio`, `combat`, `enemy`, `flow`, `player`, `render`, `weapon`, `world`
- ฉากรถ ลำดับ EX และห้อง Final Exam ที่มี Ae, Far และ Dome บนตู้เซิร์ฟเวอร์
- โต๊ะ การชน การเดินอ้อม และกระสุนบอสที่ผ่านโต๊ะได้
- ระบบ WeaponAttack/WeaponAbility และภาพ Notebook
- ท่าสายฟ้า/เลเซอร์ Dome และระบบ EX ของ Vacuum/Insecure Sword

## ระบบเดิมที่เชื่อมไว้

| ระบบเดิม | ตำแหน่งปัจจุบัน | การเชื่อม |
| --- | --- | --- |
| Ken | `enemy/boss/KenBoss.java`, `enemy/boss/KenBossBehavior.java`, `render/KenSprite.java` | KenBoss เก็บสถานะ; KenBossBehavior สุ่มระเบิด 7–9 ลูก นับเวลาเตือน และตรวจดาเมจ โดยคงสกิลเดิม |
| Ae | `enemy/boss/AeBoss.java`, `enemy/boss/AeBossBehavior.java`, `render/AeSprite.java` | ห้องบอสปกติใช้เลเซอร์แนวตั้งกวาดซ้ายไปขวา พร้อมท่าวาร์ปและกระสุนสามนัดเดิม; Final Exam ใช้ Enemy เดิม |
| Far | `enemy/boss/FarBoss.java`, `enemy/boss/FarBossBehavior.java`, `render/FarSprite.java` | ชาร์จ 1.8 วินาทีแล้วโจมตีทั้งห้อง ดาเมจ 27 ก่อนคูณค่ารอบ; หลบด้วยวงปลอดภัยรัศมี 76 หรือ dash ตอนกระทบ ใช้สกิลที่ย้ายจาก Talgo เดิม |
| M | `enemy/boss/MBoss.java`, `enemy/boss/MBossBehavior.java`, `render/MSprite.java` | MBoss เก็บค่าพลังและสถานะ; MBossBehavior แบ่งร่างหนึ่งครั้งเมื่อ HP ถึงครึ่ง และยิงกระสุนเดิม |
| Sati | `enemy/boss/SatiBoss.java`, `enemy/boss/SatiBossBehavior.java` | SatiBoss เก็บค่าพลังและสถานะ; SatiBossBehavior จัดการแปลงธาตุและหอกเดิม รัศมี 36 ความเร็ว 500 |
| Liquid Cat | `enemy/ex/LiquidCatBoss.java` | แปลงร่าง เดิน กระโดด ยิง และลดดาเมจร่างเล็ก; ใช้การชนสไลม์จาก cleanup |
| Algo/Network | `enemy/AlgoMonster.java`, `enemy/NetworkMonster.java` | ใช้ EnemyFactory ใหม่ และเก็บภาพ/กระสุน Algo ใน `render/AlgoMonSprite.java` |
| Monster artwork | `enemy/ProgramMonster.java`, `enemy/SlimeMonster.java`, `render/MonsterSprite.java` | BASIC = Algorithm; SHIELDED = Network; WARPER = Programming; RANGED/WAVE/BINDER ใช้ SlimeMonster และคงท่ายิง/คลื่น/จับเดิมไว้ รอเพิ่ม assets |
| Enemy | `combat/CombatRoom.java` → `CombatRoom.Enemy` | คลาสแม่รองรับการสืบทอดและ `updateBehavior()` พร้อมคิวเกิดศัตรูหลังจบการอัปเดต |

Ken ใช้ `KEN_ID = 4` และ M ใช้ `M_ID = 2` เป็นชื่อเฉพาะของแต่ละตัว.
Far ในห้องปกติใช้ `FAR_ID = 6` แยกจาก M โดยแทนชื่อ placeholder Firewall เดิม. Final Exam คงหมายเลขเดิม `FINAL_EXAM_FAR_ID = 2` ทั้งเงื่อนไขปลดล็อกและพฤติกรรมยิงจากตู้ตาม cleanup เพื่อรักษางานส่วนนี้ไว้เหมือนก่อนแก้ชื่อ.
วิชาของบอส: Sati = C++, M = OOP, Ken = Algorithm และ Ae/Far/Dome = Network. ID เป็นชื่อตัวบอส ไม่ใช่ชื่อวิชาหรือพฤติกรรมการต่อสู้.

บอส Ken/Ae/Far/M/Sati/Dome และ EX Liquid Cat/Insecure Sword รับดาเมจผ่านกรอบครอบพิกเซลที่มองเห็นของเฟรมปัจจุบัน แยกจาก radius สำหรับเดิน/สัมผัส. OptionalAssetTexture อ่าน alpha ตอนโหลดและเก็บขอบไว้ ไม่รวมพื้นที่โปร่งใสรอบ PNG หรือพิกเซลจางที่ alpha ต่ำกว่า 32. Sprite ส่งขอบของเฟรมที่วาดให้บอส และกลับขอบซ้ายขวาเมื่อภาพถูก flip. ขนาดภาพใช้ค่าร่วมใน `enemy/boss/BossBody.java` เช่น `KEN_DRAW_SIZE`, `M_DRAW_SIZE`; ขอบที่ตีโดนปรับตามสเกลนี้. Ae/Far ใช้สัดส่วนภาพจริงกำหนดความกว้าง ส่วน Cat ปรับกรอบตามร่างและเฟรมแปลงร่าง. การตีประชิด กระสุน ระเบิดพื้นที่ และ Notebook beam ใช้กรอบตัวเดียวกัน และเล็งกลางกรอบที่มองเห็น. หากไม่มีข้อมูลภาพจะใช้กรอบวงกลมเดิมเพื่อไม่สร้างฮิตบ็อกซ์ใหญ่ที่มองไม่เห็น. Final Exam ที่ anchored คงกรอบชนเดิม. ระเบิดของ Ken สุ่ม 7–9 ลูกต่อครั้ง.

กีกี้สไลม์เตรียมโหลดภาพเดี่ยว `assets/gigiSlime.png`. ถ้ายังไม่มีไฟล์ ให้แสดงตัวแทนเดิมจนเพิ่มรูปแล้วเริ่มเกมใหม่. ภาพ Dome, Liquid Cat, Insecure Sword และ Algorithm ย้ายขอบบนมาอยู่ที่ y + radius เพื่อให้ต่ำกว่าหลอดเลือด โดยคงขนาดภาพที่ผู้ใช้ตั้งไว้.

รายการสุ่มมอนอยู่ใน EnemyFactory.mobKind() และ BOSS_MINION_KINDS; การเรียกมอนของบอสอยู่ใน CombatRoom.queueSummons(). คืนมอนทั้ง 6 ชนิดตามเดิมแล้ว รวมตัวยิง ตัวคลื่น และตัวจับ. ตัวช่วยเริ่มห้องบอสเป็น 2/3/5 ตัวตามรอบ.

ลบ Voltage ID 5 และ Thesis ID 7 แล้ว. แต่ละรอบเลือก Sati หรือ Ken, Ae, M หรือ Far และ Dome ก่อนสลับลำดับทั้งสี่ตัว. รายชื่อ BOSSES เป็น Map ที่จับคู่ ID กับชื่อ เพื่อเก็บ FAR_ID = 6 ได้โดยไม่ต้องมีช่องหมายเลข 5. ชื่อในเกมอ่านผ่าน BOSSES.get(id).

Far ห้องปกติใช้ FAR_ID และ FarBossBehavior เท่านั้น. Final Exam ใช้ FINAL_EXAM_FAR_ID และพฤติกรรมยิงจากตู้เดิม. ไม่มีคลาส TalgoBossBehavior แล้ว. FarSprite โหลด `characters/boss/Tnetwork(Far)/farBody.png` ; ถ้าไม่พบให้ GameRenderer วาดตัวแทนเดิม.

ท่าวาร์ปเฉพาะ Ae และค่าของท่านั้นอยู่ใน `AeBossBehavior.tryWarp()`; `AjarnBehavior` เก็บเฉพาะวาร์ปพื้นฐานและการกลับจุดเดิมที่ใช้ร่วมกัน. Ae แบบ Enemy ทั่วไปใช้ AeBossBehavior ได้โดยไม่เพิ่มเลเซอร์ เลเซอร์ทำงานเฉพาะ AeBoss ในห้องบอสปกติ จึงคงการต่อสู้ Final Exam เดิม.

Ae ใช้ `characters/boss/Tnetwork(Ae)/aeBody1..7.png` ตอนยืน และ `aeActack1..8.png` ตอนยิง/ร่าย โดยชื่อ Actack ตรงกับไฟล์จริง. ถ้าไม่มีเฟรมจึงค่อยใช้ aeBody.png ถ้ามีไฟล์. เลเซอร์ไม่ต้องมี asset: วาดด้วย ShapeRenderer และสามารถใส่ aeLaser.png แนวตั้งเพื่อแทนภาพเลเซอร์ได้. ค่าปรับสกิลอยู่ใน AeBossBehavior: เตือน 0.9 วินาที, กวาด 5 วินาที, พัก 5 วินาที, ความกว้าง 32 และดาเมจ 16 ก่อนคูณค่ารอบ.

M ใช้ `characters/boss/Toop(M)/mBody1..7.png` และ `mActack1..8.png` โดยสลับเฟรมโจมตีเมื่อยิงกระสุน. Sati ใช้ satiBody1..7.png และ satiActack1..10.png ใต้ `characters/boss/Tprogram(Sati)/skill/cpp_element/` โดยเฟรมโจมตีสัมพันธ์กับ satiCharge และคงเอฟเฟกต์ธาตุ/หอกเดิม. รูปตัวละครวางใต้หลอดเลือด. ตัวโหลดเฟรมมี null guard และ dispose ทุก Texture ที่โหลด.
การเรียกมอนของ Ken/Sati จำกัดมอนที่มีชีวิต 4 ตัว; บอสอื่นใช้มอนเริ่มห้องตาม cleanup.

ภาพและเอฟเฟกต์ Dome ที่ไม่มีไฟล์จะถูกข้าม ไม่มีภาพสำรองของ Dome.
ชื่อ assets อยู่ใน `imports/dome/README.md`; เปิดเกมใหม่หลังเพิ่มไฟล์.

สำเนาโค้ดก่อนรวมอยู่ใน `build/integration-backup-before-cleanup/core-src` และ
`build/integration-backup-before-cleanup/lwjgl3-src` (ไม่รวมใน Git).

## ตรวจสอบ

```powershell
.\gradlew.bat :core:verifyCustomBosses :core:verifyDomeBoss :core:verifyGameRules :lwjgl3:classes
```

`verifyCustomBosses` ตรวจว่า Ken/M/Sati/Liquid Cat ยังเกิดและใช้ท่าเดิมผ่านโครงสร้างใหม่.
`verifyDomeBoss` ตรวจท่า Dome และการไม่มีภาพ.
`verifyGameRules` ตรวจระบบจาก cleanup รวมถึงโต๊ะ อาวุธ การเปลี่ยนห้อง และ Final Exam.
