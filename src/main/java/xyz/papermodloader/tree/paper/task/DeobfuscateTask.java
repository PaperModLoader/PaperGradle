package xyz.papermodloader.tree.paper.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import xyz.papermodloader.book.Book;
import xyz.papermodloader.book.mapping.Mappings;
import xyz.papermodloader.tree.paper.PaperConstants;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DeobfuscateTask extends DefaultTask {
    @TaskAction
    public void doTask() throws InterruptedException, IOException {
        InputStream stream = new FileInputStream(PaperConstants.MAPPINGS_FILE_CACHE.get());
        Mappings mappings = Mappings.parseMappings(stream);
        stream.close();
        Book.INSTANCE.map(mappings, PaperConstants.MERGED_JAR_CACHE.get(), PaperConstants.DEOBF_MERGED_JAR_CACHE.get());
    }
}
