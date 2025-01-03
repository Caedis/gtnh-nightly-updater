package GTNHNightlyUpdater;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Log4j2(topic = "GTNHNightlyUpdater-Main")
public class Main {
    // todo: error handling
    public static void main(String[] args) throws Throwable {
        val options = new Options();
        try {
            new CommandLine(options)
                    .setCaseInsensitiveEnumValuesAllowed(true)
                    .parseArgs(args);

            val updater = new Updater(options.useLatest);
            val cacheDir = getCacheDir().resolve("gtnh-nightly-updater");
            if (Files.notExists(cacheDir)) {
                Files.createDirectory(cacheDir);
            }
            val modExclusions = getModExclusions(cacheDir);

            val assets = updater.fetchDAXXLAssets();
            val localAssets = cacheDir.resolve("local-assets.txt");

            val modCacheDir = cacheDir.resolve("mods");
            if (Files.notExists(modCacheDir)) {
                Files.createDirectory(modCacheDir);
            }

            if (options.useLatest) {
                if (Files.exists(localAssets)) {
                    updater.addLocalAssets(assets, localAssets);
                }
                updater.updateModsFromMaven(assets);
            }
            updater.cacheMods(assets, modCacheDir);
            for (val instance : options.instances) {
                log.info("Updating {} with side {}", instance.config.minecraftDir, instance.config.side);
                updater.updateModpackMods(assets, modCacheDir, modExclusions, instance.config);
            }

        } catch (CommandLine.ParameterException e) {
            log.fatal(e);
            CommandLine.usage(options, System.out);
            System.exit(2);
        } catch (Exception e) {
            log.fatal(e);
            System.exit(1);
        }
    }


    private static Path getCacheDir() {
        val osName = System.getProperty("os.name").toLowerCase();
        Path cacheDir;
        if (osName.contains("win")) {
            cacheDir = Path.of(System.getenv("LOCALAPPDATA"));
        } else if (osName.contains("mac")) {
            cacheDir = Path.of(System.getProperty("user.home"), "Library", "Caches");
        } else {
            cacheDir = Path.of(System.getenv("XDG_CACHE_HOME"));
            if (Files.notExists(cacheDir)) {
                cacheDir = Path.of(System.getProperty("user.home"), ".cache");
            }
        }

        if (Files.notExists(cacheDir)){
            throw new RuntimeException(String.format("Cache directory not found: `%s`", cacheDir));
        }
        return cacheDir;
    }

    private static Set<String> getModExclusions(Path cacheDir) throws IOException {
        if (Files.exists(cacheDir.resolve("mod-exclusions.txt"))) {
            return new HashSet<>(Files.readAllLines(cacheDir.resolve("mod-exclusions.txt")));
        }
        return new HashSet<>();
    }


    @ToString
    static class Options {
        @CommandLine.Option(names = {"-l", "--latest"})
        private boolean useLatest = false;

        @CommandLine.ArgGroup(exclusive = false, multiplicity = "0..*")
        List<Instance> instances;

        static class Instance {
            @CommandLine.Option(names = "--add", required = true)
            boolean add_instance; // leave this for the option

            @CommandLine.ArgGroup(exclusive = false, multiplicity = "1")
            InstanceConfig config;

            static class InstanceConfig {
                @Getter
                private Path minecraftDir;
                @CommandLine.Spec
                CommandLine.Model.CommandSpec spec;

                @CommandLine.Option(names = {"-m", "--minecraft"}, required = true)
                void setMinecraftDir(String value) {
                    val path = Path.of(value);
                    if (!Files.exists(path)) {
                        throw new CommandLine.ParameterException(spec.commandLine(), String.format("Invalid value '%s' for option '--minecraft': path does not exist"));
                    }
                    this.minecraftDir = path;
                }

                @CommandLine.Option(names = {"-s", "--side"}, required = true, description = "Valid values: ${COMPLETION-CANDIDATES}")
                @Getter
                Side side;

                enum Side {
                    CLIENT,
                    SERVER
                }

                @CommandLine.Option(names = {"-S", "--symlinks"})
                @Getter
                private boolean useSymlinks = false;
            }
        }
    }
}
