package com.iafenvoy.resourceworld;

import com.iafenvoy.resourceworld.config.ResourceWorldData;
import com.iafenvoy.resourceworld.config.WorldConfig;
import com.iafenvoy.resourceworld.data.PositionLocator;
import com.iafenvoy.resourceworld.data.ResourceWorldHelper;
import com.iafenvoy.resourceworld.util.CommandHelper;
import com.iafenvoy.resourceworld.util.RLUtil;
import com.iafenvoy.server.i18n.ServerI18n;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class ResourceWorldCommand {
    private static final SuggestionProvider<CommandSourceStack> WORLD = SuggestionProviders.register(RLUtil.id("world"), (context, builder) -> WorldConfig.appendSuggestions(builder));
    private static final SuggestionProvider<CommandSourceStack> DIMENSIONS = SuggestionProviders.register(RLUtil.id("dimensions"), (context, builder) -> context.getSource() instanceof CommandSourceStack source ? SharedSuggestionProvider.suggestResource(source.registryAccess().registryOrThrow(Registries.LEVEL_STEM).holders().map(Holder.Reference::key).map(ResourceKey::location), builder) : context.getSource().customSuggestion(context));
    private static final Object2LongMap<ServerPlayer> COOLDOWNS = new Object2LongLinkedOpenHashMap<>();
    private static final Object2LongMap<String> DELETE_CONFIRM = new Object2LongLinkedOpenHashMap<>();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        RequiredArgumentBuilder<CommandSourceStack, String> settings = argument("world", StringArgumentType.word()).suggests(WORLD);
        CommandHelper.appendSetting(settings, "centerX", IntegerArgumentType.integer(), IntegerArgumentType::getInteger, ResourceWorldData.Settings::getCenterX, ResourceWorldData.Settings::setCenterX);
        CommandHelper.appendSetting(settings, "centerZ", IntegerArgumentType.integer(), IntegerArgumentType::getInteger, ResourceWorldData.Settings::getCenterZ, ResourceWorldData.Settings::setCenterZ);
        CommandHelper.appendSetting(settings, "range", IntegerArgumentType.integer(0), IntegerArgumentType::getInteger, ResourceWorldData.Settings::getRange, ResourceWorldData.Settings::setRange);
        CommandHelper.appendSettingOptional(settings, "spawnPoint", BlockPosArgument.blockPos(), BlockPosArgument::getBlockPos, ResourceWorldData.Settings::getSpawnPoint, ResourceWorldData.Settings::setSpawnPoint);
        CommandHelper.appendSetting(settings, "cooldown", IntegerArgumentType.integer(0), IntegerArgumentType::getInteger, ResourceWorldData.Settings::getCooldown, ResourceWorldData.Settings::setCooldown);
        CommandHelper.appendSetting(settings, "hideSeedHash", BoolArgumentType.bool(), BoolArgumentType::getBool, ResourceWorldData.Settings::isHideSeedHash, ResourceWorldData.Settings::setHideSeedHash);
        CommandHelper.appendSetting(settings, "allowHomeCommand", BoolArgumentType.bool(), BoolArgumentType::getBool, ResourceWorldData.Settings::isAllowHomeCommand, ResourceWorldData.Settings::setAllowHomeCommand);

        dispatcher.register(literal("resourceworld")
                .then(literal("home")
                        .requires(CommandSourceStack::isPlayer)
                        .executes(ResourceWorldCommand::home))
                .then(literal("tp")
                        .requires(CommandSourceStack::isPlayer)
                        .then(argument("world", StringArgumentType.word())
                                .suggests(WORLD)
                                .executes(ResourceWorldCommand::teleport)
                        ))
                .then(literal("create")
                        .requires(ctx -> ctx.hasPermission(4))
                        .then(argument("world", StringArgumentType.word())
                                .then(argument("target", ResourceLocationArgument.id())
                                        .suggests(DIMENSIONS)
                                        .then(argument("seed", LongArgumentType.longArg()).executes(ctx -> createWorld(ctx, LongArgumentType.getLong(ctx, "seed"))))
                                        .executes(ctx -> createWorld(ctx, 0))
                                )))
                .then(literal("reset")
                        .requires(ctx -> ctx.hasPermission(4))
                        .then(argument("world", StringArgumentType.word())
                                .suggests(WORLD)
                                .executes(ResourceWorldCommand::resetWorld)
                        ))
                .then(literal("delete")
                        .requires(ctx -> ctx.hasPermission(4))
                        .then(argument("world", StringArgumentType.word())
                                .suggests(WORLD)
                                .executes(ResourceWorldCommand::deleteWorld)
                        ))
                .then(literal("enable")
                        .requires(ctx -> ctx.hasPermission(4))
                        .then(argument("world", StringArgumentType.word())
                                .suggests(WORLD)
                                .executes(ctx -> setEnable(ctx, true))
                        ))
                .then(literal("disable")
                        .requires(ctx -> ctx.hasPermission(4))
                        .then(argument("world", StringArgumentType.word())
                                .suggests(WORLD)
                                .executes(ctx -> setEnable(ctx, false))
                        ))
                .then(literal("settings")
                        .requires(ctx -> ctx.hasPermission(4))
                        .then(settings))
        );
    }

    private static int home(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayerOrException();
        ResourceKey<Level> key = player.level().dimension();
        ResourceWorldData data = WorldConfig.get(key);
        if (data == null)
            throw new SimpleCommandExceptionType(ServerI18n.translateToLiteral(source, "message.resource_world.not_a_resource_world")).create();
        if (!data.getSettings().isAllowHomeCommand())
            throw new SimpleCommandExceptionType(ServerI18n.translateToLiteral(source, "message.resource_world.not_a_resource_world")).create();
        source.getServer().getPlayerList().respawn(player, true/*? >=1.21 {*/, Entity.RemovalReason.CHANGED_DIMENSION/*?}*/);
        return 1;
    }

    private static int teleport(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayerOrException();
        ResourceKey<Level> key = getKeyChecked(ctx);
        if (ResourceWorldHelper.RESETTING.contains(key))
            throw new SimpleCommandExceptionType(ServerI18n.translateToLiteral(source, "message.resource_world.resetting")).create();
        ResourceWorldData data = getDataChecked(ctx);
        if (!data.isEnabled())
            throw new SimpleCommandExceptionType(ServerI18n.translateToLiteral(source, "message.resource_world.disabled")).create();
        ServerLevel world = getLevelChecked(ctx);
        long delta = COOLDOWNS.getOrDefault(player, 0) + data.getSettings().getCooldown() * 1000L - System.currentTimeMillis();
        if (delta > 0)
            throw new SimpleCommandExceptionType(ServerI18n.translateToLiteral(source, "message.resource_world.teleport_cooldown", String.valueOf(delta / 1000))).create();
        source.sendSystemMessage(ServerI18n.translateToLiteral(source, "message.resource_world.finding_position"));
        BlockPos pos = PositionLocator.locate(world, data);
        if (pos == null)
            throw new SimpleCommandExceptionType(ServerI18n.translateToLiteral(source, "message.resource_world.cannot_find_position")).create();
        player.teleportTo(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, player.getYRot(), player.getXRot());
        COOLDOWNS.put(player, System.currentTimeMillis());
        return 1;
    }

    private static int createWorld(CommandContext<CommandSourceStack> ctx, long seed) throws CommandSyntaxException {
        String id = StringArgumentType.getString(ctx, "world");
        CommandSourceStack source = ctx.getSource();
        if (WorldConfig.get(ResourceWorldHelper.toRegistryKey(id)) != null)
            throw new SimpleCommandExceptionType(ServerI18n.translateToLiteral(source, "message.resource_world.duplicate_id")).create();
        ResourceLocation target = ResourceLocationArgument.getId(ctx, "target");
        if (ResourceWorldHelper.createWorld(ctx.getSource().getServer(), ResourceWorldHelper.toRegistryKey(id), target, seed))
            source.sendSystemMessage(ServerI18n.translateToLiteral(source, "message.resource_world.success"));
        return 1;
    }

    private static int resetWorld(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ResourceWorldHelper.reset(getLevelChecked(ctx));
        return 1;
    }

    private static int deleteWorld(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        MinecraftServer server = source.getServer();
        ServerLevel world = getLevelChecked(ctx);
        String id = ResourceWorldHelper.resolveId(world.dimension());
        if (DELETE_CONFIRM.containsKey(id) && DELETE_CONFIRM.getLong(id) + 60 * 1000 >= System.currentTimeMillis()) {
            ResourceWorldHelper.deleteWorld(server, world);
            source.sendSystemMessage(ServerI18n.translateToLiteral(source, "message.resource_world.success"));
            DELETE_CONFIRM.removeLong(id);
        } else {
            DELETE_CONFIRM.put(id, System.currentTimeMillis());
            source.sendSystemMessage(ServerI18n.translateToLiteral(source, "message.resource_world.confirm_in_60s"));
        }
        return 1;
    }

    private static int setEnable(CommandContext<CommandSourceStack> ctx, boolean enable) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ResourceWorldData data = getDataChecked(ctx);
        data.setEnabled(enable);
        source.sendSystemMessage(ServerI18n.translateToLiteral(source, "message.resource_world.success"));
        return 1;
    }

    @NotNull
    private static ResourceKey<Level> getKeyChecked(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ResourceKey<Level> key = ResourceWorldHelper.toRegistryKey(StringArgumentType.getString(ctx, "world"));
        if (ResourceWorldHelper.isNotResourceWorld(key))
            throw new SimpleCommandExceptionType(ServerI18n.translateToLiteral(ctx.getSource(), "message.resource_world.not_a_resource_world")).create();
        return key;
    }

    @NotNull
    private static ResourceWorldData getDataChecked(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ResourceWorldData data = WorldConfig.get(getKeyChecked(ctx));
        if (data == null)
            throw new SimpleCommandExceptionType(ServerI18n.translateToLiteral(ctx.getSource(), "message.resource_world.unknown_resource_world")).create();
        return data;
    }

    @NotNull
    private static ServerLevel getLevelChecked(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerLevel world = source.getServer().getLevel(getKeyChecked(ctx));
        if (world == null)
            throw new SimpleCommandExceptionType(ServerI18n.translateToLiteral(source, "message.resource_world.unknown_resource_world")).create();
        return world;
    }
}
