package xyz.papermodloader.papergradle.task;

import groovy.lang.Closure;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecResult;
import org.gradle.process.JavaExecSpec;
import xyz.papermodloader.papergradle.Constants;
import xyz.papermodloader.papergradle.PaperGradle;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DeobfuscateTask extends DefaultTask {
    @TaskAction
    public void doTask() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(() -> this.deobfuscate(Constants.CLIENT_JAR_CACHE.get(), Constants.DEOBF_CLIENT_JAR_CACHE.get()));
        executor.submit(() -> this.deobfuscate(Constants.SERVER_JAR_CACHE.get(), Constants.DEOBF_SERVER_JAR_CACHE.get()));
        executor.shutdown();
        while (!executor.awaitTermination(1, TimeUnit.MILLISECONDS));
    }

    private void deobfuscate(File input, File output) {
        ExecResult exec = this.getProject().javaexec(new Closure<JavaExecSpec>(this) {
            @Override
            public JavaExecSpec call() {
                if (output.exists()) {
                    output.delete();
                }
                JavaExecSpec spec = (JavaExecSpec) getDelegate();
                spec.args("deobfuscate", input, output, PaperGradle.INSTANCE.getExtension().minecraft + ".mappings");
                spec.setMain("cuchaz.enigma.CommandMain");
                spec.setWorkingDir(Constants.CACHE_DIRECTORY);
                List<String> classpath = Constants.getClassPath();
                spec.classpath(classpath);
                spec.setStandardOutput(new ByteArrayOutputStream());
                return spec;
            }

            @Override
            public JavaExecSpec call(Object obj) {
                return this.call();
            }
        });
        int exitValue = exec.getExitValue();
        if (exitValue != 0) {
            throw new RuntimeException("Enigma failed to deobfuscate");
        }
    }
}
