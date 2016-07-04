package xyz.papermodloader.tree.cardboard.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import xyz.papermodloader.book.converter.MappingsConverter;
import xyz.papermodloader.tree.cardboard.CardboardConstants;

import java.io.IOException;

public class ExportMappingsTask extends DefaultTask {
    @TaskAction
    public void doTask() throws IOException {
        MappingsConverter.main(new String[] {"--input", CardboardConstants.ENIGMA_MAPPINGS.get().getAbsolutePath(), "--output", CardboardConstants.BOOK_MAPPINGS.get().getAbsolutePath()});
    }
}
