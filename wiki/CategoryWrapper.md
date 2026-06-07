# CategoryWrapper

`CategoryWrapper` 是对 Pufferfish's Skills 分类对象的包装，提供了便捷的操作方法。

## 获取 CategoryWrapper

```javascript
// 通过 PufferfishSkills API 获取
let category = PufferfishSkills.getCategory('puffish_skills:combat').get()

// 或者从列表中获取
let categories = PufferfishSkills.getCategories()
let category = categories[0]
```

## 基本信息

```javascript
// 获取分类 ID（ResourceLocation）
let id = category.getId()

// 获取分类 ID 字符串
let idString = category.getIdString()  // "puffish_skills:combat"

// 获取技能数量
let skillCount = category.getSkillCount()
```

## 技能操作

```javascript
// 获取分类的所有技能
let skills = category.getSkills(player)  // SkillWrapper[]

// 获取指定技能
let skill = category.getSkill(player, 'strength_1')  // SkillWrapper

// 获取已解锁的技能
let unlockedSkills = category.getUnlockedSkills(player)

// 获取可用的技能
let availableSkills = category.getAvailableSkills(player)

// 获取可购买的技能
let affordableSkills = category.getAffordableSkills(player)
```

## 分类状态

```javascript
// 解锁分类
category.unlock(player)

// 锁定分类
category.lock(player)

// 检查是否已解锁
let isUnlocked = category.isUnlocked(player)
```

## 点数操作

```javascript
// 获取点数
let points = category.getPoints(player, 'puffish_skills:starting')

// 添加点数
category.addPoints(player, 'puffish_skills:starting', 5)

// 设置点数
category.setPoints(player, 'puffish_skills:starting', 10)
```

## 重置操作

```javascript
// 重置该分类的所有技能
category.reset(player)

// 解锁该分类的所有技能
category.unlockAllSkills(player)

// 锁定该分类的所有技能
category.lockAllSkills(player)
```

## 进度信息

```javascript
// 获取已解锁技能数量
let unlockedCount = category.getUnlockedCount(player)

// 获取进度百分比
let progress = category.getProgress(player)

// 获取摘要信息（Map）
let summary = category.getSummary(player)
```

## 界面操作

```javascript
// 打开该分类的界面
category.openScreen(player)
```

## 使用示例

```javascript
// 遍历所有分类并输出进度
let categories = PufferfishSkills.getCategories()
categories.forEach(category => {
    let progress = category.getProgress(player)
    console.log(`${category.getIdString()}: ${progress}%`)
})

// 解锁某个分类的所有技能
let category = PufferfishSkills.getCategory('puffish_skills:combat').get()
category.unlock(player)
category.unlockAllSkills(player)
```

## 下一步

- [SkillWrapper](./SkillWrapper) - 了解技能包装类的功能