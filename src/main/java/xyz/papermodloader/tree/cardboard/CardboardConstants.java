package xyz.papermodloader.tree.cardboard;

import xyz.papermodloader.tree.util.delayed.Delayed;
import xyz.papermodloader.tree.util.delayed.DelayedCache;

import java.io.File;

public class CardboardConstants {
    public static final String TASK_EXPORT_MAPPINGS = "exportMappings";
    public static final String TASK_IMPORT_MAPPINGS = "importMappings";

    public static final Delayed<File> ENIGMA_MAPPINGS = new DelayedCache<>(() -> new File(".", CardboardPlugin.INSTANCE.getExtension().minecraft + ".mappings"));
    public static final Delayed<File> BOOK_MAPPINGS = new DelayedCache<>(() -> new File(".", CardboardPlugin.INSTANCE.getExtension().minecraft + ".json"));
}
