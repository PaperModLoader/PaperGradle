package xyz.papermodloader.tree.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.plugins.ide.eclipse.model.EclipseModel;
import org.gradle.plugins.ide.idea.model.IdeaModel;
import xyz.papermodloader.tree.util.Initializer;
import xyz.papermodloader.tree.util.MinecraftJSON;

import java.io.File;

public class DownloadLibrariesTask extends DefaultTask {
    private Initializer initializer;
    private MinecraftJSON json;
    private String nativesConfig;
    private String dependencyConfig;
    private File[] additionalLibraries;

    @TaskAction
    public void doTask() throws InterruptedException {
        this.initializer.initialize();
        DependencyHandler dependencyHandler = this.getProject().getDependencies();
        MinecraftJSON.Library[] libraries = this.json.libraries;
        for (MinecraftJSON.Library library : libraries) {
            if (library.isAllowed()) {
                if (library.natives != null) {
                    if (this.getProject().getConfigurations().getByName(this.nativesConfig).getState() == Configuration.State.UNRESOLVED) {
                        dependencyHandler.add(this.nativesConfig, library.getArtifactName());
                        this.getLogger().info("Adding native dependency " + library.name);
                    }
                } else {
                    if (this.getProject().getConfigurations().getByName(this.dependencyConfig).getState() == Configuration.State.UNRESOLVED) {
                        dependencyHandler.add(this.dependencyConfig, library.getArtifactName());
                        this.getLogger().info("Adding dependency " + library.name);
                    }
                }
            }
        }

        dependencyHandler.add(this.dependencyConfig, "net.minecraft:launchwrapper:1.12");
        for (File file : this.additionalLibraries) {
            dependencyHandler.add(this.dependencyConfig, this.getProject().files(file));
        }

        //Configure IDEA
        IdeaModel idea = (IdeaModel) this.getProject().getExtensions().getByName("idea");
        idea.getModule().getExcludeDirs().addAll(this.getProject().files(".gradle", "build", ".idea", "out").getFiles());
        idea.getModule().setDownloadJavadoc(true);
        idea.getModule().setDownloadSources(true);
        idea.getModule().setInheritOutputDirs(true);
        idea.getModule().getScopes().get("COMPILE").get("plus").add(this.getProject().getConfigurations().getByName(this.dependencyConfig));

        //Configure Eclipse
        EclipseModel eclipse = (EclipseModel) this.getProject().getExtensions().getByName("eclipse");
        eclipse.getClasspath().getPlusConfigurations().add(this.getProject().getConfigurations().getByName(this.dependencyConfig));

        //Configure compiler
        JavaPluginConvention javaModule = (JavaPluginConvention) this.getProject().getConvention().getPlugins().get("java");
        SourceSet main = javaModule.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        SourceSet test = javaModule.getSourceSets().getByName(SourceSet.TEST_SOURCE_SET_NAME);
        main.setCompileClasspath(main.getCompileClasspath().plus(this.getProject().getConfigurations().getByName(this.dependencyConfig)));
        test.setCompileClasspath(test.getCompileClasspath().plus(this.getProject().getConfigurations().getByName(this.dependencyConfig)));
        main.setRuntimeClasspath(main.getCompileClasspath().plus(this.getProject().getConfigurations().getByName(this.dependencyConfig)));
        test.setCompileClasspath(test.getCompileClasspath().plus(this.getProject().getConfigurations().getByName(this.dependencyConfig)));
        Javadoc javadoc = (Javadoc) this.getProject().getTasks().getByName(JavaPlugin.JAVADOC_TASK_NAME);
        javadoc.setClasspath(main.getOutput().plus(main.getCompileClasspath()));
    }

    public void setInitializer(Initializer initializer) {
        this.initializer = initializer;
    }

    public void setJSON(MinecraftJSON json) {
        this.json = json;
    }

    public void setNativesConfig(String nativesConfig) {
        this.nativesConfig = nativesConfig;
    }

    public void setDependencyConfig(String dependencyConfig) {
        this.dependencyConfig = dependencyConfig;
    }

    public void setAdditionalLibraries(File... additionalLibraries) {
        this.additionalLibraries = additionalLibraries;
    }
}
