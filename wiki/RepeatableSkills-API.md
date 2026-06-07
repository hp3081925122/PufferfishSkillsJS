# RepeatableSkills API

`RepeatableSkills` 是专门用于管理可重复技能的 API 对象。

## 什么是可重复技能？

可重复技能是 PufferfishSkillsJS 的特色功能，允许玩家在解锁技能后继续多次点击该技能，每次点击都会消耗技能点数并再次触发奖励。

## 技能类型

| 类型 | 描述 |
|------|------|
| `normal` | 普通技能，解锁后不能再点击 |
| `repeatable` | 可重复技能，可以无限次点击，或设置点击上限 |

## 设置技能类型

### 基本设置

```javascript
// 设置技能类型（使用类型名称）
RepeatableSkills.setSkillType('puffish_skills:combat', 'rage_stack', 'repeatable')

// 快速设置为可重复技能
RepeatableSkills.setRepeatable('puffish_skills:combat', 'rage_stack', true)

// 设置为普通技能（取消可重复）
RepeatableSkills.setRepeatable('puffish_skills:combat', 'rage_stack', false)
```

### 设置点击上限

```javascript
// 设置为可重复技能，最多点击 5 次
RepeatableSkills.setRepeatable('puffish_skills:combat', 'rage_stack', true, 5)

// 或者单独设置上限
RepeatableSkills.setRepeatLimit('puffish_skills:combat', 'rage_stack', 5)

// 取消上限（设置为 0 或负数）
RepeatableSkills.setRepeatLimit('puffish_skills:combat', 'rage_stack', 0)
```

## 查询技能信息

```javascript
// 获取技能类型
let type = RepeatableSkills.getSkillType('puffish_skills:combat', 'rage_stack')

// 检查是否是可重复技能
let isRepeatable = RepeatableSkills.isRepeatable('puffish_skills:combat', 'rage_stack')

// 获取点击上限
let limit = RepeatableSkills.getRepeatLimit('puffish_skills:combat', 'rage_stack')

// 检查是否有上限
let hasLimit = RepeatableSkills.hasRepeatLimit('puffish_skills:combat', 'rage_stack')
```

## 玩家数据管理

### 查询玩家数据

```javascript
// 获取玩家的重复点击次数
let count = RepeatableSkills.getRepeatCount(player, 'puffish_skills:combat', 'rage_stack')

// 检查是否达到上限
let reached = RepeatableSkills.hasReachedRepeatLimit(player, 'puffish_skills:combat', 'rage_stack')

// 获取剩余可点击次数
let remaining = RepeatableSkills.getRemainingRepeats(player, 'puffish_skills:combat', 'rage_stack')
```

### 修改玩家数据

```javascript
// 设置重复次数
RepeatableSkills.setRepeatCount(player, 'puffish_skills:combat', 'rage_stack', 3)

// 清除单个技能的重复数据
RepeatableSkills.clearRepeatData(player, 'puffish_skills:combat', 'rage_stack')

// 清除整个分类的重复数据
RepeatableSkills.clearCategoryData(player, 'puffish_skills:combat')

// 清除玩家的所有重复数据
RepeatableSkills.clearAllData(player)
```

## 执行重复解锁

```javascript
// 主动执行一次重复解锁
// 会检查技能是否已解锁、是否有足够点数、是否达到上限
let success = RepeatableSkills.repeatUnlock(player, 'puffish_skills:combat', 'rage_stack')
```

## 完整配置示例

### 无限次数的可重复技能

```javascript
ServerEvents.loaded(event => {
    // 设置为无限次数的可重复技能
    RepeatableSkills.setRepeatable('puffish_skills:combat', 'rage_stack', true)
})

// 监听重复点击事件
PufferfishSkillsEvents.skillRepeatUnlock(event => {
    if (event.getCategoryId().toString() !== 'puffish_skills:combat') return
    if (event.getSkillId() !== 'rage_stack') return
    
    let player = event.getPlayer()
    let count = event.getRepeatCount()
    
    // 每次点击增加 1 点攻击力
    let attribute = player.getAttribute('minecraft:generic.attack_damage')
    attribute.addPermanentModifier({
        name: 'rage_stack_' + count,
        amount: 0.5,
        operation: 'addition'
    })
    
    player.tell(`怒气层数: ${count}，攻击力提升！`)
})
```

### 有上限的可重复技能

```javascript
ServerEvents.loaded(event => {
    // 设置为最多点击 10 次的可重复技能
    RepeatableSkills.setRepeatable('puffish_skills:mining', 'ore_sense', true, 10)
})

PufferfishSkillsEvents.skillRepeatUnlock(event => {
    if (event.getCategoryId().toString() !== 'puffish_skills:mining') return
    if (event.getSkillId() !== 'ore_sense') return
    
    let player = event.getPlayer()
    let count = event.getRepeatCount()
    let remaining = RepeatableSkills.getRemainingRepeats(player, 'puffish_skills:mining', 'ore_sense')
    
    // 给予临时效果
    player.potionEffects.add('minecraft:night_vision', 600, 0)
    
    if (remaining > 0) {
        player.tell(`矿石感知增强！还可使用 ${remaining} 次`)
    } else {
        player.tell('矿石感知已达到最大次数！')
    }
})
```

## 下一步

- [使用示例](./Examples) - 查看更多可重复技能的实际应用
- [事件系统](./Events) - 了解如何监听重复解锁事件