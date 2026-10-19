package com.akirahane.momentum.client.animation;

import com.zigythebird.playeranimcore.animation.layered.modifier.AbstractModifier;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

/**
 * 放在 modifier 链最前面的拦截器.
 * <p>
 * PAL 内部流程: {@code copyVanillaPart(part, bone)} 先把原版 setupAnim 算好的骨骼姿势
 * 复制到 bone 里, 然后跑 modifier 链的 {@code get3DTransform(bone)} 覆盖 bone.
 * <p>
 * 本 modifier 对正在瞄准/攻击/使用物品的手臂:
 * <ol>
 *   <li>保存原版手臂旋转</li>
 *   <li>让动画处理手臂 (获得正确的身体倾斜后位置)</li>
 *   <li>用 "原版手臂旋转 - 动画身体旋转" 覆盖手臂旋转,
 *       补偿因动画身体旋转造成的瞄准偏移</li>
 * </ol>
 */
public class ArmConditionModifier extends AbstractModifier {
    private final Player player;
    /**
     * 动画给身体 (torso) 施加的旋转, 用于补偿手臂瞄准方向
     */
    private final Vector3f animationBodyRot = new Vector3f();

    public ArmConditionModifier(Player player) {
        this.player = player;
    }

    @Override
    public void get3DTransform(PlayerAnimBone bone) {
        String name = bone.getName();

        if ("body".equals(name)) {
            // 身体: 正常走动画, 同时缓存动画施加的身体旋转
            super.get3DTransform(bone);
            animationBodyRot.set(bone.rotation.x, bone.rotation.y, bone.rotation.z);
            return;
        }

        if (shouldKeepVanillaArm(name)) {
            // 保存原版手臂旋转 (copyVanillaPart 已经写入)
            float vRotX = bone.rotation.x();
            float vRotY = bone.rotation.y();
            float vRotZ = bone.rotation.z();

            // 让动画处理手臂, 获得正确的身体倾斜后位置
            super.get3DTransform(bone);

            // 覆盖旋转: 原版手臂朝向 - 动画身体旋转 = 抵消身体倾斜的净手臂朝向
            bone.rotation.x = vRotX - animationBodyRot.x();
            bone.rotation.y = vRotY - animationBodyRot.y();
            bone.rotation.z = vRotZ - animationBodyRot.z();
            return;
        }

        super.get3DTransform(bone);
    }

    /**
     * 是否需要让该手臂保持原版姿势(不应用自定义动画).
     * <p>
     * {@code isUsingItem()} 已是通用判断——它覆盖所有 {@code UseAnim} 类型
     * (BOW/CROSSBOW/SPEAR/BLOCK/EAT/DRINK/SPYGLASS/BRUSH/TOOT_HORN),
     * 模组枪械只要正确使用 {@code UseAnim}, 在蓄力/瞄准期间就会触发.
     * <p>
     * 唯一需要额外处理的是<b>使用结束后仍需保持手臂姿势</b>的物品,
     * 如原版弩: 装填完毕 → isUsingItem 变 false → 但手臂仍需保持持弩瞄准姿势.
     * 模组远程武器如有类似机制, 需要在此处按需扩展.
     */
    private boolean shouldKeepVanillaArm(String boneName) {
        boolean isUsing = player.isUsingItem();
        boolean isAttacking = player.swinging;

        if ("right_arm".equals(boneName)) {
            // 使用物品 / 攻击 / 手持已装填的弩(或模组扩展的远程武器)
            return isUsing && player.getUsedItemHand() == InteractionHand.MAIN_HAND ||
                    isAttacking ||
                    isRangedWeaponReady(player.getMainHandItem());
        }
        if ("left_arm".equals(boneName)) {
            return isUsing && player.getUsedItemHand() == InteractionHand.OFF_HAND ||
                    isRangedWeaponReady(player.getOffhandItem());
        }
        return false;
    }

    /**
     * 检查远程武器是否处于"装填完毕, 待发射"状态 (isUsingItem 已结束但手臂姿势仍需保持).
     * <p>
     * 原版弩: {@link CrossbowItem#isCharged}.
     * 模组武器扩展点: 检查对应模组的蓄力/装填状态.
     */
    private boolean isRangedWeaponReady(ItemStack stack) {
        if (stack.isEmpty()) return false;
        // 原版弩
        if (stack.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(stack)) {
            return true;
        }
        // TODO: 模组武器扩展 — e.g. 某模组的枪械已装填
        return false;
    }
}
