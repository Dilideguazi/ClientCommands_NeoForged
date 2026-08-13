package net.earthcomputer.clientcommands;

import com.mojang.logging.LogUtils;
import dev.xpple.betterconfig.BetterConfig;
import dev.xpple.betterconfig.BetterConfigClient;
import dev.xpple.clientarguments.ClientArguments;
import net.earthcomputer.clientcommands.command.ItemGroupCommand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Clientcommands_neoforged.MOD_ID)
@OnlyIn(Dist.CLIENT)
public class Clientcommands_neoforged {

    public static final String MOD_ID = "client_commands_neoforged";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Clientcommands_neoforged(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        MinecraftForge.EVENT_BUS.register(this);

        ItemGroupCommand.registerItemGroups();
        ItemGroupCommand.CREATIVE_MODE_TABS.register(modEventBus);

//        ArgumentTypeRegistry.ARGUMENT_TYPE_INFOS.register(modEventBus);

        LOGGER.info("Clientcommands NeoForged Mod Initialized Successfully.");
    }

//    private void commonSetup(final FMLCommonSetupEvent event) {
//
//    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        BetterConfig.onInitializeServer();
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            ClientArguments.onInitializeClient();
            BetterConfigClient.onInitializeClient();
            ClientCommands.onInitializeClient();

//            event.enqueueWork(() -> MinecraftForge.EVENT_BUS.register(WorldRenderer.class));
        }
    }
}
