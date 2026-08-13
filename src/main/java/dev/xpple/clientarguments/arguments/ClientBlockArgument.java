package dev.xpple.clientarguments.arguments;

import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ClientBlockArgument {
    private final BlockState state;
    private final Map<Property<?>, Comparable<?>> properties;
    @Nullable
    private final CompoundTag nbt;

    ClientBlockArgument(BlockStateParser.BlockResult result) {
        this.state = result.blockState();
        this.properties = result.properties();
        this.nbt = result.nbt();
    }

    private boolean isSameBlock(Block other) {
        return this.state.getBlock().equals(other);
    }

    private boolean isSameBlockState(BlockState other) {
        return this.state == other;
    }

    private boolean isSameNbt(CompoundTag other) {
        return NbtUtils.compareNbt(this.nbt, other, true);
    }

    public Block getBlock() {
        return this.state.getBlock();
    }

    public BlockState getBlockState() {
        return this.state;
    }

    @Nullable
    public CompoundTag getNbt() {
        return this.nbt;
    }

    public Map<Property<?>, Comparable<?>> getProperties() {
        return this.properties;
    }
}
