package com.akirahane.momentum.core.common.state;

import com.akirahane.momentum.core.common.state.states.MovementState;
import com.akirahane.momentum.core.common.state.states.ground.GroundState;
import com.akirahane.momentum.core.common.state.states.ground.action.ProneState;
import lombok.Getter;

public enum MovementStateType {
    GROUND(0, new GroundState(null)),
    PRONE(1, new ProneState(null));

    @Getter
    private final int id;
    @Getter
    private final MovementState stateInstance;
    private static final MovementStateType[] BY_ID;

    static {
        BY_ID = new MovementStateType[values().length];
        for (MovementStateType type : values()) {
            BY_ID[type.id] = type;
        }
    }

    MovementStateType(int id, MovementState stateInstance) {
        this.id = id;
        this.stateInstance = stateInstance;
    }

    public static MovementStateType fromId(int id) {
        if (id < 0 || id >= BY_ID.length) return GROUND;
        return BY_ID[id];
    }
}
