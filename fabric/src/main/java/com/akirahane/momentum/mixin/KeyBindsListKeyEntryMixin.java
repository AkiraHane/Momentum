package com.akirahane.momentum.mixin;

import com.akirahane.momentum.client.input.ChangeMomentumKey;
import com.akirahane.momentum.client.input.ToggleHintKey;
import com.akirahane.momentum.client.registry.MomentumKeyMappingRegistration;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyBindsList.KeyEntry.class)
public abstract class KeyBindsListKeyEntryMixin {
    @Shadow @Final private KeyMapping key;
    @Shadow @Final private Button changeButton;
    @Shadow private boolean hasCollision;

    @Inject(method = "refreshEntry", at = @At("TAIL"))
    private void momentum$addFunctionModifierTooltip(CallbackInfo ci) {
        String name = key.getName();
        if (!hasCollision && (name.equals(MomentumKeyMappingRegistration.FUNCTION_MODIFIER_KEY_NAME)
                || name.equals(ChangeMomentumKey.KEY_NAME) || name.equals(ToggleHintKey.KEY_NAME))) {
            changeButton.setTooltip(Tooltip.create(Component.translatable("key.momentum.function_modifier.tooltip")));
        }
    }
}
