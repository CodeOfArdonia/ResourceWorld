package com.iafenvoy.resourceworld.mixin;

import com.iafenvoy.resourceworld.MixinCache;
import com.iafenvoy.resourceworld.config.ResourceWorldData;
import com.iafenvoy.resourceworld.config.WorldConfig;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldBorder.class)
public abstract class WorldBorderMixin {
    @Shadow
    public abstract void setCenter(double x, double z);

    @Shadow
    public abstract void setSize(double size);

    @Inject(method = "tick", at = @At("HEAD"))
    private void forceSetBorder(CallbackInfo ci) {
        if (MixinCache.CURRENT_TICKING_WORLD == null) return;
        ResourceWorldData data = WorldConfig.get(MixinCache.CURRENT_TICKING_WORLD);
        if (data == null) return;
        ResourceWorldData.Settings settings = data.getSettings();
        this.setCenter(settings.getCenterX(), settings.getCenterZ());
        this.setSize(settings.getRange() * 2);
    }
}
