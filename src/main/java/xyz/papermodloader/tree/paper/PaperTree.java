package xyz.papermodloader.tree.paper;

import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import xyz.papermodloader.tree.paper.task.DownloadTask;
import xyz.papermodloader.tree.Tree;
import xyz.papermodloader.tree.paper.task.*;

import java.io.IOException;
import java.net.URL;

public class PaperTree extends Tree<PaperExtension> {
    public static PaperTree INSTANCE;

    @Override
    public String getPluginName() {
        return "Paper Tree";
    }

    @Override
    public String getExtensionName() {
        return "paper";
    }

    @Override
    protected void configure() {
        PaperTree.INSTANCE = this;

        this.getProject().getConfigurations().maybeCreate(PaperConstants.CONFIG_DEPENDENCIES);
        this.getProject().getConfigurations().maybeCreate(PaperConstants.CONFIG_NATIVES);
        this.getProject().getDependencies().add(JavaPlugin.COMPILE_CONFIGURATION_NAME, this.getProject().fileTree("libs"));
        this.addMavenRepo("mojang", "https://libraries.minecraft.net/");
    }

    @Override
    protected void addTasks() {
        //Setup tasks
        this.addTask(PaperConstants.TASK_DOWNLOAD_CLIENT, DownloadTask.class, task -> task.setInit(download -> {
            try {
                task.setCache(PaperConstants.CLIENT_JAR_CACHE.get());
                task.setFile(PaperConstants.CLIENT_JAR.get());
                task.setURL(new URL(PaperConstants.CLIENT_DOWNLOAD.get().url));
                task.setSHA1(PaperConstants.CLIENT_DOWNLOAD.get().sha1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        })).dependsOn(PaperConstants.TASK_DOWNLOAD_ASSETS);
        this.addTask(PaperConstants.TASK_DOWNLOAD_MAPPINGS, DownloadTask.class, task -> task.setInit(download -> {
            try {
                task.setCache(PaperConstants.MAPPINGS_FILE_CACHE.get());
                task.setURL(new URL("http://ci.ilexiconn.net/job/Cardboard/" + this.getExtension().mappings + "/artifact/" + this.getExtension().minecraft + ".json"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
        this.addTask(PaperConstants.TASK_DOWNLOAD_SERVER, DownloadTask.class, task -> task.setInit(download -> {
            try {
                task.setCache(PaperConstants.SERVER_JAR_CACHE.get());
                task.setURL(new URL(PaperConstants.SERVER_DOWNLOAD.get().url));
                task.setSHA1(PaperConstants.SERVER_DOWNLOAD.get().sha1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
        this.addTask(PaperConstants.TASK_DOWNLOAD_ASSETS, DownloadAssetsTask.class);
        this.addTask(PaperConstants.TASK_MERGE, MergeTask.class).dependsOn(PaperConstants.TASK_DOWNLOAD_CLIENT, PaperConstants.TASK_DOWNLOAD_SERVER, PaperConstants.TASK_DOWNLOAD_MAPPINGS);
        this.addTask(PaperConstants.TASK_DEOBFUSCATE, DeobfuscateTask.class).dependsOn(PaperConstants.TASK_MERGE);
        this.addTask(PaperConstants.TASK_GENERATE_OBF_INDEX, GenerateIndexTask.class, task -> task.setInit(generate -> {
            task.setInput(PaperConstants.MERGED_JAR_CACHE.get());
            task.setOutput(PaperConstants.OBF_INDEX_CACHE.get());
        })).dependsOn(PaperConstants.TASK_DEOBFUSCATE);
        this.addTask(PaperConstants.TASK_GENERATE_DEOBF_INDEX, GenerateIndexTask.class, task -> task.setInit(generate -> {
            task.setInput(PaperConstants.DEOBF_MERGED_JAR_CACHE.get());
            task.setOutput(PaperConstants.DEOBF_INDEX_CACHE.get());
        })).dependsOn(PaperConstants.TASK_DEOBFUSCATE);
        this.addTask(PaperConstants.TASK_MAKE_MINECRAFT, MakeMinecraftTask.class).dependsOn(PaperConstants.TASK_GENERATE_OBF_INDEX, PaperConstants.TASK_GENERATE_DEOBF_INDEX);
        this.addTask(PaperConstants.TASK_SETUP, DefaultTask.class).dependsOn(PaperConstants.TASK_MAKE_MINECRAFT);
        this.addTask(PaperConstants.TASK_DOWNLOAD_LIBRARIES, DownloadLibrariesTask.class);
        this.addTask(PaperConstants.TASK_EXTRACT_NATIVES, ExtractNativesTask.class).dependsOn(PaperConstants.TASK_DOWNLOAD_LIBRARIES);
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
