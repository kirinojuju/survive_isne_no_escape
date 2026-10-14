# โครงสร้างโค้ดเกมสำหรับทีม

อัปเดต 14 ตุลาคม 2026 — Java ฝั่งเกมหลัก **51 ไฟล์** อีก 2 ไฟล์เป็น desktop launcher และ 3 ไฟล์เป็นชุดตรวจ

คู่มืออธิบาย Concept พร้อมจุดเปิดโค้ดและแนวพูดอยู่ใน [OOP_PRESENTATION_TH.md](D:/oop-game-school-life/project-school-action-roguelike-state/OOP_PRESENTATION_TH.md)

## เริ่มอ่านจาก 6 จุด

1. `schoolLife` สร้างและเชื่อมระบบ แล้วเรียกอัปเดต/วาดแต่ละเฟรม
2. `RunDirector` จัดหน้าจอ ห้อง รางวัล ฉากคั่น และลำดับเกม
3. `PlayerCharacter` รับการควบคุม ดูแลผู้เล่น และเรียกอาวุธผ่าน `WeaponAttack`
4. `CombatRoom` เก็บสิ่งที่อยู่ในห้องและประสานระบบต่อสู้
5. `Enemy` กำหนดข้อมูลและลำดับร่วม; คลาสลูก Override พฤติกรรมของตัวเอง
6. `GameRenderer` ประสานการวาดผ่าน `SceneRenderer`, `CombatRenderer` และ `GameHud`

เมื่อเปิดแอปปกติ `schoolLife.newRun()` เรียก `RunDirector.startIntro()` ครั้งแรก: Story อังกฤษ/ไทย 4 ช่วง → ปก → ทางเข้าตึก → ต่อสู้ `RunDirector` ดูแลหน้าปัจจุบัน เวลา และการกดข้าม ส่วน `SceneRenderer` วาดข้อความ/Fade ภาษาไทยใช้ภาพข้อความที่จัดรูปสระวรรณยุกต์ไว้ใน `ui/intro-story-th.png` สร้างใหม่ด้วย `tools/GenerateGameTitle.java` หลังแก้ `INTRO_LINES` การเริ่มรันใหม่ไม่เล่น Story ซ้ำ และโหมดเทสเข้าห้องเดิมได้โดยตรง

## การสืบทอด

### โฟลเดอร์ส่วนแสดงผล

```text
render/
├── GameRenderer.java          ประสานการวาดและพิกัดคลิก
├── OptionalAssetTexture.java  โหลดภาพร่วมกับภาพสำรอง
├── scene/                    SceneRenderer: ฉาก ห้อง ปก และฉากรถ
├── combat/                   CombatRenderer: ภาพการต่อสู้และกรอบเป้า
├── hud/                      GameHud: HUD และเมนู
└── sprite/
    ├── EnemySprite / AnimatedEnemySprite / MonsterSprite / StudentSprite
    ├── boss/                 Ae, Dome, Ken, Sati
    ├── ex/                   Liquid Cat, Insecure Sword
    └── effect/               Ice Wall, Sword Wave
```

ตำแหน่งโฟลเดอร์ตรงกับ `package` ใน Java เช่น `render/sprite/boss/DomeSprite.java` ใช้ `package io.github.kirinojuju.render.sprite.boss;` การอ้างข้ามโฟลเดอร์ใช้ `import` และเมธอด public ที่จำเป็น ข้อมูลภายใน Sprite ยังคง private และการสืบทอดจาก EnemySprite เหมือนเดิม

### คลาสแม่และคลาสลูก

```text
Enemy (abstract)
├── Monster
│   └── WarperMonster
└── Boss (abstract)
    ├── KenBoss / AeBoss / MBoss / SatiBoss / DomeBoss
    └── ExBoss (abstract)
        ├── LiquidCatBoss
        ├── AllergicVacuumBoss
        └── InsecureSwordBoss

WeaponAttack (interface)
└── AbstractWeaponAttack (abstract, อยู่ใน WeaponAttack.java)
    ├── LanWeaponAttack
    └── NotebookWeaponAttack

EnemySprite (abstract, implements Disposable)
└── Sprite ของมอน/บอส และ AnimatedEnemySprite
```

ศัตรูไม่มีคลาส `Behavior` ซ้อนอีกชั้น สกิลเฉพาะอยู่ในคลาสเจ้าของแล้ว `Enemy` มี private HP กับ Getter/Setter ส่วน `Enemy.update()` จัดลำดับกลาง และเรียกจุด Override เช่น `updateBehavior()`, `updateTimers()`, `spRecovery()`

`Boss` เป็น abstract class ที่ให้พฤติกรรมร่วมกับคลาสลูก FINAL_EXAM สร้าง AeBoss และ DomeBoss โดยตรง ส่วน `ExBoss` แบ่งกติกาการเคลื่อน/ขอบสนามให้ EX ทั้งสาม แล้วให้แต่ละตัว Override `act()`

## หนึ่งเฟรมทำงานอย่างไร

```text
schoolLife.render()
  → รับการควบคุม / RunDirector.update()
  → ระหว่างต่อสู้: PlayerCharacter.update()
  → CombatRoom.update()
      → เอฟเฟกต์และวัตถุ EX
      → Enemy.update() ของศัตรูแต่ละตัว
      → แยกตัวศัตรู / แก้การชนสิ่งกีดขวาง
      → ProjectileSystem.update()
      → แก้การชนหลังโดนผลัก
      → เรียก onDefeated() ก่อนลบศัตรู
  → GameRenderer.draw()
      → SceneRenderer: ฉาก ห้อง โต๊ะ ปก และฉากรถ
      → CombatRenderer: เอฟเฟกต์พื้น → Sprite → กรอบเป้า/เลือดศัตรู
      → GameHud: HP/SP ปุ่มสกิล รางวัล และข้อความเมนู
```

Cat สามารถฟื้นเลือดเปลี่ยนเฟสใน `onDefeated()` ได้ก่อนห้องลบตัวมัน ร่างแยกจะถูกลบเมื่อชนะตัวหลัก การให้รางวัลจึงเกิดหลังแก้เฟสและการตายแล้ว

## อาวุธและ Ability

`PlayerCharacter(WeaponAttack physical, WeaponAttack magical)` รับอาวุธผ่าน constructor และเก็บในอาร์เรย์ตามชนิดอาวุธ `schoolLife.newRun()` เลือก LAN/Notebook มาประกอบ ผู้เล่นเรียก `attack()` ผ่าน interface

เพื่อนเพิ่ม Ability ด้วย `WeaponAbility` แล้วส่งให้ `addWeaponAbility()`; อาวุธสร้าง `WeaponAttackSpec` สำหรับการโจมตีครั้งนั้น ก่อนให้ Ability ปรับค่า จึงไม่ต้องแก้การรับปุ่มของผู้เล่น

## การชนและตำแหน่ง

- มอนทั่วไปใช้วงกลมจากตำแหน่งและ `radius`
- บอสปกติใช้ขอบเขตพิกเซลที่มองเห็นของเฟรมภาพ ถ้ามีภาพและข้อมูลขอบเขต; ไม่มีภาพใช้วงกลมสำรอง
- `BossBody` คำนวณกรอบในพิกัดโลกและขนาดวาด ส่วน `Enemy.damageX/Y()`, `damageRadius()` และ `overlapsBeam()` เปิดการใช้งานให้ระบบโจมตี
- `OptionalAssetTexture.BodyTexture` เก็บขอบเขตภาพไว้กับ texture จึงไม่ต้องใช้ Map แยก
- กรอบล็อกเป้าและเส้น Tether ใช้ขอบเขตเดียวกับการรับดาเมจ แถบเลือดอยู่เหนือขอบบนของร่าง จึงไม่ลอยอิงจุดกลางเดิมของ M
- วงเตือน Cat ใช้ `LiquidCatBoss.ORBITAL_RADIUS` ค่าเดียวกับดาเมจ และวาดเฉพาะขอบวง พื้นกับผู้เล่นด้านในยังมองเห็น
- ห้อง MOBS/BOSS/EX ใช้ `scene/sceneroom_new_no_table.png` ถ้าไม่มีไฟล์จะใช้ฉากว่างเดิม; FINAL_EXAM ใช้ ServerRoom
- `RoomObstacles` สุ่มโต๊ะ 3 ตัวและดูแลการชน/เดินอ้อมในห้อง MOBS/BOSS; EX กับ FINAL_EXAM ไม่มีโต๊ะขวาง
- `EnemySpacing` แก้การทับกันของมอน โดยเคารพตัวที่ถูกตรึงบนตู้
- Ice Wall ล็อกตำแหน่งขณะกด วางหน้า 92 หน่วย อยู่ 4.5 วินาที และกันการเดิน/กระสุนศัตรู รวมกระสุนบอสที่ข้ามโต๊ะ

## บอสและลำดับเกมปัจจุบัน

- บอสปกติ 5 ตัว: Ken, Ae, M, Sati, Dome; หนึ่งลูปเลือก Ken หรือ Sati แล้วมี Ae, M, Dome ครบ 4 ด่าน
- EX 3 ตัว: Liquid Cat, Allergic Vacuum, Insecure Sword
- Far ถูกถอดออกจากการสุ่มและ FINAL_EXAM
- ชนะ Ae และ Dome ครบ ปลดล็อก FINAL_EXAM หนึ่งครั้งต่อรัน
- EX แรกเปิดหลัง Loop 1 และมาก่อน FINAL_EXAM ที่รออยู่ จากนั้นแสดง `Good...Let's us prepare the final EXAM`
- FINAL_EXAM มี Ae/Dome บนตู้ซ้าย/ขวา ผู้เล่นเดิน/Dash ได้เฉพาะซ้าย–ขวาในเลนล่าง

## จุดปรับสกิลที่พบบ่อย

| งาน | จุดที่เปิด |
| --- | --- |
| ปรับ HP/SP/ความเร็วตามลูป | `RunPlan` และ `EnemyFactory` |
| เพิ่มบอส | สร้างคลาสลูกของ Boss/ExBoss แล้วลงทะเบียนใน Factory และ renderer |
| ระเบิด Ken | `KenBoss.advanceBombs()` และค่าคงที่ใน KenBoss |
| วาร์ป/เลเซอร์ Ae | `AeBoss.tryWarp()` / `updateLaser()` |
| หอก Sati | `SatiBoss.useSpecial()` / `CHARGE_DURATION` / `SPEAR_SPEED` |
| สายฟ้า/เลเซอร์ Dome | `DomeBoss.State`, `LIGHTNING_RADIUS`, `LASER_HALF_WIDTH` และพิกัดมือ `LASER_*_HAND_*` |
| Cat สองเฟส/ร่างแยก | `LiquidCatBoss.onDefeated()` / `act()` / `advanceDivide()` |
| ดาบ EX | `InsecureSwordBoss.act()` / `damageMultiplier()` |
| ดาเมจ/เกราะ/สะท้อน | `CombatDamage` |
| กระสุน/การชนสิ่งกีดขวาง | `ProjectileSystem` |

## สิ่งที่ควรอธิบายตามจริง

- ใช้ Class/Object, constructor, private/getter/setter, abstract, extends, super, Overload, Override และ implements จริง
- ใช้ Factory และการเรียกผ่าน interface/คลาสแม่; ไม่ได้เพิ่ม Adapter/Chain of Responsibility เพื่อให้ครบชื่อ
- SOLID ดีขึ้นในส่วนการแบ่งหน้าที่และการขยายพฤติกรรม แต่ Player ยังรับผิดชอบหลายส่วน
- `GameRenderer` ลดจาก 1,018 เป็น 183 บรรทัด โดยเพิ่ม 3 คลาสตามหน้าที่: SceneRenderer, CombatRenderer และ GameHud; Logic การวาดยังคงอยู่ในคลาสที่เป็นเจ้าของ
- พิกัดและสถานะบางส่วนยังเป็น public; เลือดศัตรูถูกควบคุมผ่านเมธอดแล้ว
- static/final ยังใช้กับค่าคงที่และ reference ที่ไม่เปลี่ยน ไม่มีสถานะ AI ส่วนกลางที่ใช้ร่วมกันทุกบอส

## รายการไฟล์หลักทั้งหมด

ตารางต่อไปนี้สร้างจากไฟล์ปัจจุบันใต้ `core/src/main/java`:

| ไฟล์ | หน้าที่ |
| --- | --- |
| [audio/GameAudio.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/audio/GameAudio.java) | โหลดเสียง, วน BGM ด้วย Music, เล่น Game Over ครั้งเดียวตาม Phase, เล่น Sword ทันที และคืนทรัพยากร |
| [combat/CombatDamage.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/combat/CombatDamage.java) | รับดาเมจ เกราะ สะท้อน พิษ การโจมตีประชิด Beam และระเบิด |
| [combat/CombatRoom.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/combat/CombatRoom.java) | ข้อมูลห้อง คำสั่งโจมตีจากอาวุธ และลำดับระบบต่อสู้ |
| [combat/ProjectileSystem.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/combat/ProjectileSystem.java) | เดินกระสุน ตามเป้าหมาย ตรวจโต๊ะ/กำแพง/ตัวละคร และอายุเอฟเฟกต์ชน |
| [enemy/boss/AeBoss.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/enemy/boss/AeBoss.java) | วาร์ปแบบเตือนจุดและเลเซอร์กวาด พร้อม Getter ของเฟสเลเซอร์ |
| [enemy/boss/Boss.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/enemy/boss/Boss.java) | คลาสแม่บอส กติกาฟื้น SP และการอยู่/วาร์ปกลับตำแหน่งประจำ |
| [enemy/boss/BossBody.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/enemy/boss/BossBody.java) | ขนาดวาด ขอบเขตพิกเซลภาพ และการแปลง hitbox เป็นพิกัดโลก |
| [enemy/boss/DomeBoss.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/enemy/boss/DomeBoss.java) | State ของสายฟ้า/เลเซอร์ การเตือน ดาเมจ พิกัดมือ และเลเซอร์ลงจากตู้ |
| [enemy/boss/KenBoss.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/enemy/boss/KenBoss.java) | ข้อมูล/สกิลระเบิด Ken และ Override อัปเดตก่อนใช้พฤติกรรมร่วม |
| [enemy/boss/MBoss.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/enemy/boss/MBoss.java) | สร้างร่างแยกหนึ่งครั้งเมื่อ HP ครึ่งหนึ่ง และยิงกระสุนเล็งเป้า |
| [enemy/boss/SatiBoss.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/enemy/boss/SatiBoss.java) | เตรียมธาตุ C++ และปล่อยหอก พร้อมจุดกำเนิดภาพ/สกิลร่วมกัน |
| [enemy/Enemy.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/enemy/Enemy.java) | abstract base ของศัตรู private HP ลำดับอัปเดต กติกาทั่วไปและขอบเขตชน |
| [enemy/EnemyFactory.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/enemy/EnemyFactory.java) | สร้างศัตรู ค่าสถานะ จุดเกิด ลูกน้อง และบอสบนตู้ |
| [enemy/EnemySpacing.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/enemy/EnemySpacing.java) | แยกศัตรูที่ยืนซ้อนกันและเคารพตัวที่ถูกตรึงตำแหน่ง |
| [enemy/ex/AllergicVacuumBoss.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/enemy/ex/AllergicVacuumBoss.java) | ดูดผู้เล่น/ฝุ่น เพิ่มภูมิแพ้ เตือนจาม และพ่นฝุ่นกลับ |
| [enemy/ex/ExBoss.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/enemy/ex/ExBoss.java) | abstract base ของ EX สถานะเตือน/พัก การเคลื่อน/ขอบสนาม และวัตถุในห้อง EX |
| [enemy/ex/InsecureSwordBoss.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/enemy/ex/InsecureSwordBoss.java) | เกราะ กระดาษโน้ต ความมั่นใจ การฟัน และจังหวะเปิดให้โจมตี |
| [enemy/ex/LiquidCatBoss.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/enemy/ex/LiquidCatBoss.java) | สองเฟส Transform Orbital Divide ร่างแยก และกติกาดาเมจ/การตาย |
| [enemy/Monster.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/enemy/Monster.java) | มอนทั่วไป การฟื้น SP การโจมตีพื้นฐาน และการเลือกชนิดภาพ |
| [enemy/WarperMonster.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/enemy/WarperMonster.java) | คลาสลูก Monster ที่ Override การวาร์ปโจมตี |
| [flow/RunDirector.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/flow/RunDirector.java) | สถานะหน้าจอ การเปลี่ยนห้อง รางวัล ฉากคั่น และโหมดทดสอบ |
| [flow/RunPlan.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/flow/RunPlan.java) | ลูป ด่าน ลำดับบอส การเพิ่มค่าสถานะ และเงื่อนไข FINAL_EXAM |
| [player/PlayerCharacter.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/player/PlayerCharacter.java) | ผู้เล่น การเดิน/Dash ทรัพยากร การรับปุ่ม และเรียกอาวุธผ่าน interface |
| [player/PotionInventory.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/player/PotionInventory.java) | จำนวนและการใช้ยา HP/SP พร้อมตรวจว่าใช้ได้หรือไม่ |
| [player/Upgrade.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/player/Upgrade.java) | รายการอัปเกรดและผลที่เพิ่มให้ผู้เล่น/อาวุธ |
| [render/sprite/boss/AeSprite.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/render/sprite/boss/AeSprite.java) | ภาพ Ae และภาพ/สัญญาณเตือนเลเซอร์กวาด |
| [render/sprite/AnimatedEnemySprite.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/render/sprite/AnimatedEnemySprite.java) | วาดศัตรูที่ใช้ลำดับภาพ body/attack แบบทั่วไป เช่น M |
| [render/sprite/boss/DomeSprite.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/render/sprite/boss/DomeSprite.java) | เลือกเฟรมตาม State ของ Dome และวาดสายฟ้า/เลเซอร์ |
| [render/sprite/EnemySprite.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/render/sprite/EnemySprite.java) | abstract contract ของภาพศัตรู การโหลด/วาดร่วม และ ownership ของ texture |
| [render/GameRenderer.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/render/GameRenderer.java) | ประสานลำดับการวาดและแปลงตำแหน่งคลิกเป็นพิกัดเกม |
| [render/scene/SceneRenderer.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/render/scene/SceneRenderer.java) | ฉากห้อง โต๊ะ ปก ทางเข้าตึก ฉากเลือกรถ และ Fade |
| [render/combat/CombatRenderer.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/render/combat/CombatRenderer.java) | ภาพต่อสู้ กระสุน วงเตือน กรอบเป้า และแถบเลือดศัตรู; เรียก EnemySprite แบบ polymorphism |
| [render/hud/GameHud.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/render/hud/GameHud.java) | HP/SP ปุ่มสกิล สถานะอาวุธ ตัวเลือกรางวัล และเมนูระหว่างสู้ |
| [render/sprite/effect/IceWallSprite.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/render/sprite/effect/IceWallSprite.java) | ภาพชิ้นกำแพงน้ำแข็งตามตำแหน่งและทิศทางที่ล็อกไว้ |
| [render/sprite/ex/InsecureSwordSprite.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/render/sprite/ex/InsecureSwordSprite.java) | ภาพ Idle/ฟัน/ฟันพลาดของดาบ EX |
| [render/sprite/boss/KenSprite.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/render/sprite/boss/KenSprite.java) | ภาพ Ken ระเบิด วงเตือน และภาพระเบิดทำงาน |
| [render/sprite/effect/LanWaveSprite.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/render/sprite/effect/LanWaveSprite.java) | ภาพคลื่น Sword 12 เฟรม หมุนตามทิศทางการยิง |
| [render/sprite/ex/LiquidCatSprite.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/render/sprite/ex/LiquidCatSprite.java) | ภาพ Cat แต่ละเฟส Transform Divide Orbital และกระสุนร่างแยก |
| [render/sprite/MonsterSprite.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/render/sprite/MonsterSprite.java) | ภาพ Algorithm/Network/Program/Slime และลูกบอล Algorithm |
| [render/OptionalAssetTexture.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/render/OptionalAssetTexture.java) | โหลดภาพหรือภาพสำรอง; texture ของบอสเก็บขอบเขตภาพของตัวเอง |
| [render/sprite/boss/SatiSprite.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/render/sprite/boss/SatiSprite.java) | ภาพ Sati ธาตุ C++ และหอกที่หมุนตามทิศทาง |
| [render/sprite/StudentSprite.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/render/sprite/StudentSprite.java) | ภาพผู้เล่น การเดิน โจมตี สกิล และ PowerBall |
| [schoolLife.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/schoolLife.java) | จุดเริ่มเกม ประกอบระบบ รับการควบคุม และเรียกอัปเดต/วาด |
| [weapon/LanWeaponAttack.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/weapon/LanWeaponAttack.java) | โจมตีประชิดด้วย LAN และผล slow/tether/AoE |
| [weapon/NotebookWeaponAttack.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/weapon/NotebookWeaponAttack.java) | ยิง Notebook และ Heat Beam; มีข้อมูลภาพ Notebook อยู่ในไฟล์เดียวกัน |
| [weapon/WeaponAbility.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/weapon/WeaponAbility.java) | สัญญาให้เพื่อนเพิ่มผลก่อน/หลังการโจมตีของอาวุธ |
| [weapon/WeaponAttack.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/weapon/WeaponAttack.java) | สัญญาการโจมตี ข้อมูลการโจมตีหนึ่งครั้ง และ abstract base สำหรับ Ability |
| [world/EntranceScene.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/world/EntranceScene.java) | พิกัด/เงื่อนไขประตู รถ และทรัพยากรภาพฉากทางเข้า |
| [world/GameSpace.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/world/GameSpace.java) | ขนาดโลก ขอบสนาม เลนผู้เล่น และพิกัดตู้เซิร์ฟเวอร์ |
| [world/IceWall.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/world/IceWall.java) | ตำแหน่ง อายุ รูปร่าง และการขวางตัวละคร/กระสุนของกำแพง |
| [world/RoomObstacles.java](D:/oop-game-school-life/project-school-action-roguelike-state/core/src/main/java/io/github/kirinojuju/world/RoomObstacles.java) | ข้อมูล/สุ่มโต๊ะ การชน การเดินอ้อม และภาพโต๊ะในไฟล์เดียวกัน |

## การตรวจและไฟล์ที่ไม่ใช่โค้ดเกมหลัก

`core/src/test` มี GameRulesCheck, CustomBossCheck และ DomeBossCheck แยกจากเกมหลักและไม่รวมใน JAR ส่วน build เป็นไฟล์ที่สร้างอัตโนมัติ สำเนาสำรอง และเครื่องมือตรวจชั่วคราว จึงไม่นับเป็นคลาสหลักสำหรับพรีเซนต์

คำสั่งตรวจหลัก:

```powershell
.\gradlew.bat :core:verifyGameRules :core:verifyCustomBosses :core:verifyDomeBoss :lwjgl3:classes --no-daemon
```

ตรวจภาพจริงเพิ่มเติมสำหรับมอน/บอส, Cat สองเฟส และ Sword/FINAL_EXAM แล้ว การตรวจ flow ใช้การกำจัดศัตรูจำลองเพื่อเดินสถานะ จึงไม่ใช่ผลประเมินความยากจากผู้เล่นจริง
