package net.earthcomputer.clientcommands.task;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.phys.Vec3;

import java.util.Iterator;

public abstract class RenderDistanceScanTask extends SimpleTask {
    private static final long MAX_SCAN_TIME = 30_000_000L; // 30ms

    private final boolean keepSearching;

    private Iterator<BlockPos.MutableBlockPos> squarePosIterator;

    protected RenderDistanceScanTask(boolean keepSearching) {
        this.keepSearching = keepSearching;
    }

    @Override
    public void initialize() {
        squarePosIterator = createIterator();
    }

    @Override
    public boolean condition() {
        return squarePosIterator != null;
    }

    @Override
    protected void onTick() {
        Entity cameraEntity = Minecraft.getInstance().cameraEntity;
        if (cameraEntity == null) {
            _break();
            return;
        }

        long startTime = System.nanoTime();
        while (squarePosIterator.hasNext()) {
            BlockPos chunkPosAsBlockPos = squarePosIterator.next();
            ChunkPos chunkPos = new ChunkPos(chunkPosAsBlockPos.getX(), chunkPosAsBlockPos.getZ());

            if (canScanChunk(cameraEntity, chunkPos)) {
                scanChunk(cameraEntity, chunkPos);
            }

            if (System.nanoTime() - startTime > MAX_SCAN_TIME) {
                // wait till next tick
                return;
            }
        }

        if (keepSearching) {
            if (canKeepSearchingNow()) {
                squarePosIterator = createIterator();
            }
        } else {
            squarePosIterator = null;
        }
    }

    protected boolean canKeepSearchingNow() {
        return true;
    }

    protected boolean canScanChunk(Entity cameraEntity, ChunkPos pos) {
        ClientLevel world = Minecraft.getInstance().level;
        assert world != null;
        return world.getChunk(pos.x, pos.z, ChunkStatus.FULL, false) != null;
    }

    protected boolean hasAnyBlockCloserThan(Entity cameraEntity, ChunkPos chunkPos, double distanceSq) {
        Vec3 cameraPos = cameraEntity.getEyePosition(0);
        double closestX = Mth.clamp(cameraPos.x, chunkPos.x << 4, (chunkPos.x + 1) << 4);
        double closestZ = Mth.clamp(cameraPos.z, chunkPos.z << 4, (chunkPos.z + 1) << 4);
        double closestDistanceSq = Mth.square(cameraPos.x - closestX) + Mth.square(cameraPos.z - closestZ);
        return closestDistanceSq < distanceSq;
    }

    @SuppressWarnings("unused")
    protected boolean willScanBlockCloserThan(Entity cameraEntity, ChunkPos currentlyScanningChunk, double distanceSq) {
        Vec3 cameraPos = cameraEntity.getEyePosition(0);
        int cameraChunkX = Mth.floor(cameraPos.x) >> 4;
        int cameraChunkZ = Mth.floor(cameraPos.z) >> 4;
        int chunkRadius = Math.max(Math.abs(currentlyScanningChunk.x - cameraChunkX), Math.abs(currentlyScanningChunk.z - cameraChunkZ));
        double minPossibleDistance = ((chunkRadius - 1) << 4) + Math.min(
            Math.min(cameraPos.x - (cameraChunkX << 4), ((cameraChunkX + 1) << 4) - cameraPos.x),
            Math.min(cameraPos.z - (cameraChunkZ << 4), ((cameraChunkZ + 1) << 4) - cameraPos.z)
        );
        return minPossibleDistance * minPossibleDistance < distanceSq;
    }

    private void scanChunk(Entity cameraEntity, ChunkPos chunkPos) {
        ClientLevel world = Minecraft.getInstance().level;
        assert world != null;
        for (BlockPos pos : BlockPos.betweenClosed(chunkPos.getMinBlockX(), world.getMinBuildHeight(), chunkPos.getMinBlockZ(), chunkPos.getMaxBlockX(), world.getMaxBuildHeight(), chunkPos.getMaxBlockZ())) {
            scanBlock(cameraEntity, pos);
        }
    }

    protected abstract void scanBlock(Entity cameraEntity, BlockPos pos);

    private Iterator<BlockPos.MutableBlockPos> createIterator() {
        Entity cameraEntity = Minecraft.getInstance().cameraEntity;
        if (cameraEntity == null) {
            _break();
            return null;
        }
        return BlockPos.spiralAround(new BlockPos(Mth.floor(cameraEntity.getX()) >> 4, 0, Mth.floor(cameraEntity.getZ()) >> 4), Minecraft.getInstance().options.renderDistance().get(), Direction.EAST, Direction.SOUTH).iterator();
    }
}
