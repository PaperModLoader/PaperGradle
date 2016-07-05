package xyz.papermodloader.tree.paper;

import xyz.papermodloader.tree.util.LauncherManifest;
import xyz.papermodloader.tree.util.delayed.Delayed;
import xyz.papermodloader.tree.util.delayed.DelayedCache;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class PaperConstants {
    //Setup tasks
    public static final String TASK_DOWNLOAD_ASSETS = "downloadAssets";
    public static final String TASK_DOWNLOAD_CLIENT = "downloadClient";
    public static final String TASK_DOWNLOAD_LIBRARIES = "downloadLibraries";
    public static final String TASK_DOWNLOAD_MAPPINGS = "downloadMappings";
    public static final String TASK_DOWNLOAD_SERVER = "downloadServer";
    public static final String TASK_MERGE = "merge";
    public static final String TASK_DEOBFUSCATE = "deobfuscate";
    public static final String TASK_MAKE_MINECRAFT = "makeMinecraft";
    public static final String TASK_EXTRACT_NATIVES = "extractNatives";
    public static final String TASK_SETUP = "setupPaper";
    public static final String TASK_IDEA = "setupIDEA";

    //Build tasks
    public static final String TASK_MAKE_PAPER = "makePaper";
    public static final String TASK_BUILD = "buildPaper";

    public static final String CONFIG_DEPENDENCIES = "paperDependencies";
    public static final String CONFIG_NATIVES = "paperNatives";

    public static final File CACHE_DIRECTORY = new File(PaperTree.INSTANCE.getProject().getGradle().getGradleUserHomeDir(), "caches/paper/");
    public static final Delayed<File> ASSET_DIRECTORY = new DelayedCache<>(() -> new File(PaperConstants.MINECRAFT_DIRECTORY.get(), "assets"));
    public static final Delayed<File> CLIENT_JAR = new DelayedCache<>(() -> new File(PaperConstants.MINECRAFT_DIRECTORY.get(), "versions/" + PaperTree.INSTANCE.getExtension().minecraft + "/" + PaperTree.INSTANCE.getExtension().minecraft + ".jar"));

    public static final File ASSET_DIRECTORY_CACHE = new File(PaperConstants.CACHE_DIRECTORY, "assets");
    public static final Delayed<File> CLIENT_JAR_CACHE = new DelayedCache<>(() -> new File(CACHE_DIRECTORY, PaperTree.INSTANCE.getExtension().minecraft + "-client.jar"));
    public static final Delayed<File> SERVER_JAR_CACHE = new DelayedCache<>(() -> new File(CACHE_DIRECTORY, PaperTree.INSTANCE.getExtension().minecraft + "-server.jar"));
    public static final Delayed<File> MERGED_JAR_CACHE = new DelayedCache<>(() -> new File(CACHE_DIRECTORY, PaperTree.INSTANCE.getExtension().minecraft + "-merged.jar"));
    public static final Delayed<File> DEOBF_MERGED_JAR_CACHE = new DelayedCache<>(() -> new File(CACHE_DIRECTORY, PaperTree.INSTANCE.getExtension().minecraft + "-merged-deobf.jar"));
    public static final Delayed<File> MINECRAFT_LIBRARY_JAR_CACHE = new DelayedCache<>(() -> new File(CACHE_DIRECTORY, "minecraft-" + PaperTree.INSTANCE.getExtension().minecraft + ".jar"));
    public static final Delayed<File> MAPPINGS_FILE_CACHE = new DelayedCache<>(() -> new File(CACHE_DIRECTORY, "mappings" + File.separator + PaperTree.INSTANCE.getExtension().minecraft + "-" + PaperTree.INSTANCE.getExtension().mappings + ".json"));
    public static final File NATIVES_DIRECTORY_CACHE = new File(PaperConstants.CACHE_DIRECTORY, "natives");

    public static final Delayed<File> RESULT_JAR = new DelayedCache<>(() -> new File(PaperTree.INSTANCE.getProject().getRootDir(), "build" + File.separator + "libs" + File.separator + PaperTree.INSTANCE.getProject().getName().toLowerCase(Locale.ENGLISH) + "-" + PaperTree.INSTANCE.getProject().getVersion() + ".jar"));

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
            LauncherManifest.ManifestVersion manifestVersion = PaperConstants.MANIFEST_VERSION.get().getManifestVersion(PaperTree.INSTANCE.getExtension().minecraft);
            return manifestVersion.getVersion();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    });

    public static final Delayed<LauncherManifest.ManifestVersion.Version.Downloads.Download> CLIENT_DOWNLOAD = () -> PaperConstants.VERSION.get().downloads.client;
    public static final Delayed<LauncherManifest.ManifestVersion.Version.Downloads.Download> SERVER_DOWNLOAD = () -> PaperConstants.VERSION.get().downloads.server;
}