# 事件系统

PufferfishSkillsJS 提供了完整的事件系统，让你可以监听和响应技能树的各种变化。

## 事件列表

### 1. skillUnlock - 技能解锁事件

当玩家解锁某个技能时触发。

```javascript
PufferfishSkillsEvents.skillUnlock(event => {
    let player = event.getPlayer()           // ServerPlayer 对象
    let categoryId = event.getCategoryId()   // ResourceLocation
    let skillId = event.getSkillId()         // String
    
    // 示例：给解锁技能的玩家奖励
    player.give(Item.of('minecraft:diamond'))
})
```

### 2. skillLock - 技能锁定事件

当玩家的某个技能被锁定时触发。

```javascript
PufferfishSkillsEvents.skillLock(event => {
    let player = event.getPlayer()
    let categoryId = event.getCategoryId()
    let skillId = event.getSkillId()
    
    console.log(`技能 ${categoryId}:${skillId} 被锁定`)
})
```

### 3. skillRepeatUnlock - 技能重复解锁事件

当可重复技能被再次点击解锁时触发。

```javascript
PufferfishSkillsEvents.skillRepeatUnlock(event => {
    let player = event.getPlayer()
    let categoryId = event.getCategoryId()
    let skillId = event.getSkillId()
    let repeatCount = event.getRepeatCount()  // 重复次数
    
    player.tell(`你已经点击了 ${repeatCount} 次！`)
})
```

### 4. categoryUnlock - 分类解锁事件

当玩家解锁某个分类时触发。

```javascript
PufferfishSkillsEvents.categoryUnlock(event => {
    let player = event.getPlayer()
    let categoryId = event.getCategoryId()
    
    player.tell(`你解锁了分类: ${categoryId}！`)
})
```

### 5. categoryLock - 分类锁定事件

当玩家的某个分类被锁定时触发。

```javascript
PufferfishSkillsEvents.categoryLock(event => {
    let player = event.getPlayer()
    let categoryId = event.getCategoryId()
    
    console.log(`分类 ${categoryId} 被锁定`)
})
```

## 事件对象通用方法

所有事件对象都具有以下方法：

| 方法 | 返回类型 | 描述 |
|------|----------|------|
| `getPlayer()` | ServerPlayer | 获取触发事件的玩家 |
| `getCategoryId()` | ResourceLocation | 获取分类的 ID |

## 实际应用示例

### 根据解锁的技能给奖励

```javascript
PufferfishSkillsEvents.skillUnlock(event => {
    let player = event.getPlayer()
    let skillId = event.getSkillId()
    
    if (skillId === 'health_boost') {
        player.getAttribute('minecraft:generic.max_health')
            .addPermanentModifier({
                name: 'skill_bonus',
                amount: 4,
                operation: 'addition'
            })
    }
})
```

### 可重复技能的递增奖励

```javascript
PufferfishSkillsEvents.skillRepeatUnlock(event => {
    let player = event.getPlayer()
    let count = event.getRepeatCount()
    
    // 每次点击奖励更多的经验
    let expAmount = count * 10
    player.giveExperiencePoints(expAmount)
    
    player.tell(`获得了 ${expAmount} 点经验！`)
})
```

### 解锁分类时给予游戏阶段

```javascript
PufferfishSkillsEvents.categoryUnlock(event => {
    let player = event.getPlayer()
    let categoryId = event.getCategoryId().toString()
    
    if (categoryId === 'puffish_skills:combat') {
        player.stages.add('combat_master')
    }
})
```

## 下一步

- [PufferfishSkills API](./PufferfishSkills-API) - 学习如何使用主要的技能 API
- [RepeatableSkills API](./RepeatableSkills-API) - 了解可重复技能的特殊功能