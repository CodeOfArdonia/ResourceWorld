package com.iafenvoy.resourceworld;

import com.iafenvoy.resourceworld.config.ResourceGameRules;
import com.iafenvoy.resourceworld.config.SingleWorldData;
import com.iafenvoy.resourceworld.config.WorldConfig;
import com.iafenvoy.resourceworld.data.PositionLocator;
import com.iafenvoy.resourceworld.data.ResourceDimensions;
import com.iafenvoy.resourceworld.data.WorldResetHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.command.CommandException;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

import static net.minecraft.server.command.CommandManager.literal;

public final class ResourceCommand {
    private static final Object2LongMap<ServerPlayerEntity> COOLDOWNS = new Object2LongLinkedOpenHashMap<>();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("resource")
                .then(literal("tp")
                        .requires(ServerCommandSource::isExecutedByPlayer)
                        .then(literal("overworld").executes(ctx -> teleport(ctx, ResourceDimensions.RESOURCE_WORLD)))
                        .then(literal("nether").executes(ctx -> teleport(ctx, ResourceDimensions.RESOURCE_NETHER)))
                        .then(literal("end").executes(ctx -> teleport(ctx, ResourceDimensions.RESOURCE_END)))
                        .executes(ctx -> teleport(ctx, ResourceDimensions.RESOURCE_WORLD))
                ).then(literal("reset")
                        .requires(ctx -> ctx.hasPermissionLevel(4))
                        .then(literal("overworld").executes(ctx -> resetWorld(ctx, ResourceDimensions.RESOURCE_WORLD)))
                        .then(literal("nether").executes(ctx -> resetWorld(ctx, ResourceDimensions.RESOURCE_NETHER)))
                        .then(literal("end").executes(ctx -> resetWorld(ctx, ResourceDimensions.RESOURCE_END)))
                        .executes(ctx -> resetWorld(ctx, List.of(ResourceDimensions.RESOURCE_WORLD, ResourceDimensions.RESOURCE_NETHER, ResourceDimensions.RESOURCE_END)))
                ));
    }

    public static int teleport(CommandContext<ServerCommandSource> ctx, RegistryKey<World> key) throws CommandSyntaxException {
        SingleWorldData data = WorldConfig.getData(key);
        if (data == null) throw new CommandException(Text.literal("Unknown resource world key"));
        ServerCommandSource source = ctx.getSource();
        MinecraftServer server = source.getServer();
        ServerWorld world = server.getWorld(key);
        if (world == null) throw new CommandException(Text.literal("Cannot find world"));
        ServerPlayerEntity player = source.getPlayerOrThrow();
        long delta = COOLDOWNS.getOrDefault(player, 0) + world.getGameRules().getInt(ResourceGameRules.COOLDOWN_SECOND) * 1000L - System.currentTimeMillis();
        if (delta > 0) {
            source.sendError(Text.literal("Teleport cooldown, remaining time: %s seconds.".formatted(delta / 1000)));
            return 0;
        }
        source.sendFeedback(() -> Text.literal("Finding valid position, please wait..."), false);
        BlockPos pos = PositionLocator.locate(world, data);
        if (pos == null)
            throw new CommandException(Text.literal("Cannot find valid location for teleport! (Retry or check config)"));
        player.teleport(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, player.getYaw(), player.getPitch());
        COOLDOWNS.put(player, System.currentTimeMillis());
        return 1;
    }

    public static int resetWorld(CommandContext<ServerCommandSource> ctx, RegistryKey<World> key) {
        ServerCommandSource source = ctx.getSource();
        MinecraftServer server = source.getServer();
        ServerWorld world = server.getWorld(key);
        if (world == null) return 0;
        WorldResetHelper.reset(world);
        return 1;
    }

    public static int resetWorld(CommandContext<ServerCommandSource> ctx, List<RegistryKey<World>> keys) {
        for (RegistryKey<World> key : keys) resetWorld(ctx, key);
        return 1;
    }
}
