package net.earthcomputer.clientcommands.mixin.commands.translate;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Neo Edit: Unknown (maybe handled by NeoForge)
@Mixin(Component.class)
public interface ComponentMixin {
    @Inject(method = "translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;", at = @At("HEAD"), cancellable = true)
    private static void translatable(String key, Object[] args, CallbackInfoReturnable<MutableComponent> cir) {
        Object[] objs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Identifier) {
                objs[i] = args[i].toString();
            } else {
                objs[i] = args[i];
            }
        }

        cir.setReturnValue(MutableComponent.create(new TranslatableContents(key, null, objs)));
    }
}
