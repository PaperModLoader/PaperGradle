package xyz.papermodloader.tree.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.logging.progress.ProgressLoggerFactory;
import xyz.papermodloader.book.Book;
import xyz.papermodloader.book.mapping.Mappings;
import xyz.papermodloader.book.util.ProgressLogger;
import xyz.papermodloader.tree.util.Initializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DeobfuscateTask extends DefaultTask implements ProgressLogger {
    private org.gradle.internal.logging.progress.ProgressLogger progressLogger;

    private Initializer initializer;
    private File mappings;
    private File mergedJar;
    private File mergedDeobfJar;

    @TaskAction
    public void doTask() throws InterruptedException, IOException {
        this.initializer.initialize();
        this.progressLogger = this.getServices().get(ProgressLoggerFactory.class).newOperation(this.getClass());
        this.progressLogger.start(":deobfuscate", ":deobfuscate");
        InputStream stream = new FileInputStream(this.mappings);
        Mappings mappings = Mappings.parseMappings(stream);
        stream.close();
        Book.INSTANCE.map(mappings, this.mergedJar, this.mergedDeobfJar, this);
        this.progressLogger.completed();
    }

    @Override
    public void onProgress(int current, int total) {
        this.progressLogger.progress(current + "/" + total + " (" + (int) ((current / (float) total) * 100) + "%)");
    }

    public void setInitializer(Initializer initializer) {
        this.initializer = initializer;
    }

    public void setMappings(File mappings) {
        this.mappings = mappings;
    }

    public void setMergedJar(File mergedJar) {
        this.mergedJar = mergedJar;
    }

    public void setMergedDeobfJar(File mergedDeobfJar) {
        this.mergedDeobfJar = mergedDeobfJar;
    }
}
