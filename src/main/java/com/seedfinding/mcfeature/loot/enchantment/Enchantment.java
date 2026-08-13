package com.seedfinding.mcfeature.loot.enchantment;

import java.util.HashSet;
import java.util.function.BiPredicate;

public class Enchantment {
	private String name;
	private Integer rarity;
	private HashSet<String> category;
	private Integer minLevel;
	private Integer maxLevel;
	private BiPredicate<Integer, Integer> isLowerThanMinCost;
	private BiPredicate<Integer, Integer> isHigherThanMaxCost;
	private HashSet<String> incompatible;
	private boolean isTreasure;
	private boolean isDiscoverable;


	public Enchantment(String name, Integer rarity, HashSet<String> category, Integer minLevel, Integer maxLevel, HashSet<String> incompatible) {
		this(name, rarity, category, minLevel, maxLevel, (n, i) -> (n < 1 + (i * 10)), (n, i) -> (n > (6 + (i * 10))), incompatible);
	}

	public Enchantment(String name, Integer rarity, HashSet<String> category, Integer minLevel, Integer maxLevel, BiPredicate<Integer, Integer> minCost, BiPredicate<Integer, Integer> maxCost, HashSet<String> incompatible) {
		this(name, rarity, category, minLevel, maxLevel, minCost, maxCost, incompatible, false, true);
	}

	public Enchantment(String name, Integer rarity, HashSet<String> category, Integer minLevel, Integer maxLevel, BiPredicate<Integer, Integer> minCost, BiPredicate<Integer, Integer> maxCost, HashSet<String> incompatible, boolean isTreasure) {
		this(name, rarity, category, minLevel, maxLevel, minCost, maxCost, incompatible, isTreasure, true);
	}

	public Enchantment(String name, Integer rarity, HashSet<String> category, Integer minLevel, Integer maxLevel, BiPredicate<Integer, Integer> minCost, BiPredicate<Integer, Integer> maxCost, HashSet<String> incompatible, boolean isTreasure, boolean isDiscoverable) {
		this.name = name;
		this.rarity = rarity;
		this.category = category;
		this.minLevel = minLevel;
		this.maxLevel = maxLevel;
		this.isLowerThanMinCost = minCost;
		this.isHigherThanMaxCost = maxCost;
		this.incompatible = incompatible;
		this.isTreasure = isTreasure;
		this.isDiscoverable = isDiscoverable;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getRarity() {
		return rarity;
	}

	@SuppressWarnings("unused")
	public void setRarity(Integer rarity) {
		this.rarity = rarity;
	}

	public HashSet<String> getCategory() {
		return category;
	}

	public void setCategory(HashSet<String> category) {
		this.category = category;
	}

	public Integer getMinLevel() {
		return minLevel;
	}

	@SuppressWarnings("unused")
	public void setMinLevel(Integer minLevel) {
		this.minLevel = minLevel;
	}

	public Integer getMaxLevel() {
		return maxLevel;
	}

	@SuppressWarnings("unused")
	public void setMaxLevel(Integer maxLevel) {
		this.maxLevel = maxLevel;
	}

	public boolean isTreasure() {
		return isTreasure;
	}

	@SuppressWarnings("unused")
	public void setTreasure(boolean treasure) {
		isTreasure = treasure;
	}

	public BiPredicate<Integer, Integer> getIsLowerThanMinCost() {
		return isLowerThanMinCost;
	}

	@SuppressWarnings("unused")
	public void setIsLowerThanMinCost(
		BiPredicate<Integer, Integer> isLowerThanMinCost) {
		this.isLowerThanMinCost = isLowerThanMinCost;
	}

	public BiPredicate<Integer, Integer> getIsHigherThanMaxCost() {
		return isHigherThanMaxCost;
	}

	@SuppressWarnings("unused")
	public void setIsHigherThanMaxCost(
		BiPredicate<Integer, Integer> isHigherThanMaxCost) {
		this.isHigherThanMaxCost = isHigherThanMaxCost;
	}

	public boolean isDiscoverable() {
		return isDiscoverable;
	}

	@SuppressWarnings("unused")
	public void setDiscoverable(boolean discoverable) {
		isDiscoverable = discoverable;
	}

	public HashSet<String> getIncompatible() {
		return incompatible;
	}

	public void setIncompatible(HashSet<String> incompatible) {
		this.incompatible = incompatible;
	}
}
