package xyz.papermodloader.papergradle;

import xyz.papermodloader.papergradle.util.LauncherManifest;
import xyz.papermodloader.papergradle.util.delayed.Delayed;
import xyz.papermodloader.papergradle.util.delayed.DelayedCache;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class Constants {
    public static final String TASK_DOWNLOAD_ASSETS = "downloadAssets";
    public static final String TASK_DOWNLOAD_CLIENT = "downloadClient";
    public static final String TASK_DOWNLOAD_LIBRARIES = "downloadLibraries";
    public static final String TASK_DOWNLOAD_SERVER = "downloadServer";
    public static final String TASK_DEOBFUSCATE = "deobfuscate";
    public static final String TASK_MERGE = "merge";
    public static final String TASK_DECOMPILE = "decompile";
    public static final String TASK_SETUP = "setupPaper";

    public static final File CACHE_DIRECTORY = new File(PaperGradle.INSTANCE.getProject().getGradle().getGradleUserHomeDir(), "caches/paper/");
    public static final Delayed<File> ASSET_DIRECTORY = new DelayedCache<>(() -> new File(Constants.MINECRAFT_DIRECTORY.get(), "assets"));
    public static final Delayed<File> CLIENT_JAR = new DelayedCache<>(() -> new File(Constants.MINECRAFT_DIRECTORY.get(), "versions/" + PaperGradle.INSTANCE.getExtension().minecraft + "/" + PaperGradle.INSTANCE.getExtension().minecraft + ".jar"));

    public static final File ASSET_DIRECTORY_CACHE = new File(Constants.CACHE_DIRECTORY, "assets");
    public static final Delayed<File> CLIENT_JAR_CACHE = new DelayedCache<>(() -> new File(CACHE_DIRECTORY, PaperGradle.INSTANCE.getExtension().minecraft + "-client.jar"));
    public static final Delayed<File> SERVER_JAR_CACHE = new DelayedCache<>(() -> new File(CACHE_DIRECTORY, PaperGradle.INSTANCE.getExtension().minecraft + "-server.jar"));
    public static final Delayed<File> DEOBF_CLIENT_JAR_CACHE = new DelayedCache<>(() -> new File(CACHE_DIRECTORY, PaperGradle.INSTANCE.getExtension().minecraft + "-client-deobf.jar"));
    public static final Delayed<File> DEOBF_SERVER_JAR_CACHE = new DelayedCache<>(() -> new File(CACHE_DIRECTORY, PaperGradle.INSTANCE.getExtension().minecraft + "-server-deobf.jar"));
    public static final Delayed<File> DEOBF_MERGED_JAR_CACHE = new DelayedCache<>(() -> new File(CACHE_DIRECTORY, PaperGradle.INSTANCE.getExtension().minecraft + "-merged-deobf.jar"));
    public static final File MINECRAFT_DECOMPILED = new File(CACHE_DIRECTORY, "decompiled");

    public static final String CONFIG_DEPENDENCIES = "minecraftDependencies";
    public static final String CONFIG_DEPENDENCIES_CLIENT = "minecraftDependenciesClient";
    public static final String CONFIG_NATIVES = "minecraftNatives";

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
            LauncherManifest.ManifestVersion manifestVersion = Constants.MANIFEST_VERSION.get().getManifestVersion(PaperGradle.INSTANCE.getExtension().minecraft);
            return manifestVersion.getVersion();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    });

    public static final Delayed<LauncherManifest.ManifestVersion.Version.Downloads.Download> CLIENT_DOWNLOAD = () -> Constants.VERSION.get().downloads.client;
    public static final Delayed<LauncherManifest.ManifestVersion.Version.Downloads.Download> SERVER_DOWNLOAD = () -> Constants.VERSION.get().downloads.server;

    public static List<String> getClassPath() {
        URL[] urls = ((URLClassLoader) Constants.class.getClassLoader()).getURLs();
        ArrayList<String> list = new ArrayList<>();
        for (URL url : urls) {
            list.add(url.getPath());
        }
        return list;
    }
}
