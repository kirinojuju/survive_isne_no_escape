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
| Ken | `enemy/boss/KenBoss.java`, `render/KenSprite.java` | ท่าระเบิด 3–5 ลูก, เตือน/ระเบิด, เรียกมอน และชื่อเฟรม 1–17 ที่แก้แล้ว |
| M | `enemy/boss/MBoss.java`, `render/MSprite.java` | แบ่งร่างหนึ่งครั้งเมื่อ HP ถึงครึ่ง และยิงกระสุนเดิม |
| Sati | `enemy/boss/SatiBoss.java` | ท่าแปลงธาตุและหอกเดิม รัศมี 36 ความเร็ว 500; เชื่อมกับ SatiBossBehavior |
| Liquid Cat | `enemy/ex/LiquidCatBoss.java` | แปลงร่าง เดิน กระโดด ยิง และลดดาเมจร่างเล็ก; ใช้การชนสไลม์จาก cleanup |
| Algo/Network | `enemy/AlgoMonster.java`, `enemy/NetworkMonster.java` | ใช้ EnemyFactory ใหม่ และเก็บภาพ/กระสุน Algo ใน `render/AlgoMonSprite.java` |
| Enemy | `combat/CombatRoom.java` → `CombatRoom.Enemy` | คลาสแม่รองรับการสืบทอดและ `updateBehavior()` พร้อมคิวเกิดศัตรูหลังจบการอัปเดต |

Ken ใช้ ID 4 (`Talgo (Ken)`) เพื่อไม่ชน Ae ID 1 ที่ใช้ใน Final Exam.
M ใช้ ID 2 (`Far (M)`) ในห้องบอสปกติ ส่วน Far ใน Final Exam ใช้พฤติกรรมยิงจากตู้ตาม cleanup.
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
