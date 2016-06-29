package xyz.papermodloader.tree.task;

import cuchaz.enigma.Deobfuscator;
import cuchaz.enigma.mapping.MappingParseException;
import cuchaz.enigma.mapping.MappingsReader;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.logging.progress.ProgressLogger;
import org.gradle.internal.logging.progress.ProgressLoggerFactory;
import xyz.papermodloader.tree.Constants;

import java.io.FileReader;
import java.io.IOException;
import java.util.jar.JarFile;

public class DeobfuscateTask extends DefaultTask implements Deobfuscator.ProgressListener {
    private ProgressLogger progressLogger;
    private int total;

    @TaskAction
    public void doTask() throws InterruptedException, IOException, MappingParseException {
        Deobfuscator deobfuscator = new Deobfuscator(new JarFile(Constants.MERGED_JAR_CACHE.get()));
        deobfuscator.setMappings(new MappingsReader().read(new FileReader(Constants.MAPPINGS_FILE_CACHE.get())));
        deobfuscator.writeJar(Constants.DEOBF_MERGED_JAR_CACHE.get(), this);
        this.progressLogger.completed();
    }

    @Override
    public void init(int total, String title) {
        this.total = total;
        this.progressLogger = this.getServices().get(ProgressLoggerFactory.class).newOperation(this.getClass());
        this.progressLogger.start(":deobfuscate", ":deobfuscate");
    }

    @Override
    public void onProgress(int current, String message) {
        this.progressLogger.progress(current + "/" + this.total + " (" + (int) ((current / (float) this.total) * 100) + "%)");
    }
}
