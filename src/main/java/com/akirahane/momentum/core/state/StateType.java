package com.akirahane.momentum.core.state;

import com.akirahane.momentum.core.state.states.OriginalState;
import com.akirahane.momentum.core.state.states.air.AirborneState;
import com.akirahane.momentum.core.state.states.air.BreakFallReadyState;
import com.akirahane.momentum.core.state.states.ground.ProneState;
import com.akirahane.momentum.core.state.states.ground.SlideState;
import com.akirahane.momentum.core.state.states.ground.WalkState;
import com.akirahane.momentum.core.state.states.special.BreakFallState;
import com.akirahane.momentum.core.state.states.special.DodgeState;
import com.akirahane.momentum.core.state.states.wall.*;
import com.akirahane.momentum.core.state.states.water.SwimDashState;
import com.akirahane.momentum.core.state.states.water.SwimState;
import com.mojang.logging.LogUtils;
import lombok.Getter;
import org.slf4j.Logger;

import java.util.*;

public enum StateType {
    ORIGINAL(new OriginalState(), "state.momentum.original"),
    WALK(new WalkState(), "state.momentum.walk"),
    PRONE(new ProneState(), "state.momentum.prone"),
    SLIDE(new SlideState(), "state.momentum.slide"),
    AIRBORNE(new AirborneState(), "state.momentum.airborne"),
    BREAK_FALL_READY(new BreakFallReadyState(), "state.momentum.break_fall_ready"),
    SWIM(new SwimState(), "state.momentum.swim"),
    BREAK_FALL(new BreakFallState(), "state.momentum.break_fall"),
    DODGE(new DodgeState(), "state.momentum.dodge"),
    WALL_CLIMB(new WallClimbState(), "state.momentum.wall_climb"),
    WALL_SLIDE(new WallSlideState(), "state.momentum.wall_slide"),
    WALL_RUN(new WallRunState(), "state.momentum.wall_run"),
    WALL_HANG(new WallHangState(), "state.momentum.wall_hang"),
    POWER_JUMP(new PowerJumpState(), "state.momentum.power_jump"),
    WALL_KICK(new WallKickState(), "state.momentum.wall_kick"),
    VAULT_UP(new VaultUpState(), "state.momentum.vault_up"),
    VAULT_IN(new VaultInState(), "state.momentum.vault_in"),
    SWIM_DASH(new SwimDashState(), "state.momentum.swim_dash")
    ;
    // 日志
    static final Logger LOGGER = LogUtils.getLogger();


    @Getter
    private final int id;
    private static final StateType[] BY_ID;
    private static final Map<Class<? extends BaseState>, StateType> CLASS_MAP;
    @Getter
    private final BaseState state;
    @Getter
    private final String i18nKey;

    static {
        BY_ID = new StateType[values().length];
        CLASS_MAP = new HashMap<>();
        for (StateType type : values()) {
            BY_ID[type.id] = type;
            CLASS_MAP.put(type.state.getClass(), type);
        }
    }

    StateType(BaseState state, String i18nKey) {
        this.i18nKey = i18nKey;
        this.id = ordinal();
        this.state = state;
    }

    public static StateType fromId(int id) {
        if (id < 0 || id >= BY_ID.length) {
            LOGGER.error("Invalid state id: {}", id);
            throw new IllegalArgumentException("Invalid state id: " + id);
        }
        return BY_ID[id];
    }
}
