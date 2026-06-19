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
import com.akirahane.momentum.core.state.states.water.SwimState;
import com.mojang.logging.LogUtils;
import lombok.Getter;
import org.slf4j.Logger;

import java.util.*;

public enum StateType {
    ORIGINAL(new OriginalState()),
    WALK(new WalkState()),
    PRONE(new ProneState()),
    SLIDE(new SlideState()),
    AIRBORNE(new AirborneState()),
    BREAK_FALL_READY(new BreakFallReadyState()),
    SWIM(new SwimState()),
    BREAK_FALL(new BreakFallState()),
    DODGE(new DodgeState()),
    WALL_CLIMB(new WallClimbState()),
    WALL_SLIDE(new WallSlideState()),
    WALL_RUN(new WallRunState()),
    WALL_HANG(new WallHangState()),
    VAULT_UP(new VaultUpState()),
    VAULT_IN(new VaultInState()),
    ;
    // 日志
    static final Logger LOGGER = LogUtils.getLogger();


    @Getter
    private final int id;
    private static final StateType[] BY_ID;
    private static final Map<Class<? extends BaseState>, StateType> CLASS_MAP;
    @Getter
    private final BaseState state;

    static {
        BY_ID = new StateType[values().length];
        CLASS_MAP = new HashMap<>();
        for (StateType type : values()) {
            BY_ID[type.id] = type;
            CLASS_MAP.put(type.state.getClass(), type);
        }
    }

    StateType(BaseState state) {
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
