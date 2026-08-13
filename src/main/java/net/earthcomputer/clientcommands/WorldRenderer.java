package net.earthcomputer.clientcommands;

import com.mojang.blaze3d.vertex.PoseStack;
import net.earthcomputer.clientcommands.features.WorldRendererDataHolder;
import net.earthcomputer.clientcommands.render.RenderQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

@OnlyIn(Dist.CLIENT)
//@Mod.EventBusSubscriber(modid = Clientcommands_neoforged.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
@Deprecated
public class WorldRenderer {
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        poseStack.pushPose();

        Vec3 cameraPos = event.getCamera().getPosition();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        RenderQueue.render(RenderQueue.Layer.ON_TOP, Objects.requireNonNull(bufferSource).getBuffer(RenderQueue.NO_DEPTH_LAYER), poseStack, WorldRendererDataHolder.getTickDelta());

        poseStack.popPose();
    }
}
