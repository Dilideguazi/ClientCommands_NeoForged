package net.earthcomputer.clientcommands.mixin;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import net.cortex.clientAddon.cracker.SeedCracker;
import net.earthcomputer.clientcommands.ClientcommandsDataQueryHandler;
import net.earthcomputer.clientcommands.Configs;
import net.earthcomputer.clientcommands.ServerBrandManager;
import net.earthcomputer.clientcommands.command.AliasCommand;
import net.earthcomputer.clientcommands.features.FishingCracker;
import net.earthcomputer.clientcommands.features.PlayerRandCracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.network.chat.LastSeenMessagesTracker;
import net.minecraft.network.chat.SignableCommand;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.chat.SignedMessageChain;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.util.Crypt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ClientCommandHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.time.Instant;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPlayNetworkHandler implements ClientcommandsDataQueryHandler.IClientPlayNetworkHandler {
    @Shadow @Final private Minecraft minecraft;

    @Shadow public abstract void send(Packet<?> packet);

    @Shadow protected abstract ParseResults<SharedSuggestionProvider> parseCommand(String command);

    @Shadow private LastSeenMessagesTracker lastSeenMessages;

    @Shadow private SignedMessageChain.Encoder signedMessageEncoder;

    @Unique
    private final ClientcommandsDataQueryHandler ccDataQueryHandler = new ClientcommandsDataQueryHandler((ClientPacketListener) (Object) this);

    @Inject(method = "handleAddEntity", at = @At("TAIL"))
    public void onOnEntitySpawn(ClientboundAddEntityPacket packet, CallbackInfo ci) {
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }

        SeedCracker.onEntityCreation(packet);

        if (FishingCracker.canManipulateFishing()) {
            if (packet.getData() == player.getId() && packet.getType() == EntityType.FISHING_BOBBER) {
                FishingCracker.processBobberSpawn(packet.getUUID(), new Vec3(packet.getX(), packet.getY(), packet.getZ()), new Vec3(packet.getXa(), packet.getYa(), packet.getZa()));
            }
        }
    }

    @Inject(method = "handleAddEntity", at = @At("HEAD"))
    public void onOnEntitySpawnPre(ClientboundAddEntityPacket packet, CallbackInfo ci) {
        // Called on network thread first, FishingCracker.waitingForFishingRod

        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }

        if (!FishingCracker.canManipulateFishing() || packet.getData() != player.getId() || packet.getType() != EntityType.FISHING_BOBBER) {
            return;
        }

        FishingCracker.onFishingBobberEntity();
    }

    @Inject(method = "sendCommand", at = @At("HEAD"), cancellable = true)
    private void onSendChatCommand(String command, CallbackInfo ci) {
        try {
            String convertedAlias = AliasCommand.convertAlias(command).replace("/", "");
            StringReader reader = new StringReader(convertedAlias);
            String commandName = reader.canRead() ? reader.readUnquotedString() : "";
            Instant instant = Instant.now();
            long i = Crypt.SaltSupplier.getLong();
            LastSeenMessagesTracker.Update lastseenmessagestracker$update = this.lastSeenMessages.generateAndApplyUpdate();
            ArgumentSignatures argumentsignatures = ArgumentSignatures.signCommand(SignableCommand.of(this.parseCommand(command)), (p_247875_) -> {
                SignedMessageBody signedmessagebody = new SignedMessageBody(p_247875_, instant, i, lastseenmessagestracker$update.lastSeen());
                return this.signedMessageEncoder.pack(signedmessagebody);
            });

            if (!(convertedAlias.equals(command))) {
                ci.cancel();
                if (!(ClientCommandHandler.runCommand(convertedAlias))) {
                    this.send(new ServerboundChatCommandPacket(convertedAlias, instant, i, argumentsignatures, lastseenmessagestracker$update.update()));
                }
            }
            if ("give".equals(commandName)) {
                PlayerRandCracker.onGiveCommand();
            }
        } catch (NullPointerException ignored) {
        }
    }

    @Inject(method = "sendUnsignedCommand", at = @At("HEAD"), cancellable = true)
    private void onSendUnsignedCommand(String pCommand, CallbackInfoReturnable<Boolean> cir) {
        try {
            boolean result = ClientCommandHandler.runCommand(pCommand);
            if (result) {
                cir.setReturnValue(true);
                cir.cancel();
            }
        } catch (Exception e) {
            //noinspection all
            e.printStackTrace();
        }
    }

    @Inject(method = "handleAddExperienceOrb", at = @At("TAIL"))
    public void onOnExperienceOrbSpawn(ClientboundAddExperienceOrbPacket packet, CallbackInfo ci) {
        if (FishingCracker.canManipulateFishing()) {
            FishingCracker.processExperienceOrbSpawn(packet.getX(), packet.getY(), packet.getZ(), packet.getValue());
        }
    }

    @Inject(method = "handleSetTime", at = @At("HEAD"))
    private void onOnWorldTimeUpdatePre(CallbackInfo ci) {
        if (Configs.getFishingManipulation().isEnabled() && !Minecraft.getInstance().isSameThread()) {
            FishingCracker.onTimeSync();
        }
    }

    @Inject(method = "handleCustomPayload", at = @At("TAIL"))
    public void onOnCustomPayload(ClientboundCustomPayloadPacket packet, CallbackInfo ci) {
        if (ClientboundCustomPayloadPacket.BRAND.equals(packet.getIdentifier())) {
            assert this.minecraft.player != null;
            ServerBrandManager.setServerBrand(this.minecraft.player.getServerBrand());
        }
    }

    @Inject(method = "handleTagQueryPacket", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/util/thread/BlockableEventLoop;)V", shift = At.Shift.AFTER), cancellable = true)
    private void onOnNbtQueryResponse(ClientboundTagQueryPacket packet, CallbackInfo ci) {
        if (ccDataQueryHandler.handleQueryResponse(packet.getTransactionId(), packet.getTag())) {
            ci.cancel();
        }
    }

    @Override
    public ClientcommandsDataQueryHandler clientcommands_getCCDataQueryHandler() {
        return ccDataQueryHandler;
    }
}
