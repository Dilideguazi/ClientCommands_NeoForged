package net.earthcomputer.clientcommands.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.neoforged.fml.loading.FMLLoader;
import org.spongepowered.asm.mixin.MixinEnvironment;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class AuditMixinsCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        boolean enableAuditMixins = !FMLLoader.isProduction() || Boolean.getBoolean("clientcommands.enableAuditMixins");
        if (!enableAuditMixins) {
            return;
        }
        dispatcher.register(literal("cauditmixins").executes(ctx -> auditMixins()));
    }

    private static int auditMixins() {
        MixinEnvironment.getCurrentEnvironment().audit();
        return Command.SINGLE_SUCCESS;
    }
}
