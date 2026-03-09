package com.iafenvoy.resourceworld.mixin;

import com.iafenvoy.resourceworld.config.ResourceWorldData;
import com.iafenvoy.resourceworld.config.WorldConfig;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.DifficultyCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DifficultyCommand.class)
public class DifficultyCommandMixin {
    @Shadow
    @Final
    private static DynamicCommandExceptionType ERROR_ALREADY_DIFFICULT;

    @Inject(method = "setDifficulty", at = @At("HEAD"), cancellable = true)
    private static void handleResourceWorldDifficulty(CommandSourceStack source, Difficulty difficulty, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        if (source.isPlayer()) {
            ServerLevel world = source.getPlayerOrException().serverLevel();
            ResourceWorldData config = WorldConfig.get(world.dimension());
            if (config == null) return;
            if (config.getDifficulty() == difficulty) throw ERROR_ALREADY_DIFFICULT.create(difficulty.getKey());
            else {
                config.setDifficulty(difficulty);
                source.sendSuccess(() -> Component.translatable("commands.difficulty.success", difficulty.getDisplayName()), true);
                cir.setReturnValue(0);
                WorldConfig.saveConfig();
            }
        }
    }
}
