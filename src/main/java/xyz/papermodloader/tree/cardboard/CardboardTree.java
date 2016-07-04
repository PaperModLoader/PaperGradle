package xyz.papermodloader.tree.cardboard;

import xyz.papermodloader.tree.Tree;
import xyz.papermodloader.tree.cardboard.task.ExportMappingsTask;
import xyz.papermodloader.tree.cardboard.task.ImportMappingsTask;

public class CardboardTree extends Tree<CardboardExtension> {
    public static CardboardTree INSTANCE;

    @Override
    public String getPluginName() {
        return "Cardboard Tree";
    }

    @Override
    public String getExtensionName() {
        return "cardboard";
    }

    @Override
    protected void configure() {
        CardboardTree.INSTANCE = this;
    }

    @Override
    protected void addTasks() {
        this.addTask(CardboardConstants.TASK_EXPORT_MAPPINGS, ExportMappingsTask.class, task -> task.setDescription("This task converts Enigma's mapping format to Book's format."));
        this.addTask(CardboardConstants.TASK_IMPORT_MAPPINGS, ImportMappingsTask.class, task -> task.setDescription("This task converts Book's mapping format to Enigma's format."));
    }

    @Override
    protected Class<CardboardExtension> getExtensionClass() {
        return CardboardExtension.class;
    }
}
