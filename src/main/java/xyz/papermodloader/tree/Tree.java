package xyz.papermodloader.tree;

import com.google.common.collect.ImmutableMap;
import org.gradle.api.*;
import xyz.papermodloader.tree.task.*;

import java.io.IOException;
import java.net.URL;

public class Tree implements Plugin<Project> {
    public static Tree INSTANCE;
    public static final String VERSION = "0.1.0";

    private Project project;
    private boolean headerDisplayed;

    @Override
    public void apply(Project project) {
        Tree.INSTANCE = this;
        this.project = project;
        this.project.apply(ImmutableMap.of("plugin", "java"));
        this.project.apply(ImmutableMap.of("plugin", "idea"));
        this.project.afterEvaluate(p -> this.afterEvaluate());
        this.project.getExtensions().create("paper", TreeExtension.class);

        this.addTask(Constants.TASK_DOWNLOAD_CLIENT, DownloadTask.class, task -> task.setInit(download -> {
            try {
                task.setCache(Constants.CLIENT_JAR_CACHE.get());
                task.setFile(Constants.CLIENT_JAR.get());
                task.setURL(new URL(Constants.CLIENT_DOWNLOAD.get().url));
                task.setSHA1(Constants.CLIENT_DOWNLOAD.get().sha1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        })).dependsOn(Constants.TASK_DOWNLOAD_ASSETS, Constants.TASK_DOWNLOAD_LIBRARIES);
        this.addTask(Constants.TASK_DOWNLOAD_LIBRARIES, DownloadLibrariesTask.class).dependsOn(Constants.TASK_DOWNLOAD_MAPPINGS);
        this.addTask(Constants.TASK_DOWNLOAD_MAPPINGS, DownloadTask.class, task -> task.setInit(download -> {
            try {
                task.setCache(Constants.MAPPINGS_FILE_CACHE.get());
                task.setURL(new URL("http://ci.ilexiconn.net/job/Cardboard/" + this.getExtension().mappings + "/artifact/" + this.getExtension().minecraft + ".mappings"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
        this.addTask(Constants.TASK_DOWNLOAD_SERVER, DownloadTask.class, task -> task.setInit(download -> {
            try {
                task.setCache(Constants.SERVER_JAR_CACHE.get());
                task.setURL(new URL(Constants.SERVER_DOWNLOAD.get().url));
                task.setSHA1(Constants.SERVER_DOWNLOAD.get().sha1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        })).dependsOn(Constants.TASK_DOWNLOAD_LIBRARIES);
        this.addTask(Constants.TASK_DOWNLOAD_ASSETS, DownloadAssetsTask.class);
        this.addTask(Constants.TASK_MERGE, MergeTask.class).dependsOn(Constants.TASK_DOWNLOAD_CLIENT, Constants.TASK_DOWNLOAD_SERVER);
        this.addTask(Constants.TASK_DEOBFUSCATE, DeobfuscateTask.class).dependsOn(Constants.TASK_MERGE);
        this.addTask(Constants.TASK_GENERATE_OBF_INDEX, GenerateIndexTask.class, task -> task.setInit(generate -> {
            task.setInput(Constants.MERGED_JAR_CACHE.get());
            task.setOutput(Constants.OBF_INDEX_CACHE.get());
        })).dependsOn(Constants.TASK_DEOBFUSCATE);
        this.addTask(Constants.TASK_GENERATE_DEOBF_INDEX, GenerateIndexTask.class, task -> task.setInit(generate -> {
            task.setInput(Constants.DEOBF_MERGED_JAR_CACHE.get());
            task.setOutput(Constants.DEOBF_INDEX_CACHE.get());
        })).dependsOn(Constants.TASK_DEOBFUSCATE);
        this.addTask(Constants.TASK_MAKE_MINECRAFT, MakeMinecraftTask.class).dependsOn(Constants.TASK_GENERATE_OBF_INDEX, Constants.TASK_GENERATE_DEOBF_INDEX);
        this.addTask(Constants.TASK_SETUP, DefaultTask.class).dependsOn(Constants.TASK_MAKE_MINECRAFT);
        this.addTask(Constants.TASK_IDEA, IDEAProjectTask.class).dependsOn("idea");
    }

    private void afterEvaluate() {
        if (!this.headerDisplayed) {
            this.getProject().getLogger().lifecycle("======================================");
            this.getProject().getLogger().lifecycle("Tree " + Tree.VERSION);
            this.getProject().getLogger().lifecycle("https://github.com/PaperModLoader/Tree");
            this.getProject().getLogger().lifecycle("======================================");
            this.headerDisplayed = true;
        }
    }

    private <T extends Task> T addTask(String name, Class<? extends T> task) {
        return this.project.getTasks().create(name, task);
    }

    private <T extends Task> T addTask(String name, Class<? extends T> task, Action<T> action) {
        return this.project.getTasks().create(name, task, action);
    }

    public TreeExtension getExtension() {
        return this.project.getExtensions().findByType(TreeExtension.class);
    }

    public Project getProject() {
        return project;
    }
}
