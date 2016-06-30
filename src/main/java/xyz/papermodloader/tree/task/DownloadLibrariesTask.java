package xyz.papermodloader.tree.task;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.logging.progress.ProgressLogger;
import org.gradle.internal.logging.progress.ProgressLoggerFactory;
import xyz.papermodloader.tree.Constants;
import xyz.papermodloader.tree.util.HashUtil;
import xyz.papermodloader.tree.util.LauncherManifest;
import xyz.papermodloader.tree.util.OperatingSystem;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class DownloadLibrariesTask extends DefaultTask {
    @TaskAction
    public void doTask() throws InterruptedException {
        LauncherManifest.ManifestVersion.Version.Library[] libraries = Constants.VERSION.get().libraries;
        LauncherManifest.ManifestVersion.Version.Library launchWrapper = new LauncherManifest.ManifestVersion.Version.Library();
        launchWrapper.name = "net.minecraft:launchwrapper:1.11";
        Constants.VERSION.get().libraries = libraries = ArrayUtils.add(libraries, launchWrapper);
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
                        if (file.exists() && HashUtil.equalHash(file, library.getSHA1())) {
                            FileUtils.copyFile(file, target);
                        } else {
                            this.getLogger().info(":downloading library " + library.name);
                            FileUtils.copyURLToFile(new URL(library.getURL()), target);
                            if (library.name.contains("-platform")) {
                                ZipFile zip = new ZipFile(target);
                                Enumeration<? extends ZipEntry> entries = zip.entries();
                                File nativesDirectory = Constants.NATIVES_DIRECTORY;
                                if (!nativesDirectory.exists()) {
                                    nativesDirectory.mkdir();
                                }
                                while (entries.hasMoreElements()) {
                                    ZipEntry entry = entries.nextElement();
                                    if (!entry.getName().contains("META_INF")) {
                                        FileUtils.copyToFile(zip.getInputStream(entry), new File(nativesDirectory, entry.getName()));
                                    }
                                }
                                zip.close();
                            }
                        }
                    } else {
                        this.getLogger().lifecycle(":library " + library.name + " not allowed on os " + OperatingSystem.getOS());
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
