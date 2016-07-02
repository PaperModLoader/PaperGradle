package xyz.papermodloader.tree.task;

import groovy.lang.Closure;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecResult;
import org.gradle.process.JavaExecSpec;
import xyz.papermodloader.tree.Constants;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class GenerateHooksTask extends DefaultTask {
    @TaskAction
    public void doTask() {
        ExecResult exec = this.getProject().javaexec(new Closure<JavaExecSpec>(this) {
            @Override
            public JavaExecSpec call() {
                JavaExecSpec exec = (JavaExecSpec) this.getDelegate();
                exec.args("--hooks", "true", "--input", Constants.DEOBF_MERGED_JAR_CACHE.get(), "--output", Constants.TEMP_HOOKS_FILE.get());
                exec.setMain("xyz.papermodloader.paper.PaperStart");
                List<String> classpath = GenerateHooksTask.this.getClasspath();
                classpath.add(Constants.RESULT_JAR.get().getAbsolutePath());
                for (File source : GenerateHooksTask.this.getProject().getConfigurations().getByName(Constants.CONFIG_DEPENDENCIES)) {
                    classpath.add(source.getAbsolutePath());
                }
                exec.classpath(classpath);
                return exec;
            }

            @Override
            public JavaExecSpec call(Object obj) {
                return call();
            }
        });
        int exitValue = exec.getExitValue();
        if (exitValue != 0) {
            throw new RuntimeException("Paper failed to generate hooks");
        }
    }

    public List<String> getClasspath() {
        URL[] urls = ((URLClassLoader) GenerateHooksTask.class.getClassLoader()).getURLs();
        List<String> list = new ArrayList<>();
        for (URL url : urls) {
            list.add(url.getPath());
        }
        return list;
    }
}
