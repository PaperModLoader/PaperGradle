package xyz.papermodloader.papergradle.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class DecompileTask extends DefaultTask {
    @TaskAction
    public void doTask() {
        /*ExecResult exec = this.getProject().javaexec(new Closure<JavaExecSpec>(this) {
            @Override
            public JavaExecSpec call() {
                if (Constants.MINECRAFT_DECOMPILED.exists()) {
                    Constants.MINECRAFT_DECOMPILED.delete();
                }
                Constants.MINECRAFT_DECOMPILED.mkdir();
                JavaExecSpec spec = (JavaExecSpec) getDelegate();
                spec.args("decompile", Constants.DEOBF_MERGED_JAR_CACHE, Constants.MINECRAFT_DECOMPILED);
                spec.setMain("cuchaz.enigma.CommandMain");
                spec.setWorkingDir(Constants.CACHE_DIRECTORY);
                List<String> classpath = Constants.getClassPath();
                spec.classpath(classpath);
                return spec;
            }

            @Override
            public JavaExecSpec call(Object obj) {
                return this.call();
            }
        });
        int exitValue = exec.getExitValue();
        if (exitValue != 0) {
            throw new RuntimeException("Enigma failed to decompile");
        }*/
    }
}
