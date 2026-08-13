package net.earthcomputer.clientcommands.mixin;

import net.earthcomputer.clientcommands.features.PlayerRandCracker;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(MushroomCow.class)
public class MixinMooshroomEntity {
    @Unique
    Player player;

    @Unique
    InteractionHand hand;

    @Inject(method = "mobInteract", at = @At("HEAD"))
    private void onMobInteract(Player pPlayer, InteractionHand pHand, CallbackInfoReturnable<InteractionResult> cir){
        this.player = pPlayer;
        this.hand = pHand;
    }

    @Inject(method = "shearInternal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isClientSide()Z"))
    public void onInteract(SoundSource pCategory, CallbackInfoReturnable<List<ItemStack>> cir) {
        PlayerRandCracker.onItemDamage(1, player, player.getItemInHand(hand));
    }

}
