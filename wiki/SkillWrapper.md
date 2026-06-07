# SkillWrapper

`SkillWrapper` 是对 Pufferfish's Skills 技能对象的包装，提供了便捷的操作方法。

## 获取 SkillWrapper

```javascript
// 通过 CategoryWrapper 获取
let category = PufferfishSkills.getCategory('puffish_skills:combat').get()
let skill = category.getSkill(player, 'strength_1')

// 通过 PufferfishSkills API 获取技能列表
let skills = PufferfishSkills.getSkills(player, 'puffish_skills:combat')
let skill = skills[0]
```

## 基本信息

```javascript
// 获取技能 ID
let id = skill.getId()  // "strength_1"

// 获取完整 ID（包含分类）
let fullId = skill.getFullId()  // "puffish_skills:combat:strength_1"

// 获取所属分类
let category = skill.getCategory()  // CategoryWrapper
```

## 技能状态

### 状态检查

```javascript
// 获取状态字符串
let state = skill.getState()
// 可能的值: "LOCKED", "AVAILABLE", "AFFORDABLE", "UNLOCKED", "EXCLUDED"

// 检查是否锁定
let isLocked = skill.isLocked()

// 检查是否可用（满足解锁条件）
let isAvailable = skill.isAvailable()

// 检查是否可购买（有足够点数）
let isAffordable = skill.isAffordable()

// 检查是否已解锁
let isUnlocked = skill.isUnlocked()

// 检查是否被排除
let isExcluded = skill.isExcluded()

// 检查是否可以解锁
let canUnlock = skill.canUnlock()
```

### 可重复技能相关

```javascript
// 获取技能类型
let type = skill.getSkillType()

// 检查是否是可重复技能
let isRepeatable = skill.isRepeatable()

// 获取重复点击次数
let repeatCount = skill.getRepeatCount()

// 获取重复上限
let repeatLimit = skill.getRepeatLimit()

// 检查是否有上限
let hasRepeatLimit = skill.hasRepeatLimit()

// 检查是否达到上限
let reachedLimit = skill.hasReachedRepeatLimit()

// 获取剩余次数
let remaining = skill.getRemainingRepeats()
```

## 技能操作

```javascript
// 正常解锁（需要满足条件和有足够点数）
skill.unlock()

// 强制解锁（无视条件和点数）
skill.forceUnlock()

// 锁定技能
skill.lock()
```

## 使用示例

### 检查并解锁技能

```javascript
let category = PufferfishSkills.getCategory('puffish_skills:combat').get()
let skill = category.getSkill(player, 'strength_1')

if (skill.isAvailable() && skill.isAffordable()) {
    skill.unlock()
    player.tell('技能已解锁！')
}
```

### 遍历可重复技能

```javascript
let skills = PufferfishSkills.getSkills(player, 'puffish_skills:combat')
skills.forEach(skill => {
    if (skill.isRepeatable()) {
        let count = skill.getRepeatCount()
        let remaining = skill.getRemainingRepeats()
        console.log(`${skill.getId()}: ${count}/${remaining || '∞'}`)
    }
})
```

## 下一步

- [使用示例](./Examples) - 查看更多实际应用示例