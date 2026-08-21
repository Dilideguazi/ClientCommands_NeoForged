package dev.xpple.clientarguments;

import com.mojang.brigadier.AmbiguityConsumer;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import dev.xpple.clientarguments.arguments.CEntitySelectorOptions;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;
import net.neoforged.fml.loading.FMLLoader;

import static dev.xpple.clientarguments.arguments.CAngleArgument.angle;
import static dev.xpple.clientarguments.arguments.CAngleArgument.getAngle;
import static dev.xpple.clientarguments.arguments.CBlockPosArgument.blockPos;
import static dev.xpple.clientarguments.arguments.CBlockPosArgument.getBlockPos;
import static dev.xpple.clientarguments.arguments.CBlockPredicateArgument.blockPredicate;
import static dev.xpple.clientarguments.arguments.CBlockPredicateArgument.getBlockPredicate;
import static dev.xpple.clientarguments.arguments.CBlockStateArgument.blockState;
import static dev.xpple.clientarguments.arguments.CBlockStateArgument.getBlockState;
import static dev.xpple.clientarguments.arguments.CColorArgument.color;
import static dev.xpple.clientarguments.arguments.CColorArgument.getColor;
import static dev.xpple.clientarguments.arguments.CColumnPosArgument.columnPos;
import static dev.xpple.clientarguments.arguments.CColumnPosArgument.getColumnPos;
import static dev.xpple.clientarguments.arguments.CComponentArgument.component;
import static dev.xpple.clientarguments.arguments.CComponentArgument.getComponent;
import static dev.xpple.clientarguments.arguments.CCompoundTagArgument.compoundTag;
import static dev.xpple.clientarguments.arguments.CCompoundTagArgument.getCompoundTag;
import static dev.xpple.clientarguments.arguments.CDimensionArgument.dimension;
import static dev.xpple.clientarguments.arguments.CDimensionArgument.getDimension;
import static dev.xpple.clientarguments.arguments.CEntityAnchorArgument.entityAnchor;
import static dev.xpple.clientarguments.arguments.CEntityAnchorArgument.getEntityAnchor;
import static dev.xpple.clientarguments.arguments.CEntityArgument.entity;
import static dev.xpple.clientarguments.arguments.CEntityArgument.getEntity;
import static dev.xpple.clientarguments.arguments.CEnumArgument.enumArg;
import static dev.xpple.clientarguments.arguments.CEnumArgument.getEnum;
import static dev.xpple.clientarguments.arguments.CGameProfileArgument.gameProfile;
import static dev.xpple.clientarguments.arguments.CGameProfileArgument.getProfileArgument;
import static dev.xpple.clientarguments.arguments.CHexColorArgument.getHexColor;
import static dev.xpple.clientarguments.arguments.CHexColorArgument.hexColor;
import static dev.xpple.clientarguments.arguments.CIdentifierArgument.getId;
import static dev.xpple.clientarguments.arguments.CIdentifierArgument.id;
import static dev.xpple.clientarguments.arguments.CItemArgument.getItemStackArgument;
import static dev.xpple.clientarguments.arguments.CItemArgument.itemStack;
import static dev.xpple.clientarguments.arguments.CItemPredicateArgument.getItemStackPredicate;
import static dev.xpple.clientarguments.arguments.CItemPredicateArgument.itemPredicate;
import static dev.xpple.clientarguments.arguments.CMessageArgument.getMessage;
import static dev.xpple.clientarguments.arguments.CMessageArgument.message;
import static dev.xpple.clientarguments.arguments.CNbtPathArgument.getNbtPath;
import static dev.xpple.clientarguments.arguments.CNbtPathArgument.nbtPath;
import static dev.xpple.clientarguments.arguments.CNbtTagArgument.getNbtTag;
import static dev.xpple.clientarguments.arguments.CNbtTagArgument.nbtTag;
import static dev.xpple.clientarguments.arguments.CObjectiveArgument.getObjective;
import static dev.xpple.clientarguments.arguments.CObjectiveArgument.objective;
import static dev.xpple.clientarguments.arguments.CObjectiveCriteriaArgument.criteria;
import static dev.xpple.clientarguments.arguments.CObjectiveCriteriaArgument.getCriteria;
import static dev.xpple.clientarguments.arguments.COperationArgument.getOperation;
import static dev.xpple.clientarguments.arguments.COperationArgument.operation;
import static dev.xpple.clientarguments.arguments.CParticleArgument.getParticle;
import static dev.xpple.clientarguments.arguments.CParticleArgument.particle;
import static dev.xpple.clientarguments.arguments.CRangeArgument.*;
import static dev.xpple.clientarguments.arguments.CResourceArgument.getEnchantment;
import static dev.xpple.clientarguments.arguments.CResourceArgument.resource;
import static dev.xpple.clientarguments.arguments.CResourceKeyArgument.getKey;
import static dev.xpple.clientarguments.arguments.CResourceKeyArgument.key;
import static dev.xpple.clientarguments.arguments.CResourceOrIdArgument.getLootTable;
import static dev.xpple.clientarguments.arguments.CResourceOrIdArgument.lootTable;
import static dev.xpple.clientarguments.arguments.CResourceOrTagArgument.getResourceOrTag;
import static dev.xpple.clientarguments.arguments.CResourceOrTagArgument.resourceOrTag;
import static dev.xpple.clientarguments.arguments.CResourceOrTagKeyArgument.getResourceOrTag;
import static dev.xpple.clientarguments.arguments.CResourceOrTagKeyArgument.resourceOrTag;
import static dev.xpple.clientarguments.arguments.CResourceSelectorArgument.getSelectedResources;
import static dev.xpple.clientarguments.arguments.CResourceSelectorArgument.resourceSelector;
import static dev.xpple.clientarguments.arguments.CRotationArgument.getRotation;
import static dev.xpple.clientarguments.arguments.CRotationArgument.rotation;
import static dev.xpple.clientarguments.arguments.CScoreHolderArgument.getScoreHolder;
import static dev.xpple.clientarguments.arguments.CScoreHolderArgument.scoreHolder;
import static dev.xpple.clientarguments.arguments.CScoreboardSlotArgument.getScoreboardSlot;
import static dev.xpple.clientarguments.arguments.CScoreboardSlotArgument.scoreboardSlot;
import static dev.xpple.clientarguments.arguments.CSlotArgument.getItemSlot;
import static dev.xpple.clientarguments.arguments.CSlotArgument.itemSlot;
import static dev.xpple.clientarguments.arguments.CSlotsArgument.getSlots;
import static dev.xpple.clientarguments.arguments.CSlotsArgument.slots;
import static dev.xpple.clientarguments.arguments.CStyleArgument.getStyle;
import static dev.xpple.clientarguments.arguments.CStyleArgument.style;
import static dev.xpple.clientarguments.arguments.CSwizzleArgument.getSwizzle;
import static dev.xpple.clientarguments.arguments.CSwizzleArgument.swizzle;
import static dev.xpple.clientarguments.arguments.CTeamArgument.getTeam;
import static dev.xpple.clientarguments.arguments.CTeamArgument.team;
import static dev.xpple.clientarguments.arguments.CTimeArgument.getTime;
import static dev.xpple.clientarguments.arguments.CTimeArgument.time;
import static dev.xpple.clientarguments.arguments.CUuidArgument.getUuid;
import static dev.xpple.clientarguments.arguments.CUuidArgument.uuid;
import static dev.xpple.clientarguments.arguments.CVec2Argument.getVec2;
import static dev.xpple.clientarguments.arguments.CVec2Argument.vec2;
import static dev.xpple.clientarguments.arguments.CVec3Argument.getVec3;
import static dev.xpple.clientarguments.arguments.CVec3Argument.vec3;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.*;

public class ClientArguments {
    private static final DynamicCommandExceptionType STRUCTURE_INVALID_EXCEPTION = new DynamicCommandExceptionType(id -> Component.translatableEscape("commands.locate.structure.invalid", id));

    public static void onInitializeClient() {
        CEntitySelectorOptions.register();

        if (!FMLLoader.getCurrent().isProduction()) {
            ClientCommandRegistrationCallback.EVENT.register(ClientArguments::registerTestCommand);
        }
    }

    /**
     * <p>
     * Registering this test command will trigger {@link com.mojang.brigadier.tree.CommandNode#findAmbiguities(AmbiguityConsumer)},
     * which checks the validity of the example inputs - and with that also the validity of the argument in question.
     */
    private static void registerTestCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(literal("clientarguments:test")
            .then(literal("angle").then(argument("angle", angle())
                .executes(ctx -> consume(getAngle(ctx, "angle")))))
            .then(literal("blockpos").then(argument("blockpos", blockPos())
                .executes(ctx -> consume(getBlockPos(ctx, "blockpos")))))
            .then(literal("blockpredicate").then(argument("blockpredicate", blockPredicate(registryAccess))
                .executes(ctx -> consume(getBlockPredicate(ctx, "blockpredicate")))))
            .then(literal("blockstate").then(argument("blockstate", blockState(registryAccess))
                .executes(ctx -> consume(getBlockState(ctx, "blockstate")))))
            .then(literal("color").then(argument("color", color())
                .executes(ctx -> consume(getColor(ctx, "color")))))
            .then(literal("columnpos").then(argument("columnpos", columnPos())
                .executes(ctx -> consume(getColumnPos(ctx, "columnpos")))))
            .then(literal("dimension").then(argument("dimension", dimension())
                .executes(ctx -> consume(getDimension(ctx, "dimension")))))
            .then(literal("entityanchor").then(argument("entityanchor", entityAnchor())
                .executes(ctx -> consume(getEntityAnchor(ctx, "entityanchor")))))
            .then(literal("entity").then(argument("entity", entity())
                .executes(ctx -> consume(getEntity(ctx, "entity")))))
            .then(literal("enum").then(argument("enum", enumArg(GameType.class))
                .executes(ctx -> consume(getEnum(ctx, "enum")))))
            .then(literal("gameprofile").then(argument("gameprofile", gameProfile())
                .executes(ctx -> consume(getProfileArgument(ctx, "gameprofile")))))
            .then(literal("hexcolor").then(argument("hexcolor", hexColor())
                .executes(ctx -> consume(getHexColor(ctx, "hexcolor")))))
            .then(literal("identifier").then(argument("identifier", id())
                .executes(ctx -> consume(getId(ctx, "identifier")))))
            .then(literal("itempredicate").then(argument("itempredicate", itemPredicate(registryAccess))
                .executes(ctx -> consume(getItemStackPredicate(ctx, "itempredicate")))))
            .then(literal("itemslot").then(argument("itemslot", itemSlot())
                .executes(ctx -> consume(getItemSlot(ctx, "itemslot")))))
            .then(literal("itemstack").then(argument("itemstack", itemStack(registryAccess))
                .executes(ctx -> consume(getItemStackArgument(ctx, "itemstack")))))
            .then(literal("message").then(argument("message", message())
                .executes(ctx -> consume(getMessage(ctx, "message")))))
            .then(literal("compoundtag").then(argument("compoundtag", compoundTag())
                .executes(ctx -> consume(getCompoundTag(ctx, "compoundtag")))))
            .then(literal("nbttag").then(argument("nbttag", nbtTag())
                .executes(ctx -> consume(getNbtTag(ctx, "nbttag")))))
            .then(literal("nbtpath").then(argument("nbtpath", nbtPath())
                .executes(ctx -> consume(getNbtPath(ctx, "nbtpath")))))
            .then(literal("intrange").then(argument("intrange", intRange())
                .executes(ctx -> consume(Ints.getRangeArgument(ctx, "intrange")))))
            .then(literal("floatrange").then(argument("floatrange", floatRange())
                .executes(ctx -> consume(Floats.getRangeArgument(ctx, "floatrange")))))
            .then(literal("operation").then(argument("operation", operation())
                .executes(ctx -> consume(getOperation(ctx, "operation")))))
            .then(literal("particleeffect").then(argument("particleeffect", particle(registryAccess))
                .executes(ctx -> consume(getParticle(ctx, "particleeffect")))))
            .then(literal("resourceholder").then(argument("resourceholder", lootTable(registryAccess))
                .executes(ctx -> consume(getLootTable(ctx, "resourceholder")))))
            .then(literal("resourceholderpredicate").then(argument("resourceholderpredicate", resourceOrTag(registryAccess, Registries.BIOME))
                .executes(ctx -> consume(getResourceOrTag(ctx, "resourceholderpredicate", Registries.BIOME)))))
            .then(literal("resourceholderreference").then(argument("resourceholderreference", resource(registryAccess, Registries.ENCHANTMENT))
                .executes(ctx -> consume(getEnchantment(ctx, "resourceholderreference")))))
            .then(literal("resourcekey").then(argument("resourcekey", key(Registries.STRUCTURE))
                .executes(ctx -> consume(getKey(ctx, "resourcekey", Registries.STRUCTURE, STRUCTURE_INVALID_EXCEPTION)))))
            .then(literal("resourceortag").then(argument("resourceortag", resourceOrTag(Registries.STRUCTURE))
                .executes(ctx -> consume(getResourceOrTag(ctx, "resourceortag", Registries.STRUCTURE, STRUCTURE_INVALID_EXCEPTION)))))
            .then(literal("resourceselector").then(argument("resourceselector", resourceSelector(registryAccess, Registries.ITEM))
                .executes(ctx -> consume(getSelectedResources(ctx, "resourceselector", Registries.ITEM)))))
            .then(literal("rotation").then(argument("rotation", rotation())
                .executes(ctx -> consume(getRotation(ctx, "rotation")))))
            .then(literal("scoreboardcriterion").then(argument("scoreboardcriterion", criteria())
                .executes(ctx -> consume(getCriteria(ctx, "scoreboardcriterion")))))
            .then(literal("scoreboardobjective").then(argument("scoreboardobjective", objective())
                .executes(ctx -> consume(getObjective(ctx, "scoreboardobjective")))))
            .then(literal("scoreboardslot").then(argument("scoreboardslot", scoreboardSlot())
                .executes(ctx -> consume(getScoreboardSlot(ctx, "scoreboardslot")))))
            .then(literal("scoreholder").then(argument("scoreholder", scoreHolder())
                .executes(ctx -> consume(getScoreHolder(ctx, "scoreholder")))))
            .then(literal("slotrange").then(argument("slotrange", slots())
                .executes(ctx -> consume(getSlots(ctx, "slotrange")))))
            .then(literal("style").then(argument("style", style(registryAccess))
                .executes(ctx -> consume(getStyle(ctx, "style")))))
            .then(literal("swizzle").then(argument("swizzle", swizzle())
                .executes(ctx -> consume(getSwizzle(ctx, "swizzle")))))
            .then(literal("team").then(argument("team", team())
                .executes(ctx -> consume(getTeam(ctx, "team")))))
            .then(literal("component").then(argument("component", component(registryAccess))
                .executes(ctx -> consume(getComponent(ctx, "component")))))
            .then(literal("time").then(argument("time", time())
                .executes(ctx -> consume(getTime(ctx, "time")))))
            .then(literal("uuid").then(argument("uuid", uuid())
                .executes(ctx -> consume(getUuid(ctx, "uuid")))))
            .then(literal("vec2").then(argument("vec2", vec2())
                .executes(ctx -> consume(getVec2(ctx, "vec2")))))
            .then(literal("vec3").then(argument("vec3", vec3())
                .executes(ctx -> consume(getVec3(ctx, "vec3")))))
        );
    }

    private static int consume(Object object) {
        return Command.SINGLE_SUCCESS;
    }
}
