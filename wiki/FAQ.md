# 常见问题

本页收集了使用 PufferfishSkillsJS 时常见的问题和解答。

## 安装与配置

### Q: PufferfishSkillsJS 需要哪些依赖？

A: 需要以下模组：
- Minecraft 1.20.1
- Forge 47.0.0+
- KubeJS 2001.6.5+
- Pufferfish's Skills 0.17.0+

### Q: 如何确认模组是否正常加载？

A: 在游戏中运行 `/kubejs reload` 命令，或者在启动日志中搜索 `PufferfishSkillsJS`。

### Q: 我的脚本放在哪里？

A: 服务器端脚本放在 `kubejs/server_scripts/` 文件夹中。

## 可重复技能

### Q: 可重复技能的数据会保存吗？

A: 会的，数据会保存在玩家的存档中。

### Q: 如何将已有的技能设置为可重复技能？

A: 使用 `RepeatableSkills.setRepeatable()` 方法：

```javascript
ServerEvents.loaded(event => {
    RepeatableSkills.setRepeatable('puffish_skills:combat', 'skill_id', true)
})
```

### Q: 可重复技能的上限可以动态修改吗？

A: 可以，使用 `RepeatableSkills.setRepeatLimit()` 可以随时修改上限。

### Q: 达到上限后还能再点击吗？

A: 不能，达到上限后技能会显示为不可点击状态。

## API 使用

### Q: 如何获取玩家对象？

A: 从事件中获取：
```javascript
PufferfishSkillsEvents.skillUnlock(event => {
    let player = event.getPlayer()
})
```

或者在 tick 事件中遍历：
```javascript
ServerEvents.tick(event => {
    event.server.getPlayers().forEach(player => {
        // 使用 player
    })
})
```

### Q: 资源位置（ResourceLocation）是什么格式？

A: 通常是 `namespace:path` 格式，例如 `puffish_skills:combat`。

### Q: 如何检查分类是否存在？

A: 使用 `PufferfishSkills.hasCategory()` 方法。

### Q: 强制解锁技能会消耗点数吗？

A: `forceUnlockSkill()` 不会检查点数，也不会消耗点数。

### Q: 如何重置玩家的所有技能数据？

A: 使用 `PufferfishSkills.resetAll(player)`。

## 事件相关

### Q: 事件在客户端还是服务端触发？

A: 所有事件都在服务端触发，你可以安全地使用服务端 API。

### Q: 如何取消事件？

A: 当前版本的事件不可取消。

### Q: 事件监听的顺序是怎样的？

A: 按照脚本注册的顺序执行。

## 经验系统

### Q: 所有分类都有经验系统吗？

A: 不是，经验系统是可选的，需要在 Pufferfish's Skills 的数据包中配置。

### Q: 如何检查分类是否配置了经验系统？

A: 可以尝试获取经验，如果返回 0 或无效值，可能没有配置。

### Q: 经验等级有上限吗？

A: 这取决于 Pufferfish's Skills 的配置。

## 性能与优化

### Q: 在 tick 事件中检查所有玩家会影响性能吗？

A: 如果玩家数量不多，影响不大。但建议避免在 tick 事件中进行太复杂的操作。

### Q: 如何优化性能？

A: 
- 使用游戏阶段（stages）来避免重复检查
- 减少不必要的 API 调用
- 缓存频繁使用的数据

## 故障排除

### Q: 我的脚本没有生效

A: 检查以下几点：
1. 脚本文件是否在正确的文件夹（`server_scripts`）
2. 语法是否正确（检查日志）
3. 是否运行了 `/kubejs reload`
4. 分类和技能 ID 是否正确

### Q: 出现 "Invalid resource location" 错误

A: 检查你的分类 ID 或技能 ID 格式是否正确，应该是 `namespace:path` 格式。

### Q: 可重复技能没有生效

A: 确保：
1. 在 `ServerEvents.loaded` 事件中设置
2. 技能在解锁后才能重复点击
3. 玩家有足够的点数

### Q: 数据不同步

A: PufferfishSkillsJS 会自动同步可重复技能数据到客户端，如有问题请检查网络连接。

## 其他

### Q: 这个模组支持多人游戏吗？

A: 支持，完全兼容多人服务器。

### Q: 可以和其他模组一起使用吗？

A: 可以，PufferfishSkillsJS 只依赖 Pufferfish's Skills 和 KubeJS。

### Q: 如何获取帮助？

A: 可以在 CurseForge 页面留言，或者查看示例代码和文档。

## 下一步

- [Home](./Home) - 返回首页查看所有文档