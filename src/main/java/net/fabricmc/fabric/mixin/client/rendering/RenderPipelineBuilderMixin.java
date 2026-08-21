/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.mixin.client.rendering;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.PolygonMode;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderPipeline;
import net.fabricmc.fabric.impl.client.rendering.FabricRenderPipelineImpl;
import net.fabricmc.fabric.impl.client.rendering.FabricRenderPipelineInternals;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(RenderPipeline.Builder.class)
class RenderPipelineBuilderMixin implements FabricRenderPipeline.Builder {
	@Unique
	private Optional<Boolean> usePipelineDrawModeForGui = Optional.empty();

	@Override
	public RenderPipeline.Builder withUsePipelineDrawModeForGui(boolean usePipelineDrawMode) {
		this.usePipelineDrawModeForGui = Optional.of(usePipelineDrawMode);
		return (RenderPipeline.Builder) (Object) this;
	}

	@Override
	public RenderPipeline.Builder withoutUsePipelineDrawModeForGui() {
		this.usePipelineDrawModeForGui = Optional.empty();
		return (RenderPipeline.Builder) (Object) this;
	}

	@Inject(
			method = "withSnippet",
			at = @At("TAIL")
	)
	private void copyUsePipelineDrawModeForGuiFromSnippet(RenderPipeline.Snippet snippet, CallbackInfo ci) {
		((FabricRenderPipeline.Snippet) (Object) snippet).usePipelineDrawModeForGui().ifPresent(value -> this.usePipelineDrawModeForGui = Optional.of(value));
	}

	// Neo Edit: Edited to Redirect
	@Redirect(
			method = "buildSnippet",
			at = @At(
					value = "NEW",
					target = "Lcom/mojang/blaze3d/pipeline/RenderPipeline$Snippet;"
			)
	)
	private RenderPipeline.Snippet copyUsePipelineDrawModeForGuiToSnippet(
			Optional vertexShader, Optional fragmentShader, Optional shaderDefines, Optional bindGroupLayouts, ColorTargetState[] colorTargetStates, int activeColorTargetStateCount, Optional depthStencilState, Optional polygonMode, Optional cull, VertexFormat[] vertexFormatPerBuffer, Optional vertexFormatMode, Optional stencilTest
	) {
		return FabricRenderPipelineInternals.withSnippetUsePipelineVertexFormatForGui(() -> new RenderPipeline.Snippet(vertexShader, fragmentShader, shaderDefines, bindGroupLayouts, colorTargetStates, activeColorTargetStateCount, depthStencilState, polygonMode, cull, vertexFormatPerBuffer, vertexFormatMode), usePipelineDrawModeForGui);
	}

	@ModifyReturnValue(
			method = "build",
			at = @At("RETURN")
	)
	private RenderPipeline copyUsePipelineDrawModeForGuiToPipeline(RenderPipeline original) {
		((FabricRenderPipelineImpl) original).fabric$setUsePipelineDrawModeForGuiSetter(this.usePipelineDrawModeForGui.orElse(false));
		return original;
	}
}
