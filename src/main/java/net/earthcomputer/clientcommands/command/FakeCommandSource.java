package net.earthcomputer.clientcommands.command;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.RegistryAccess;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

public class FakeCommandSource extends CommandSourceStack {
    @SuppressWarnings("ConstantConditions")
    public FakeCommandSource(LocalPlayer player) {
        super(player, player.position(), player.getRotationVector(), null, 314159265, player.getScoreboardName(), player.getName(), null, player);
    }

    @Override
    public @NotNull Collection<String> getOnlinePlayerNames() {
        return Objects.requireNonNull(Minecraft.getInstance().getConnection()).getOnlinePlayers()
                .stream().map(e -> e.getProfile().getName()).collect(Collectors.toList());
    }

    @Override
    public @NotNull RegistryAccess registryAccess() {
        return Objects.requireNonNull(Minecraft.getInstance().getConnection()).registryAccess();
    }
}
