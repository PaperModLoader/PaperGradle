package xyz.papermodloader.tree.task;

import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import xyz.papermodloader.tree.util.HashUtil;
import xyz.papermodloader.tree.util.Initializer;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class DownloadTask extends DefaultTask {
    private Initializer initializer;
    private File cache;
    private File file;
    private String url;
    private String sha1;

    @TaskAction
    public void doTask() throws IOException {
        this.initializer.initialize();
        if (!this.cache.exists() || (this.sha1 != null && !HashUtil.equalHash(this.cache, this.sha1))) {
            if (this.file != null && this.file.exists()) {
                FileUtils.copyFile(this.file, this.cache);
                this.getLogger().info(":found fallback for " + this.cache.getName());
            } else {
                FileUtils.copyURLToFile(new URL(this.url), this.cache);
                this.getLogger().info(":downloading " + this.cache.getName());
            }
        }
    }

    public void setInitializer(Initializer initializer) {
        this.initializer = initializer;
    }

    public void setCache(File cache) {
        this.cache = cache;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setURL(String url) {
        this.url = url;
    }

    public void setSHA1(String sha1) {
        this.sha1 = sha1;
    }
}
