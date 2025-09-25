package net.earthcomputer.clientcommands.util;

import com.demonwav.mcdev.annotations.Translatable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CComponentUtil {
    private static final Map<UUID, Callback> callbacks = new ConcurrentHashMap<>();

    private CComponentUtil() {
    }

    public static Component getLookCoordsTextComponent(BlockPos pos) {
        return getCommandTextComponent(Component.translatable("commands.client.blockpos", pos.getX(), pos.getY(), pos.getZ()),
            String.format("/clook block %d %d %d", pos.getX(), pos.getY(), pos.getZ()));
    }

    public static Component getLookCoordsTextComponent(MutableComponent component, BlockPos pos) {
        return getCommandTextComponent(component, String.format("/clook block %d %d %d", pos.getX(), pos.getY(), pos.getZ()));
    }

    public static Component getGlowCoordsTextComponent(BlockPos pos) {
        return getCommandTextComponent(Component.translatable("commands.client.blockpos", pos.getX(), pos.getY(), pos.getZ()),
            String.format("/cglow block %d %d %d 10", pos.getX(), pos.getY(), pos.getZ()));
    }

    public static Component getGlowButtonTextComponent(BlockPos pos) {
        return getCommandTextComponent(Component.translatable("commands.client.glow"), String.format("/cglow block %d %d %d 60", pos.getX(), pos.getY(), pos.getZ()));
    }

    public static Component getGlowButtonTextComponent(Entity entity) {
        return getCommandTextComponent(Component.translatable("commands.client.glow"), "/cglow entities " + entity.getStringUUID());
    }

    public static Component getCommandTextComponent(@Translatable String translationKey, String command) {
        return getCommandTextComponent(Component.translatable(translationKey), command);
    }

    public static Component getCommandTextComponent(MutableComponent component, String command) {
        return component.withStyle(style -> style.applyFormat(ChatFormatting.UNDERLINE)
            .withClickEvent(new ClickEvent.RunCommand(command))
            .withHoverEvent(new HoverEvent.ShowText(Component.literal(command))));
    }

    public static ClickEvent callbackClickEvent(Runnable runnable) {
        return callbackClickEvent(runnable, 60_000_000_000L); // 1 minute timeout
    }

    public static ClickEvent callbackClickEvent(Runnable callback, long timeoutNanos) {
        UUID callbackId = UUID.randomUUID();
        callbacks.put(callbackId, new Callback(callback, System.nanoTime() + timeoutNanos));
        return new ClickEvent.RunCommand("/ccallback " + callbackId);
    }

    public static boolean runCallback(UUID callbackId) {
        Callback callback = callbacks.get(callbackId);
        if (callback == null) {
            return false;
        }
        callback.callback.run();
        return true;
    }

    private record Callback(Runnable callback, long timeout) {
        static {
            Thread.ofVirtual().name("Clientcommands callback cleanup").start(() -> {
                while (true) {
                    try {
                        // This isn't really "busy-waiting" since the wait time is so long
                        //noinspection BusyWait
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        break;
                    }
                    long now = System.nanoTime();
                    callbacks.values().removeIf(callback -> now - callback.timeout <= 0);
                }
            });
        }
    }
}
