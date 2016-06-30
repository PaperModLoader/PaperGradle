package xyz.papermodloader.tree.task;

import cuchaz.enigma.Constants;
import cuchaz.enigma.analysis.JarClassIterator;
import cuchaz.enigma.analysis.TranslationIndex;
import cuchaz.enigma.bytecode.ClassRenamer;
import javassist.CtClass;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.jar.JarFile;

public class GenerateIndexTask extends DefaultTask {
    private Consumer<GenerateIndexTask> init;
    private File input;
    private File output;

    @TaskAction
    public void doTask() throws IOException {
        this.init.accept(this);
        if (this.output.exists()) {
            return;
        }
        TranslationIndex index = new TranslationIndex();
        for (CtClass ct : JarClassIterator.classes(new JarFile(this.input))) {
            ClassRenamer.moveAllClassesOutOfDefaultPackage(ct, Constants.NonePackage);
            index.indexClass(ct);
        }
        if (!this.output.getParentFile().exists()) {
            this.output.getParentFile().mkdirs();
        }
        try (FileOutputStream out = new FileOutputStream(this.output)) {
            index.write(out);
        }
    }

    public void setInit(Consumer<GenerateIndexTask> init) {
        this.init = init;
    }

    public void setInput(File input) {
        this.input = input;
    }

    public void setOutput(File output) {
        this.output = output;
    }
}
