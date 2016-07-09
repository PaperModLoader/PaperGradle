package xyz.papermodloader.tree.task;

import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.logging.progress.ProgressLogger;
import org.gradle.internal.logging.progress.ProgressLoggerFactory;
import xyz.papermodloader.tree.util.HashUtil;
import xyz.papermodloader.tree.util.Initializer;
import xyz.papermodloader.tree.util.MinecraftJSON;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DownloadAssetsTask extends DefaultTask {
    private Initializer initializer;
    private MinecraftJSON json;
    private File assets;
    private File mcAssets;

    @TaskAction
    public void doTask() throws IOException, InterruptedException {
        this.initializer.initialize();
        MinecraftJSON.AssetIndex.Assets assets = this.json.assetIndex.getAssets();
        ProgressLogger progressLogger = this.getServices().get(ProgressLoggerFactory.class).newOperation(this.getClass());
        progressLogger.start(":downloadAssets", ":assets");
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
        int total = assets.objects.size();
        final int[] current = {0};
        for (Map.Entry<String, MinecraftJSON.AssetIndex.Assets.AssetEntry> entry : assets.objects.entrySet()) {
            executor.submit(() -> {
                try {
                    MinecraftJSON.AssetIndex.Assets.AssetEntry object = entry.getValue();
                    String sha1 = object.hash;
                    String directory = "objects" + File.separator + sha1.substring(0, 2) + File.separator + sha1;
                    File file = new File(this.assets, directory);
                    if (!file.exists() || !HashUtil.equalHash(file, sha1)) {
                        File installationAsset = new File(this.mcAssets, directory);
                        if (installationAsset.exists() && HashUtil.equalHash(installationAsset, sha1)) {
                            FileUtils.copyFile(installationAsset, file);
                        } else {
                            this.getLogger().info(":downloading asset " + entry.getKey());
                            FileUtils.copyURLToFile(new URL("http://resources.download.minecraft.net/" + sha1.substring(0, 2) + "/" + sha1), file);
                        }
                    }
                    current[0]++;
                    progressLogger.progress(current[0] + "/" + total + " (" + (int) ((current[0] / (float) total) * 100) + "%)");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        executor.shutdown();
        while (!executor.awaitTermination(1, TimeUnit.MILLISECONDS)) {
            ;
        }
        progressLogger.completed();
    }

    public void setInitializer(Initializer initializer) {
        this.initializer = initializer;
    }

    public void setJSON(MinecraftJSON json) {
        this.json = json;
    }

    public void setAssets(File assets) {
        this.assets = assets;
    }

    public void setMCAssets(File mcAssets) {
        this.mcAssets = mcAssets;
    }
}
