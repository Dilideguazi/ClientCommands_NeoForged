package dev.xpple.clientarguments.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;

import java.util.Arrays;
import java.util.Collection;

public class CNbtCompoundArgumentType implements ArgumentType<CompoundTag> {

	private static final Collection<String> EXAMPLES = Arrays.asList("{}", "{foo=bar}");

	public static CNbtCompoundArgumentType nbtCompound() {
		return new CNbtCompoundArgumentType();
	}

	public static <S> CompoundTag getCNbtCompound(CommandContext<S> context, String name) {
		return context.getArgument(name, CompoundTag.class);
	}

	@Override
	public CompoundTag parse(final StringReader stringReader) throws CommandSyntaxException {
		return new TagParser(stringReader).readStruct();
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}
