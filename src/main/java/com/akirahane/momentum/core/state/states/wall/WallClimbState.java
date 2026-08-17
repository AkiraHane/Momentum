package com.akirahane.momentum.core.state.states.wall;

import com.akirahane.momentum.client.config.ClientConfig;
import com.akirahane.momentum.client.hud.HintManager;
import com.akirahane.momentum.client.hud.WallHangHints;
import com.akirahane.momentum.config.ServerConfig;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.effect.MomentumEffectType;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.state.states.OriginalState;
import com.akirahane.momentum.core.state.states.air.AirborneState;
import com.akirahane.momentum.core.state.states.air.BreakFallReadyState;
import com.akirahane.momentum.core.state.states.ground.PowerJumpState;
import com.akirahane.momentum.core.state.states.ground.ProneState;
import com.akirahane.momentum.core.state.states.ground.SlideState;
import com.akirahane.momentum.core.state.states.ground.WalkState;
import com.akirahane.momentum.core.state.states.special.BreakFallState;
import com.akirahane.momentum.core.state.states.special.DodgeState;
import com.akirahane.momentum.core.state.states.water.SwimDashState;
import com.akirahane.momentum.core.state.states.water.SwimState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import static com.akirahane.momentum.client.init.InitSounds.JET3;
import static com.akirahane.momentum.core.context.PlayerMovementContext.*;

public class WallClimbState extends BaseState {
    // 动画名称
    public static String WALL_CLIMB = "wall_climb";

    private static final int SOUND_TICK = 10;

    public static boolean canWallClimb(Player player, PlayerMovementContext context) {
        return  ServerConfig.ENABLE_WALL_CLIMB.getAsBoolean() && ClientConfig.ENABLE_WALL_CLIMB.getAsBoolean() &&
                !Minecraft.getInstance().options.keyShift.isDown() &&
                (!Vec3.ZERO.equals(context.getWallNormal()) &&
                        context.isHasFaceWall() &&
                        !Vec3.ZERO.equals(context.getInputVec()) && Mth.abs(context.getInputWallAngle()) < 30 &&
                        Mth.abs(context.getLookWallAngle()) < 30 &&
                        (context.getSpeed().y >= 0 || context.isHasJetBooster()) &&
                        checkKey(player, context)
        );
    }

    // 维持
    public static boolean canWallClimbHold(Player player, PlayerMovementContext context) {
        return ServerConfig.ENABLE_WALL_CLIMB.getAsBoolean() && ClientConfig.ENABLE_WALL_CLIMB.getAsBoolean() &&
                !Minecraft.getInstance().options.keyShift.isDown() &&
                (!Vec3.ZERO.equals(context.getWallNormal()) &&
                context.isHasFaceWall() &&
                !Vec3.ZERO.equals(context.getInputVec()) && Mth.abs(context.getInputWallAngle()) < 60 &&
                Mth.abs(context.getLookWallAngle()) < 60 &&
                (context.getSpeed().y >= 0 || context.isHasJetBooster()) &&
                checkKey(player, context)
        );
    }

    public static boolean checkKey(Player player, PlayerMovementContext context) {
        if (!player.onClimbable()){
            HintManager.add(WallHangHints.WALL_CLIMB);
            return Minecraft.getInstance().options.keyJump.isDown();
        }
        HintManager.add(WallHangHints.CLIMB_ACCELERATION);
        return true;
    }

    @Override
    protected java.util.List<Transition> transitionChain() {
        // 爬墙/滑墙用更宽松的保持检查
        return withPredicate(
                withPredicate(DEFAULT_CHAIN, StateType.WALL_CLIMB, WallClimbState::canWallClimbHold),
                StateType.WALL_SLIDE, WallSlideState::canWallSlideHold);
    }

    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        // 同步 wallNormal 给远程玩家
        if (player.level().isClientSide() && Minecraft.getInstance().player == player) {
            int wallIndex = PlayerMovementContext.encodeWallNormal(context.getWallNormal());
            context.setTransitionWallData((byte)(wallIndex >= 0 ? wallIndex : -1));
        }
        playStateAnimation(player, WALL_CLIMB, context);
        if (!player.onClimbable()){
            context.addPermanentEffect(MomentumEffectType.LIMIT_ACCELERATION_SPEED, AIR_LIMIT_ACCELERATION);
            var instance = player.getAttribute(Attributes.GRAVITY);
            if (instance != null && instance.getModifier(WALL_GRAVITY_ID) == null) {
                instance.addOrReplacePermanentModifier(new AttributeModifier(
                        WALL_GRAVITY_ID,
                        -0.8,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                ));
            }
            player.addDeltaMovement(new Vec3(0, player.getDeltaMovement().y * 0.2, 0));
        }
        context.setNeedSoundTick(SOUND_TICK);
        context.playWallSound(player, STEP, 0.15F, 1);
        context.setTargetArmTransform(0.15F, -20F);
    }

    @Override
    public void clientTick(Player player, PlayerMovementContext context) {
        super.clientTick(player, context);
        if (context.isHasJetBooster() && !player.onClimbable()){
            player.setDeltaMovement(
                    player.getDeltaMovement().x,
                    Math.max(0.3, player.getDeltaMovement().y),
                    player.getDeltaMovement().z
            );
        }
    }

    @Override
    public void clientTickRemote(Player player, PlayerMovementContext context) {
        float speed = (float) Math.min(context.getSpeed().y * 6, 5);
        playStateAnimation(player, WALL_CLIMB, context, 0, speed);
        context.setNeedSoundTick(context.getNeedSoundTick() - speed);
        if (context.getNeedSoundTick() <= 0){
            if (context.isHasJetBooster()){
                player.playSound(JET3.value(), 0.2F, 1.5F + player.getRandom().nextFloat() * 0.4F - 0.2F);
            }
            context.playWallSound(player, STEP, 0.15F, 1);
            context.setNeedSoundTick(context.getNeedSoundTick() + SOUND_TICK);
        }
    }

    @Override
    public void onExit(Player player, PlayerMovementContext context) {
        super.onExit(player, context);
        context.removeEffect(MomentumEffectType.LIMIT_ACCELERATION_SPEED, AIR_LIMIT_ACCELERATION);
        var instance = player.getAttribute(Attributes.GRAVITY);
        if (instance != null) {
            instance.removeModifier(WALL_GRAVITY_ID);
        }
    }

    @Override
    public StateType getStateType() {
        return StateType.WALL_CLIMB;
    }
}
