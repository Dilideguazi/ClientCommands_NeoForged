/*
 * Decompiled with CFR 0.152.
 */
package dev.xpple.betterconfig.api;

import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value={ElementType.FIELD})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface Config {
    String comment() default "";

    Setter setter() default @Setter;

    Adder adder() default @Adder;

    Putter putter() default @Putter;

    Remover remover() default @Remover;

    boolean readOnly() default false;

    boolean temporary() default false;

    String condition() default "";

    @ApiStatus.Internal
    final class EMPTY {
        private EMPTY() {
        }
    }

    @Target(value={})
    @interface Remover {
        String value() default "";

        Class<?> type() default EMPTY.class;
    }

    @Target(value={})
    @interface Putter {
        String value() default "";

        Class<?> keyType() default EMPTY.class;

        Class<?> valueType() default EMPTY.class;
    }

    @Target(value={})
    @interface Adder {
        String value() default "";

        Class<?> type() default EMPTY.class;
    }

    @Target(value={})
    @interface Setter {
        String value() default "";

        Class<?> type() default EMPTY.class;
    }
}

