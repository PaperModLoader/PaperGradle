package xyz.papermodloader.papergradle.task;

import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import xyz.papermodloader.papergradle.Constants;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

public class DownloadTask extends DefaultTask {
    private String cache;
    private File file;
    private URL url;
    private Consumer<DownloadTask> init;

    @TaskAction
    public void doTask() throws IOException {
        this.init.accept(this);
        File cache = new File(Constants.CACHE_DIRECTORY, this.cache);
        if (!cache.exists()) {
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
}
