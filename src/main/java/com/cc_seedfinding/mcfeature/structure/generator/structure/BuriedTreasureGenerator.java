package com.cc_seedfinding.mcfeature.structure.generator.structure;

import com.cc_seedfinding.mccore.rand.ChunkRand;
import com.cc_seedfinding.mccore.util.data.Pair;
import com.cc_seedfinding.mccore.util.pos.BPos;
import com.cc_seedfinding.mccore.util.pos.CPos;
import com.cc_seedfinding.mccore.version.MCVersion;
import com.cc_seedfinding.mcfeature.loot.ChestContent;
import com.cc_seedfinding.mcfeature.loot.LootTable;
import com.cc_seedfinding.mcfeature.loot.MCLootTables;
import com.cc_seedfinding.mcfeature.structure.generator.Generator;
import com.cc_seedfinding.mcterrain.TerrainGenerator;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class BuriedTreasureGenerator extends Generator {
	private CPos cPos;

	public BuriedTreasureGenerator(MCVersion version) {
		super(version);
	}

	@Override
	public boolean generate(TerrainGenerator generator, int chunkX, int chunkZ, ChunkRand rand) {
		cPos = new CPos(chunkX, chunkZ);
		return true;
	}

	@Override
	public List<Pair<ILootType, BPos>> getLootPos() {
		return getChestsPos();
	}

	@Override
	public List<Pair<ILootType, BPos>> getChestsPos() {
		return Collections.singletonList(new Pair<>(LootType.BURIED_CHEST, cPos.toBlockPos().add(9, 90, 9)));
	}

	@Override
	public ILootType[] getLootTypes() {
		return LootType.values();
	}

	public enum LootType implements ILootType {
		BURIED_CHEST(MCLootTables.BURIED_TREASURE_CHEST, ChestContent.ChestType.SINGLE_CHEST),
		;

		public final Supplier<LootTable> lootTable;
		public final ChestContent.ChestType chestType;

		LootType(Supplier<LootTable> lootTable, ChestContent.ChestType chestType) {
			this.lootTable = lootTable;
			this.chestType = chestType;
		}

		@Override
		public LootTable getLootTableUncached(MCVersion version) {
			return lootTable.get().apply(version);
		}

		@Override
		public ChestContent.ChestType getChestType() {
			return chestType;
		}
	}

}
