package net.earthcomputer.clientcommands.command;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

import static dev.xpple.clientarguments.arguments.CItemStackArgumentType.getCItemStackArgument;
import static dev.xpple.clientarguments.arguments.CItemStackArgumentType.itemStack;
import static net.earthcomputer.clientcommands.command.ClientCommandHelper.getFlag;
import static net.earthcomputer.clientcommands.command.ClientCommandHelper.withFlags;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class TooltipCommand {

    private static final int FLAG_ADVANCED = 1;

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        var ctooltip = dispatcher.register(literal("ctooltip"));
        dispatcher.register(literal("ctooltip")
            .then(literal("--advanced")
                .redirect(ctooltip, ctx -> withFlags(ctx.getSource(), FLAG_ADVANCED, true)))
            .then(literal("held")
                .executes(ctx -> showTooltip(ctx.getSource(), ctx.getSource().getPlayer().getMainHandItem(), "held")))
            .then(literal("stack")
                .then(argument("stack", itemStack(registryAccess))
                    .executes(ctx -> showTooltip(ctx.getSource(), getCItemStackArgument(ctx, "stack").createItemStack(1, false), "stack")))));
    }

    private static int showTooltip(FabricClientCommandSource source, ItemStack stack, String type) {
        source.sendFeedback(Component.translatable("commands.ctooltip.header." + type));

        TooltipFlag context = getFlag(source, FLAG_ADVANCED) ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL;

        List<Component> tooltip = stack.getTooltipLines(source.getPlayer(), context);
        for (Component line : tooltip) {
            source.sendFeedback(line);
        }

        return tooltip.size();
    }
}
