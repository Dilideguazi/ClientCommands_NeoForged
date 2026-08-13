package net.earthcomputer.clientcommands.mixin;

import net.earthcomputer.clientcommands.interfaces.IFlaggedCommandSource;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClientSuggestionProvider.class)
public class MixinClientCommandSource implements IFlaggedCommandSource {

    @Shadow
    @Final
    private ClientPacketListener connection;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Unique
    private int flags;

    @Override
    public int getFlags() {
        return this.flags;
    }

    @Override
    public IFlaggedCommandSource withFlags(int flags) {
        MixinClientCommandSource source = (MixinClientCommandSource) (Object) new ClientSuggestionProvider(this.connection, this.minecraft);
        source.flags = flags;

        return source;
    }

}
