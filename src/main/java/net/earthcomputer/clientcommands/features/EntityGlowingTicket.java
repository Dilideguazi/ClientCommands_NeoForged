package net.earthcomputer.clientcommands.features;

public class EntityGlowingTicket {

    private int ticksLeft;
    private final int color;

    public EntityGlowingTicket(int ticksLeft, int color) {
        this.ticksLeft = ticksLeft;
        this.color = color;
    }

    public boolean tick() {
        return ticksLeft-- >= 0;
    }

    public int getColor() {
        return color;
    }

}
