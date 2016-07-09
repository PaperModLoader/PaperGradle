package xyz.papermodloader.tree.task;

import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import xyz.papermodloader.tree.util.Initializer;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ExtractNativesTask extends DefaultTask {
    private Initializer initializer;
    private String nativesConfig;
    private File nativesDirectory;

    @TaskAction
    public void doTask() {
        this.initializer.initialize();
        if (!this.nativesDirectory.exists()) {
            for (File source : this.getProject().getConfigurations().getByName(this.nativesConfig)) {
                try {
                    ZipFile zip = new ZipFile(source);
                    Enumeration<? extends ZipEntry> entries = zip.entries();
                    if (!this.nativesDirectory.exists()) {
                        this.nativesDirectory.mkdir();
                    }
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();
                        if (!entry.getName().contains("META-INF")) {
                            FileUtils.copyToFile(zip.getInputStream(entry), new File(this.nativesDirectory, entry.getName()));
                        }
                    }
                    zip.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setInitializer(Initializer initializer) {
        this.initializer = initializer;
    }

    public void setNativesConfig(String nativesConfig) {
        this.nativesConfig = nativesConfig;
    }

    public void setNativesDirectory(File nativesDirectory) {
        this.nativesDirectory = nativesDirectory;
    }
}
