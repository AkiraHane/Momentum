package com.akirahane.momentum.core.common.state;

import com.akirahane.momentum.core.common.state.states.MovementState;
import com.akirahane.momentum.core.common.state.states.ground.GroundState;
import com.akirahane.momentum.core.common.state.states.ground.action.ProneState;
import com.akirahane.momentum.core.common.state.states.ground.action.SlideState;
import com.akirahane.momentum.core.content.PlayerMovementContext;
import com.mojang.logging.LogUtils;
import lombok.Getter;
import org.slf4j.Logger;

public enum MovementStateType {
    GROUND(0),
    PRONE(1),
    SLIDE(2);
    // 日志
    static final Logger LOGGER = LogUtils.getLogger();

    @Getter
    private final int id;
    private static final MovementStateType[] BY_ID;

    static {
        BY_ID = new MovementStateType[values().length];
        for (MovementStateType type : values()) {
            BY_ID[type.id] = type;
        }
    }

    MovementStateType(int id) {
        this.id = id;
    }

    public static MovementStateType fromId(int id) {
        if (id < 0 || id >= BY_ID.length) return GROUND;
        return BY_ID[id];
    }

    public MovementState getStateInstance(PlayerMovementContext data) {
        return switch (this) {
            case GROUND -> new GroundState(data);
            case PRONE -> new ProneState(data);
            case SLIDE -> new SlideState(data);
        };
    }
}
