package dev.xpple.clientarguments.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.coordinates.WorldCoordinate;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class CLookingPosArgument implements CPosArgument {
	public static final char CARET = '^';
	private final double x;
	private final double y;
	private final double z;

	public CLookingPosArgument(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vec3 toAbsolutePos(FabricClientCommandSource source) {
		Vec2 rotation = source.getRotation();
		Vec3 pos = CEntityAnchorArgumentType.EntityAnchor.FEET.positionAt(source);
		final float PiDividedBy180 = 0.017453292F;
		float f = Mth.cos((rotation.y + 90.0F) * PiDividedBy180);
		float g = Mth.sin((rotation.y + 90.0F) * PiDividedBy180);
		float h = Mth.cos(-rotation.x * PiDividedBy180);
		float i = Mth.sin(-rotation.x * PiDividedBy180);
		float j = Mth.cos((-rotation.x + 90.0F) * PiDividedBy180);
		float k = Mth.sin((-rotation.x + 90.0F) * PiDividedBy180);
		Vec3 vec3d2 = new Vec3((f * h), i, (g * h));
		Vec3 vec3d3 = new Vec3((f * j), k, (g * j));
		Vec3 vec3d4 = vec3d2.cross(vec3d3).scale(-1.0D);
		double d = vec3d2.x * this.z + vec3d3.x * this.y + vec3d4.x * this.x;
		double e = vec3d2.y * this.z + vec3d3.y * this.y + vec3d4.y * this.x;
		double l = vec3d2.z * this.z + vec3d3.z * this.y + vec3d4.z * this.x;
		return new Vec3(pos.x + d, pos.y + e, pos.z + l);
	}

	public Vec2 toAbsoluteRotation(FabricClientCommandSource source) {
		return Vec2.ZERO;
	}

	public boolean isXRelative() {
		return true;
	}

	public boolean isYRelative() {
		return true;
	}

	public boolean isZRelative() {
		return true;
	}

	public static CLookingPosArgument parse(StringReader reader) throws CommandSyntaxException {
		int cursor = reader.getCursor();
		double x = readCoordinate(reader, cursor);
		if (reader.canRead() && reader.peek() == ' ') {
			reader.skip();
			double y = readCoordinate(reader, cursor);
			if (reader.canRead() && reader.peek() == ' ') {
				reader.skip();
				double z = readCoordinate(reader, cursor);
				return new CLookingPosArgument(x, y, z);
			} else {
				reader.setCursor(cursor);
				throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(reader);
			}
		} else {
			reader.setCursor(cursor);
			throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(reader);
		}
	}

	private static double readCoordinate(StringReader reader, int startingCursorPos) throws CommandSyntaxException {
		if (!reader.canRead()) {
			throw WorldCoordinate.ERROR_EXPECTED_DOUBLE.createWithContext(reader);
		} else if (reader.peek() != '^') {
			reader.setCursor(startingCursorPos);
			throw Vec3Argument.ERROR_MIXED_TYPE.createWithContext(reader);
		}
		reader.skip();
		return reader.canRead() && reader.peek() != ' ' ? reader.readDouble() : 0.0D;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (!(o instanceof CLookingPosArgument lookingPosArgument)) {
			return false;
		} else {
			return this.x == lookingPosArgument.x && this.y == lookingPosArgument.y && this.z == lookingPosArgument.z;
		}
	}

	public int hashCode() {
		return Objects.hash(this.x, this.y, this.z);
	}
}
