package dev.xpple.clientarguments.arguments;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CEntitySelector {

	private static final EntityTypeTest<Entity, ?> PASSTHROUGH_FILTER = new EntityTypeTest<>() {
		@Override
		public Entity tryCast(@NotNull Entity entity) {
			return entity;
		}

		@Override
		public @NotNull Class<? extends Entity> getBaseClass() {
			return Entity.class;
		}
	};
	private final int limit;
	private final boolean includesNonPlayers;
	private final Predicate<Entity> basePredicate;
	private final MinMaxBounds.Doubles distance;
	private final Function<Vec3, Vec3> positionOffset;
	@Nullable
	private final AABB box;
	private final BiConsumer<Vec3, List<? extends Entity>> sorter;
	private final boolean senderOnly;
	@Nullable
	private final String playerName;
	@Nullable
	private final UUID uuid;
	private final EntityTypeTest<Entity, ?> entityFilter;
	private final boolean usesAt;

	public CEntitySelector(int count, boolean includesNonPlayers, Predicate<Entity> basePredicate, MinMaxBounds.Doubles distance, Function<Vec3, Vec3> positionOffset, @Nullable AABB box, BiConsumer<Vec3, List<? extends Entity>> sorter, boolean senderOnly, @Nullable String playerName, @Nullable UUID uuid, @Nullable EntityType<?> type, boolean usesAt) {
		this.limit = count;
		this.includesNonPlayers = includesNonPlayers;
		this.basePredicate = basePredicate;
		this.distance = distance;
		this.positionOffset = positionOffset;
		this.box = box;
		this.sorter = sorter;
		this.senderOnly = senderOnly;
		this.playerName = playerName;
		this.uuid = uuid;
		this.entityFilter = type == null ? PASSTHROUGH_FILTER : type;
		this.usesAt = usesAt;
	}

	public int getLimit() {
		return this.limit;
	}

	public boolean includesNonPlayers() {
		return this.includesNonPlayers;
	}

	public boolean isSenderOnly() {
		return this.senderOnly;
	}

	public boolean usesAt() {
		return this.usesAt;
	}

	public Entity getEntity(FabricClientCommandSource source) throws CommandSyntaxException {
		List<? extends Entity> list = this.getEntities(source);
		if (list.isEmpty()) {
			throw CEntityArgumentType.ENTITY_NOT_FOUND_EXCEPTION.create();
		}
		if (list.size() > 1) {
			throw CEntityArgumentType.TOO_MANY_ENTITIES_EXCEPTION.create();
		}
		return list.get(0);
	}

	public List<? extends Entity> getEntities(FabricClientCommandSource source) throws CommandSyntaxException {
		if (!this.includesNonPlayers) {
			return this.getPlayers(source);
		}
		if (this.playerName != null) {
			AbstractClientPlayer abstractClientPlayerEntity = Streams.stream(source.getWorld().entitiesForRendering())
					.filter(entity -> entity instanceof AbstractClientPlayer)
					.map(entity -> (AbstractClientPlayer) entity)
					.filter(abstractPlayer -> abstractPlayer.getName().getString().equals(this.playerName))
					.findAny().orElse(null);
			return abstractClientPlayerEntity == null ? Collections.emptyList() : Lists.newArrayList(abstractClientPlayerEntity);
		}
		if (this.uuid != null) {
			Entity foundEntity = Streams.stream(source.getWorld().entitiesForRendering())
					.filter(entity -> entity.getUUID().equals(this.uuid))
					.findAny().orElse(null);
			return foundEntity == null ? Collections.emptyList() : Lists.newArrayList(foundEntity);
		}
		Vec3 pos = this.positionOffset.apply(source.getPosition());
		Predicate<Entity> predicate = this.getPositionPredicate(pos);
		if (this.senderOnly) {
			if (source.getEntity() != null && predicate.test(source.getEntity())) {
				return Lists.newArrayList(source.getEntity());
			}
			return Collections.emptyList();
		}
		ArrayList<Entity> entity = new ArrayList<>();
		this.appendEntitiesFromWorld(entity, source.getWorld(), pos, predicate);
		return this.getEntities(pos, entity);
	}

	private void appendEntitiesFromWorld(List<Entity> result, ClientLevel clientWorld, Vec3 pos, Predicate<Entity> predicate) {
		if (this.box != null) {
			result.addAll(clientWorld.getEntities(this.entityFilter, this.box.move(pos), predicate));
		} else {
			clientWorld.entitiesForRendering().forEach(entity -> {
				if (predicate.test(entity)) {
					result.add(entity);
				}
				if (entity instanceof EnderDragon enderDragon) {
					for (EnderDragonPart bodyPart : enderDragon.getSubEntities()) {
						Entity e = entityFilter.tryCast(bodyPart);
						if (e == null || !predicate.test(e)) {
							continue;
						}
						result.add(e);
					}
				}
			});
		}
	}

	public AbstractClientPlayer getPlayer(FabricClientCommandSource source) throws CommandSyntaxException {
		List<AbstractClientPlayer> list = this.getPlayers(source);
		if (list.size() != 1) {
			throw CEntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();
		}
		return list.get(0);
	}

	public List<AbstractClientPlayer> getPlayers(FabricClientCommandSource source) throws CommandSyntaxException {
		AbstractClientPlayer abstractClientPlayerEntity;
		if (this.playerName != null) {
			abstractClientPlayerEntity = Streams.stream(source.getWorld().entitiesForRendering())
					.filter(entity -> entity instanceof AbstractClientPlayer)
					.map(entity -> (AbstractClientPlayer) entity)
					.filter(abstractPlayer -> abstractPlayer.getName().getString().equals(this.playerName))
					.findAny().orElse(null);
			return abstractClientPlayerEntity == null ? Collections.emptyList() : Lists.newArrayList(abstractClientPlayerEntity);
		}
		if (this.uuid != null) {
			abstractClientPlayerEntity = Streams.stream(source.getWorld().entitiesForRendering())
					.filter(entity -> entity instanceof AbstractClientPlayer)
					.map(entity -> (AbstractClientPlayer) entity)
					.filter(entity -> entity.getUUID().equals(this.uuid))
					.findAny().orElse(null);
			return abstractClientPlayerEntity == null ? Collections.emptyList() : Lists.newArrayList(abstractClientPlayerEntity);
		}
		Vec3 pos = this.positionOffset.apply(source.getPosition());
		Predicate<Entity> predicate = this.getPositionPredicate(pos);
		if (this.senderOnly) {
			if (source.getEntity() instanceof AbstractClientPlayer player && predicate.test(player)) {
				return Lists.newArrayList(player);
			}
			return Collections.emptyList();
		}
		List<AbstractClientPlayer> entities = source.getWorld().players().stream()
				.filter(predicate)
				.collect(Collectors.toList());
		return this.getEntities(pos, entities);
	}

	private Predicate<Entity> getPositionPredicate(Vec3 pos) {
		Predicate<Entity> predicate = this.basePredicate;
		if (this.box != null) {
			AABB box = this.box.move(pos);
			predicate = predicate.and(entity -> box.intersects(entity.getBoundingBox()));
		}
		if (!this.distance.isAny()) {
			predicate = predicate.and(entity -> this.distance.matchesSqr(entity.distanceToSqr(pos)));
		}
		return predicate;
	}

	private <T extends Entity> List<T> getEntities(Vec3 pos, List<T> entities) {
		if (entities.size() > 1) {
			this.sorter.accept(pos, entities);
		}
		return entities.subList(0, Math.min(this.limit, entities.size()));
	}

	public static Component getNames(List<? extends Entity> entities) {
		return ComponentUtils.formatList(entities, Entity::getDisplayName);
	}
}
