package xyz.papermodloader.tree.paper.task;

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
import xyz.papermodloader.tree.paper.PaperConstants;
import xyz.papermodloader.tree.util.LauncherManifest;

public class DownloadLibrariesTask extends DefaultTask {
    @TaskAction
    public void doTask() throws InterruptedException {
        DependencyHandler dependencyHandler = this.getProject().getDependencies();

        LauncherManifest.ManifestVersion.Version.Library[] libraries = PaperConstants.VERSION.get().libraries;
        for (LauncherManifest.ManifestVersion.Version.Library library : libraries) {
            if (library.isAllowed()) {
                if (library.natives != null) {
                    if (this.getProject().getConfigurations().getByName(PaperConstants.CONFIG_NATIVES).getState() == Configuration.State.UNRESOLVED) {
                        dependencyHandler.add(PaperConstants.CONFIG_NATIVES, library.getArtifactName());
                        this.getLogger().info("Adding native dependency " + library.name);
                    }
                } else {
                    if (this.getProject().getConfigurations().getByName(PaperConstants.CONFIG_DEPENDENCIES).getState() == Configuration.State.UNRESOLVED) {
                        dependencyHandler.add(PaperConstants.CONFIG_DEPENDENCIES, library.getArtifactName());
                        this.getLogger().info("Adding dependency " + library.name);
                    }
                }
            }
        }

        dependencyHandler.add(PaperConstants.CONFIG_DEPENDENCIES, "net.minecraft:launchwrapper:1.12");
        dependencyHandler.add(PaperConstants.CONFIG_DEPENDENCIES, this.getProject().files(PaperConstants.MINECRAFT_LIBRARY_JAR_CACHE.get()));

        //Configure IDEA
        IdeaModel idea = (IdeaModel) this.getProject().getExtensions().getByName("idea");
        idea.getModule().getExcludeDirs().addAll(this.getProject().files(".gradle", "build", ".idea", "out").getFiles());
        idea.getModule().setDownloadJavadoc(true);
        idea.getModule().setDownloadSources(true);
        idea.getModule().setInheritOutputDirs(true);
        idea.getModule().getScopes().get("COMPILE").get("plus").add(this.getProject().getConfigurations().getByName(PaperConstants.CONFIG_DEPENDENCIES));

        //Configure Eclipse
        EclipseModel eclipse = (EclipseModel) this.getProject().getExtensions().getByName("eclipse");
        eclipse.getClasspath().getPlusConfigurations().add(this.getProject().getConfigurations().getByName(PaperConstants.CONFIG_DEPENDENCIES));

        //Configure compiler
        JavaPluginConvention javaModule = (JavaPluginConvention) this.getProject().getConvention().getPlugins().get("java");
        SourceSet main = javaModule.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        SourceSet test = javaModule.getSourceSets().getByName(SourceSet.TEST_SOURCE_SET_NAME);
        main.setCompileClasspath(main.getCompileClasspath().plus(this.getProject().getConfigurations().getByName(PaperConstants.CONFIG_DEPENDENCIES)));
        test.setCompileClasspath(test.getCompileClasspath().plus(this.getProject().getConfigurations().getByName(PaperConstants.CONFIG_DEPENDENCIES)));
        main.setRuntimeClasspath(main.getCompileClasspath().plus(this.getProject().getConfigurations().getByName(PaperConstants.CONFIG_DEPENDENCIES)));
        test.setCompileClasspath(test.getCompileClasspath().plus(this.getProject().getConfigurations().getByName(PaperConstants.CONFIG_DEPENDENCIES)));
        Javadoc javadoc = (Javadoc) this.getProject().getTasks().getByName(JavaPlugin.JAVADOC_TASK_NAME);
        javadoc.setClasspath(main.getOutput().plus(main.getCompileClasspath()));
    }
}
