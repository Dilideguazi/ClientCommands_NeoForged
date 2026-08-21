package com.cc_seedfinding.mcmath.component;

@FunctionalInterface
public interface Norm<C, R> {

	R get(C component);

}
