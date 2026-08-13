/*
 * Decompiled with CFR 0.152.
 */
package dev.xpple.betterconfig.api;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.nio.file.Path;

public interface ModConfig {
    String getModId();

    Class<?> getConfigsClass();

    Path getConfigsPath();

    Object get(String var1);

    String asString(String var1);

    void reset(String var1);

    void set(String var1, Object var2) throws CommandSyntaxException;

    void add(String var1, Object var2) throws CommandSyntaxException;

    void put(String var1, Object var2, Object var3) throws CommandSyntaxException;

    void remove(String var1, Object var2) throws CommandSyntaxException;

    void resetTemporaryConfigs();

    boolean save();
}

