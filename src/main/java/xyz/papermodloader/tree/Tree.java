package xyz.papermodloader.tree;

import com.google.common.collect.ImmutableMap;
import org.gradle.api.*;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
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

        //Configure project
        this.project.apply(ImmutableMap.of("plugin", "java"));
        this.project.apply(ImmutableMap.of("plugin", "idea"));
        this.project.apply(ImmutableMap.of("plugin", "eclipse"));
        this.project.afterEvaluate(p -> this.afterEvaluate());
        this.project.getExtensions().create("paper", TreeExtension.class);
        this.project.getConfigurations().maybeCreate(Constants.CONFIG_DEPENDENCIES);
        this.project.getConfigurations().maybeCreate(Constants.CONFIG_NATIVES);
        this.project.getDependencies().add(JavaPlugin.COMPILE_CONFIGURATION_NAME, project.fileTree("libs"));
        this.addMavenRepo("mojang", "https://libraries.minecraft.net/");

        //Setup tasks
        this.addTask(Constants.TASK_DOWNLOAD_CLIENT, DownloadTask.class, task -> task.setInit(download -> {
            try {
                task.setCache(Constants.CLIENT_JAR_CACHE.get());
                task.setFile(Constants.CLIENT_JAR.get());
                task.setURL(new URL(Constants.CLIENT_DOWNLOAD.get().url));
                task.setSHA1(Constants.CLIENT_DOWNLOAD.get().sha1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        })).dependsOn(Constants.TASK_DOWNLOAD_ASSETS);
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
        }));
        this.addTask(Constants.TASK_DOWNLOAD_ASSETS, DownloadAssetsTask.class);
        this.addTask(Constants.TASK_MERGE, MergeTask.class).dependsOn(Constants.TASK_DOWNLOAD_CLIENT, Constants.TASK_DOWNLOAD_SERVER, Constants.TASK_DOWNLOAD_MAPPINGS);
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
        this.addTask(Constants.TASK_DOWNLOAD_LIBRARIES, DownloadLibrariesTask.class);
        this.addTask(Constants.TASK_EXTRACT_NATIVES, ExtractNativesTask.class).dependsOn(Constants.TASK_DOWNLOAD_LIBRARIES);
        this.addTask(Constants.TASK_IDEA, IDEAProjectTask.class).dependsOn("idea", Constants.TASK_EXTRACT_NATIVES);

        //Build tasks
        this.project.getTasks().getByName("compileJava").dependsOn(Constants.TASK_DOWNLOAD_LIBRARIES);
        this.addTask(Constants.TASK_GENERATE_HOOKS, GenerateHooksTask.class).dependsOn(JavaBasePlugin.BUILD_TASK_NAME);
        this.addTask(Constants.TASK_MAKE_PAPER, MakePaperTask.class).dependsOn(Constants.TASK_GENERATE_HOOKS);
        this.addTask(Constants.TASK_BUILD, DefaultTask.class).dependsOn(Constants.TASK_MAKE_PAPER);
    }

    private void afterEvaluate() {
        if (!this.headerDisplayed) {
            this.project.getLogger().lifecycle("======================================");
            this.project.getLogger().lifecycle("Tree " + Tree.VERSION);
            this.project.getLogger().lifecycle("https://github.com/PaperModLoader/Tree");
            this.project.getLogger().lifecycle("======================================");
            this.project.getLogger().lifecycle("Powered by Enigma");
            this.project.getLogger().lifecycle("http://www.cuchazinteractive.com/");
            this.project.getLogger().lifecycle("======================================");
            this.headerDisplayed = true;
        }
    }

    private <T extends Task> T addTask(String name, Class<? extends T> task) {
        T t = this.project.getTasks().create(name, task);
        t.setGroup("Tree");
        return t;
    }

    private <T extends Task> T addTask(String name, Class<? extends T> task, Action<T> action) {
        T t = this.project.getTasks().create(name, task, action);
        t.setGroup("Tree");
        return t;
    }

    public MavenArtifactRepository addMavenRepo(String name, String url) {
        return this.project.getRepositories().maven(repo -> {
            repo.setName(name);
            repo.setUrl(url);
        });
    }

    public TreeExtension getExtension() {
        return this.project.getExtensions().findByType(TreeExtension.class);
    }

    public Project getProject() {
        return project;
    }
}
