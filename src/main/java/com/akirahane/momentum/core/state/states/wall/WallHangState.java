package com.akirahane.momentum.core.state.states.wall;

import com.akirahane.momentum.core.enumerate.StateType;
import com.akirahane.momentum.core.state.base.BaseState;

public class WallHangState extends BaseState {
    @Override
    public StateType getStateType() {
        return StateType.WALL_HANG;
    }
}
