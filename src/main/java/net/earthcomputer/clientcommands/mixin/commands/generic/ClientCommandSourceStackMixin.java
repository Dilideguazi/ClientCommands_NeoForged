package net.earthcomputer.clientcommands.mixin.commands.generic;

import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.suggestion.Suggestion;
import net.earthcomputer.clientcommands.command.Flag;
import net.earthcomputer.clientcommands.interfaces.IClientSuggestionsProvider;
import net.earthcomputer.clientcommands.interfaces.IClientSuggestionsProvider_Alias;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.ClientCommandSourceStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Neo Edit: throws ClassCastException when executing commands
@Mixin(ClientCommandSourceStack.class)
public abstract class ClientCommandSourceStackMixin extends CommandSourceStack implements IClientSuggestionsProvider, IClientSuggestionsProvider_Alias {
    @Unique
    private final Set<String> seenAliases = new HashSet<>();

    @Unique
    private ImmutableMap<Flag<?>, Object> flags = ImmutableMap.of();

    public ClientCommandSourceStackMixin(CommandSource source, Vec3 position, Vec2 rotation, ServerLevel level, PermissionSet permissions, String textName, Component displayName, MinecraftServer server, @Nullable Entity entity) {
        super(source, position, rotation, level, permissions, textName, displayName, server, entity);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T clientcommands_getFlag(Flag<T> flag) {
        return (T) this.flags.getOrDefault(flag, flag.getDefaultValue());
    }

    @Override
    public <T> IClientSuggestionsProvider clientcommands_withFlag(Flag<T> flag, T value) {
        ClientCommandSourceStackMixin source = this;
        source.flags = ImmutableMap.<Flag<?>, Object>builderWithExpectedSize(this.flags.size() + 1).putAll(this.flags).put(flag, value).build();
        return source;
    }

    @Override
    @Nullable
    public List<Suggestion> clientcommands_filterSuggestions(List<Suggestion> suggestions) {
        if (flags.isEmpty()) {
            return null;
        } else {
            return suggestions.stream().filter(suggestion -> {
                String text = suggestion.getText();
                return !Flag.isFlag(text) || flags.keySet().stream().noneMatch(arg -> !arg.isRepeatable() && (text.equals(arg.getFlag()) || text.equals(arg.getShortFlag())));
            }).toList();
        }
    }

    @Override
    public void clientcommands_addSeenAlias(String alias) {
        seenAliases.add(alias);
    }

    @Override
    public void clientcommands_removeSeenAlias(String alias) {
        seenAliases.remove(alias);
    }

    @Override
    public boolean clientcommands_isAliasSeen(String alias) {
        return seenAliases.contains(alias);
    }
}
