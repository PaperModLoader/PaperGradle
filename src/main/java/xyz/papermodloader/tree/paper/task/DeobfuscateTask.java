package xyz.papermodloader.tree.paper.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.logging.progress.ProgressLoggerFactory;
import xyz.papermodloader.book.Book;
import xyz.papermodloader.book.mapping.Mappings;
import xyz.papermodloader.book.util.ProgressLogger;
import xyz.papermodloader.tree.paper.PaperConstants;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DeobfuscateTask extends DefaultTask implements ProgressLogger {
    public org.gradle.internal.logging.progress.ProgressLogger progressLogger;

    @TaskAction
    public void doTask() throws InterruptedException, IOException {
        this.progressLogger = this.getServices().get(ProgressLoggerFactory.class).newOperation(this.getClass());
        this.progressLogger.start(":deobfuscate", ":deobfuscate");
        InputStream stream = new FileInputStream(PaperConstants.MAPPINGS_FILE_CACHE.get());
        Mappings mappings = Mappings.parseMappings(stream);
        stream.close();
        Book.INSTANCE.map(mappings, PaperConstants.MERGED_JAR_CACHE.get(), PaperConstants.DEOBF_MERGED_JAR_CACHE.get(), this);
        this.progressLogger.completed();
    }

    @Override
    public void onProgress(int current, int total) {
        this.progressLogger.progress(current + "/" + total + " (" + (int) ((current / (float) total) * 100) + "%)");
    }
}
