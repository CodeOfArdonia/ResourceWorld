package com.iafenvoy.resourceworld;

import com.iafenvoy.resourceworld.data.PositionLocator;
import com.iafenvoy.resourceworld.data.SingleWorldData;
import com.iafenvoy.resourceworld.data.WorldConfig;
import com.iafenvoy.resourceworld.data.RWDimensions;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("resource")
                .then(literal("tp")
                        .requires(ServerCommandSource::isExecutedByPlayer)
                        .then(literal("overworld").executes(ctx -> teleport(ctx, RWDimensions.RESOURCE_WORLD)))
                        .then(literal("nether").executes(ctx -> teleport(ctx, RWDimensions.RESOURCE_NETHER)))
                        .then(literal("end").executes(ctx -> teleport(ctx, RWDimensions.RESOURCE_END)))
                        .executes(ctx -> teleport(ctx, RWDimensions.RESOURCE_WORLD))
                ).then(literal("reset")
                        .requires(ctx -> ctx.hasPermissionLevel(4))
                        .then(literal("overworld").executes(ctx -> resetWorld(ctx, RWDimensions.RESOURCE_WORLD)))
                        .then(literal("nether").executes(ctx -> resetWorld(ctx, RWDimensions.RESOURCE_NETHER)))
                        .then(literal("end").executes(ctx -> resetWorld(ctx, RWDimensions.RESOURCE_END)))
                        .executes(ctx -> resetWorld(ctx, List.of(RWDimensions.RESOURCE_WORLD, RWDimensions.RESOURCE_NETHER, RWDimensions.RESOURCE_END)))
                )
        );
    }

    public static int teleport(CommandContext<ServerCommandSource> ctx, RegistryKey<World> key) throws CommandSyntaxException {
        SingleWorldData data = WorldConfig.getData(key);
        if (data == null) throw new CommandException(Text.literal("Unknown resource world key"));
        ServerCommandSource source = ctx.getSource();
        MinecraftServer server = source.getServer();
        ServerWorld world = server.getWorld(key);
        if (world == null) throw new CommandException(Text.literal("Cannot find world"));
        source.sendFeedback(() -> Text.literal("Finding vaild position, please wait..."), false);
        BlockPos pos = PositionLocator.locate(world, data);
        if (pos == null)
            throw new CommandException(Text.literal("Cannot find valid location for teleport! (Retry or check config)"));
        ServerPlayerEntity player = source.getPlayerOrThrow();
        player.teleport(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, player.getYaw(), player.getPitch());
        return 1;
    }

    public static int resetWorld(CommandContext<ServerCommandSource> ctx, RegistryKey<World> key) {
        return resetWorld(ctx, List.of(key));
    }

    public static int resetWorld(CommandContext<ServerCommandSource> ctx, List<RegistryKey<World>> keys) {
        return 1;
    }
}
