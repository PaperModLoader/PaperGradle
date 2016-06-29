package xyz.papermodloader.tree.task;

import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.logging.progress.ProgressLogger;
import org.gradle.internal.logging.progress.ProgressLoggerFactory;
import xyz.papermodloader.tree.Constants;
import xyz.papermodloader.tree.util.HashUtil;
import xyz.papermodloader.tree.util.LauncherManifest;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DownloadLibrariesTask extends DefaultTask {
    @TaskAction
    public void doTask() throws InterruptedException {
        LauncherManifest.ManifestVersion.Version.Library[] libraries = Constants.VERSION.get().libraries;
        ProgressLogger progressLogger = this.getServices().get(ProgressLoggerFactory.class).newOperation(this.getClass());
        progressLogger.start(":downloadLibraries", ":libraries");
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
        int total = libraries.length;
        int[] current = {0};
        for (LauncherManifest.ManifestVersion.Version.Library library : libraries) {
            executor.submit(() -> {
                try {
                    if (library.isAllowed()) {
                        String path = "libraries" + File.separator + library.getFile();
                        File file = new File(Constants.MINECRAFT_DIRECTORY.get(), path);
                        File target = new File(Constants.CACHE_DIRECTORY, path);
                        if (!file.exists() || !HashUtil.equalHash(file, library.getSHA1())) {
                            FileUtils.copyFile(file, target);
                        } else {
                            this.getLogger().info(":downloading library " + library.name);
                            FileUtils.copyURLToFile(new URL(library.getURL()), target);
                        }
                    }
                    current[0]++;
                    progressLogger.progress(current[0] + "/" + total + " (" + (int) ((current[0] / (float) total) * 100) + "%)");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        executor.shutdown();
        while (!executor.awaitTermination(1, TimeUnit.MILLISECONDS));
        progressLogger.completed();
    }
}
