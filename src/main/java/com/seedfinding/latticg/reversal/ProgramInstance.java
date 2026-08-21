/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.ApiStatus$Internal
 */
package com.seedfinding.latticg.reversal;

import com.seedfinding.latticg.JavaRandomReverser;
import com.seedfinding.latticg.reversal.Program;
import com.seedfinding.latticg.reversal.calltype.CallType;
import com.seedfinding.latticg.reversal.calltype.java.NextBooleanCall;
import com.seedfinding.latticg.reversal.calltype.java.NextDoubleCall;
import com.seedfinding.latticg.reversal.calltype.java.NextFloatCall;
import com.seedfinding.latticg.reversal.calltype.java.NextIntCall;
import com.seedfinding.latticg.reversal.calltype.java.NextLongCall;
import com.seedfinding.latticg.reversal.calltype.java.UnboundedNextIntCall;
import com.seedfinding.latticg.util.LCG;
import com.seedfinding.latticg.util.Range;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;
import org.jetbrains.annotations.ApiStatus;

public class ProgramInstance {
    private final Program program;
    private final List<Object> observations = new ArrayList<Object>();
    private int callIndex = 0;

    @ApiStatus.Internal
    protected ProgramInstance(Program program) {
        this.program = program;
    }

    public Program getProgram() {
        return this.program;
    }

    public <T> ProgramInstance add(Object value) {
        if (this.callIndex >= this.program.getCalls().size()) {
            throw new IndexOutOfBoundsException("Too many observations for the number of calls specified");
        }
        CallType<?> callType = this.program.getCalls().get(this.callIndex++);
        Object observation = callType.getType().cast(value);
        this.observations.add(observation);
        return this;
    }

    public LongStream reverse() {
        if (!LCG.JAVA.equals(this.program.getLcg())) {
            throw new IllegalStateException("Only the Java LCG is currently supported");
        }
        if (this.callIndex != this.program.getCalls().size()) {
            throw new IllegalStateException("Not all specified calls have been given observations");
        }
        JavaRandomReverser reverser = new JavaRandomReverser(this.program.getFilteredSkips());
        if (this.program.isVerbose()) {
            reverser.setVerbose(true);
        }
        List<CallType<?>> calls = this.program.getCalls();
        List<Long> skips = this.program.getSkips();
        for (int i = 0; i < calls.size(); ++i) {
            int max;
            int value;
            CallType<?> call = calls.get(i);
            Object observation = this.observations.get(i);
            reverser.addUnmeasuredSeeds(skips.get(i));
            if (call instanceof NextBooleanCall) {
                NextBooleanCall booleanCall = (NextBooleanCall)call;
                value = (Boolean) observation ? 1 : 0;
                if (booleanCall.isInverted()) {
                    value = value == 0 ? 1 : 0;
                }
                reverser.addNextBooleanCall(value != 0);
                continue;
            }
            if (call instanceof NextDoubleCall) {
                double value2 = (Double)observation;
                reverser.addNextDoubleCall(value2, value2, true, true);
                continue;
            }
            if (call instanceof NextFloatCall) {
                float value3 = ((Float)observation).floatValue();
                reverser.addNextFloatCall(value3, value3, true, true);
                continue;
            }
            if (call instanceof NextIntCall) {
                NextIntCall intCall = (NextIntCall)call;
                value = (Integer)observation;
                reverser.addNextIntCall(intCall.getBound(), value, value);
                continue;
            }
            if (call instanceof UnboundedNextIntCall) {
                int value4 = (Integer)observation;
                reverser.addNextIntCall(value4, value4);
                continue;
            }
            if (call instanceof NextLongCall) {
                long value5 = (Long)observation;
                reverser.addNextLongCall(value5, value5);
                continue;
            }
            if (call instanceof NextFloatCall.FloatRange) {
                NextFloatCall.FloatRange floatRange = (NextFloatCall.FloatRange)call;
                value = ((Boolean)observation).booleanValue() ? 1 : 0;
                if (floatRange.isInverted()) {
                    int n = value = value == 0 ? 1 : 0;
                }
                if (((Float)floatRange.getMin()).floatValue() == 0.0f && !floatRange.isMinStrict() && ((Float)floatRange.getMax()).floatValue() == 1.0f && floatRange.isMaxStrict()) {
                    reverser.addUnmeasuredSeeds(1L);
                    continue;
                }
                if (value != 0) {
                    reverser.addNextFloatCall(((Float)floatRange.getMin()).floatValue(), ((Float)floatRange.getMax()).floatValue(), !floatRange.isMinStrict(), !floatRange.isMaxStrict());
                    continue;
                }
                if (((Float)floatRange.getMin()).floatValue() == 0.0f && !floatRange.isMinStrict()) {
                    reverser.addNextFloatCall(((Float)floatRange.getMax()).floatValue(), 1.0f, floatRange.isMaxStrict(), false);
                    continue;
                }
                if (((Float)floatRange.getMax()).floatValue() == 1.0f && floatRange.isMaxStrict()) {
                    reverser.addNextFloatCall(0.0f, ((Float)floatRange.getMin()).floatValue(), true, floatRange.isMinStrict());
                    continue;
                }
                reverser.addUnmeasuredSeeds(1L);
                continue;
            }
            if (call instanceof NextIntCall.IntRange) {
                NextIntCall.IntRange intRange = (NextIntCall.IntRange)call;
                value = ((Boolean)observation).booleanValue() ? 1 : 0;
                if (intRange.isInverted()) {
                    value = value == 0 ? 1 : 0;
                }
                int min = (Integer)intRange.getMin();
                max = (Integer)intRange.getMax();
                if (intRange.isMinStrict()) {
                    ++min;
                }
                if (intRange.isMaxStrict()) {
                    --max;
                }
                if (intRange.getBound() == max - min + 1) {
                    reverser.addUnmeasuredSeeds(1L);
                    continue;
                }
                if (value != 0) {
                    reverser.addNextIntCall(intRange.getBound(), min, max);
                    continue;
                }
                if (min == 0) {
                    reverser.addNextIntCall(intRange.getBound(), max + 1, intRange.getBound() - 1);
                    continue;
                }
                if (max == intRange.getBound() - 1) {
                    reverser.addNextIntCall(intRange.getBound(), 0, min - 1);
                    continue;
                }
                reverser.addUnmeasuredSeeds(1L);
                continue;
            }
            if (call instanceof UnboundedNextIntCall.IntRange) {
                UnboundedNextIntCall.IntRange intRange = (UnboundedNextIntCall.IntRange)call;
                value = ((Boolean)observation).booleanValue() ? 1 : 0;
                if (intRange.isInverted()) {
                    value = value == 0 ? 1 : 0;
                }
                int min = (Integer)intRange.getMin();
                max = (Integer)intRange.getMax();
                if (intRange.isMinStrict()) {
                    ++min;
                }
                if (intRange.isMaxStrict()) {
                    --max;
                }
                if (min == Integer.MIN_VALUE && max == Integer.MAX_VALUE) {
                    reverser.addUnmeasuredSeeds(1L);
                    continue;
                }
                if (value != 0) {
                    reverser.addNextIntCall(min, max);
                    continue;
                }
                reverser.addUnmeasuredSeeds(1L);
                continue;
            }
            if (call instanceof NextDoubleCall.DoubleRange) {
                NextDoubleCall.DoubleRange doubleRange = (NextDoubleCall.DoubleRange)call;
                value = ((Boolean)observation).booleanValue() ? 1 : 0;
                if (doubleRange.isInverted()) {
                    int n = value = value == 0 ? 1 : 0;
                }
                if ((Double)doubleRange.getMin() == 0.0 && !doubleRange.isMinStrict() && (Double)doubleRange.getMax() == 1.0 && doubleRange.isMaxStrict()) {
                    reverser.addUnmeasuredSeeds(2L);
                    continue;
                }
                if (value != 0) {
                    reverser.addNextDoubleCall((Double)doubleRange.getMin(), (Double)doubleRange.getMax(), !doubleRange.isMinStrict(), !doubleRange.isMaxStrict());
                    continue;
                }
                if ((Double)doubleRange.getMin() == 0.0 && !doubleRange.isMinStrict()) {
                    reverser.addNextDoubleCall((Double)doubleRange.getMax(), 1.0, doubleRange.isMaxStrict(), false);
                    continue;
                }
                if ((Double)doubleRange.getMax() == 1.0 && doubleRange.isMaxStrict()) {
                    reverser.addNextDoubleCall(0.0, (Double)doubleRange.getMin(), true, doubleRange.isMinStrict());
                    continue;
                }
                reverser.addUnmeasuredSeeds(2L);
                continue;
            }
            if (call instanceof NextLongCall.LongRange) {
                NextLongCall.LongRange intRange = (NextLongCall.LongRange)call;
                value = ((Boolean)observation).booleanValue() ? 1 : 0;
                if (intRange.isInverted()) {
                    value = value == 0 ? 1 : 0;
                }
                long min = (Long)intRange.getMin();
                long max2 = (Long)intRange.getMax();
                if (intRange.isMinStrict()) {
                    ++min;
                }
                if (intRange.isMaxStrict()) {
                    --max2;
                }
                if (value != 0) {
                    reverser.addNextLongCall(min, max2);
                    continue;
                }
                reverser.addUnmeasuredSeeds(2L);
                continue;
            }
            if (call instanceof NextFloatCall.Ranged) {
                Range value6 = (Range)observation;
                reverser.addNextFloatCall(Math.max(0.0f, ((Float)value6.min()).floatValue()), Math.min(1.0f, ((Float)value6.max()).floatValue()), value6.minInclusive(), value6.maxInclusive());
                continue;
            }
            if (call instanceof NextIntCall.Ranged) {
                Range value7 = (Range)observation;
                int bound = ((NextIntCall.Ranged)call).getBound();
                reverser.addNextIntCall(bound, Math.max(0, value7.minInclusive() ? (Integer)value7.min() : (Integer)value7.min() + 1), Math.min(bound - 1, value7.maxInclusive() ? (Integer)value7.max() : (Integer)value7.max() - 1));
                continue;
            }
            if (call instanceof UnboundedNextIntCall.Ranged) {
                Range value8 = (Range)observation;
                reverser.addNextIntCall(value8.maxInclusive() ? (Integer)value8.min() : (Integer)value8.min() + 1, value8.maxInclusive() ? (Integer)value8.max() : (Integer)value8.max() - 1);
                continue;
            }
            if (call instanceof NextDoubleCall.Ranged) {
                Range value9 = (Range)observation;
                reverser.addNextDoubleCall(Math.max(0.0, (Double)value9.min()), Math.min(1.0, (Double)value9.max()), value9.minInclusive(), value9.maxInclusive());
                continue;
            }
            if (call instanceof NextLongCall.Ranged) {
                Range value10 = (Range)observation;
                reverser.addNextLongCall(value10.minInclusive() ? (Long)value10.min() : (Long)value10.min() + 1L, value10.maxInclusive() ? (Long)value10.max() : (Long)value10.max() - 1L);
                continue;
            }
            throw new IllegalStateException("Unsupported call type: " + call.getClass().getName());
        }
        return reverser.findAllValidSeeds();
    }
}

