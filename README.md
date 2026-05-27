# 模组正在开发中 目前处于alpha测试版本，功能缺失较多 不建议使用

### TODD

- 滑行不禁止输入, 但是转向难度和空中相同
- 滑行下坡速度会向下坡方向偏移
- 地面状态处于水中时, 根据水面占比碰撞箱调整减速
- 装备喷气助推器时, 基础速度和跳跃提升增加
- 添加墙面/游泳/特殊姿态

---

## 以下是NeoForge模板内容

---
Installation information
=======

This template repository can be directly cloned to get you started with a new
mod. Simply create a new repository cloned from this one, by following the
instructions provided
by [GitHub](https://docs.github.com/en/repositories/creating-and-managing-repositories/creating-a-repository-from-a-template).

Once you have your clone, simply open the repository in the IDE of your choice. The usual recommendation for an IDE is
either IntelliJ IDEA or Eclipse.

If at any point you are missing libraries in your IDE, or you've run into problems you can
run `gradlew --refresh-dependencies` to refresh the local cache. `gradlew clean` to reset everything
{this does not affect your code} and then start the process again.

Mapping Names:
============
By default, the MDK is configured to use the official mapping names from Mojang for methods and fields
in the Minecraft codebase. These names are covered by a specific license. All modders should be aware of this
license. For the latest license text, refer to the mapping file itself, or the reference copy here:
https://github.com/NeoForged/NeoForm/blob/main/Mojang.md

Additional Resources:
==========
Community Documentation: https://docs.neoforged.net/  
NeoForged Discord: https://discord.neoforged.net/
