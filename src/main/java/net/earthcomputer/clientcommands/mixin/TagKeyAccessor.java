package net.earthcomputer.clientcommands.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TagKey.class)
public interface TagKeyAccessor {
    @Accessor
    ResourceLocation getLocation();
}
