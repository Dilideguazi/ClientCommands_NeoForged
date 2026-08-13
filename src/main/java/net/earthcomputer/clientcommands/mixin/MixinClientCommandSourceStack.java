package net.earthcomputer.clientcommands.mixin;

import net.earthcomputer.clientcommands.interfaces.IFlaggedCommandSource;
import net.minecraft.commands.CommandSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ClientCommandSourceStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommandSourceStack.class)
public class MixinClientCommandSourceStack implements IFlaggedCommandSource {
    @Unique
    public CommandSource source;

    @Unique
    private Vec3 worldPosition;

    @Unique
    private Vec2 rotation;

    @Unique
    private int permissionLevel;

    @Unique
    private String textName;

    @Unique
    private Component displayName;

    @Unique
    private Entity entity;

    @Unique
    private int flags;

    @Override
    public int getFlags() {
        return this.flags;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CommandSource source, Vec3 position, Vec2 rotation, int permission, String plainTextName, Component displayName, Entity executing, CallbackInfo ci) {
        this.source = source;
        this.worldPosition = position;
        this.rotation = rotation;
        this.permissionLevel = permission;
        this.textName = plainTextName;
        this.displayName = displayName;
        this.entity = executing;
    }

    @Override
    public IFlaggedCommandSource withFlags(int flags) {
        MixinClientCommandSourceStack source = (MixinClientCommandSourceStack) (Object)
                new ClientCommandSourceStack(this.source, this.worldPosition, this.rotation, this.permissionLevel, this.textName, this.displayName, this.entity);
        source.flags = flags;

        return source;
    }
}
