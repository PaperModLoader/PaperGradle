package xyz.papermodloader.tree.bag;

import xyz.papermodloader.tree.paper.PaperConstants;
import xyz.papermodloader.tree.paper.PaperPlugin;
import xyz.papermodloader.tree.util.delayed.Delayed;
import xyz.papermodloader.tree.util.delayed.DelayedCache;

import java.io.File;

public class BagConstants {
    //Setup tasks
    public static final String TASK_DOWNLOAD_ASSETS = "downloadAssets";
    public static final String TASK_DOWNLOAD_CLIENT = "downloadClient";
    public static final String TASK_DOWNLOAD_SERVER = "downloadServer";
    public static final String TASK_DOWNLOAD_LIBRARIES = "downloadLibraries";
    public static final String TASK_DOWNLOAD_FERNFLOWER = "downloadFernflower";
    public static final String TASK_MERGE = "merge";
    public static final String TASK_DEOBFUSCATE = "deobfuscate";
    public static final String TASK_DECOMPILE = "decompile";
    public static final String TASK_MAKE_MINECRAFT = "makeMinecraft";
    public static final String TASK_EXTRACT_NATIVES = "extractNatives";
    public static final String TASK_SETUP = "setupWorkspace";
    public static final String TASK_IDEA = "setupIDEA";

    //Build tasks
    public static final String TASK_BUILD = "buildMod";

    //Configurations
    public static final String CONFIG_DEPENDENCIES = "paperDependencies";
    public static final String CONFIG_NATIVES = "paperNatives";

    //MC installation
    public static final Delayed<File> MINECRAFT_DIRECTORY = new DelayedCache<>(() -> {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return new File(System.getenv("APPDATA"), ".minecraft");
        } else if (os.contains("mac")) {
            return new File(System.getProperty("user.home", "/Library/Application Support/minecraft"));
        }
        return new File(System.getProperty("user.home", ".minecraft"));
    });
    public static final Delayed<File> ASSET_DIRECTORY = new DelayedCache<>(() -> new File(PaperConstants.MINECRAFT_DIRECTORY.get(), "assets"));
    public static final Delayed<File> CLIENT_JAR = new DelayedCache<>(() -> new File(PaperConstants.MINECRAFT_DIRECTORY.get(), "versions/" + PaperPlugin.INSTANCE.getExtension().minecraft + "/" + PaperPlugin.INSTANCE.getExtension().minecraft + ".jar"));
}
