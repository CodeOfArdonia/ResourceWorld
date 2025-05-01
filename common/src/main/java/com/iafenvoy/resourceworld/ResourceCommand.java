package com.iafenvoy.resourceworld;

import com.iafenvoy.resourceworld.config.ResourceGameRules;
import com.iafenvoy.resourceworld.config.ResourceWorldData;
import com.iafenvoy.resourceworld.config.WorldConfig;
import com.iafenvoy.resourceworld.data.PositionLocator;
import com.iafenvoy.resourceworld.data.ResourceWorldHelper;
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
import net.minecraft.text.Text;
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
        dispatcher.register(literal("resource")
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
            throw new CommandException(Text.literal("This is not a resource world!"));
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
        RegistryKey<World> key = ResourceWorldHelper.toRegistryKey(StringArgumentType.getString(ctx, "world"));
        ResourceWorldData data = WorldConfig.get(key);
        if (data == null) throw new CommandException(Text.literal("Unknown resource world key"));
        if (ResourceWorldHelper.isNotResourceWorld(key))
            throw new CommandException(Text.literal("This is not a resource world!"));
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

    private static int createWorld(CommandContext<ServerCommandSource> ctx, long seed) {
        String id = StringArgumentType.getString(ctx, "world");
        if (WorldConfig.get(ResourceWorldHelper.toRegistryKey(id)) != null)
            throw new CommandException(Text.literal("This world id has been used!"));
        Identifier target = IdentifierArgumentType.getIdentifier(ctx, "target");
        if (ResourceWorldHelper.createWorld(ctx.getSource().getServer(), ResourceWorldHelper.toRegistryKey(id), target, seed))
            ctx.getSource().sendFeedback(() -> Text.literal("Success!"), false);
        return 1;
    }

    private static int resetWorld(CommandContext<ServerCommandSource> ctx) {
        RegistryKey<World> key = ResourceWorldHelper.toRegistryKey(StringArgumentType.getString(ctx, "world"));
        if (ResourceWorldHelper.isNotResourceWorld(key))
            throw new CommandException(Text.literal("This is not a resource world!"));
        ServerCommandSource source = ctx.getSource();
        MinecraftServer server = source.getServer();
        ServerWorld world = server.getWorld(key);
        if (world == null) throw new CommandException(Text.literal("Unknown resource world key"));
        ResourceWorldHelper.reset(world);
        return 1;
    }

    private static int deleteWorld(CommandContext<ServerCommandSource> ctx) {
        RegistryKey<World> key = ResourceWorldHelper.toRegistryKey(StringArgumentType.getString(ctx, "world"));
        if (ResourceWorldHelper.isNotResourceWorld(key))
            throw new CommandException(Text.literal("This is not a resource world!"));
        ServerCommandSource source = ctx.getSource();
        MinecraftServer server = source.getServer();
        ServerWorld world = server.getWorld(key);
        if (world == null) throw new CommandException(Text.literal("Unknown resource world key"));
        String id = ResourceWorldHelper.resolveId(world.getRegistryKey());
        if (DELETE_CONFIRM.containsKey(id) && DELETE_CONFIRM.getLong(id) + 60 * 1000 >= System.currentTimeMillis()) {
            ResourceWorldHelper.deleteWorld(server, world);
            source.sendFeedback(() -> Text.literal("Success!"), false);
            DELETE_CONFIRM.removeLong(id);
        } else {
            DELETE_CONFIRM.put(id, System.currentTimeMillis());
            source.sendFeedback(() -> Text.literal("Type again in 60s to confirm deletion"), false);
        }
        return 1;
    }

    private static int setEnable(CommandContext<ServerCommandSource> ctx, boolean enable) {
        RegistryKey<World> key = ResourceWorldHelper.toRegistryKey(StringArgumentType.getString(ctx, "world"));
        if (ResourceWorldHelper.isNotResourceWorld(key))
            throw new CommandException(Text.literal("This is not a resource world!"));
        ResourceWorldData data = WorldConfig.get(key);
        if (data == null) throw new CommandException(Text.literal("Unknown resource world key"));
        data.setEnabled(enable);
        ctx.getSource().sendFeedback(() -> Text.literal("Success!"), false);
        return 1;
    }

    private static int getRange(CommandContext<ServerCommandSource> ctx) {
        RegistryKey<World> key = ResourceWorldHelper.toRegistryKey(StringArgumentType.getString(ctx, "world"));
        if (ResourceWorldHelper.isNotResourceWorld(key))
            throw new CommandException(Text.literal("This is not a resource world!"));
        ResourceWorldData data = WorldConfig.get(key);
        if (data == null) throw new CommandException(Text.literal("Unknown resource world key"));
        ctx.getSource().sendFeedback(() -> Text.literal("Center: x=%s, z=%s | Range: %s".formatted(data.getCenterX(), data.getCenterZ(), data.getRange())), false);
        return 1;
    }

    private static int setRange(CommandContext<ServerCommandSource> ctx) {
        RegistryKey<World> key = ResourceWorldHelper.toRegistryKey(StringArgumentType.getString(ctx, "world"));
        if (ResourceWorldHelper.isNotResourceWorld(key))
            throw new CommandException(Text.literal("This is not a resource world!"));
        ResourceWorldData data = WorldConfig.get(key);
        if (data == null) throw new CommandException(Text.literal("Unknown resource world key"));
        data.setRange(IntegerArgumentType.getInteger(ctx, "range"));
        ctx.getSource().sendFeedback(() -> Text.literal("Success!"), false);
        return 1;
    }

    private static int setCenter(CommandContext<ServerCommandSource> ctx) {
        RegistryKey<World> key = ResourceWorldHelper.toRegistryKey(StringArgumentType.getString(ctx, "world"));
        if (ResourceWorldHelper.isNotResourceWorld(key))
            throw new CommandException(Text.literal("This is not a resource world!"));
        ResourceWorldData data = WorldConfig.get(key);
        if (data == null) throw new CommandException(Text.literal("Unknown resource world key"));
        data.setCenterX(IntegerArgumentType.getInteger(ctx, "x"));
        data.setCenterZ(IntegerArgumentType.getInteger(ctx, "z"));
        ctx.getSource().sendFeedback(() -> Text.literal("Success!"), false);
        return 1;
    }
}
