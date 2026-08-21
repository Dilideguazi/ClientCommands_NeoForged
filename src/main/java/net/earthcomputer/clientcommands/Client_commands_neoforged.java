package net.earthcomputer.clientcommands;

import com.mojang.logging.LogUtils;
import dev.xpple.betterconfig.BetterConfig;
import dev.xpple.betterconfig.BetterConfigClient;
import dev.xpple.clientarguments.ClientArguments;
import dev.xpple.simplewaypoints.SimpleWaypoints;
import net.earthcomputer.clientcommands.command.CreativeTabCommand;
import net.earthcomputer.clientcommands.server.ClientCommandsServer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.impl.client.event.lifecycle.ClientLifecycleEventsImpl;
import net.fabricmc.fabric.impl.client.rendering.hud.HudStatusBarHeightRegistryImpl;
import net.fabricmc.fabric.impl.event.lifecycle.LifecycleEventsImpl;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

@Mod(Client_commands_neoforged.MOD_ID)
public class Client_commands_neoforged {
    public static final String MOD_ID = "client_commands_neoforged";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static PayloadRegistrar registerar = new PayloadRegistrar("1");

    public Client_commands_neoforged(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);

        CreativeTabCommand.registerCreativeTabs();
        CreativeTabCommand.CREATIVE_MODE_TABS.register(modEventBus);
        ArgumentTypeRegistry.ARGUMENT_TYPE_INFOS.register(modEventBus);

        LOGGER.info(MOD_ID + " Mod initialized successfully.");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LifecycleEventsImpl.onInitialize();
        ClientCommandsServer.onInitialize();
    }

    @EventBusSubscriber(modid = MOD_ID, value = Dist.DEDICATED_SERVER)
    public static class ServerModEvents {
        @SubscribeEvent
        public static void onServerSetup(FMLDedicatedServerSetupEvent event) {
            BetterConfig.onInitializeServer();
        }
    }

    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            ClientLifecycleEventsImpl.onInitializeClient();
            HudStatusBarHeightRegistryImpl.onInitializeClient();
            ClientArguments.onInitializeClient();
            BetterConfigClient.onInitializeClient();
            SimpleWaypoints.onInitializeClient();
            ClientCommands.onInitializeClient();
        }
    }
}
