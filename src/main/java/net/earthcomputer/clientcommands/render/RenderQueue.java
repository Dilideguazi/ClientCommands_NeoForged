package net.earthcomputer.clientcommands.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;

public class RenderQueue {
    private static int tickCounter = 0;
    private static final List<AddQueueEntry> addQueue = new ArrayList<>();
    private static final List<RemoveQueueEntry> removeQueue = new ArrayList<>();
    private static final EnumMap<Layer, Map<Object, Shape>> queue = new EnumMap<>(Layer.class);

    private static final RenderPipeline NO_DEPTH_PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_COLOR_SNIPPET)
        .withLocation(ResourceLocation.fromNamespaceAndPath("clientcommands", "pipeline/no_depth"))
        .withVertexShader("core/position_color")
        .withFragmentShader("core/position_color")
        .withCull(false)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.DEBUG_LINE_STRIP)
        .build();
    public static final RenderType NO_DEPTH_LAYER = RenderType.create("clientcommands_no_depth", 3 * 512, NO_DEPTH_PIPELINE, RenderType.CompositeState.builder()
        .setLayeringState(RenderType.VIEW_OFFSET_Z_LAYERING)
        .setLineState(new RenderType.LineStateShard(OptionalDouble.of(Line.THICKNESS)))
        .createCompositeState(true));

    static {
        ClientTickEvents.START_CLIENT_TICK.register(RenderQueue::tick);
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            context.matrixStack().pushPose();

            Vec3 cameraPos = context.camera().getPosition();
            context.matrixStack().translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
            RenderQueue.render(RenderQueue.Layer.ON_TOP, Objects.requireNonNull(context.consumers()).getBuffer(RenderQueue.NO_DEPTH_LAYER), context.matrixStack(), context.tickCounter().getRealtimeDeltaTicks());

            context.matrixStack().popPose();
        });

        RenderPipelines.register(NO_DEPTH_PIPELINE);
    }

    public static void register() {
        // load class
    }

    public static void add(Layer layer, Object key, Shape shape, int life) {
        addQueue.add(new AddQueueEntry(layer, key, shape, life));
    }

    public static void addCuboid(Layer layer, Object key, Vec3 from, Vec3 to, int color, int life) {
        add(layer, key, new Cuboid(from, to, color), life);
    }

    public static void addCuboid(Layer layer, Object key, AABB cuboid, int color, int life) {
        add(layer, key, new Cuboid(cuboid, color), life);
    }

    public static void addLine(Layer layer, Object key, Vec3 from, Vec3 to, int color, int life) {
        add(layer, key, new Line(from, to, color), life);
    }

    public static void remove(Layer layer, Object key) {
        removeQueue.add(new RemoveQueueEntry(layer, key));
    }

    private static void doAdd(AddQueueEntry entry) {
        Map<Object, Shape> shapes = queue.computeIfAbsent(entry.layer(), k -> new LinkedHashMap<>());
        Shape oldShape = shapes.get(entry.key());
        if (oldShape != null) {
            entry.shape().prevPos = oldShape.prevPos;
        } else {
            entry.shape().prevPos = entry.shape().getPos();
        }
        entry.shape().deathTime = tickCounter + entry.life();
        shapes.put(entry.key(), entry.shape());
    }

    private static void tick(Minecraft mc) {
        for (RemoveQueueEntry entry : removeQueue) {
            Map<Object, Shape> shapes = queue.get(entry.layer());
            if (shapes != null) {
                shapes.remove(entry.key());
            }
        }
        removeQueue.clear();

        queue.values().forEach(shapes -> shapes.values().forEach(shape -> shape.prevPos = shape.getPos()));
        tickCounter++;
        for (AddQueueEntry entry : addQueue) {
            doAdd(entry);
        }
        addQueue.clear();
        for (Map<Object, Shape> shapes : queue.values()) {
            Iterator<Shape> itr = shapes.values().iterator();
            while (itr.hasNext()) {
                Shape shape = itr.next();
                if (tickCounter == shape.deathTime) {
                    itr.remove();
                }
                shape.tick();
            }
        }
    }

    public static void render(Layer layer, VertexConsumer vertexConsumer, PoseStack poseStack, float delta) {
        if (!queue.containsKey(layer)) {
            return;
        }
        queue.get(layer).values().forEach(shape -> shape.render(poseStack, vertexConsumer, delta));
    }

    public enum Layer {
        ON_TOP
    }

    private record AddQueueEntry(Layer layer, Object key, Shape shape, int life) {}

    private record RemoveQueueEntry(Layer layer, Object key) {}
}
