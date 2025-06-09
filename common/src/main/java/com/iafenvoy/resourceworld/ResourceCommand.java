package com.iafenvoy.resourceworld;

import com.iafenvoy.resourceworld.config.ResourceGameRules;
import com.iafenvoy.resourceworld.config.ResourceWorldData;
import com.iafenvoy.resourceworld.config.WorldConfig;
import com.iafenvoy.resourceworld.data.PositionLocator;
import com.iafenvoy.resourceworld.data.ResourceWorldHelper;
import com.iafenvoy.server.i18n.ServerI18n;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class ResourceCommand {
    private static final SuggestionProvider<ServerCommandSource> WORLD = SuggestionProviders.register(Identifier.of(ResourceWorld.MOD_ID, "world"), (context, builder) -> WorldConfig.appendSuggestions(builder));
    private static final SuggestionProvider<ServerCommandSource> DIMENSIONS = SuggestionProviders.register(Identifier.of(ResourceWorld.MOD_ID, "dimensions"), (context, builder) -> context.getSource() instanceof ServerCommandSource source ? CommandSource.suggestIdentifiers(source.getRegistryManager().get(RegistryKeys.DIMENSION).streamEntries().map(RegistryEntry.Reference::registryKey).map(RegistryKey::getValue), builder) : context.getSource().getCompletions(context));
    private static final Object2LongMap<ServerPlayerEntity> COOLDOWNS = new Object2LongLinkedOpenHashMap<>();
    private static final Object2LongMap<String> DELETE_CONFIRM = new Object2LongLinkedOpenHashMap<>();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("resourceworld")
                .then(literal("home")
                        .requires(ServerCommandSource::isExecutedByPlayer)
                        .executes(ResourceCommand::home))
                .then(literal("tp")
                        .requires(ServerCommandSource::isExecutedByPlayer)
                        .then(argument("world", StringArgumentType.word())
                                .suggests(WORLD)
                                .executes(ResourceCommand::teleport)
                        ))
                .then(literal("create")
                        .requires(ctx -> ctx.hasPermissionLevel(4))
                        .then(argument("world", StringArgumentType.word())
                                .then(argument("target", IdentifierArgumentType.identifier())
                                        .suggests(DIMENSIONS)
                                        .then(argument("seed", LongArgumentType.longArg()).executes(ctx -> createWorld(ctx, LongArgumentType.getLong(ctx, "seed"))))
                                        .executes(ctx -> createWorld(ctx, 0))
                                )))
                .then(literal("reset")
                        .requires(ctx -> ctx.hasPermissionLevel(4))
                        .then(argument("world", StringArgumentType.word())
                                .suggests(WORLD)
                                .executes(ResourceCommand::resetWorld)
                        ))
                .then(literal("delete")
                        .requires(ctx -> ctx.hasPermissionLevel(4))
                        .then(argument("world", StringArgumentType.word())
                                .suggests(WORLD)
                                .executes(ResourceCommand::deleteWorld)
                        ))
                .then(literal("enable")
                        .requires(ctx -> ctx.hasPermissionLevel(4))
                        .then(argument("world", StringArgumentType.word())
                                .suggests(WORLD)
                                .executes(ctx -> setEnable(ctx, true))
                        ))
                .then(literal("disable")
                        .requires(ctx -> ctx.hasPermissionLevel(4))
                        .then(argument("world", StringArgumentType.word())
                                .suggests(WORLD)
                                .executes(ctx -> setEnable(ctx, false))
                        ))
                .then(literal("range")
                        .requires(ctx -> ctx.hasPermissionLevel(4))
                        .then(literal("get")
                                .then(argument("world", StringArgumentType.word())
                                        .suggests(WORLD)
                                        .executes(ResourceCommand::getRange)
                                ))
                        .then(literal("set-range")
                                .then(argument("world", StringArgumentType.word())
                                        .suggests(WORLD)
                                        .then(argument("range", IntegerArgumentType.integer(1))
                                                .executes(ResourceCommand::setRange)
                                        )))
                        .then(literal("set-center")
                                .then(argument("world", StringArgumentType.word())
                                        .suggests(WORLD)
                                        .then(argument("x", IntegerArgumentType.integer(1))
                                                .then(argument("z", IntegerArgumentType.integer(1))
                                                        .executes(ResourceCommand::setCenter)
                                                ))))
                ));
    }

    private static int home(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();
        if (ResourceWorldHelper.isNotResourceWorld(player.getWorld().getRegistryKey()))
            throw new CommandException(ServerI18n.translateToLiteral(source, "message.resource_world.not_a_resource_world"));
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

    private static int teleport(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();
        RegistryKey<World> key = ResourceWorldHelper.toRegistryKey(StringArgumentType.getString(ctx, "world"));
        if (ResourceWorldHelper.isNotResourceWorld(key))
            throw new CommandException(ServerI18n.translateToLiteral(source, "message.resource_world.not_a_resource_world"));
        if (ResourceWorldHelper.RESETTING.contains(key))
            throw new CommandException(ServerI18n.translateToLiteral(source, "message.resource_world.resetting"));
        ResourceWorldData data = WorldConfig.get(key);
        if (data == null)
            throw new CommandException(ServerI18n.translateToLiteral(source, "message.resource_world.unknown_resource_world"));
        if (!data.isEnabled())
            throw new CommandException(ServerI18n.translateToLiteral(source, "message.resource_world.disabled"));
        MinecraftServer server = source.getServer();
        ServerWorld world = server.getWorld(key);
        if (world == null)
            throw new CommandException(ServerI18n.translateToLiteral(source, "message.resource_world.unknown_resource_world"));
        long delta = COOLDOWNS.getOrDefault(player, 0) + world.getGameRules().getInt(ResourceGameRules.COOLDOWN_SECOND) * 1000L - System.currentTimeMillis();
        if (delta > 0)
            throw new CommandException(ServerI18n.translateToLiteral("message.resource_world.teleport_cooldown", String.valueOf(delta / 1000)));
        source.sendMessage(ServerI18n.translateToLiteral(source, "message.resource_world.finding_position"));
        BlockPos pos = PositionLocator.locate(world, data);
        if (pos == null)
            throw new CommandException(ServerI18n.translateToLiteral(source, "message.resource_world.cannot_find_position"));
        player.teleport(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, player.getYaw(), player.getPitch());
        COOLDOWNS.put(player, System.currentTimeMillis());
        return 1;
    }

    private static int createWorld(CommandContext<ServerCommandSource> ctx, long seed) throws CommandSyntaxException {
        String id = StringArgumentType.getString(ctx, "world");
        ServerCommandSource source = ctx.getSource();
        if (WorldConfig.get(ResourceWorldHelper.toRegistryKey(id)) != null)
            throw new CommandException(ServerI18n.translateToLiteral(source, "message.resource_world.duplicate_id"));
        Identifier target = IdentifierArgumentType.getIdentifier(ctx, "target");
        if (ResourceWorldHelper.createWorld(ctx.getSource().getServer(), ResourceWorldHelper.toRegistryKey(id), target, seed))
            source.sendMessage(ServerI18n.translateToLiteral(source, "message.resource_world.success"));
        return 1;
    }

    private static int resetWorld(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        RegistryKey<World> key = ResourceWorldHelper.toRegistryKey(StringArgumentType.getString(ctx, "world"));
        ServerCommandSource source = ctx.getSource();
        if (ResourceWorldHelper.isNotResourceWorld(key))
            throw new CommandException(ServerI18n.translateToLiteral(source, "message.resource_world.not_a_resource_world"));
        MinecraftServer server = source.getServer();
        ServerWorld world = server.getWorld(key);
        if (world == null)
            throw new CommandException(ServerI18n.translateToLiteral(source, "message.resource_world.unknown_resource_world"));
        ResourceWorldHelper.reset(world);
        return 1;
    }

    private static int deleteWorld(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        RegistryKey<World> key = ResourceWorldHelper.toRegistryKey(StringArgumentType.getString(ctx, "world"));
        ServerCommandSource source = ctx.getSource();
        if (ResourceWorldHelper.isNotResourceWorld(key))
            throw new CommandException(ServerI18n.translateToLiteral(source, "message.resource_world.not_a_resource_world"));
        MinecraftServer server = source.getServer();
        ServerWorld world = server.getWorld(key);
        if (world == null)
            throw new CommandException(ServerI18n.translateToLiteral(source, "message.resource_world.unknown_resource_world"));
        String id = ResourceWorldHelper.resolveId(world.getRegistryKey());
        if (DELETE_CONFIRM.containsKey(id) && DELETE_CONFIRM.getLong(id) + 60 * 1000 >= System.currentTimeMillis()) {
            ResourceWorldHelper.deleteWorld(server, world);
            source.sendMessage(ServerI18n.translateToLiteral(source, "message.resource_world.success"));
            DELETE_CONFIRM.removeLong(id);
        } else {
            DELETE_CONFIRM.put(id, System.currentTimeMillis());
            source.sendMessage(ServerI18n.translateToLiteral(source, "message.resource_world.confirm_in_60s"));
        }
        return 1;
    }

    private static int setEnable(CommandContext<ServerCommandSource> ctx, boolean enable) throws CommandSyntaxException {
        RegistryKey<World> key = ResourceWorldHelper.toRegistryKey(StringArgumentType.getString(ctx, "world"));
        ServerCommandSource source = ctx.getSource();
        if (ResourceWorldHelper.isNotResourceWorld(key))
            throw new CommandException(ServerI18n.translateToLiteral(source, "message.resource_world.not_a_resource_world"));
        ResourceWorldData data = WorldConfig.get(key);
        if (data == null)
            throw new CommandException(ServerI18n.translateToLiteral(source, "message.resource_world.unknown_resource_world"));
        data.setEnabled(enable);
        source.sendMessage(ServerI18n.translateToLiteral(source, "message.resource_world.success"));
        return 1;
    }

    private static int getRange(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        RegistryKey<World> key = ResourceWorldHelper.toRegistryKey(StringArgumentType.getString(ctx, "world"));
        ServerCommandSource source = ctx.getSource();
        if (ResourceWorldHelper.isNotResourceWorld(key))
            throw new CommandException(ServerI18n.translateToLiteral(source, "message.resource_world.not_a_resource_world"));
        ResourceWorldData data = WorldConfig.get(key);
        if (data == null)
            throw new CommandException(ServerI18n.translateToLiteral(source, "message.resource_world.unknown_resource_world"));
        source.sendMessage(ServerI18n.translateToLiteral(source, "message.resource_world.range_info", String.valueOf(data.getCenterX()), String.valueOf(data.getCenterZ()), String.valueOf(data.getCenterZ())));
        return 1;
    }

    private static int setRange(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        RegistryKey<World> key = ResourceWorldHelper.toRegistryKey(StringArgumentType.getString(ctx, "world"));
        ServerCommandSource source = ctx.getSource();
        if (ResourceWorldHelper.isNotResourceWorld(key))
            throw new CommandException(ServerI18n.translateToLiteral(source, "message.resource_world.not_a_resource_world"));
        ResourceWorldData data = WorldConfig.get(key);
        if (data == null)
            throw new CommandException(ServerI18n.translateToLiteral(source, "message.resource_world.unknown_resource_world"));
        data.setRange(IntegerArgumentType.getInteger(ctx, "range"));
        source.sendMessage(ServerI18n.translateToLiteral(source, "message.resource_world.success"));
        return 1;
    }

    private static int setCenter(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        RegistryKey<World> key = ResourceWorldHelper.toRegistryKey(StringArgumentType.getString(ctx, "world"));
        ServerCommandSource source = ctx.getSource();
        if (ResourceWorldHelper.isNotResourceWorld(key))
            throw new CommandException(ServerI18n.translateToLiteral(source, "message.resource_world.not_a_resource_world"));
        ResourceWorldData data = WorldConfig.get(key);
        if (data == null)
            throw new CommandException(ServerI18n.translateToLiteral(source, "message.resource_world.unknown_resource_world"));
        data.setCenterX(IntegerArgumentType.getInteger(ctx, "x"));
        data.setCenterZ(IntegerArgumentType.getInteger(ctx, "z"));
        source.sendMessage(ServerI18n.translateToLiteral(source, "message.resource_world.success"));
        return 1;
    }
}
