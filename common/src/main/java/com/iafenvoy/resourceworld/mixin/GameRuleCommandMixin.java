package com.iafenvoy.resourceworld.mixin;

import com.iafenvoy.resourceworld.config.WorldConfig;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.GameRuleCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRuleCommand.class)
public class GameRuleCommandMixin {
    @ModifyExpressionValue(method = "executeSet", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getGameRules()Lnet/minecraft/world/GameRules;"))
    private static GameRules changeSetGameRules(GameRules original, @Local(ordinal = 0) ServerCommandSource source) throws CommandSyntaxException {
        ServerWorld world = source.getPlayerOrThrow().getServerWorld();
        return WorldConfig.getGameRules(world.getRegistryKey()).orElse(original);
    }

    @ModifyExpressionValue(method = "executeQuery", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getGameRules()Lnet/minecraft/world/GameRules;"))
    private static GameRules changeQueryGameRules(GameRules original, @Local(ordinal = 0, argsOnly = true) ServerCommandSource source) throws CommandSyntaxException {
        ServerWorld world = source.getPlayerOrThrow().getServerWorld();
        return WorldConfig.getGameRules(world.getRegistryKey()).orElse(original);
    }

    @Inject(method = "executeSet", at = @At("RETURN"))
    private static void invokeSave(CallbackInfoReturnable<Integer> cir) {
        WorldConfig.saveConfig();
    }
}
