package xyz.papermodloader.tree.task;

import com.strobel.decompiler.DecompilerSettings;
import cuchaz.enigma.Deobfuscator;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.logging.progress.ProgressLogger;
import org.gradle.internal.logging.progress.ProgressLoggerFactory;
import xyz.papermodloader.tree.Constants;

import java.lang.reflect.Field;
import java.util.jar.JarFile;

public class DecompileTask extends DefaultTask implements Deobfuscator.ProgressListener {
    private ProgressLogger progressLogger;
    private int total;

    @TaskAction
    public void doTask() {
        try {
            if (Constants.MINECRAFT_DECOMPILED_CACHE.exists()) {
                return;
            }
            Deobfuscator deobfuscator = new Deobfuscator(new JarFile(Constants.DEOBF_MERGED_JAR_CACHE.get()));
            Field settings = Deobfuscator.class.getDeclaredField("m_settings");
            settings.setAccessible(true);
            ((DecompilerSettings) settings.get(deobfuscator)).setShowDebugLineNumbers(false);
            deobfuscator.writeSources(Constants.MINECRAFT_DECOMPILED_CACHE, this);
            this.progressLogger.completed();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init(int total, String title) {
        this.total = total;
        this.progressLogger = this.getServices().get(ProgressLoggerFactory.class).newOperation(this.getClass());
        this.progressLogger.start(":decompile", ":decompile");
    }

    @Override
    public void onProgress(int current, String message) {
        this.progressLogger.progress(current + "/" + this.total + " (" + (int) ((current / (float) this.total) * 100) + "%)");
    }
}
