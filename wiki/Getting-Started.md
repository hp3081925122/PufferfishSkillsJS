# 快速开始

本页将帮助你快速上手 PufferfishSkillsJS。

## 安装

1. 确保已安装以下依赖：
   - Minecraft 1.20.1
   - Forge 47.0.0+
   - KubeJS 2001.6.5+
   - Pufferfish's Skills 0.17.0+

2. 将 PufferfishSkillsJS 模组文件放入 `mods` 文件夹

3. 启动游戏即可

## 目录结构

在你的 KubeJS 脚本文件夹中，你可以创建以下文件：

```
kubejs/
└── server_scripts/
    ├── skills.js          # 技能相关脚本
    ├── repeatable.js      # 可重复技能配置
    └── events.js          # 事件监听脚本
```

## 第一个脚本

创建一个简单的脚本来测试：

```javascript
// server_scripts/test.js
ServerEvents.loaded(event => {
    console.log('PufferfishSkillsJS 已加载！')
})

// 监听技能解锁事件
PufferfishSkillsEvents.skillUnlock(event => {
    let player = event.getPlayer()
    let categoryId = event.getCategoryId()
    let skillId = event.getSkillId()
    
    player.tell(`你解锁了技能: ${categoryId}:${skillId}！`)
})
```

## 下一步

现在你已经准备好使用 PufferfishSkillsJS 了！继续阅读以下文档来了解更多功能：

- [事件系统](./Events) - 学习如何监听各种技能事件
- [PufferfishSkills API](./PufferfishSkills-API) - 了解主要的 API 功能
- [使用示例](./Examples) - 查看各种实用的示例代码