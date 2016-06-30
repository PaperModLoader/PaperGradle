package xyz.papermodloader.tree;

import xyz.papermodloader.tree.util.LauncherManifest;
import xyz.papermodloader.tree.util.delayed.Delayed;
import xyz.papermodloader.tree.util.delayed.DelayedCache;

import java.io.File;
import java.io.IOException;

public class Constants {
    public static final String TASK_DOWNLOAD_ASSETS = "downloadAssets";
    public static final String TASK_DOWNLOAD_CLIENT = "downloadClient";
    public static final String TASK_DOWNLOAD_LIBRARIES = "downloadLibraries";
    public static final String TASK_DOWNLOAD_MAPPINGS = "downloadMappings";
    public static final String TASK_DOWNLOAD_SERVER = "downloadServer";
    public static final String TASK_MERGE = "merge";
    public static final String TASK_DEOBFUSCATE = "deobfuscate";
    public static final String TASK_DECOMPILE = "decompile";
    public static final String TASK_MAKE_MINECRAFT = "makeMinecraft";
    public static final String TASK_SETUP = "setupPaper";
    public static final String TASK_IDEA = "setupIDEA";

    public static final File CACHE_DIRECTORY = new File(Tree.INSTANCE.getProject().getGradle().getGradleUserHomeDir(), "caches/paper/");
    public static final Delayed<File> ASSET_DIRECTORY = new DelayedCache<>(() -> new File(Constants.MINECRAFT_DIRECTORY.get(), "assets"));
    public static final Delayed<File> CLIENT_JAR = new DelayedCache<>(() -> new File(Constants.MINECRAFT_DIRECTORY.get(), "versions/" + Tree.INSTANCE.getExtension().minecraft + "/" + Tree.INSTANCE.getExtension().minecraft + ".jar"));

    public static final File ASSET_DIRECTORY_CACHE = new File(Constants.CACHE_DIRECTORY, "assets");
    public static final Delayed<File> CLIENT_JAR_CACHE = new DelayedCache<>(() -> new File(CACHE_DIRECTORY, Tree.INSTANCE.getExtension().minecraft + "-client.jar"));
    public static final Delayed<File> SERVER_JAR_CACHE = new DelayedCache<>(() -> new File(CACHE_DIRECTORY, Tree.INSTANCE.getExtension().minecraft + "-server.jar"));
    public static final Delayed<File> MERGED_JAR_CACHE = new DelayedCache<>(() -> new File(CACHE_DIRECTORY, Tree.INSTANCE.getExtension().minecraft + "-merged.jar"));
    public static final Delayed<File> DEOBF_MERGED_JAR_CACHE = new DelayedCache<>(() -> new File(CACHE_DIRECTORY, Tree.INSTANCE.getExtension().minecraft + "-merged-deobf.jar"));
    public static final File MINECRAFT_DECOMPILED_CACHE = new File(CACHE_DIRECTORY, "unpackaged");
    public static final Delayed<File> MINECRAFT_LIBRARY_JAR_CACHE = new DelayedCache<>(() -> new File(CACHE_DIRECTORY, "minecraft-" + Tree.INSTANCE.getExtension().minecraft + ".jar"));
    public static final Delayed<File> MAPPINGS_FILE_CACHE = new DelayedCache<>(() -> new File(CACHE_DIRECTORY, "mappings" + File.separator + Tree.INSTANCE.getExtension().minecraft + "-" + Tree.INSTANCE.getExtension().mappings + ".mappings"));

    public static final File NATIVES_DIRECTORY = new File(Constants.CACHE_DIRECTORY, "natives");

    public static final Delayed<File> MINECRAFT_DIRECTORY = new DelayedCache<>(() -> {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return new File(System.getenv("APPDATA"), ".minecraft");
        } else if (os.contains("mac")) {
            return new File(System.getProperty("user.home", "/Library/Application Support/minecraft"));
        }
        return new File(System.getProperty("user.home", ".minecraft"));
    });

    public static final Delayed<LauncherManifest> MANIFEST_VERSION = new DelayedCache<>(() -> {
        try {
            return LauncherManifest.getLauncherManifest();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    });

    public static final Delayed<LauncherManifest.ManifestVersion.Version> VERSION = new DelayedCache<>(() -> {
        try {
            LauncherManifest.ManifestVersion manifestVersion = Constants.MANIFEST_VERSION.get().getManifestVersion(Tree.INSTANCE.getExtension().minecraft);
            return manifestVersion.getVersion();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    });

    public static final Delayed<LauncherManifest.ManifestVersion.Version.Downloads.Download> CLIENT_DOWNLOAD = () -> Constants.VERSION.get().downloads.client;
    public static final Delayed<LauncherManifest.ManifestVersion.Version.Downloads.Download> SERVER_DOWNLOAD = () -> Constants.VERSION.get().downloads.server;
}
