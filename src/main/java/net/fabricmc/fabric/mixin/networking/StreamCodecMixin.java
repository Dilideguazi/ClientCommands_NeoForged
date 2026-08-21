package net.fabricmc.fabric.mixin.networking;

import net.fabricmc.fabric.impl.networking.RegistrationPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;
import net.neoforged.neoforge.network.payload.MinecraftRegisterPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.function.Function;

@Mixin(StreamCodec.class)
public interface StreamCodecMixin<B, V> extends StreamEncoder<B, V>, StreamDecoder<B, V> {
    @Inject(method = "map", at = @At("HEAD"), cancellable = true)
    private <O> void onMap(Function<? super V, ? extends O> _to, Function<? super O, ? extends V> from, CallbackInfoReturnable<StreamCodec<B, O>> cir) {
        cir.setReturnValue(new StreamCodec<>() {
            @Override
            public O decode(B input) {
                return _to.apply(StreamCodecMixin.this.decode(input));
            }

            @Override
            public void encode(B output, O value) {
                V res = from.apply(value);
                if (res instanceof RegistrationPayload payload) {
                    // noinspection all
                    StreamCodecMixin.this.encode(output, (V) new MinecraftRegisterPayload(new HashSet<>(payload.channels())));
                } else {
                    StreamCodecMixin.this.encode(output, res);
                }
            }
        });
        cir.cancel();
    }
}
