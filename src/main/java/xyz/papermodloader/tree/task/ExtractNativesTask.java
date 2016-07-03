package xyz.papermodloader.tree.task;

import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import xyz.papermodloader.tree.Constants;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ExtractNativesTask extends DefaultTask {
    @TaskAction
    public void doTask() {
        if (!Constants.NATIVES_DIRECTORY_CACHE.exists()) {
            for (File source : this.getProject().getConfigurations().getByName(Constants.CONFIG_NATIVES)) {
                try {
                    ZipFile zip = new ZipFile(source);
                    Enumeration<? extends ZipEntry> entries = zip.entries();
                    File nativesDirectory = Constants.NATIVES_DIRECTORY_CACHE;
                    if (!nativesDirectory.exists()) {
                        nativesDirectory.mkdir();
                    }
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();
                        if (!entry.getName().contains("META-INF")) {
                            FileUtils.copyToFile(zip.getInputStream(entry), new File(nativesDirectory, entry.getName()));
                        }
                    }
                    zip.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
