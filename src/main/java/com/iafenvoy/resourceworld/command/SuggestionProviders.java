package com.iafenvoy.resourceworld.command;

import com.iafenvoy.resourceworld.config.WorldConfig;
import com.iafenvoy.resourceworld.util.RLUtil;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;

final class SuggestionProviders {
    static final SuggestionProvider<CommandSourceStack> WORLD = net.minecraft.commands.synchronization.SuggestionProviders.register(RLUtil.id("world"), (context, builder) -> WorldConfig.appendSuggestions(builder));
    static final SuggestionProvider<CommandSourceStack> DIMENSIONS = net.minecraft.commands.synchronization.SuggestionProviders.register(RLUtil.id("dimensions"), (context, builder) -> context.getSource() instanceof CommandSourceStack source ? SharedSuggestionProvider.suggestResource(source.registryAccess().registryOrThrow(Registries.LEVEL_STEM).holders().map(Holder.Reference::key).map(ResourceKey::location), builder) : context.getSource().customSuggestion(context));
}
