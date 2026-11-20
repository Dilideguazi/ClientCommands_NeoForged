package net.earthcomputer.clientcommands.mixin.commands.generic;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Component.class)
public interface ComponentMixin {
    @WrapOperation(method = "translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;", at = @At(value = "NEW", target = "(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/contents/TranslatableContents;"))
    private static TranslatableContents onTranslatable(String key, String fallback, Object[] args, Operation<TranslatableContents> original) {
        Object[] objs = new Object[args.length];

        for (int i = 0;i < args.length;i++) {
            if (args[i] instanceof Identifier) {
                objs[i] = args[i].toString();
            } else {
                objs[i] = args[i];
            }
        }

        return new TranslatableContents(key, fallback, objs);
    }
}
