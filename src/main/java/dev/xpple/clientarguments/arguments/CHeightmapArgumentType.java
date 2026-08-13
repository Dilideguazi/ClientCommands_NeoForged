package dev.xpple.clientarguments.arguments;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.arguments.StringRepresentableArgument;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Arrays;
import java.util.Locale;

public class CHeightmapArgumentType extends StringRepresentableArgument<Heightmap.Types> {

    private static final Codec<Heightmap.Types> HEIGHTMAP_CODEC = StringRepresentable.fromEnumWithMapping(CHeightmapArgumentType::getHeightmapTypes, (name) -> name.toLowerCase(Locale.ROOT));

    private static Heightmap.Types[] getHeightmapTypes() {
        return Arrays.stream(Heightmap.Types.values()).filter(Heightmap.Types::keepAfterWorldgen).toArray(Heightmap.Types[]::new);
    }

    private CHeightmapArgumentType() {
        super(HEIGHTMAP_CODEC, CHeightmapArgumentType::getHeightmapTypes);
    }

    public static CHeightmapArgumentType heightmap() {
        return new CHeightmapArgumentType();
    }

    public static Heightmap.Types getCHeightmap(final CommandContext<FabricClientCommandSource> context, final String id) {
        return context.getArgument(id, Heightmap.Types.class);
    }

    protected String transformValueName(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}
