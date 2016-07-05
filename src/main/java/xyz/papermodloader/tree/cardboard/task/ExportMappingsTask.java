package xyz.papermodloader.tree.cardboard.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.logging.progress.ProgressLoggerFactory;
import xyz.papermodloader.book.converter.exporter.Exporters;
import xyz.papermodloader.book.util.ProgressLogger;
import xyz.papermodloader.tree.cardboard.CardboardConstants;

import java.io.IOException;

public class ExportMappingsTask extends DefaultTask implements ProgressLogger {
    public org.gradle.internal.logging.progress.ProgressLogger progressLogger;

    @TaskAction
    public void doTask() throws IOException {
        this.progressLogger = this.getServices().get(ProgressLoggerFactory.class).newOperation(this.getClass());
        this.progressLogger.start(":exportMappings", ":export");
        Exporters.getExporter("json").export(CardboardConstants.ENIGMA_MAPPINGS.get(), CardboardConstants.BOOK_MAPPINGS.get(), this);
        this.progressLogger.completed();
    }

    @Override
    public void onProgress(int current, int total) {
        this.progressLogger.progress(current + "/" + total + " (" + (int) ((current / (float) total) * 100) + "%)");
    }
}
