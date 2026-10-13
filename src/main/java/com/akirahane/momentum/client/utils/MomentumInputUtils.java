package com.akirahane.momentum.client.utils;

import com.akirahane.momentum.core.context.PlayerMomentumContext;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import static com.akirahane.momentum.core.context.PlayerMomentumContext.JUMP;

public class MomentumInputUtils {
    public static boolean isDoubleClick(PlayerMomentumContext context, int key) {
        int len = context.getInputBuffer().length;
        int current = context.getInputBufferIndex();

        if (!context.getInputBuffer()[current].contains(key)) return false;

        boolean foundFirst = false;
        int idx = current;
        for (int i = 1; i < len; i++) {
            int lastIdx = idx;
            idx = (current + i) % len;
            if (context.getInputBuffer()[idx].contains(key) && !foundFirst) {
                foundFirst = true;
                continue;
            }
            if (foundFirst) {
                for (int clickKey : context.getInputBuffer()[idx]) {
                    if (!context.getInputBuffer()[lastIdx].contains(clickKey)) {
                        // 跳跃以外的其他新按键会打断双击匹配。
                        if (clickKey != JUMP && clickKey != key) foundFirst = false;
                    }
                }
            }
        }
        return foundFirst;
    }

    // 前 offset 个 tick，不含当前 tick。
    public static boolean wasKeyPressedRecently(PlayerMomentumContext context, int key, int offset) {
        int len = context.getInputBuffer().length;
        int current = context.getInputBufferIndex();

        for (int i = 1; i <= Math.min(offset, context.getInputBuffer().length - 1); i++) {
            int idx = (current - i + len) % len;
            if (context.getInputBuffer()[idx].contains(key)) {
                return true;
            }
        };
        return false;
    }

    public static void setWorldInputVec(Player player, PlayerMomentumContext context) {
        Vec2 moveVec = ((LocalPlayer) player).input.getMoveVector();
        float forward = moveVec.y;
        float strafe = moveVec.x;

        float yaw = player.getYRot();
        double rad = Math.toRadians(yaw);
        double sin = Math.sin(rad);
        double cos = Math.cos(rad);

        double x = strafe * cos - forward * sin;
        double z = forward * cos + strafe * sin;

        context.setInputVec(new Vec3(x, 0, z));
    }
}
