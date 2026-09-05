package com.akirahane.momentum.platform.client;

/** A stable snapshot of local movement input for one game tick. */
public record MovementInput(
        boolean up,
        boolean down,
        boolean left,
        boolean right,
        boolean jump,
        boolean sprint,
        boolean shift,
        boolean lower,
        float strafe,
        float forward
) {
    public static final MovementInput EMPTY = new MovementInput(
            false, false, false, false, false, false, false, false, 0.0F, 0.0F
    );
}
