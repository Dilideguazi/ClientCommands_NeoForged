package dev.xpple.simplewaypoints.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import dev.xpple.simplewaypoints.SimpleWaypoints;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;

public final class BuildInfo {
    private BuildInfo() {
    }

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String VERSION;
    public static final String BRANCH;
    public static final String SHORT_COMMIT_HASH;
    public static final String COMMIT_HASH;

    static {
        String version, branch, shortCommitHash, commitHash;
        version = branch = shortCommitHash = commitHash = "unknown";
        // Neo Edit: Loader method
//        try (BufferedReader reader = Files.newBufferedReader(FabricLoader.getInstance()
//            .getModContainer(SimpleWaypoints.MOD_ID).orElseThrow()
//            .findPath("build_info.json").orElseThrow())
        try (InputStream is = ModList.get()
                .getModContainerById(SimpleWaypoints.MOD_ID)
                .flatMap(container -> Optional.ofNullable(
                        container.getClass().getClassLoader().getResourceAsStream("build_info.json")
                ))
                .orElseThrow();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))
        ) {
            JsonObject object = JsonParser.parseReader(reader).getAsJsonObject();
            version = object.get("version").getAsString();
            branch = object.get("branch").getAsString();
            shortCommitHash = object.get("shortCommitHash").getAsString();
            commitHash = object.get("commitHash").getAsString();
        } catch (RuntimeException | IOException e) {
            LOGGER.error("Error while reading build_info.json", e);
        }
        VERSION = version;
        BRANCH = branch;
        SHORT_COMMIT_HASH = shortCommitHash;
        COMMIT_HASH = commitHash;
    }
}
