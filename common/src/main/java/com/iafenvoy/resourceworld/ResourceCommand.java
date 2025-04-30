package com.iafenvoy.resourceworld;

import com.iafenvoy.resourceworld.config.ResourceGameRules;
import com.iafenvoy.resourceworld.config.SingleWorldData;
import com.iafenvoy.resourceworld.config.WorldConfig;
import com.iafenvoy.resourceworld.data.PositionLocator;
import com.iafenvoy.resourceworld.data.ResourceDimensions;
import com.iafenvoy.resourceworld.data.WorldResetHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class ResourceCommand {
    private static final Object2LongMap<ServerPlayerEntity> COOLDOWNS = new Object2LongLinkedOpenHashMap<>();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("resource")
                .then(literal("home")
                        .requires(ServerCommandSource::isExecutedByPlayer)
                        .executes(ResourceCommand::home))
                .then(literal("tp")
                        .requires(ServerCommandSource::isExecutedByPlayer)
                        .then(literal("overworld").executes(ctx -> teleport(ctx, ResourceDimensions.RESOURCE_WORLD)))
                        .then(literal("nether").executes(ctx -> teleport(ctx, ResourceDimensions.RESOURCE_NETHER)))
                        .then(literal("end").executes(ctx -> teleport(ctx, ResourceDimensions.RESOURCE_END)))
                        .executes(ctx -> teleport(ctx, ResourceDimensions.RESOURCE_WORLD)))
                .then(literal("reset")
                        .requires(ctx -> ctx.hasPermissionLevel(4))
                        .then(literal("overworld").executes(ctx -> resetWorld(ctx, ResourceDimensions.RESOURCE_WORLD)))
                        .then(literal("nether").executes(ctx -> resetWorld(ctx, ResourceDimensions.RESOURCE_NETHER)))
                        .then(literal("end").executes(ctx -> resetWorld(ctx, ResourceDimensions.RESOURCE_END)))
                        .executes(ctx -> resetWorld(ctx, List.of(ResourceDimensions.RESOURCE_WORLD, ResourceDimensions.RESOURCE_NETHER, ResourceDimensions.RESOURCE_END))))
                .then(literal("enable")
                        .requires(ctx -> ctx.hasPermissionLevel(4))
                        .then(literal("overworld").executes(ctx -> setEnable(ctx, ResourceDimensions.RESOURCE_WORLD, true)))
                        .then(literal("nether").executes(ctx -> setEnable(ctx, ResourceDimensions.RESOURCE_NETHER, true)))
                        .then(literal("end").executes(ctx -> setEnable(ctx, ResourceDimensions.RESOURCE_END, true))))
                .then(literal("disable")
                        .requires(ctx -> ctx.hasPermissionLevel(4))
                        .then(literal("overworld").executes(ctx -> setEnable(ctx, ResourceDimensions.RESOURCE_WORLD, false)))
                        .then(literal("nether").executes(ctx -> setEnable(ctx, ResourceDimensions.RESOURCE_NETHER, false)))
                        .then(literal("end").executes(ctx -> setEnable(ctx, ResourceDimensions.RESOURCE_END, false))))
                .then(literal("range")
                        .requires(ctx -> ctx.hasPermissionLevel(4))
                        .then(literal("get")
                                .then(literal("overworld").executes(ctx -> getRange(ctx, ResourceDimensions.RESOURCE_WORLD)))
                                .then(literal("nether").executes(ctx -> getRange(ctx, ResourceDimensions.RESOURCE_NETHER)))
                                .then(literal("end").executes(ctx -> getRange(ctx, ResourceDimensions.RESOURCE_END))))
                        .then(literal("set-range")
                                .then(literal("overworld").then(argument("range", IntegerArgumentType.integer(1)).executes(ctx -> setRange(ctx, ResourceDimensions.RESOURCE_WORLD))))
                                .then(literal("nether").then(argument("range", IntegerArgumentType.integer(1)).executes(ctx -> setRange(ctx, ResourceDimensions.RESOURCE_NETHER))))
                                .then(literal("end").then(argument("range", IntegerArgumentType.integer(1)).executes(ctx -> setRange(ctx, ResourceDimensions.RESOURCE_END)))))
                        .then(literal("set-center")
                                .then(literal("overworld").then(argument("x", IntegerArgumentType.integer()).then(argument("z", IntegerArgumentType.integer()).executes(ctx -> setCenter(ctx, ResourceDimensions.RESOURCE_WORLD)))))
                                .then(literal("nether").then(argument("x", IntegerArgumentType.integer()).then(argument("z", IntegerArgumentType.integer()).executes(ctx -> setCenter(ctx, ResourceDimensions.RESOURCE_NETHER)))))
                                .then(literal("end").then(argument("x", IntegerArgumentType.integer()).then(argument("z", IntegerArgumentType.integer()).executes(ctx -> setCenter(ctx, ResourceDimensions.RESOURCE_END))))))
                ));
    }

    private static int home(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();
        MinecraftServer server = source.getServer();
        ServerWorld overworld = server.getOverworld();
        BlockPos spawnPoint = player.getSpawnPointPosition();
        ServerWorld spawnDimension = server.getWorld(player.getSpawnPointDimension());
        if (spawnPoint == null) {
            spawnPoint = overworld.getSpawnPos();
            spawnDimension = overworld;
        }
        player.teleport(spawnDimension, spawnPoint.getX() + 0.5, spawnPoint.getY(), spawnPoint.getZ() + 0.5, player.getYaw(), player.getPitch());
        return 1;
    }

    private static int teleport(CommandContext<ServerCommandSource> ctx, RegistryKey<World> key) throws CommandSyntaxException {
        SingleWorldData data = WorldConfig.getData(key);
        if (data == null) throw new CommandException(Text.literal("Unknown resource world key"));
        ServerCommandSource source = ctx.getSource();
        if (!data.isEnabled()) {
            source.sendError(Text.literal("This resource world is disabled"));
            return 0;
        }
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

    private static int resetWorld(CommandContext<ServerCommandSource> ctx, RegistryKey<World> key) {
        ServerCommandSource source = ctx.getSource();
        MinecraftServer server = source.getServer();
        ServerWorld world = server.getWorld(key);
        if (world == null) return 0;
        WorldResetHelper.reset(world);
        return 1;
    }

    private static int resetWorld(CommandContext<ServerCommandSource> ctx, List<RegistryKey<World>> keys) {
        for (RegistryKey<World> key : keys) resetWorld(ctx, key);
        return 1;
    }

    private static int setEnable(CommandContext<ServerCommandSource> ctx, RegistryKey<World> key, boolean enable) {
        SingleWorldData data = WorldConfig.getData(key);
        if (data == null) throw new CommandException(Text.literal("Unknown resource world key"));
        data.setEnabled(enable);
        ctx.getSource().sendFeedback(() -> Text.literal("Success!"), false);
        return 1;
    }

    private static int getRange(CommandContext<ServerCommandSource> ctx, RegistryKey<World> key) {
        SingleWorldData data = WorldConfig.getData(key);
        if (data == null) throw new CommandException(Text.literal("Unknown resource world key"));
        ctx.getSource().sendFeedback(() -> Text.literal("Center: x=%s, z=%s | Range: %s".formatted(data.getCenterX(), data.getCenterZ(), data.getRange())), false);
        return 1;
    }

    private static int setRange(CommandContext<ServerCommandSource> ctx, RegistryKey<World> key) {
        SingleWorldData data = WorldConfig.getData(key);
        if (data == null) throw new CommandException(Text.literal("Unknown resource world key"));
        data.setRange(IntegerArgumentType.getInteger(ctx, "range"));
        ctx.getSource().sendFeedback(() -> Text.literal("Success!"), false);
        return 1;
    }

    private static int setCenter(CommandContext<ServerCommandSource> ctx, RegistryKey<World> key) {
        SingleWorldData data = WorldConfig.getData(key);
        if (data == null) throw new CommandException(Text.literal("Unknown resource world key"));
        data.setCenterX(IntegerArgumentType.getInteger(ctx, "x"));
        data.setCenterZ(IntegerArgumentType.getInteger(ctx, "z"));
        ctx.getSource().sendFeedback(() -> Text.literal("Success!"), false);
        return 1;
    }
}
