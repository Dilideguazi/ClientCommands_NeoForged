package dev.xpple.clientarguments.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.arguments.coordinates.WorldCoordinate;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.Collection;

public class CRotationArgumentType implements ArgumentType<CPosArgument> {

	private static final Collection<String> EXAMPLES = Arrays.asList("0 0", "~ ~", "~-5 ~5");
	public static final SimpleCommandExceptionType INCOMPLETE_ROTATION_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("argument.rotation.incomplete"));

	public static CRotationArgumentType rotation() {
		return new CRotationArgumentType();
	}

	public static CPosArgument getCRotation(final CommandContext<FabricClientCommandSource> context, final String name) {
		return context.getArgument(name, CPosArgument.class);
	}

	@Override
	public CPosArgument parse(final StringReader stringReader) throws CommandSyntaxException {
		int cursor = stringReader.getCursor();
		if (!stringReader.canRead()) {
			throw INCOMPLETE_ROTATION_EXCEPTION.createWithContext(stringReader);
		}
		WorldCoordinate coordinateArgument = WorldCoordinate.parseDouble(stringReader, false);
		if (stringReader.canRead() && stringReader.peek() == ' ') {
			stringReader.skip();
			WorldCoordinate coordinateArgument2 = WorldCoordinate.parseDouble(stringReader, false);
			return new CDefaultPosArgument(coordinateArgument2, coordinateArgument, new WorldCoordinate(true, 0.0D));
		}
		stringReader.setCursor(cursor);
		throw INCOMPLETE_ROTATION_EXCEPTION.createWithContext(stringReader);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}
