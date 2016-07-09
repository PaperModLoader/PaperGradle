package xyz.papermodloader.tree;

import com.google.common.collect.ImmutableMap;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;

public abstract class TreePlugin<K> implements Plugin<Project> {
    public static final String VERSION = "0.1.1";

    private Project project;
    private boolean headerDisplayed;

    @Override
    public final void apply(Project project) {
        this.project = project;

        this.project.afterEvaluate(p -> this.afterEvaluate());
        this.getProject().getExtensions().create(this.getExtensionName(), this.getExtensionClass());
        this.getProject().apply(ImmutableMap.of("plugin", "java"));
        this.getProject().apply(ImmutableMap.of("plugin", "idea"));
        this.getProject().apply(ImmutableMap.of("plugin", "eclipse"));

        this.configure();
        this.addTasks();
    }

    public abstract String getPluginName();

    public abstract String getExtensionName();

    protected abstract void configure();

    protected abstract void addTasks();

    private void afterEvaluate() {
        if (!this.headerDisplayed) {
            this.project.getLogger().lifecycle("======================================");
            this.project.getLogger().lifecycle(this.getPluginName() + " " + TreePlugin.VERSION);
            this.project.getLogger().lifecycle("https://github.com/PaperModLoader/Tree");
            this.project.getLogger().lifecycle("======================================");
            this.headerDisplayed = true;
        }
    }

    protected <T extends Task> T addTask(String name, Class<? extends T> type) {
        T task = this.project.getTasks().create(name, type);
        task.setGroup(this.getPluginName());
        return task;
    }

    protected <T extends Task> T addTask(String name, Class<? extends T> type, Action<T> action) {
        T task = this.project.getTasks().create(name, type, action);
        task.setGroup(this.getPluginName());
        return task;
    }

    protected MavenArtifactRepository addMavenRepo(String name, String url) {
        return this.project.getRepositories().maven(repo -> {
            repo.setName(name);
            repo.setUrl(url);
        });
    }

    public Project getProject() {
        return this.project;
    }

    public K getExtension() {
        return this.getProject().getExtensions().findByType(this.getExtensionClass());
    }

    protected abstract Class<K> getExtensionClass();
}
