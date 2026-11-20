package com.cc_seedfinding.mcfeature.structure.generator;

import com.cc_seedfinding.mcbiome.source.BiomeSource;
import com.cc_seedfinding.mccore.rand.ChunkRand;
import com.cc_seedfinding.mccore.state.Dimension;
import com.cc_seedfinding.mccore.util.data.Pair;
import com.cc_seedfinding.mccore.util.pos.BPos;
import com.cc_seedfinding.mccore.util.pos.CPos;
import com.cc_seedfinding.mccore.version.MCVersion;
import com.cc_seedfinding.mcfeature.loot.ChestContent;
import com.cc_seedfinding.mcfeature.loot.LootTable;
import com.cc_seedfinding.mcfeature.loot.entry.ItemEntry;
import com.cc_seedfinding.mcfeature.loot.item.Item;
import com.cc_seedfinding.mcterrain.TerrainGenerator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Generator {
	protected final MCVersion version;
	public static final ConcurrentHashMap<String, LootTable> LOOT_TABLE_CACHE = new ConcurrentHashMap<>();

	public Generator(MCVersion version) {
		this.version = version;
	}

	public MCVersion getVersion() {
		return version;
	}

	public boolean generate(long worldSeed, Dimension dimension, int chunkX, int chunkZ) {
		BiomeSource biomeSource = BiomeSource.of(dimension, this.getVersion(), worldSeed);
		TerrainGenerator generator = TerrainGenerator.of(dimension, biomeSource);
		return this.generate(generator, chunkX, chunkZ);
	}

	public boolean generate(TerrainGenerator generator, CPos cPos) {
		return this.generate(generator, cPos, new ChunkRand());
	}

	public boolean generate(TerrainGenerator generator, int chunkX, int chunkZ) {
		return this.generate(generator, chunkX, chunkZ, new ChunkRand());
	}

	public boolean generate(TerrainGenerator generator, CPos cPos, ChunkRand rand) {
		return this.generate(generator, cPos.getX(), cPos.getZ(), rand);
	}

	public abstract boolean generate(TerrainGenerator generator, int chunkX, int chunkZ, ChunkRand rand);

	public abstract List<Pair<ILootType, BPos>> getChestsPos();

	public abstract List<Pair<ILootType, BPos>> getLootPos();

	public List<Pair<ILootType, CPos>> getChestsChunkPos() {
		return this.getChestsPos().stream().map(e -> new Pair<>(e.getFirst(), e.getSecond().toChunkPos())).collect(Collectors.toList());
	}

	public interface ILootType {
		default LootTable getLootTable(MCVersion version) {
			String className = this.getClass().getCanonicalName();
			String enumName = ((Enum<?>)this).name();
			return LOOT_TABLE_CACHE.computeIfAbsent(className + enumName + version.name(), ignored -> getLootTableUncached(version));
		}

		LootTable getLootTableUncached(MCVersion version);

		ChestContent.ChestType getChestType();

		default boolean belongSameStructure(ILootType other) {
			return this == other;
		}
	}

	@FunctionalInterface
	public interface GeneratorFactory<T extends Generator> {
		T create(MCVersion version);
	}

	public abstract ILootType[] getLootTypes();

	public Set<Item> getPossibleLootItems() {
		Set<Item> items = new HashSet<>();
		ILootType[] lootTypes = getLootTypes();
		for(ILootType lootType : lootTypes) {
			LootTable lootTable = lootType.getLootTable(this.getVersion());
			if(lootTable != null) {
				items.addAll(Arrays.stream(lootTable.lootPools)
					.map(e -> e.lootEntries).flatMap(Stream::of)
					.filter(e -> e instanceof ItemEntry)
					.map(e -> ((ItemEntry)e).item).collect(Collectors.toList()));
			}

		}
		return items;
	}

}
