package com.iafenvoy.resourceworld.mixin;

import com.iafenvoy.resourceworld.config.WorldConfig;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.GameRuleCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRuleCommand.class)
public class GameRuleCommandMixin {
    @ModifyExpressionValue(method = "setRule", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getGameRules()Lnet/minecraft/world/level/GameRules;"))
    private static GameRules changeSetGameRules(GameRules original, @Local(ordinal = 0) CommandSourceStack source) throws CommandSyntaxException {
        ServerLevel world = source.getPlayerOrException().serverLevel();
        return WorldConfig.getGameRules(world.dimension()).orElse(original);
    }

    @ModifyExpressionValue(method = "queryRule", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getGameRules()Lnet/minecraft/world/level/GameRules;"))
    private static GameRules changeQueryGameRules(GameRules original, @Local(ordinal = 0, argsOnly = true) CommandSourceStack source) throws CommandSyntaxException {
        ServerLevel world = source.getPlayerOrException().serverLevel();
        return WorldConfig.getGameRules(world.dimension()).orElse(original);
    }

    @Inject(method = "setRule", at = @At("RETURN"))
    private static void invokeSave(CallbackInfoReturnable<Integer> cir) {
        WorldConfig.saveConfig();
    }
}
