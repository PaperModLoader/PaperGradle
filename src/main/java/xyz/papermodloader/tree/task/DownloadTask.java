package xyz.papermodloader.tree.task;

import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import xyz.papermodloader.tree.Constants;
import xyz.papermodloader.tree.util.HashUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

public class DownloadTask extends DefaultTask {
    private String cache;
    private File file;
    private URL url;
    private String sha1;
    private Consumer<DownloadTask> init;

    @TaskAction
    public void doTask() throws IOException {
        this.init.accept(this);
        File cache = new File(Constants.CACHE_DIRECTORY, this.cache);
        if (!cache.exists() || (this.sha1 != null && !HashUtil.equalHash(cache, this.sha1))) {
            if (this.file != null && this.file.exists()) {
                FileUtils.copyFile(this.file, cache);
                this.getLogger().info(":found fallback for " + this.cache);
            } else {
                FileUtils.copyURLToFile(this.url, cache);
                this.getLogger().info(":downloading " + this.cache);
            }
        }
    }

    public void setInit(Consumer<DownloadTask> function) {
        this.init = function;
    }

    public void setCache(String cache) {
        this.cache = cache;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setURL(URL url) {
        this.url = url;
    }

    public void setSHA1(String sha1) {
        this.sha1 = sha1;
    }
}
