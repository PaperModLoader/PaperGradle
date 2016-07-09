package xyz.papermodloader.tree.bag;

import org.gradle.api.plugins.JavaPlugin;
import xyz.papermodloader.tree.TreePlugin;

public class BagPlugin extends TreePlugin<BagExtension> {
    @Override
    public String getPluginName() {
        return "Paper Bag";
    }

    @Override
    public String getExtensionName() {
        return "paper";
    }

    @Override
    protected void configure() {
        this.getProject().getDependencies().add(JavaPlugin.COMPILE_CONFIGURATION_NAME, this.getProject().fileTree("libs"));
        this.addMavenRepo("ilexiconn", "http://maven.ilexiconn.net");
        this.addMavenRepo("mojang", "https://libraries.minecraft.net/");
    }

    @Override
    protected void addTasks() {
        //Setup tasks

    }

    @Override
    protected Class<BagExtension> getExtensionClass() {
        return BagExtension.class;
    }
}
