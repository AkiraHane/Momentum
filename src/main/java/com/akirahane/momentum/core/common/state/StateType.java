package com.akirahane.momentum.core.common.state;

import com.akirahane.momentum.core.common.state.base.BaseState;
import com.akirahane.momentum.core.common.state.states.OriginalState;
import com.akirahane.momentum.core.common.state.states.air.AirborneState;
import com.akirahane.momentum.core.common.state.states.ground.action.ProneState;
import com.akirahane.momentum.core.common.state.states.ground.action.SlideState;
import com.akirahane.momentum.core.common.state.states.ground.action.WalkState;
import com.mojang.logging.LogUtils;
import lombok.Getter;
import org.slf4j.Logger;

import java.util.*;

public enum StateType {
    ORIGINAL(new OriginalState()),
    WALK(new WalkState()),
    PRONE(new ProneState()),
    SLIDE(new SlideState()),
    AIR(new AirborneState());
    // 日志
    static final Logger LOGGER = LogUtils.getLogger();


    @Getter
    private final int id;
    private static final StateType[] BY_ID;
    private static final Map<Class<? extends State>, StateType> CLASS_MAP;
    @Getter
    private static final Map<Class<? extends State>, List<Class<? extends State>>> ANCESTOR_CHAIN_MAP; // 预计算的祖先链
    @Getter
    private final State state;

    static {
        BY_ID = new StateType[values().length];
        CLASS_MAP = new HashMap<>();
        ANCESTOR_CHAIN_MAP = new HashMap<>();
        for (StateType type : values()) {
            BY_ID[type.id] = type;
            CLASS_MAP.put(type.state.getClass(), type);
            List<Class<? extends State>> chain = new ArrayList<>();
            Class<? extends State> clazz = type.state.getClass();
            chain.add(clazz);
            while (State.class.isAssignableFrom(clazz)) {
                Class<?> superclass = clazz.getSuperclass();
                if (superclass == null || !State.class.isAssignableFrom(superclass)) {
                    LOGGER.debug("[{}] has no parent state", clazz.getSimpleName());
                    break;
                }
                clazz = superclass.asSubclass(State.class);  // 安全的转换
                chain.add(clazz);
            }
            ANCESTOR_CHAIN_MAP.put(type.state.getClass(), chain);
            for (Class<? extends State> ancestor : chain) {
                if (ANCESTOR_CHAIN_MAP.containsKey(ancestor)) {
                    continue;
                }
                ANCESTOR_CHAIN_MAP.put(ancestor, List.copyOf(chain.subList(chain.indexOf(ancestor), chain.size())));
            }
        }
    }

    StateType(State state) {
        this.id = ordinal();
        this.state = state;
    }

    public static StateType fromId(int id) {
        if (id < 0 || id >= BY_ID.length) {
            LOGGER.warn("Invalid state id: {}", id);
            return ORIGINAL;
        }
        return BY_ID[id];
    }

    public static StateType getStateType(Class<? extends State> clazz) {
        if (CLASS_MAP.containsKey(clazz)) {
            return CLASS_MAP.get(clazz);
        } else {
            LOGGER.warn("Invalid state class: {}", clazz.getName());
            return ORIGINAL;
        }
    }

    public List<Class<? extends State>> getAncestorChain() {
        return ANCESTOR_CHAIN_MAP.get(this.state.getClass());
    }
}
