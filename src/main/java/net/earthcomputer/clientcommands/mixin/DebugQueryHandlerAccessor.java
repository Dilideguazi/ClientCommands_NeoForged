package net.earthcomputer.clientcommands.mixin;

import net.minecraft.client.DebugQueryHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DebugQueryHandler.class)
public interface DebugQueryHandlerAccessor {
    @Accessor
    int getTransactionId();

    @Accessor
    void setTransactionId(int transactionId);
}
