package net.earthcomputer.clientcommands.mixin.rngevents;

import com.google.common.base.Objects;
import net.earthcomputer.clientcommands.features.PlayerRandCracker;
import net.earthcomputer.clientcommands.util.CUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FrostedIceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.common.CommonHooks;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommonHooks.class)
public class CommonHooksMixin {
    @Inject(method = "onLivingBreathe", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/Level;isClientSide:Z", opcode = Opcodes.GETFIELD))
    private static void testFrostWalker(LivingEntity entity, int consumeAirAmount, int refillAirAmount, CallbackInfo ci) {
        if (!(entity instanceof LocalPlayer)) {
            return;
        }

        BlockPos pos = entity.blockPosition();
        if (!Objects.equal(pos, entity.lastPos)) {
            entity.lastPos = pos;
            if (entity.onGround()) {
                int frostWalkerLevel = CUtil.getEnchantmentLevel(Enchantments.FROST_WALKER, entity);
                if (frostWalkerLevel > 0) {
                    BlockState frostedIce = Blocks.FROSTED_ICE.defaultBlockState();
                    int radius = Math.min(16, frostWalkerLevel + 2);
                    for (BlockPos offsetPos : BlockPos.betweenClosed(pos.offset(-radius, -1, -radius), pos.offset(radius, -1, radius))) {
                        if (offsetPos.closerToCenterThan(entity.position(), radius)) {
                            BlockState offsetState = entity.level().getBlockState(offsetPos);
                            if (offsetState == FrostedIceBlock.meltsInto() && entity.level().isUnobstructed(frostedIce, offsetPos, CollisionContext.empty())) {
                                if (entity.level().isEmptyBlock(offsetPos.above())) {
                                    PlayerRandCracker.onFrostWalker();
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
