# PufferfishSkills API

`PufferfishSkills` 是主要的 API 对象，提供了技能树操作的核心功能。

## 分类操作

### 获取分类

```javascript
// 获取所有分类
let categories = PufferfishSkills.getCategories()  // 返回 CategoryWrapper[]

// 获取指定分类
let category = PufferfishSkills.getCategory('puffish_skills:combat')  // 返回 Optional<CategoryWrapper>

// 检查分类是否存在
let exists = PufferfishSkills.hasCategory('puffish_skills:combat')  // 返回 boolean
```

### 分类状态管理

```javascript
// 解锁分类
PufferfishSkills.unlockCategory(player, 'puffish_skills:combat')

// 锁定分类
PufferfishSkills.lockCategory(player, 'puffish_skills:combat')

// 检查分类是否已解锁
let unlocked = PufferfishSkills.isCategoryUnlocked(player, 'puffish_skills:combat')

// 获取玩家已解锁的分类
let unlockedCategories = PufferfishSkills.getUnlockedCategories(player)
```

## 技能操作

### 查询技能

```javascript
// 检查技能是否存在
let hasSkill = PufferfishSkills.hasSkill('puffish_skills:combat', 'strength_1')

// 获取分类的所有技能
let skills = PufferfishSkills.getSkills(player, 'puffish_skills:combat')  // SkillWrapper[]

// 获取已解锁的技能
let unlockedSkills = PufferfishSkills.getUnlockedSkills(player, 'puffish_skills:combat')

// 获取可用的技能（满足解锁条件的）
let availableSkills = PufferfishSkills.getAvailableSkills(player, 'puffish_skills:combat')

// 获取可购买的技能（有足够点数的）
let affordableSkills = PufferfishSkills.getAffordableSkills(player, 'puffish_skills:combat')

// 获取技能 ID 列表
let skillIds = PufferfishSkills.getSkillIds('puffish_skills:combat')
let unlockedSkillIds = PufferfishSkills.getUnlockedSkillIds(player, 'puffish_skills:combat')
```

### 解锁/锁定技能

```javascript
// 检查技能是否已解锁
let isUnlocked = PufferfishSkills.isSkillUnlocked(player, 'puffish_skills:combat', 'strength_1')

// 正常解锁（需要满足条件和有足够点数）
let success = PufferfishSkills.unlockSkill(player, 'puffish_skills:combat', 'strength_1')

// 强制解锁（无视条件和点数）
PufferfishSkills.forceUnlockSkill(player, 'puffish_skills:combat', 'strength_1')

// 锁定技能
PufferfishSkills.lockSkill(player, 'puffish_skills:combat', 'strength_1')

// 批量操作
PufferfishSkills.unlockSkills(player, 'puffish_skills:combat', ['strength_1', 'health_1'])
PufferfishSkills.forceUnlockSkills(player, 'puffish_skills:combat', ['strength_1', 'health_1'])
PufferfishSkills.lockSkills(player, 'puffish_skills:combat', ['strength_1', 'health_1'])
```

## 点数管理

### 查询点数

```javascript
// 获取指定来源的点数
let points = PufferfishSkills.getPoints(player, 'puffish_skills:combat', 'puffish_skills:starting')

// 获取已消费的点数
let spent = PufferfishSkills.getSpentPoints(player, 'puffish_skills:combat')

// 获取总点数
let total = PufferfishSkills.getPointsTotal(player, 'puffish_skills:combat')

// 获取剩余点数
let left = PufferfishSkills.getPointsLeft(player, 'puffish_skills:combat')

// 获取所有点数来源
let sources = PufferfishSkills.getPointsSources(player, 'puffish_skills:combat')
```

### 修改点数

```javascript
// 设置点数
PufferfishSkills.setPoints(player, 'puffish_skills:combat', 'puffish_skills:starting', 10)

// 添加点数
PufferfishSkills.addPoints(player, 'puffish_skills:combat', 'puffish_skills:starting', 5)

// 静默修改（不触发提示）
PufferfishSkills.setPointsSilently(player, 'puffish_skills:combat', 'puffish_skills:starting', 10)
PufferfishSkills.addPointsSilently(player, 'puffish_skills:combat', 'puffish_skills:starting', 5)
```

## 重置功能

```javascript
// 重置所有分类和技能
PufferfishSkills.resetAll(player)

// 重置特定分类（包括技能）
PufferfishSkills.resetCategory(player, 'puffish_skills:combat')

// 只重置技能，不锁定分类
PufferfishSkills.resetSkills(player, 'puffish_skills:combat')

// 彻底擦除分类数据
PufferfishSkills.eraseCategory(player, 'puffish_skills:combat')
```

## 界面操作

```javascript
// 打开技能树主界面
PufferfishSkills.openScreen(player)

// 打开特定分类的界面
PufferfishSkills.openCategoryScreen(player, 'puffish_skills:combat')
```

## 可重复技能相关

这些方法也在 `PufferfishSkills` 对象中，但也可以通过 [RepeatableSkills API](./RepeatableSkills-API) 更方便地使用：

```javascript
// 获取技能类型
let type = PufferfishSkills.getSkillType('puffish_skills:combat', 'rage_stack')

// 检查是否是可重复技能
let isRepeatable = PufferfishSkills.isRepeatableSkill('puffish_skills:combat', 'rage_stack')

// 获取重复次数
let count = PufferfishSkills.getRepeatCount(player, 'puffish_skills:combat', 'rage_stack')

// 获取重复上限
let limit = PufferfishSkills.getRepeatLimit('puffish_skills:combat', 'rage_stack')

// 检查是否达到上限
let reached = PufferfishSkills.hasReachedRepeatLimit(player, 'puffish_skills:combat', 'rage_stack')

// 获取剩余次数
let remaining = PufferfishSkills.getRemainingRepeats(player, 'puffish_skills:combat', 'rage_stack')

// 执行重复解锁
PufferfishSkills.repeatUnlockSkill(player, 'puffish_skills:combat', 'rage_stack')
```

## 经验系统相关

这些方法也在 `PufferfishSkills` 对象中，但更推荐使用 [SkillExperience API](./SkillExperience-API)：

```javascript
// 获取经验等级
let level = PufferfishSkills.getLevel(player, 'puffish_skills:combat')

// 获取当前经验
let currentExp = PufferfishSkills.getCurrentExperience(player, 'puffish_skills:combat')

// 获取下一级所需经验
let required = PufferfishSkills.getRequiredForNextLevel(player, 'puffish_skills:combat')

// 获取升级进度百分比
let progress = PufferfishSkills.getProgressToNextLevel(player, 'puffish_skills:combat')

// 设置等级
PufferfishSkills.setLevel(player, 'puffish_skills:combat', 5)

// 强制升级
PufferfishSkills.levelUp(player, 'puffish_skills:combat')
```

## 下一步

- [CategoryWrapper](./CategoryWrapper) - 了解分类包装类的更多功能
- [SkillWrapper](./SkillWrapper) - 了解技能包装类的更多功能
- [RepeatableSkills API](./RepeatableSkills-API) - 专用于可重复技能的 API