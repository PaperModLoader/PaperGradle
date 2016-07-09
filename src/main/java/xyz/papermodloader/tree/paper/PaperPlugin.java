package xyz.papermodloader.tree.paper;

import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import xyz.papermodloader.tree.TreePlugin;
import xyz.papermodloader.tree.paper.task.IDEAProjectTask;
import xyz.papermodloader.tree.paper.task.MakeMinecraftTask;
import xyz.papermodloader.tree.paper.task.MakePaperTask;
import xyz.papermodloader.tree.task.*;

public class PaperPlugin extends TreePlugin<PaperExtension> {
    public static PaperPlugin INSTANCE;

    @Override
    public String getPluginName() {
        return "Paper";
    }

    @Override
    public String getExtensionName() {
        return "paper";
    }

    @Override
    protected void configure() {
        PaperPlugin.INSTANCE = this;

        this.getProject().getConfigurations().maybeCreate(PaperConstants.CONFIG_DEPENDENCIES);
        this.getProject().getConfigurations().maybeCreate(PaperConstants.CONFIG_NATIVES);
        this.getProject().getDependencies().add(JavaPlugin.COMPILE_CONFIGURATION_NAME, this.getProject().fileTree("libs"));
        this.addMavenRepo("mojang", "https://libraries.minecraft.net/");
    }

    @Override
    protected void addTasks() {
        //Setup tasks
        this.addTask(PaperConstants.TASK_DOWNLOAD_JSON, DownloadJSONTask.class, task -> task.setInitializer(() -> {
            task.setJSON(PaperConstants.MINECRAFT_JSON_CACHE.get());
            task.setURL("https://launchermeta.mojang.com/mc/game/version_manifest.json");
            task.setVersion(this.getExtension().minecraft);
        }));
        this.addTask(PaperConstants.TASK_DOWNLOAD_ASSETS, DownloadAssetsTask.class, task -> task.setInitializer(() -> {
            task.setAssets(PaperConstants.ASSET_DIRECTORY_CACHE);
            task.setMCAssets(PaperConstants.ASSET_DIRECTORY.get());
            task.setJSON(PaperConstants.VERSION_JSON.get());
        })).dependsOn(PaperConstants.TASK_DOWNLOAD_JSON);
        this.addTask(PaperConstants.TASK_DOWNLOAD_CLIENT, DownloadTask.class, task -> task.setInitializer(() -> {
            task.setCache(PaperConstants.CLIENT_JAR_CACHE.get());
            task.setFile(PaperConstants.CLIENT_JAR.get());
            task.setURL(PaperConstants.VERSION_JSON.get().downloads.client.url);
            task.setSHA1(PaperConstants.VERSION_JSON.get().downloads.client.sha1);
        })).dependsOn(PaperConstants.TASK_DOWNLOAD_JSON, PaperConstants.TASK_DOWNLOAD_ASSETS);
        this.addTask(PaperConstants.TASK_DOWNLOAD_MAPPINGS, DownloadTask.class, task -> task.setInitializer(() -> {
            task.setCache(PaperConstants.MAPPINGS_FILE_CACHE.get());
            task.setURL("http://ci.ilexiconn.net/job/Cardboard/" + this.getExtension().mappings + "/artifact/" + this.getExtension().minecraft + ".json");
        }));
        this.addTask(PaperConstants.TASK_DOWNLOAD_SERVER, DownloadTask.class, task -> task.setInitializer(() -> {
            task.setCache(PaperConstants.SERVER_JAR_CACHE.get());
            task.setURL(PaperConstants.VERSION_JSON.get().downloads.server.url);
            task.setSHA1(PaperConstants.VERSION_JSON.get().downloads.server.sha1);
        })).dependsOn(PaperConstants.TASK_DOWNLOAD_JSON);
        this.addTask(PaperConstants.TASK_MERGE, MergeTask.class, task -> task.setInitializer(() -> {
            task.setClientJar(PaperConstants.CLIENT_JAR_CACHE.get());
            task.setServerJar(PaperConstants.SERVER_JAR_CACHE.get());
            task.setMergedJar(PaperConstants.MERGED_JAR_CACHE.get());
        })).dependsOn(PaperConstants.TASK_DOWNLOAD_CLIENT, PaperConstants.TASK_DOWNLOAD_SERVER, PaperConstants.TASK_DOWNLOAD_MAPPINGS);
        this.addTask(PaperConstants.TASK_DEOBFUSCATE, DeobfuscateTask.class, task -> task.setInitializer(() -> {
            task.setMappings(PaperConstants.MAPPINGS_FILE_CACHE.get());
            task.setMergedJar(PaperConstants.MERGED_JAR_CACHE.get());
            task.setMergedDeobfJar(PaperConstants.DEOBF_MERGED_JAR_CACHE.get());
        })).dependsOn(PaperConstants.TASK_MERGE);
        this.addTask(PaperConstants.TASK_MAKE_MINECRAFT, MakeMinecraftTask.class).dependsOn(PaperConstants.TASK_DEOBFUSCATE);
        this.addTask(PaperConstants.TASK_SETUP, DefaultTask.class).dependsOn(PaperConstants.TASK_MAKE_MINECRAFT);
        this.addTask(PaperConstants.TASK_DOWNLOAD_LIBRARIES, DownloadLibrariesTask.class, task -> task.setInitializer(() -> {
            task.setJSON(PaperConstants.VERSION_JSON.get());
            task.setDependencyConfig(PaperConstants.CONFIG_DEPENDENCIES);
            task.setNativesConfig(PaperConstants.CONFIG_NATIVES);
            task.setAdditionalLibraries(PaperConstants.MINECRAFT_LIBRARY_JAR_CACHE.get());
        }));
        this.addTask(PaperConstants.TASK_EXTRACT_NATIVES, ExtractNativesTask.class, task -> task.setInitializer(() -> {
            task.setNativesConfig(PaperConstants.CONFIG_NATIVES);
            task.setNativesDirectory(PaperConstants.NATIVES_DIRECTORY_CACHE);
        })).dependsOn(PaperConstants.TASK_DOWNLOAD_LIBRARIES);
        this.addTask(PaperConstants.TASK_IDEA, IDEAProjectTask.class).dependsOn("idea", PaperConstants.TASK_EXTRACT_NATIVES);

        //Build tasks
        this.getProject().getTasks().getByName("compileJava").dependsOn(PaperConstants.TASK_DOWNLOAD_LIBRARIES);
        this.addTask(PaperConstants.TASK_MAKE_PAPER, MakePaperTask.class).dependsOn(JavaBasePlugin.BUILD_TASK_NAME);
        this.addTask(PaperConstants.TASK_BUILD, DefaultTask.class).dependsOn(PaperConstants.TASK_MAKE_PAPER);
    }

    @Override
    protected Class<PaperExtension> getExtensionClass() {
        return PaperExtension.class;
    }
}
