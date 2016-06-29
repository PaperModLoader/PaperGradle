package xyz.papermodloader.papergradle;

import com.google.common.collect.ImmutableMap;
import org.gradle.api.*;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import xyz.papermodloader.papergradle.task.*;

import java.io.IOException;
import java.net.URL;

public class PaperGradle implements Plugin<Project> {
    public static PaperGradle INSTANCE;
    public static final String VERSION = "0.1.0-SNAPSHOT";

    private Project project;
    private boolean headerDisplayed;

    @Override
    public void apply(Project project) {
        PaperGradle.INSTANCE = this;
        this.project = project;
        this.project.apply(ImmutableMap.of("plugin", "java"));
        this.project.afterEvaluate(p -> this.afterEvaluate());
        this.project.getExtensions().create("paper", PaperExtension.class);
        this.project.getConfigurations().maybeCreate(Constants.CONFIG_DEPENDENCIES);
        this.project.getConfigurations().maybeCreate(Constants.CONFIG_DEPENDENCIES_CLIENT);
        this.project.getConfigurations().maybeCreate(Constants.CONFIG_NATIVES);

        this.addMavenRepo("mojang", "https://libraries.minecraft.net/");

        this.addTask(Constants.TASK_DOWNLOAD_CLIENT, DownloadTask.class, task -> task.setInit(download -> {
            try {
                task.setCache(Constants.CLIENT_JAR_CACHE.get().getName());
                task.setFile(Constants.CLIENT_JAR.get());
                task.setURL(new URL(Constants.CLIENT_DOWNLOAD.get().url));
            } catch (IOException e) {
                e.printStackTrace();
            }
        })).dependsOn(Constants.TASK_DOWNLOAD_ASSETS, Constants.TASK_DOWNLOAD_LIBRARIES);
        this.addTask(Constants.TASK_DOWNLOAD_LIBRARIES, DownloadLibrariesTask.class);
        this.addTask(Constants.TASK_DOWNLOAD_SERVER, DownloadTask.class, task -> task.setInit(download -> {
            try {
                task.setCache(Constants.SERVER_JAR_CACHE.get().getName());
                task.setURL(new URL(Constants.SERVER_DOWNLOAD.get().url));
            } catch (IOException e) {
                e.printStackTrace();
            }
        })).dependsOn(Constants.TASK_DOWNLOAD_LIBRARIES);
        this.addTask(Constants.TASK_DOWNLOAD_ASSETS, DownloadAssetsTask.class);
        this.addTask(Constants.TASK_DEOBFUSCATE, DeobfuscateTask.class).dependsOn(Constants.TASK_DOWNLOAD_CLIENT, Constants.TASK_DOWNLOAD_SERVER);
        this.addTask(Constants.TASK_MERGE, MergeTask.class).dependsOn(Constants.TASK_DEOBFUSCATE);
        this.addTask(Constants.TASK_DECOMPILE, DecompileTask.class).dependsOn(Constants.TASK_MERGE);
        this.addTask(Constants.TASK_SETUP, DefaultTask.class).dependsOn(Constants.TASK_DECOMPILE);
    }

    private void afterEvaluate() {
        if (!this.headerDisplayed) {
            this.getProject().getLogger().lifecycle("=============================================");
            this.getProject().getLogger().lifecycle("PaperGradle " + PaperGradle.VERSION);
            this.getProject().getLogger().lifecycle("https://github.com/PaperModLoader/PaperGradle");
            this.getProject().getLogger().lifecycle("=============================================");
            this.headerDisplayed = true;
        }
    }

    private <T extends Task> T addTask(String name, Class<? extends T> task) {
        return this.project.getTasks().create(name, task);
    }

    private <T extends Task> T addTask(String name, Class<? extends T> task, Action<T> action) {
        return this.project.getTasks().create(name, task, action);
    }

    public MavenArtifactRepository addMavenRepo(String name, String url) {
        return this.project.getRepositories().maven(repo -> {
            repo.setName(name);
            repo.setUrl(url);
        });
    }

    public PaperExtension getExtension() {
        return this.project.getExtensions().findByType(PaperExtension.class);
    }

    public Project getProject() {
        return project;
    }
}
