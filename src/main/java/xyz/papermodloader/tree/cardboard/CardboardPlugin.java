package xyz.papermodloader.tree.cardboard;

import xyz.papermodloader.tree.TreePlugin;
import xyz.papermodloader.tree.cardboard.task.ExportMappingsTask;
import xyz.papermodloader.tree.cardboard.task.ImportMappingsTask;

public class CardboardPlugin extends TreePlugin<CardboardExtension> {
    public static CardboardPlugin INSTANCE;

    @Override
    public String getPluginName() {
        return "Cardboard";
    }

    @Override
    public String getExtensionName() {
        return "cardboard";
    }

    @Override
    protected void configure() {
        CardboardPlugin.INSTANCE = this;
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
