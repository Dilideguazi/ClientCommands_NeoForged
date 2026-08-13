package net.earthcomputer.clientcommands.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.earthcomputer.clientcommands.features.WorldRendererDataHolder;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
@Deprecated
public class MixinWorldRenderer {
    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void beforeRender(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
        WorldRendererDataHolder.setTickDelta(tickDelta);
    }
}
