/*
 * Decompiled with CFR 0.152.
 */
package dev.xpple.betterconfig.impl;

import dev.xpple.betterconfig.api.BetterConfigAPI;
import dev.xpple.betterconfig.api.ModConfig;
import java.util.HashMap;
import java.util.Map;

public class BetterConfigImpl
implements BetterConfigAPI {
    private static final Map<String, ModConfigImpl> modConfigs = new HashMap<>();
    public static final BetterConfigImpl INSTANCE = new BetterConfigImpl();

    @Override
    public ModConfig getModConfig(String modId) {
        ModConfig modConfig = modConfigs.get(modId);
        if (modConfig == null) {
            throw new IllegalArgumentException(modId);
        }
        return modConfig;
    }

    public static Map<String, ModConfigImpl> getModConfigs() {
        return modConfigs;
    }
}

