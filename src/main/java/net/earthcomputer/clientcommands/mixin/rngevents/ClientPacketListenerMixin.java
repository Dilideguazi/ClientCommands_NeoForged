package net.earthcomputer.clientcommands.mixin.rngevents;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import net.earthcomputer.clientcommands.command.AliasCommand;
import net.earthcomputer.clientcommands.features.PlayerRandCracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.LastSeenMessagesTracker;
import net.minecraft.network.chat.SignableCommand;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.chat.SignedMessageChain;
import net.minecraft.network.protocol.game.ServerboundChatCommandSignedPacket;
import net.minecraft.util.Crypt;
import net.neoforged.neoforge.client.ClientCommandHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Instant;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin extends ClientCommonPacketListenerImpl {
    @Shadow
    private LastSeenMessagesTracker lastSeenMessages;

    @Shadow
    protected abstract ParseResults<SharedSuggestionProvider> parseCommand(String command);

    @Shadow
    private SignedMessageChain.Encoder signedMessageEncoder;

    protected ClientPacketListenerMixin(Minecraft minecraft, Connection connection, CommonListenerCookie commonListenerCookie) {
        super(minecraft, connection, commonListenerCookie);
    }

    @Inject(method = "sendCommand", at = @At("HEAD"), cancellable = true)
    private void onSendCommand(String command, CallbackInfo ci) {
        String convertedAlias = AliasCommand.convertAlias(command).replace("/", "");
        StringReader reader = new StringReader(command);
        String commandName = reader.canRead() ? reader.readUnquotedString() : "";
        if (!(convertedAlias.equals(command))) {
            ci.cancel();
            reader = new StringReader(convertedAlias);
            commandName = reader.canRead() ? reader.readUnquotedString() : "";
            if (!(ClientCommandHandler.runCommand(convertedAlias))) {
                SignableCommand<SharedSuggestionProvider> signablecommand = SignableCommand.of(this.parseCommand(convertedAlias));
                Instant instant = Instant.now();
                long i = Crypt.SaltSupplier.getLong();
                LastSeenMessagesTracker.Update lastseenmessagestracker$update = this.lastSeenMessages.generateAndApplyUpdate();
                ArgumentSignatures argumentsignatures = ArgumentSignatures.signCommand(signablecommand, p_247875_ -> {
                    SignedMessageBody signedmessagebody = new SignedMessageBody(p_247875_, instant, i, lastseenmessagestracker$update.lastSeen());
                    return this.signedMessageEncoder.pack(signedmessagebody);
                });
                this.send(new ServerboundChatCommandSignedPacket(convertedAlias, instant, i, argumentsignatures, lastseenmessagestracker$update.update()));
            }
        }
        if ("give".equals(commandName)) {
            PlayerRandCracker.onGiveCommand();
        }
    }
}
