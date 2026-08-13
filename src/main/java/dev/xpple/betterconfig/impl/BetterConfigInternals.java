/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  net.minecraft.command.CommandSource
 */
package dev.xpple.betterconfig.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.betterconfig.BetterConfig;
import dev.xpple.betterconfig.api.Config;
import net.minecraft.commands.SharedSuggestionProvider;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("rawtypes")
public class BetterConfigInternals {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void init(ModConfigImpl modConfig) {
        JsonObject root = null;
        try (BufferedReader reader2 = Files.newBufferedReader(modConfig.getConfigsPath());){
            root = JsonParser.parseReader(reader2).getAsJsonObject();
        }
        catch (IOException ignored) {
        }
        catch (Exception e) {
            BetterConfig.LOGGER.warn("Could not read config file, default values will be used.");
            BetterConfig.LOGGER.warn("The old config file will be renamed.");
            try {
                Files.move(modConfig.getConfigsPath(), modConfig.getConfigsPath().resolveSibling("config_old.json"), StandardCopyOption.REPLACE_EXISTING);
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
        finally {
            root = Objects.requireNonNullElse(root, new JsonObject());
        }
        for (Field field : modConfig.getConfigsClass().getDeclaredFields()) {
            Config annotation = field.getAnnotation(Config.class);
            if (annotation == null) continue;
            field.setAccessible(true);
            String fieldName = field.getName();
            modConfig.getConfigs().put(fieldName, field);
            try {
                modConfig.getDefaults().put(fieldName, field.get(null));
            }
            catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
            modConfig.getAnnotations().put(fieldName, annotation);
            if (!annotation.comment().isEmpty()) {
                modConfig.getComments().put(fieldName, annotation.comment());
            }
            if (!annotation.temporary()) {
                try {
                    if (root.has(fieldName)) {
                        Object value = modConfig.getGson().fromJson(root.get(fieldName), field.getGenericType());
                        if (Modifier.isFinal(field.getModifiers())) {
                            throw new AssertionError(("Config field '" + fieldName + "' should not be final"));
                        }
                        field.set(null, value);
                    } else {
                        root.add(fieldName, modConfig.getGson().toJsonTree(field.get(null)));
                    }
                }
                catch (Exception e) {
                    throw new AssertionError(e);
                }
            }
            if (annotation.condition().isEmpty()) {
                modConfig.getConditions().put(fieldName, source -> true);
            } else {
                Method predicateMethod;
                boolean hasParameter = false;
                try {
                    predicateMethod = modConfig.getConfigsClass().getDeclaredMethod(annotation.condition());
                }
                catch (ReflectiveOperationException e) {
                    hasParameter = true;
                    try {
                        predicateMethod = modConfig.getConfigsClass().getDeclaredMethod(annotation.condition(), SharedSuggestionProvider.class);
                    }
                    catch (ReflectiveOperationException e1) {
                        throw new AssertionError(e1);
                    }
                }
                if (predicateMethod.getReturnType() != Boolean.TYPE) {
                    throw new AssertionError(("Condition method '" + annotation.condition() + "' does not return boolean"));
                }
                if (!Modifier.isStatic(predicateMethod.getModifiers())) {
                    throw new AssertionError(("Condition method '" + annotation.condition() + "' is not static"));
                }
                predicateMethod.setAccessible(true);
                Method predicateMethod_f = predicateMethod;
                if (hasParameter) {
                    modConfig.getConditions().put(fieldName, source -> {
                        try {
                            return (Boolean)predicateMethod_f.invoke(null, source);
                        }
                        catch (ReflectiveOperationException e) {
                            throw new AssertionError(e);
                        }
                    });
                } else {
                    modConfig.getConditions().put(fieldName, source -> {
                        try {
                            return (Boolean)predicateMethod_f.invoke(null, new Object[0]);
                        }
                        catch (ReflectiveOperationException e) {
                            throw new AssertionError(e);
                        }
                    });
                }
            }
            if (annotation.readOnly()) continue;
            Class<?> type = field.getType();
            if (Collection.class.isAssignableFrom(type)) {
                BetterConfigInternals.initCollection(modConfig, field, annotation);
                continue;
            }
            if (Map.class.isAssignableFrom(type)) {
                BetterConfigInternals.initMap(modConfig, field, annotation);
                continue;
            }
            BetterConfigInternals.initObject(modConfig, field, annotation);
        }
        //noinspection all
        modConfig.getConfigsPath().getParent().toFile().mkdirs();
        try (BufferedWriter writer = Files.newBufferedWriter(modConfig.getConfigsPath())){
            writer.write(modConfig.getGson().toJson((JsonElement)root));
        }
        catch (IOException e) {
            BetterConfig.LOGGER.error("Could not save config file.");
            //noinspection all
            e.printStackTrace();
        }
    }

    private static void initCollection(ModConfigImpl modConfig, Field field, Config annotation) {
        Config.Remover remover;
        String removerMethodName;
        String fieldName = field.getName();
        Type[] types = ((ParameterizedType)field.getGenericType()).getActualTypeArguments();
        Config.Adder adder = annotation.adder();
        String adderMethodName = adder.value();
        if (!adderMethodName.equals("none")) {
            if (adderMethodName.isEmpty()) {
                Method add;
                try {
                    add = Collection.class.getDeclaredMethod("add", Object.class);
                }
                catch (ReflectiveOperationException e) {
                    throw new AssertionError(e);
                }
                modConfig.getAdders().put(fieldName, value -> {
                    try {
                        add.invoke(field.get(null), value);
                    }
                    catch (ReflectiveOperationException e) {
                        throw new AssertionError(e);
                    }
                });
            } else {
                Method adderMethod;
                Class type = adder.type() == Config.EMPTY.class ? (Class)types[0] : adder.type();
                try {
                    adderMethod = modConfig.getConfigsClass().getDeclaredMethod(adderMethodName, type);
                }
                catch (ReflectiveOperationException e) {
                    throw new AssertionError(e);
                }
                adderMethod.setAccessible(true);
                modConfig.getAdders().put(fieldName, value -> {
                    try {
                        adderMethod.invoke(null, value);
                    }
                    catch (ReflectiveOperationException e) {
                        Throwable patt7601$temp = e.getCause();
                        if (patt7601$temp instanceof CommandSyntaxException) {
                            @SuppressWarnings("all")
                            CommandSyntaxException commandSyntaxException = (CommandSyntaxException)patt7601$temp;
                            throw commandSyntaxException;
                        }
                        throw new AssertionError(e);
                    }
                });
            }
        }
        if (!(removerMethodName = (remover = annotation.remover()).value()).equals("none")) {
            if (removerMethodName.isEmpty()) {
                Method remove;
                try {
                    remove = Collection.class.getDeclaredMethod("remove", Object.class);
                }
                catch (ReflectiveOperationException e) {
                    throw new AssertionError(e);
                }
                modConfig.getRemovers().put(fieldName, value -> {
                    try {
                        remove.invoke(field.get(null), value);
                    }
                    catch (ReflectiveOperationException e) {
                        throw new AssertionError(e);
                    }
                });
            } else {
                Method removerMethod;
                Class type = remover.type() == Config.EMPTY.class ? (Class)types[0] : remover.type();
                try {
                    removerMethod = modConfig.getConfigsClass().getDeclaredMethod(removerMethodName, type);
                }
                catch (ReflectiveOperationException e) {
                    throw new AssertionError(e);
                }
                removerMethod.setAccessible(true);
                modConfig.getRemovers().put(fieldName, value -> {
                    try {
                        removerMethod.invoke(null, value);
                    }
                    catch (ReflectiveOperationException e) {
                        Throwable patt9284$temp = e.getCause();
                        if (patt9284$temp instanceof CommandSyntaxException) {
                            @SuppressWarnings("all")
                            CommandSyntaxException commandSyntaxException = (CommandSyntaxException)patt9284$temp;
                            throw commandSyntaxException;
                        }
                        throw new AssertionError(e);
                    }
                });
            }
        }
    }

    private static void initMap(ModConfigImpl modConfig, Field field, Config annotation) {
        Config.Remover remover;
        String removerMethodName;
        Config.Putter putter;
        String putterMethodName;
        String fieldName = field.getName();
        Type[] types = ((ParameterizedType)field.getGenericType()).getActualTypeArguments();
        Config.Adder adder = annotation.adder();
        String adderMethodName = adder.value();
        if (!adderMethodName.equals("none") && !adderMethodName.isEmpty()) {
            Method adderMethod;
            Class type = adder.type() == Config.EMPTY.class ? (Class)types[0] : adder.type();
            try {
                adderMethod = modConfig.getConfigsClass().getDeclaredMethod(adderMethodName, type);
            }
            catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
            adderMethod.setAccessible(true);
            modConfig.getAdders().put(fieldName, key -> {
                try {
                    adderMethod.invoke(null, key);
                }
                catch (ReflectiveOperationException e) {
                    Throwable patt10624$temp = e.getCause();
                    if (patt10624$temp instanceof CommandSyntaxException) {
                        @SuppressWarnings("all")
                        CommandSyntaxException commandSyntaxException = (CommandSyntaxException)patt10624$temp;
                        throw commandSyntaxException;
                    }
                    throw new AssertionError(e);
                }
            });
        }
        if (!(putterMethodName = (putter = annotation.putter()).value()).equals("none")) {
            if (putterMethodName.isEmpty()) {
                Method put;
                try {
                    put = Map.class.getDeclaredMethod("put", Object.class, Object.class);
                }
                catch (ReflectiveOperationException e) {
                    throw new AssertionError(e);
                }
                modConfig.getPutters().put(fieldName, (key, value) -> {
                    try {
                        put.invoke(field.get(null), key, value);
                    }
                    catch (ReflectiveOperationException e) {
                        throw new AssertionError(e);
                    }
                });
            } else {
                Method putterMethod;
                Class keyType = putter.keyType() == Config.EMPTY.class ? (Class)types[0] : putter.keyType();
                Class valueType = putter.valueType() == Config.EMPTY.class ? (Class)types[1] : putter.valueType();
                try {
                    putterMethod = modConfig.getConfigsClass().getDeclaredMethod(putterMethodName, keyType, valueType);
                }
                catch (ReflectiveOperationException e) {
                    throw new AssertionError(e);
                }
                putterMethod.setAccessible(true);
                modConfig.getPutters().put(fieldName, (key, value) -> {
                    try {
                        putterMethod.invoke(null, key, value);
                    }
                    catch (ReflectiveOperationException e) {
                        Throwable patt12451$temp = e.getCause();
                        if (patt12451$temp instanceof CommandSyntaxException) {
                            @SuppressWarnings("all")
                            CommandSyntaxException commandSyntaxException = (CommandSyntaxException)patt12451$temp;
                            throw commandSyntaxException;
                        }
                        throw new AssertionError(e);
                    }
                });
            }
        }
        if (!(removerMethodName = (remover = annotation.remover()).value()).equals("none")) {
            if (removerMethodName.isEmpty()) {
                Method remove;
                try {
                    remove = Map.class.getDeclaredMethod("remove", Object.class);
                }
                catch (ReflectiveOperationException e) {
                    throw new AssertionError(e);
                }
                modConfig.getRemovers().put(fieldName, key -> {
                    try {
                        remove.invoke(field.get(null), key);
                    }
                    catch (ReflectiveOperationException e) {
                        throw new AssertionError(e);
                    }
                });
            } else {
                Method removerMethod;
                Class type = remover.type() == Config.EMPTY.class ? (Class)types[0] : remover.type();
                try {
                    removerMethod = modConfig.getConfigsClass().getDeclaredMethod(removerMethodName, type);
                }
                catch (ReflectiveOperationException e) {
                    throw new AssertionError(e);
                }
                removerMethod.setAccessible(true);
                modConfig.getRemovers().put(fieldName, key -> {
                    try {
                        removerMethod.invoke(null, key);
                    }
                    catch (ReflectiveOperationException e) {
                        Throwable patt14119$temp = e.getCause();
                        if (patt14119$temp instanceof CommandSyntaxException) {
                            @SuppressWarnings("all")
                            CommandSyntaxException commandSyntaxException = (CommandSyntaxException)patt14119$temp;
                            throw commandSyntaxException;
                        }
                        throw new AssertionError(e);
                    }
                });
            }
        }
    }

    private static void initObject(ModConfigImpl modConfig, Field field, Config annotation) {
        String fieldName = field.getName();
        Config.Setter setter = annotation.setter();
        String setterMethodName = setter.value();
        if (!setterMethodName.equals("none")) {
            if (setterMethodName.isEmpty()) {
                modConfig.getSetters().put(fieldName, value -> {
                    try {
                        field.set(null, value);
                    }
                    catch (ReflectiveOperationException e) {
                        throw new AssertionError(e);
                    }
                });
            } else {
                Method setterMethod;
                Class<?> type = setter.type() == Config.EMPTY.class ? field.getType() : setter.type();
                try {
                    setterMethod = modConfig.getConfigsClass().getDeclaredMethod(setterMethodName, type);
                }
                catch (ReflectiveOperationException e) {
                    throw new AssertionError(e);
                }
                setterMethod.setAccessible(true);
                modConfig.getSetters().put(fieldName, value -> {
                    try {
                        setterMethod.invoke(null, value);
                    }
                    catch (ReflectiveOperationException e) {
                        Throwable patt15668$temp = e.getCause();
                        if (patt15668$temp instanceof CommandSyntaxException) {
                            @SuppressWarnings("all")
                            CommandSyntaxException commandSyntaxException = (CommandSyntaxException)patt15668$temp;
                            throw commandSyntaxException;
                        }
                        throw new AssertionError(e);
                    }
                });
            }
        }
    }
}

