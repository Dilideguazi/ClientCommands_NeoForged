package net.earthcomputer.clientcommands.features;

@Deprecated
public class WorldRendererDataHolder {
    private static float tickDelta;

    public static void setTickDelta(float value) {
        tickDelta = value;
    }

    public static float getTickDelta() {
        return tickDelta;
    }
}
