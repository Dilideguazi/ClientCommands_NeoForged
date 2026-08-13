package dev.xpple.clientarguments.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.advancements.Advancement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

public class CIdentifierArgumentType implements ArgumentType<ResourceLocation> {

	private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");
	private static final DynamicCommandExceptionType UNKNOWN_ADVANCEMENT_EXCEPTION = new DynamicCommandExceptionType(id -> Component.translatable("advancement.advancementNotFound", id));
	private static final DynamicCommandExceptionType UNKNOWN_RECIPE_EXCEPTION = new DynamicCommandExceptionType(id -> Component.translatable("recipe.notFound", id));
	private static final DynamicCommandExceptionType UNKNOWN_ATTRIBUTE_EXCEPTION = new DynamicCommandExceptionType(id -> Component.translatable("attribute.unknown", id));

	public static CIdentifierArgumentType identifier() {
		return new CIdentifierArgumentType();
	}

	public static Advancement getCAdvancementArgument(CommandContext<FabricClientCommandSource> context, String argumentName) throws CommandSyntaxException {
		ResourceLocation identifier = context.getArgument(argumentName, ResourceLocation.class);
		Advancement advancement = Objects.requireNonNull(context.getSource().getClient().getConnection()).getAdvancements().getAdvancements().get(identifier);
		if (advancement == null) {
			throw UNKNOWN_ADVANCEMENT_EXCEPTION.create(identifier);
		}
		return advancement;
	}

	public static Recipe<?> getCRecipeArgument(CommandContext<FabricClientCommandSource> context, String argumentName) throws CommandSyntaxException {
		RecipeManager recipeManager = context.getSource().getWorld().getRecipeManager();
		ResourceLocation identifier = context.getArgument(argumentName, ResourceLocation.class);
		return recipeManager.byKey(identifier).orElseThrow(() -> UNKNOWN_RECIPE_EXCEPTION.create(identifier));
	}

	/*
	public static LootCondition getCPredicateArgument(CommandContext<FabricClientCommandSource> context, String argumentName) throws CommandSyntaxException {
		Identifier identifier = context.getArgument(argumentName, Identifier.class);
		LootConditionManager lootConditionManager = context.getSource().getServer().getPredicateManager();
		LootCondition lootCondition = lootConditionManager.get(identifier);
		if (lootCondition == null) {
			throw UNKNOWN_PREDICATE_EXCEPTION.create(identifier);
		}
		return lootCondition;
	}
	 */

	/*
	public static LootFunction getCItemModifierArgument(CommandContext<FabricClientCommandSource> context, String argumentName) throws CommandSyntaxException {
		Identifier identifier = context.getArgument(argumentName, Identifier.class);
		LootFunctionManager lootFunctionManager = context.getSource().getServer().getItemModifierManager();
		LootFunction lootFunction = lootFunctionManager.get(identifier);
		if (lootFunction == null) {
			throw UNKNOWN_ITEM_MODIFIER_EXCEPTION.create(identifier);
		}
		return lootFunction;
	}
	 */

	public static Attribute getCAttributeArgument(CommandContext<FabricClientCommandSource> context, String argumentName) throws CommandSyntaxException {
		ResourceLocation identifier = context.getArgument(argumentName, ResourceLocation.class);
		Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(identifier);
		if (attribute == null) throw UNKNOWN_ATTRIBUTE_EXCEPTION.create(identifier);
		return attribute;
	}

	public static ResourceLocation getCIdentifier(final CommandContext<FabricClientCommandSource> context, final String name) {
		return context.getArgument(name, ResourceLocation.class);
	}

	@Override
	public ResourceLocation parse(final StringReader stringReader) throws CommandSyntaxException {
		return ResourceLocation.read(stringReader);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}
