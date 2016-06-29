package xyz.papermodloader.papergradle.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.tasks.TaskAction;
import xyz.papermodloader.papergradle.Constants;
import xyz.papermodloader.papergradle.util.LauncherManifest;

public class DownloadLibrariesTask extends DefaultTask {
    @TaskAction
    public void doTask() throws InterruptedException {
        LauncherManifest.ManifestVersion.Version version = Constants.VERSION.get();
        LauncherManifest.ManifestVersion.Version.Library[] libraries = version.libraries;
        DependencyHandler dependencyHandler = this.getProject().getDependencies();
        if (this.getProject().getConfigurations().getByName(Constants.CONFIG_DEPENDENCIES).getState() == Configuration.State.UNRESOLVED) {
            for (LauncherManifest.ManifestVersion.Version.Library library : libraries) {
                if (library.natives == null) {
                    String configName = Constants.CONFIG_DEPENDENCIES;
                    if (library.name.contains("java3d") || library.name.contains("paulscode") || library.name.contains("lwjgl") || library.name.contains("twitch") || library.name.contains("jinput")) {
                        configName = Constants.CONFIG_DEPENDENCIES_CLIENT;
                    }
                    dependencyHandler.add(configName, library.name);
                }
            }
        }
        if (this.getProject().getConfigurations().getByName(Constants.CONFIG_NATIVES).getState() == Configuration.State.UNRESOLVED) {
            for (LauncherManifest.ManifestVersion.Version.Library library : libraries) {
                if (library.natives != null) {
                    dependencyHandler.add(Constants.CONFIG_NATIVES, library.name);
                }
            }
        }
        dependencyHandler.add(Constants.CONFIG_DEPENDENCIES, "net.minecraft:launchwrapper:1.11");
    }
}
