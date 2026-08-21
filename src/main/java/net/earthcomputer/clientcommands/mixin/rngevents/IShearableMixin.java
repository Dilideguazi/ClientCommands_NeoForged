package net.earthcomputer.clientcommands.mixin.rngevents;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import net.earthcomputer.clientcommands.features.PlayerRandCracker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.IShearable;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

// Neo Edit: Handled by NeoForge
@Mixin(IShearable.class)
public interface IShearableMixin {
    @Definition(id = "ServerLevel", type = ServerLevel.class)
    @Expression("? instanceof ServerLevel")
    @Inject(method = "onSheared", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void onSheared(@Nullable Player player, ItemStack item, Level level, BlockPos pos, CallbackInfoReturnable<List<ItemStack>> cir) {
        assert player != null;
        PlayerRandCracker.onItemDamage(1, player, item);
    }
}
