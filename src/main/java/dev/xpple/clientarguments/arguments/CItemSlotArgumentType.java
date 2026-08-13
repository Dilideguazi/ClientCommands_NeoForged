package dev.xpple.clientarguments.arguments;

import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.Util;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CItemSlotArgumentType implements ArgumentType<Integer> {

	private static final Collection<String> EXAMPLES = Arrays.asList("container.5", "12", "weapon");
	private static final DynamicCommandExceptionType UNKNOWN_SLOT_EXCEPTION = new DynamicCommandExceptionType(name -> Component.translatable("slot.unknown", name));
	private static final Map<String, Integer> SLOT_NAMES_TO_SLOT_COMMAND_ID = Util.make(Maps.newHashMap(), map -> {
		int n;

		for(n = 0; n < 54; ++n) {
			map.put("container." + n, n);
		}
		for(n = 0; n < 9; ++n) {
			map.put("hotbar." + n, n);
		}
		for(n = 0; n < 27; ++n) {
			map.put("inventory." + n, 9 + n);
		}
		for(n = 0; n < 27; ++n) {
			map.put("enderchest." + n, 200 + n);
		}
		for(n = 0; n < 8; ++n) {
			map.put("villager." + n, 300 + n);
		}
		for(n = 0; n < 15; ++n) {
			map.put("horse." + n, 500 + n);
		}
		map.put("weapon", EquipmentSlot.MAINHAND.getIndex(98));
		map.put("weapon.mainhand", EquipmentSlot.MAINHAND.getIndex(98));
		map.put("weapon.offhand", EquipmentSlot.OFFHAND.getIndex(98));
		map.put("armor.head", EquipmentSlot.HEAD.getIndex(100));
		map.put("armor.chest", EquipmentSlot.CHEST.getIndex(100));
		map.put("armor.legs", EquipmentSlot.LEGS.getIndex(100));
		map.put("armor.feet", EquipmentSlot.FEET.getIndex(100));
		map.put("horse.saddle", 400);
		map.put("horse.armor", 401);
		map.put("horse.chest", 499);
	});

	public static CItemSlotArgumentType itemSlot() {
		return new CItemSlotArgumentType();
	}

	public static Integer getCItemSlot(final CommandContext<FabricClientCommandSource> context, final String name) {
		return context.getArgument(name, Integer.class);
	}

	@Override
	public Integer parse(final StringReader stringReader) throws CommandSyntaxException {
		String string = stringReader.readUnquotedString();
		if (SLOT_NAMES_TO_SLOT_COMMAND_ID.containsKey(string)) {
			return SLOT_NAMES_TO_SLOT_COMMAND_ID.get(string);
		}
		throw UNKNOWN_SLOT_EXCEPTION.create(string);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
		return SharedSuggestionProvider.suggest(SLOT_NAMES_TO_SLOT_COMMAND_ID.keySet(), builder);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}
