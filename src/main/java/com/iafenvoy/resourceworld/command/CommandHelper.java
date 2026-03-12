package com.iafenvoy.resourceworld.command;

import com.iafenvoy.resourceworld.config.ResourceWorldData;
import com.iafenvoy.resourceworld.config.WorldConfig;
import com.iafenvoy.resourceworld.data.ResourceWorldHelper;
import com.iafenvoy.resourceworld.util.ObjectUtil;
import com.iafenvoy.server.i18n.ServerI18n;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.util.Locale;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public final class CommandHelper {
    @NotNull
    public static ResourceKey<Level> createKey(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String id = StringArgumentType.getString(ctx, "world");
        ObjectUtil.assertOrThrow(WorldConfig.get(ResourceWorldHelper.toRegistryKey(id)) == null, () -> ExceptionTypes.DUPLICATE_ID.create(ctx.getSource()));
        return ResourceWorldHelper.toRegistryKey(id);
    }

    @NotNull
    public static ResourceKey<Level> getKeyChecked(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ResourceKey<Level> key = ResourceWorldHelper.toRegistryKey(StringArgumentType.getString(ctx, "world"));
        ObjectUtil.assertOrThrow(ResourceWorldHelper.isResourceWorld(key), () -> ExceptionTypes.NOT_A_RESOURCE_WORLD.create(ctx.getSource()));
        return key;
    }

    @NotNull
    public static ResourceWorldData getDataChecked(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return ObjectUtil.nonNullOrThrow(WorldConfig.get(getKeyChecked(ctx)), () -> ExceptionTypes.NOT_A_RESOURCE_WORLD.create(ctx.getSource()));
    }

    @NotNull
    public static ServerLevel getLevelChecked(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        return ObjectUtil.nonNullOrThrow(source.getServer().getLevel(getKeyChecked(ctx)), () -> ExceptionTypes.NOT_A_RESOURCE_WORLD.create(source));
    }

    public static void sendMessage(CommandSourceStack source, String key, String... format) {
        ServerI18n.sendMessage(source, String.format(Locale.ROOT, "message.resource_world.%s", key), format);
    }
}
