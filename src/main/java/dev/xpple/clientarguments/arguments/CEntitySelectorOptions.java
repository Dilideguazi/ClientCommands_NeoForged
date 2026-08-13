package dev.xpple.clientarguments.arguments;

import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.earthcomputer.clientcommands.mixin.TagKeyAccessor;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.WrappedMinMaxBounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.function.Predicate;

public class CEntitySelectorOptions {

	private static final Map<String, SelectorOption> OPTIONS = new HashMap<>();
	public static final DynamicCommandExceptionType UNKNOWN_OPTION_EXCEPTION = new DynamicCommandExceptionType(arg -> Component.translatable("argument.entity.options.unknown", arg));
	public static final DynamicCommandExceptionType INAPPLICABLE_OPTION_EXCEPTION = new DynamicCommandExceptionType(arg -> Component.translatable("argument.entity.options.inapplicable", arg));
	public static final SimpleCommandExceptionType NEGATIVE_DISTANCE_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("argument.entity.options.distance.negative"));
	public static final SimpleCommandExceptionType NEGATIVE_LEVEL_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("argument.entity.options.level.negative"));
	public static final SimpleCommandExceptionType TOO_SMALL_LEVEL_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("argument.entity.options.limit.toosmall"));
	public static final DynamicCommandExceptionType IRREVERSIBLE_SORT_EXCEPTION = new DynamicCommandExceptionType(arg -> Component.translatable("argument.entity.options.sort.irreversible", arg));
	public static final DynamicCommandExceptionType INVALID_MODE_EXCEPTION = new DynamicCommandExceptionType(arg -> Component.translatable("argument.entity.options.mode.invalid", arg));
	public static final DynamicCommandExceptionType INVALID_TYPE_EXCEPTION = new DynamicCommandExceptionType(arg -> Component.translatable("argument.entity.options.type.invalid", arg));

	private static void putOption(String id, SelectorHandler handler, Predicate<CEntitySelectorReader> condition, Component description) {
		OPTIONS.put(id, new SelectorOption(handler, condition, description));
	}

	public static void register() {
		if (!OPTIONS.isEmpty()) {
			return;
		}
		CEntitySelectorOptions.putOption("name", reader2 -> {
			int i = reader2.getReader().getCursor();
			boolean bl = reader2.readNegationCharacter();
			String string = reader2.getReader().readString();
			if (reader2.excludesName() && !bl) {
				reader2.getReader().setCursor(i);
				throw INAPPLICABLE_OPTION_EXCEPTION.createWithContext(reader2.getReader(), "name");
			}
			if (bl) {
				reader2.setExcludesName(true);
			} else {
				reader2.setSelectsName(true);
			}
			reader2.setPredicate(reader -> reader.getName().getString().equals(string) != bl);
		}, reader -> !reader.selectsName(), Component.translatable("argument.entity.options.name.description"));
		CEntitySelectorOptions.putOption("distance", reader -> {
			int i = reader.getReader().getCursor();
			MinMaxBounds.Doubles floatRange = MinMaxBounds.Doubles.fromReader(reader.getReader());
			if (floatRange.getMin() != null && floatRange.getMin() < 0.0 || floatRange.getMax() != null && floatRange.getMax() < 0.0) {
				reader.getReader().setCursor(i);
				throw NEGATIVE_DISTANCE_EXCEPTION.createWithContext(reader.getReader());
			}
			reader.setDistance(floatRange);
		}, reader -> reader.getDistance().isAny(), Component.translatable("argument.entity.options.distance.description"));
		CEntitySelectorOptions.putOption("level", reader -> {
			int i = reader.getReader().getCursor();
			MinMaxBounds.Ints intRange = MinMaxBounds.Ints.fromReader(reader.getReader());
			if (intRange.getMin() != null && intRange.getMin() < 0 || intRange.getMax() != null && intRange.getMax() < 0) {
				reader.getReader().setCursor(i);
				throw NEGATIVE_LEVEL_EXCEPTION.createWithContext(reader.getReader());
			}
			reader.setLevelRange(intRange);
			reader.setIncludesNonPlayers(false);
		}, reader -> reader.getLevelRange().isAny(), Component.translatable("argument.entity.options.level.description"));
		CEntitySelectorOptions.putOption("x", reader -> reader.setX(reader.getReader().readDouble()), reader -> reader.getX() == null, Component.translatable("argument.entity.options.x.description"));
		CEntitySelectorOptions.putOption("y", reader -> reader.setY(reader.getReader().readDouble()), reader -> reader.getY() == null, Component.translatable("argument.entity.options.y.description"));
		CEntitySelectorOptions.putOption("z", reader -> reader.setZ(reader.getReader().readDouble()), reader -> reader.getZ() == null, Component.translatable("argument.entity.options.z.description"));
		CEntitySelectorOptions.putOption("dx", reader -> reader.setDx(reader.getReader().readDouble()), reader -> reader.getDx() == null, Component.translatable("argument.entity.options.dx.description"));
		CEntitySelectorOptions.putOption("dy", reader -> reader.setDy(reader.getReader().readDouble()), reader -> reader.getDy() == null, Component.translatable("argument.entity.options.dy.description"));
		CEntitySelectorOptions.putOption("dz", reader -> reader.setDz(reader.getReader().readDouble()), reader -> reader.getDz() == null, Component.translatable("argument.entity.options.dz.description"));
		CEntitySelectorOptions.putOption("x_rotation", reader -> reader.setPitchRange(WrappedMinMaxBounds.fromReader(reader.getReader(), true, Mth::wrapDegrees)), reader -> reader.getPitchRange() == WrappedMinMaxBounds.ANY, Component.translatable("argument.entity.options.x_rotation.description"));
		CEntitySelectorOptions.putOption("y_rotation", reader -> reader.setYawRange(WrappedMinMaxBounds.fromReader(reader.getReader(), true, Mth::wrapDegrees)), reader -> reader.getYawRange() == WrappedMinMaxBounds.ANY, Component.translatable("argument.entity.options.y_rotation.description"));
		CEntitySelectorOptions.putOption("limit", reader -> {
			int i = reader.getReader().getCursor();
			int j = reader.getReader().readInt();
			if (j < 1) {
				reader.getReader().setCursor(i);
				throw TOO_SMALL_LEVEL_EXCEPTION.createWithContext(reader.getReader());
			}
			reader.setLimit(j);
			reader.setHasLimit(true);
		}, reader -> !reader.isSenderOnly() && !reader.hasLimit(), Component.translatable("argument.entity.options.limit.description"));
		CEntitySelectorOptions.putOption("sort", reader -> {
			int i = reader.getReader().getCursor();
			String string = reader.getReader().readUnquotedString();
			reader.setSuggestionProvider((builder, consumer) -> SharedSuggestionProvider.suggest(Arrays.asList("nearest", "furthest", "random", "arbitrary"), builder));
			reader.setSorter(switch (string) {
				case "nearest" -> EntitySelectorParser.ORDER_NEAREST;
				case "furthest" -> EntitySelectorParser.ORDER_FURTHEST;
				case "random" -> EntitySelectorParser.ORDER_RANDOM;
				case "arbitrary" -> EntitySelector.ORDER_ARBITRARY;
				default -> {
					reader.getReader().setCursor(i);
					throw IRREVERSIBLE_SORT_EXCEPTION.createWithContext(reader.getReader(), string);
				}
			});
			reader.setHasSorter(true);
		}, reader -> !reader.isSenderOnly() && !reader.hasSorter(), Component.translatable("argument.entity.options.sort.description"));
		CEntitySelectorOptions.putOption("gamemode", reader -> {
			reader.setSuggestionProvider((builder, consumer) -> {
				String string = builder.getRemaining().toLowerCase(Locale.ROOT);
				boolean bl = !reader.excludesGameMode();
				boolean bl2 = true;
				if (!string.isEmpty()) {
					if (string.charAt(0) == '!') {
						bl = false;
						string = string.substring(1);
					} else {
						bl2 = false;
					}
				}
				for (GameType gameMode : GameType.values()) {
					if (!gameMode.getName().toLowerCase(Locale.ROOT).startsWith(string)) continue;
					if (bl2) {
						builder.suggest("!" + gameMode.getName());
					}
					if (!bl) continue;
					builder.suggest(gameMode.getName());
				}
				return builder.buildFuture();
			});
			int i = reader.getReader().getCursor();
			boolean bl = reader.readNegationCharacter();
			if (reader.excludesGameMode() && !bl) {
				reader.getReader().setCursor(i);
				throw INAPPLICABLE_OPTION_EXCEPTION.createWithContext(reader.getReader(), "gamemode");
			}
			String string = reader.getReader().readUnquotedString();
			GameType gameMode = GameType.byName(string, null);
			if (gameMode == null) {
				reader.getReader().setCursor(i);
				throw INVALID_MODE_EXCEPTION.createWithContext(reader.getReader(), string);
			}
			reader.setIncludesNonPlayers(false);
			reader.setPredicate(entity -> {
				if (!(entity instanceof AbstractClientPlayer player)) {
					return false;
				}

                assert Minecraft.getInstance().player != null;
                GameType gameMode2 = Objects.requireNonNull(Minecraft.getInstance().player.connection.getPlayerInfo(player.getUUID())).getGameMode();
				return bl == (gameMode2 != gameMode);
			});
			if (bl) {
				reader.setExcludesGameMode(true);
			} else {
				reader.setSelectsGameMode(true);
			}
		}, reader -> !reader.selectsGameMode(), Component.translatable("argument.entity.options.gamemode.description"));
		CEntitySelectorOptions.putOption("team", reader -> {
			boolean bl = reader.readNegationCharacter();
			String string = reader.getReader().readUnquotedString();
			reader.setPredicate(entity -> {
				if (!(entity instanceof LivingEntity)) {
					return false;
				}
				Team abstractTeam = entity.getTeam();
				String string2 = abstractTeam == null ? "" : abstractTeam.getName();
				return string2.equals(string) != bl;
			});
			if (bl) {
				reader.setExcludesTeam(true);
			} else {
				reader.setSelectsTeam(true);
			}
		}, reader -> !reader.selectsTeam(), Component.translatable("argument.entity.options.team.description"));
		CEntitySelectorOptions.putOption("type", reader -> {
			reader.setSuggestionProvider((builder, consumer) -> {
				SharedSuggestionProvider.suggestResource(ForgeRegistries.ENTITY_TYPES.getKeys(), builder, String.valueOf('!'));
				SharedSuggestionProvider.suggestResource(ForgeRegistries.ENTITY_TYPES.getKeys().stream().map(tagKey -> ((TagKeyAccessor) tagKey).getLocation()), builder, "!#");
				if (!reader.excludesEntityType()) {
					SharedSuggestionProvider.suggestResource(ForgeRegistries.ENTITY_TYPES.getKeys(), builder);
					SharedSuggestionProvider.suggestResource(ForgeRegistries.ENTITY_TYPES.getKeys().stream().map(tagKey -> ((TagKeyAccessor) tagKey).getLocation()), builder, String.valueOf('#'));
				}
				return builder.buildFuture();
			});
			int i = reader.getReader().getCursor();
			boolean bl = reader.readNegationCharacter();
			if (reader.excludesEntityType() && !bl) {
				reader.getReader().setCursor(i);
				throw INAPPLICABLE_OPTION_EXCEPTION.createWithContext(reader.getReader(), "type");
			}
			if (bl) {
				reader.setExcludesEntityType();
			}
			if (reader.readTagCharacter()) {
				TagKey<EntityType<?>> tagKey = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.read(reader.getReader()));
				reader.setPredicate(entity -> entity.getType().is(tagKey) != bl);
			} else {
				ResourceLocation tagKey = ResourceLocation.read(reader.getReader());
				EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(tagKey);
				if (entityType == null) {
					reader.getReader().setCursor(i);
					throw INVALID_TYPE_EXCEPTION.createWithContext(reader.getReader(), tagKey.toString());
				}
				if (Objects.equals(EntityType.PLAYER, entityType) && !bl) {
					reader.setIncludesNonPlayers(false);
				}
				reader.setPredicate(entity -> Objects.equals(entityType, entity.getType()) != bl);
				if (!bl) {
					reader.setEntityType(entityType);
				}
			}
		}, reader -> !reader.selectsEntityType(), Component.translatable("argument.entity.options.type.description"));
		CEntitySelectorOptions.putOption("tag", reader -> {
			boolean bl = reader.readNegationCharacter();
			String string = reader.getReader().readUnquotedString();
			reader.setPredicate(entity -> {
				if ("".equals(string)) {
					return entity.getTags().isEmpty() != bl;
				}
				return entity.getTags().contains(string) != bl;
			});
		}, reader -> true, Component.translatable("argument.entity.options.tag.description"));
		CEntitySelectorOptions.putOption("nbt", reader -> {
			boolean bl = reader.readNegationCharacter();
			CompoundTag nbtCompound = new TagParser(reader.getReader()).readStruct();
			reader.setPredicate(entity -> {
				ItemStack itemStack;
				CompoundTag nbtCompound2 = entity.saveWithoutId(new CompoundTag());
				if (entity instanceof RemotePlayer && !(itemStack = ((RemotePlayer) entity).getInventory().player.getMainHandItem()).isEmpty()) {
					nbtCompound2.put("SelectedItem", itemStack.save(new CompoundTag()));
				}
				return NbtUtils.compareNbt(nbtCompound, nbtCompound2, true) != bl;
			});
		}, reader -> true, Component.translatable("argument.entity.options.nbt.description"));
		CEntitySelectorOptions.putOption("scores", reader -> {
			StringReader stringReader = reader.getReader();
			HashMap<String, MinMaxBounds.Ints> map = Maps.newHashMap();
			stringReader.expect('{');
			stringReader.skipWhitespace();
			while (stringReader.canRead() && stringReader.peek() != '}') {
				stringReader.skipWhitespace();
				String string = stringReader.readUnquotedString();
				stringReader.skipWhitespace();
				stringReader.expect('=');
				stringReader.skipWhitespace();
				MinMaxBounds.Ints intRange = MinMaxBounds.Ints.fromReader(stringReader);
				map.put(string, intRange);
				stringReader.skipWhitespace();
				if (!stringReader.canRead() || stringReader.peek() != ',') continue;
				stringReader.skip();
			}
			stringReader.expect('}');
			if (!map.isEmpty()) {
				reader.setPredicate(entity -> {
					Scoreboard scoreboard = entity.level().getScoreboard();
					String string = entity.getScoreboardName();
					for (Map.Entry<String, MinMaxBounds.Ints> entry : map.entrySet()) {
						Objective scoreboardObjective = scoreboard.getObjective(entry.getKey());
						if (scoreboardObjective == null) {
							return false;
						}
						if (!scoreboard.hasPlayerScore(string, scoreboardObjective)) {
							return false;
						}
						Score scoreboardPlayerScore = scoreboard.getOrCreatePlayerScore(string, scoreboardObjective);
						int i = scoreboardPlayerScore.getScore();
						if (entry.getValue().matches(i)) {
							continue;
						}
						return false;
					}
					return true;
				});
			}
			reader.setSelectsScores(true);
		}, reader -> !reader.selectsScores(), Component.translatable("argument.entity.options.scores.description"));
		CEntitySelectorOptions.putOption("advancements", reader -> {
			StringReader stringReader = reader.getReader();
			Map<ResourceLocation, Predicate<AdvancementProgress>> map = new HashMap<>();
			stringReader.expect('{');
			stringReader.skipWhitespace();
			while (stringReader.canRead() && stringReader.peek() != '}') {
				stringReader.skipWhitespace();
				ResourceLocation identifier = ResourceLocation.read(stringReader);
				stringReader.skipWhitespace();
				stringReader.expect('=');
				stringReader.skipWhitespace();
				if (stringReader.canRead() && stringReader.peek() == '{') {
					HashMap<String, Predicate<CriterionProgress>> map2 = Maps.newHashMap();
					stringReader.skipWhitespace();
					stringReader.expect('{');
					stringReader.skipWhitespace();
					while (stringReader.canRead() && stringReader.peek() != '}') {
						stringReader.skipWhitespace();
						String string = stringReader.readUnquotedString();
						stringReader.skipWhitespace();
						stringReader.expect('=');
						stringReader.skipWhitespace();
						boolean bl = stringReader.readBoolean();
						map2.put(string, criterionProgress -> criterionProgress.isDone() == bl);
						stringReader.skipWhitespace();
						if (!stringReader.canRead() || stringReader.peek() != ',') continue;
						stringReader.skip();
					}
					stringReader.skipWhitespace();
					stringReader.expect('}');
					stringReader.skipWhitespace();
					map.put(identifier, advancementProgress -> {
						for (Map.Entry<String, Predicate<CriterionProgress>> entry : map2.entrySet()) {
							CriterionProgress criterionProgress = advancementProgress.getCriterion(entry.getKey());
							if (criterionProgress != null && entry.getValue().test(criterionProgress)) continue;
							return false;
						}
						return true;
					});
				} else {
					boolean map2 = stringReader.readBoolean();
					map.put(identifier, advancementProgress -> advancementProgress.isDone() == map2);
				}
				stringReader.skipWhitespace();
				if (!stringReader.canRead() || stringReader.peek() != ',') {
					continue;
				}
				stringReader.skip();
			}
			stringReader.expect('}');
			if (!map.isEmpty()) {
				reader.setPredicate(entity -> false);
				reader.setIncludesNonPlayers(false);
			}
			reader.setSelectsAdvancements(true);
		}, reader -> !reader.selectsAdvancements(), Component.translatable("argument.entity.options.advancements.description"));
		CEntitySelectorOptions.putOption("predicate", reader -> reader.setPredicate(entity -> false), reader -> true, Component.translatable("argument.entity.options.predicate.description"));
	}

	public static SelectorHandler getHandler(CEntitySelectorReader reader, String option, int restoreCursor) throws CommandSyntaxException {
		SelectorOption selectorOption = OPTIONS.get(option);
		if (selectorOption != null) {
			if (selectorOption.condition.test(reader)) {
				return selectorOption.handler;
			}
			throw INAPPLICABLE_OPTION_EXCEPTION.createWithContext(reader.getReader(), option);
		}
		reader.getReader().setCursor(restoreCursor);
		throw UNKNOWN_OPTION_EXCEPTION.createWithContext(reader.getReader(), option);
	}

	public static void suggestOptions(CEntitySelectorReader reader, SuggestionsBuilder suggestionBuilder) {
		String string = suggestionBuilder.getRemaining().toLowerCase(Locale.ROOT);
		for (Map.Entry<String, SelectorOption> entry : OPTIONS.entrySet()) {
			if (!entry.getValue().condition.test(reader) || !entry.getKey().toLowerCase(Locale.ROOT).startsWith(string)) {
				continue;
			}
			suggestionBuilder.suggest(entry.getKey() + "=", entry.getValue().description);
		}
	}

	private static class SelectorOption {
		public final SelectorHandler handler;
		public final Predicate<CEntitySelectorReader> condition;
		public final Component description;

		SelectorOption(SelectorHandler handler, Predicate<CEntitySelectorReader> condition, Component description) {
			this.handler = handler;
			this.condition = condition;
			this.description = description;
		}
	}

	public interface SelectorHandler {
		void handle(CEntitySelectorReader reader) throws CommandSyntaxException;
	}
}
