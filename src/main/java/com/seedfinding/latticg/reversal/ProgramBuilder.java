/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.ApiStatus$Experimental
 */
package com.seedfinding.latticg.reversal;

import com.seedfinding.latticg.reversal.Program;
import com.seedfinding.latticg.reversal.calltype.CallType;
import com.seedfinding.latticg.reversal.calltype.FilteredSkip;
import com.seedfinding.latticg.util.LCG;
import com.seedfinding.latticg.util.Rand;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public class ProgramBuilder {
    private final LCG lcg;
    private final List<CallType<?>> calls = new ArrayList();
    private final List<Long> skips = new ArrayList<Long>();
    private final List<FilteredSkip> filteredSkips = new ArrayList<FilteredSkip>();
    private long currentSkip = 0L;
    private long currentIndex = 0L;

    ProgramBuilder(LCG lcg) {
        this.lcg = lcg;
    }

    public ProgramBuilder add(CallType<?> call) {
        this.calls.add(call);
        this.skips.add(this.currentSkip);
        this.currentSkip = 0L;
        this.currentIndex += (long)call.getNumCalls();
        return this;
    }

    public ProgramBuilder skip(long steps) {
        this.currentSkip += steps;
        this.currentIndex += steps;
        return this;
    }

    public ProgramBuilder filteredSkip(Predicate<Rand> filter, long steps) {
        this.filteredSkips.add(new FilteredSkip(this.currentIndex, filter));
        this.currentSkip += steps;
        this.currentIndex += steps;
        return this;
    }

    public Program build() {
        return new Program(this.lcg, this.calls, this.skips, this.filteredSkips);
    }
}

