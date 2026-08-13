package net.earthcomputer.clientcommands.mixin;

import net.minecraft.client.renderer.RenderStateShard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderStateShard.class)
public interface RenderStateShardAccessor {
    @Accessor
    static RenderStateShard.ShaderStateShard getRENDERTYPE_LINES_SHADER(){
        throw new AssertionError();
    }

    @Accessor
    static RenderStateShard.WriteMaskStateShard getCOLOR_WRITE(){
        throw new AssertionError();
    }

    @Accessor
    static RenderStateShard.CullStateShard getNO_CULL(){
        throw new AssertionError();
    }

    @Accessor
    static RenderStateShard.DepthTestStateShard getNO_DEPTH_TEST(){
        throw new AssertionError();
    }

    @Accessor
    static RenderStateShard.LayeringStateShard getVIEW_OFFSET_Z_LAYERING(){
        throw new AssertionError();
    }
}
