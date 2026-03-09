package com.iafenvoy.resourceworld.util;

import com.iafenvoy.resourceworld.config.ResourceWorldData;
import com.iafenvoy.resourceworld.config.WorldConfig;
import com.iafenvoy.resourceworld.data.ResourceWorldHelper;
import com.iafenvoy.server.i18n.ServerI18n;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class CommandHelper {
    public static <T> void appendSetting(ArgumentBuilder<CommandSourceStack, ?> builder, String name, ArgumentType<?> type, BiFunction<CommandContext<CommandSourceStack>, String, T> parser, Function<ResourceWorldData.Settings, T> getter, BiConsumer<ResourceWorldData.Settings, T> setter) {
        builder.then(literal(name)
                .then(literal("get").executes(ctx -> {
                    ResourceKey<Level> key = ResourceWorldHelper.toRegistryKey(StringArgumentType.getString(ctx, "world"));
                    CommandSourceStack source = ctx.getSource();
                    if (ResourceWorldHelper.isNotResourceWorld(key))
                        throw new SimpleCommandExceptionType(ServerI18n.translateToLiteral(source, "message.resource_world.not_a_resource_world")).create();
                    ResourceWorldData data = WorldConfig.get(key);
                    if (data == null)
                        throw new SimpleCommandExceptionType(ServerI18n.translateToLiteral(source, "message.resource_world.unknown_resource_world")).create();
                    source.sendSystemMessage(ServerI18n.translateToLiteral(source, "message.resource_world.setting.get", name, String.valueOf(getter.apply(data.getSettings()))));
                    return 1;
                }))
                .then(literal("set").then(argument("value", type).executes(ctx -> {
                    ResourceKey<Level> key = ResourceWorldHelper.toRegistryKey(StringArgumentType.getString(ctx, "world"));
                    CommandSourceStack source = ctx.getSource();
                    if (ResourceWorldHelper.isNotResourceWorld(key))
                        throw new SimpleCommandExceptionType(ServerI18n.translateToLiteral(source, "message.resource_world.not_a_resource_world")).create();
                    ResourceWorldData data = WorldConfig.get(key);
                    if (data == null)
                        throw new SimpleCommandExceptionType(ServerI18n.translateToLiteral(source, "message.resource_world.unknown_resource_world")).create();
                    T value = parser.apply(ctx, "value");
                    setter.accept(data.getSettings(), value);
                    source.sendSystemMessage(ServerI18n.translateToLiteral(source, "message.resource_world.setting.set", name, String.valueOf(value)));
                    return 1;
                }))));
    }

    public static <T> void appendSettingOptional(ArgumentBuilder<CommandSourceStack, ?> builder, String name, ArgumentType<?> type, BiFunction<CommandContext<CommandSourceStack>, String, T> parser, Function<ResourceWorldData.Settings, Optional<T>> getter, BiConsumer<ResourceWorldData.Settings, T> setter) {
        builder.then(literal(name)
                .then(literal("get").executes(ctx -> {
                    ResourceKey<Level> key = ResourceWorldHelper.toRegistryKey(StringArgumentType.getString(ctx, "world"));
                    CommandSourceStack source = ctx.getSource();
                    if (ResourceWorldHelper.isNotResourceWorld(key))
                        throw new SimpleCommandExceptionType(ServerI18n.translateToLiteral(source, "message.resource_world.not_a_resource_world")).create();
                    ResourceWorldData data = WorldConfig.get(key);
                    if (data == null)
                        throw new SimpleCommandExceptionType(ServerI18n.translateToLiteral(source, "message.resource_world.unknown_resource_world")).create();
                    Optional<T> value = getter.apply(data.getSettings());
                    source.sendSystemMessage(ServerI18n.translateToLiteral(source, "message.resource_world.setting.get", name, String.valueOf(value.orElse(null))));
                    return 1;
                }))
                .then(literal("set").then(argument("value", type).executes(ctx -> {
                    ResourceKey<Level> key = ResourceWorldHelper.toRegistryKey(StringArgumentType.getString(ctx, "world"));
                    CommandSourceStack source = ctx.getSource();
                    if (ResourceWorldHelper.isNotResourceWorld(key))
                        throw new SimpleCommandExceptionType(ServerI18n.translateToLiteral(source, "message.resource_world.not_a_resource_world")).create();
                    ResourceWorldData data = WorldConfig.get(key);
                    if (data == null)
                        throw new SimpleCommandExceptionType(ServerI18n.translateToLiteral(source, "message.resource_world.unknown_resource_world")).create();
                    T value = parser.apply(ctx, "value");
                    setter.accept(data.getSettings(), value);
                    source.sendSystemMessage(ServerI18n.translateToLiteral(source, "message.resource_world.setting.set", name, String.valueOf(value)));
                    return 1;
                })))
                .then(literal("clear").executes(ctx -> {
                    ResourceKey<Level> key = ResourceWorldHelper.toRegistryKey(StringArgumentType.getString(ctx, "world"));
                    CommandSourceStack source = ctx.getSource();
                    if (ResourceWorldHelper.isNotResourceWorld(key))
                        throw new SimpleCommandExceptionType(ServerI18n.translateToLiteral(source, "message.resource_world.not_a_resource_world")).create();
                    ResourceWorldData data = WorldConfig.get(key);
                    if (data == null)
                        throw new SimpleCommandExceptionType(ServerI18n.translateToLiteral(source, "message.resource_world.unknown_resource_world")).create();
                    setter.accept(data.getSettings(), null);
                    source.sendSystemMessage(ServerI18n.translateToLiteral(source, "message.resource_world.setting.set", name, null));
                    return 1;
                })));
    }
}
