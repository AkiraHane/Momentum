package com.linqiyinya.momentum.client.input;

import com.linqiyinya.momentum.Constants;
import com.linqiyinya.momentum.network.ToggleMomentumPacket;
import com.linqiyinya.momentum.platform.Services;
import net.minecraft.client.KeyMapping;

import java.util.function.Supplier;

public class ChangeMomentumKey {
    public static final String KEY_NAME = "key.momentum.change_momentum";
    public static final Supplier<KeyMapping> CHANGE_MOMENTUM_KEY_MAPPING = () -> KeyMapping.get(KEY_NAME);

    private static boolean wasToggleDown = false;

    public static void onClientTick(boolean isDown) {
        if (isDown && !wasToggleDown) {
            Services.PLATFORM.sendToServer(new ToggleMomentumPacket());
            Constants.LOG.debug("[INPUT] TOGGLE_MOMENTUM side=client packet=ToggleMomentumPacket");
        }
        wasToggleDown = isDown;
    }
}
