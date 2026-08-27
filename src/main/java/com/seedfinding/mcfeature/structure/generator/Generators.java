package com.seedfinding.mcfeature.structure.generator;

import com.seedfinding.mcfeature.Feature;
import com.seedfinding.mcfeature.structure.*;
import com.seedfinding.mcfeature.structure.generator.structure.*;

import java.util.HashMap;
import java.util.Map;

public class Generators {
	private static final Map<Class<? extends Feature<?, ?>>, Generator.GeneratorFactory<?>> REGISTRY = new HashMap<>();

	static {
		register(DesertPyramid.class, DesertPyramidGenerator::new);
		register(BuriedTreasure.class, BuriedTreasureGenerator::new);
		register(Shipwreck.class, ShipwreckGenerator::new);
		register(EndCity.class, EndCityGenerator::new);
		register(RuinedPortal.class, RuinedPortalGenerator::new);
		register(Village.class, VillageGenerator::new);
	}

	public static <T extends Feature<?, ?>> void register(Class<T> clazz, Generator.GeneratorFactory<?> lootFactory) {
		REGISTRY.put(clazz, lootFactory);
	}

	public static <T extends Feature<?, ?>> Generator.GeneratorFactory<?> get(Class<T> clazz) {
		return REGISTRY.get(clazz);
	}
}
