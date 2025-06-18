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
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.World;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class CommandHelper {
    public static <T> void appendSetting(ArgumentBuilder<ServerCommandSource, ?> builder, String name, ArgumentType<?> type, BiFunction<CommandContext<ServerCommandSource>, String, T> parser, Function<ResourceWorldData.Settings, T> getter, BiConsumer<ResourceWorldData.Settings, T> setter) {
        builder.then(literal(name)
                .then(literal("get").executes(ctx -> {
                    RegistryKey<World> key = ResourceWorldHelper.toRegistryKey(StringArgumentType.getString(ctx, "world"));
                    ServerCommandSource source = ctx.getSource();
                    if (ResourceWorldHelper.isNotResourceWorld(key))
                        throw new SimpleCommandExceptionType(ServerI18n.translateToLiteral(source, "message.resource_world.not_a_resource_world")).create();
                    ResourceWorldData data = WorldConfig.get(key);
                    if (data == null)
                        throw new SimpleCommandExceptionType(ServerI18n.translateToLiteral(source, "message.resource_world.unknown_resource_world")).create();
                    source.sendMessage(ServerI18n.translateToLiteral(source, "message.resource_world.setting.get", name, String.valueOf(getter.apply(data.getSettings()))));
                    return 1;
                }))
                .then(literal("set").then(argument("value", type).executes(ctx -> {
                    RegistryKey<World> key = ResourceWorldHelper.toRegistryKey(StringArgumentType.getString(ctx, "world"));
                    ServerCommandSource source = ctx.getSource();
                    if (ResourceWorldHelper.isNotResourceWorld(key))
                        throw new SimpleCommandExceptionType(ServerI18n.translateToLiteral(source, "message.resource_world.not_a_resource_world")).create();
                    ResourceWorldData data = WorldConfig.get(key);
                    if (data == null)
                        throw new SimpleCommandExceptionType(ServerI18n.translateToLiteral(source, "message.resource_world.unknown_resource_world")).create();
                    T value = parser.apply(ctx, "value");
                    setter.accept(data.getSettings(), value);
                    source.sendMessage(ServerI18n.translateToLiteral(source, "message.resource_world.setting.set", name, String.valueOf(value)));
                    return 1;
                }))));
    }
}
