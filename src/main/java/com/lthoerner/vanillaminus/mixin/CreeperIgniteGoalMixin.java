package com.lthoerner.vanillaminus.mixin;

import com.lthoerner.vanillaminus.VanillaMinusShearable;
import net.minecraft.entity.ai.goal.CreeperIgniteGoal;
import net.minecraft.entity.mob.CreeperEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreeperIgniteGoal.class)
public class CreeperIgniteGoalMixin {
    @Final
    @Shadow
    private CreeperEntity creeper;

    @Inject(
            method = "tick()V",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void vanillaminus$preventFuseIfSheared(CallbackInfo info) {
        if (((VanillaMinusShearable) creeper).isSheared()) {
            this.creeper.setFuseSpeed(-1);
            info.cancel();
        }
    }
}
