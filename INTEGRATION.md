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
| Ken | `enemy/boss/KenBoss.java`, `enemy/boss/KenBossBehavior.java`, `render/KenSprite.java` | KenBoss เก็บสถานะ; KenBossBehavior สุ่มระเบิด 3–5 ลูก นับเวลาเตือน และตรวจดาเมจ โดยคงสกิลเดิม |
| Ae | `enemy/boss/AeBoss.java`, `enemy/boss/AeBossBehavior.java`, `render/AeSprite.java` | ห้องบอสปกติใช้เลเซอร์แนวตั้งกวาดซ้ายไปขวา พร้อมท่าวาร์ปและกระสุนสามนัดเดิม; Final Exam ใช้ Enemy เดิม |
| M | `enemy/boss/MBoss.java`, `enemy/boss/MBossBehavior.java`, `render/MSprite.java` | MBoss เก็บค่าพลังและสถานะ; MBossBehavior แบ่งร่างหนึ่งครั้งเมื่อ HP ถึงครึ่ง และยิงกระสุนเดิม |
| Sati | `enemy/boss/SatiBoss.java`, `enemy/boss/SatiBossBehavior.java` | SatiBoss เก็บค่าพลังและสถานะ; SatiBossBehavior จัดการแปลงธาตุและหอกเดิม รัศมี 36 ความเร็ว 500 |
| Liquid Cat | `enemy/ex/LiquidCatBoss.java` | แปลงร่าง เดิน กระโดด ยิง และลดดาเมจร่างเล็ก; ใช้การชนสไลม์จาก cleanup |
| Algo/Network | `enemy/AlgoMonster.java`, `enemy/NetworkMonster.java` | ใช้ EnemyFactory ใหม่ และเก็บภาพ/กระสุน Algo ใน `render/AlgoMonSprite.java` |
| Enemy | `combat/CombatRoom.java` → `CombatRoom.Enemy` | คลาสแม่รองรับการสืบทอดและ `updateBehavior()` พร้อมคิวเกิดศัตรูหลังจบการอัปเดต |

Ken ใช้ `KEN_ID = 4` และ M ใช้ `M_ID = 2` เป็นชื่อเฉพาะของแต่ละตัว.
Far ในห้องปกติใช้ `FAR_ID = 6` แยกจาก M โดยแทนชื่อ placeholder Firewall เดิม. Final Exam คงหมายเลขเดิม `FINAL_EXAM_FAR_ID = 2` ทั้งเงื่อนไขปลดล็อกและพฤติกรรมยิงจากตู้ตาม cleanup เพื่อรักษางานส่วนนี้ไว้เหมือนก่อนแก้ชื่อ.
วิชาของบอส: Sati = C++, M = OOP, Ken = Algorithm และ Ae/Far/Dome = Network. ID เป็นชื่อตัวบอส ไม่ใช่ชื่อวิชาหรือพฤติกรรมการต่อสู้.

Ae โหลด `characters/boss/Tnetwork(Ae)/aeBody.png` ถ้ามี หรือใช้ `aeTest.jpg` ที่ใส่ไว้ก่อน. JPG ปัจจุบันเป็นภาพพร้อมพื้นหลังและ UI โปรแกรมวาด ไม่ใช่ภาพตัวละครโปร่งใส. เลเซอร์ไม่ต้องมี asset: วาดด้วย ShapeRenderer และสามารถใส่ `aeLaser.png` แนวตั้งเพื่อแทนภาพเลเซอร์ได้. ค่าปรับสกิลอยู่ใน AeBossBehavior: เตือน 0.9 วินาที, กวาด 3.5 วินาที, พัก 5 วินาที, ความกว้าง 32 และดาเมจ 16 ก่อนคูณค่ารอบ.
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
