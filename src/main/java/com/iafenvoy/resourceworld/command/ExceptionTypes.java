package com.iafenvoy.resourceworld.command;

import com.iafenvoy.server.i18n.ServerI18nExceptionType;

import java.util.Locale;

final class ExceptionTypes {
    static final ServerI18nExceptionType NOT_A_RESOURCE_WORLD = create("not_a_resource_world");
    static final ServerI18nExceptionType DUPLICATE_ID = create("duplicate_id");
    static final ServerI18nExceptionType HOME_COMMAND_BANNED = create("home_command_banned");
    static final ServerI18nExceptionType RESETTING = create("resetting");
    static final ServerI18nExceptionType DISABLED = create("disabled");
    static final ServerI18nExceptionType TELEPORT_COOLDOWN = create("teleport_cooldown");
    static final ServerI18nExceptionType CANNOT_FIND_POSITION = create("cannot_find_position");

    private static ServerI18nExceptionType create(String key) {
        return new ServerI18nExceptionType(String.format(Locale.ROOT, "message.resource_world.%s", key));
    }
}