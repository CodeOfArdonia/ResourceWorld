package com.iafenvoy.resourceworld.mixin;

import com.iafenvoy.resourceworld.config.ResourceWorldData;
import com.iafenvoy.resourceworld.config.WorldConfig;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.server.command.DifficultyCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
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
    private static DynamicCommandExceptionType FAILURE_EXCEPTION;

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void handleResourceWorldDifficulty(ServerCommandSource source, Difficulty difficulty, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        if (source.isExecutedByPlayer()) {
            ServerWorld world = source.getPlayerOrThrow().getServerWorld();
            ResourceWorldData config = WorldConfig.get(world.getRegistryKey());
            if (config == null) return;
            if (config.getDifficulty() == difficulty) throw FAILURE_EXCEPTION.create(difficulty.getName());
            else {
                config.setDifficulty(difficulty);
                source.sendFeedback(() -> Text.translatable("commands.difficulty.success", difficulty.getTranslatableName()), true);
                cir.setReturnValue(0);
                WorldConfig.saveConfig();
            }
        }
    }
}
