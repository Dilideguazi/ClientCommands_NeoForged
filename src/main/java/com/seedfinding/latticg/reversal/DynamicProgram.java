/*
 * Decompiled with CFR 0.152.
 */
package com.seedfinding.latticg.reversal;

import com.seedfinding.latticg.reversal.Program;
import com.seedfinding.latticg.reversal.ProgramBuilder;
import com.seedfinding.latticg.reversal.ProgramInstance;
import com.seedfinding.latticg.reversal.calltype.CallType;
import com.seedfinding.latticg.util.LCG;
import com.seedfinding.latticg.util.Rand;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.LongStream;

public final class DynamicProgram {
    private final ProgramBuilder programBuilder;
    private final List<Object> values = new ArrayList<Object>();
    private boolean verbose = false;
    private boolean valid = true;

    private DynamicProgram(LCG lcg) {
        this.programBuilder = Program.builder(lcg);
    }

    public static DynamicProgram create(LCG lcg) {
        return new DynamicProgram(lcg);
    }

    public <T> DynamicProgram add(CallType<T> callType, T value) {
        this.checkValid();
        this.programBuilder.add(callType);
        this.values.add(value);
        return this;
    }

    public DynamicProgram add(CallType<Boolean> callType) {
        return this.add(callType, true);
    }

    public DynamicProgram filteredSkip(Predicate<Rand> filter, long steps) {
        this.checkValid();
        this.programBuilder.filteredSkip(filter, steps);
        return this;
    }

    public DynamicProgram skip(long steps) {
        this.checkValid();
        this.programBuilder.skip(steps);
        return this;
    }

    public LongStream reverse() {
        this.checkValid();
        Program program = this.programBuilder.build();
        if (this.verbose) {
            program.setVerbose(true);
        }
        ProgramInstance instance = program.start();
        for (Object value : this.values) {
            instance.add(value);
        }
        LongStream seeds = instance.reverse();
        this.valid = false;
        return seeds;
    }

    private void checkValid() {
        if (!this.valid) {
            throw new IllegalStateException("This DynamicProgram has already been used");
        }
    }

    public boolean isVerbose() {
        return this.verbose;
    }

    public DynamicProgram setVerbose(boolean verbose) {
        this.verbose = verbose;
        return this;
    }
}

