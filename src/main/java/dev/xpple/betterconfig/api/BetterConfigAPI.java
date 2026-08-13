/*
 * Decompiled with CFR 0.152.
 */
package dev.xpple.betterconfig.api;

import dev.xpple.betterconfig.impl.BetterConfigImpl;

public interface BetterConfigAPI {
    static BetterConfigAPI getInstance() {
        return BetterConfigImpl.INSTANCE;
    }

    ModConfig getModConfig(String var1);
}

